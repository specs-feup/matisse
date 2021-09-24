/**
 * Copyright 2015 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package org.specs.matisselib.passes.posttype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.ForLoopBuilderResult;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.EndInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Removes end instructions, replacing them by lower-level instructions.
 * 
 * @author Luís Reis
 *
 */
public class EndEliminationPass extends InstructionRemovalPass<EndInstruction> {

    public EndEliminationPass() {
        super(EndInstruction.class);
    }

    @Override
    protected void removeInstruction(TypedInstance instance,
            SsaBlock block,
            int blockId,
            int instructionId,
            EndInstruction instruction,
            DataStore passData) {

        DataProviderService dataProvider = passData.get(ProjectPassServices.DATA_PROVIDER);
        dataProvider.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);

        String output = instruction.getOutputs().get(0);
        String input = instruction.getInputVariable();

        int index = instruction.getIndex();
        int numIndices = instruction.getNumIndices();

        VariableType matrixType = instance.getVariableType(input).get();

        SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

        List<SsaInstruction> nextInstructions = new ArrayList<>(
                block.getInstructions().subList(instructionId + 1, block.getInstructions().size()));
        block.removeInstructionsFrom(instructionId);

        BlockEditorHelper editor = new BlockEditorHelper(instance, systemFunctions, blockId);

        int totalDims = getNumberOfDimensions(matrixType);

        if (numIndices == 1) {
            // A(end), end refers to numel of A

            editor.addCallWithExistentOutputs("numel", Arrays.asList(output), false, input);
        } else if (index < numIndices - 1 || (totalDims > 0 && numIndices == totalDims)) {
            // A(end, 1) -> end is not last
            // A(1, end) -> end is last, and ndims(A) == position of end
            // end refers to size(A, position of end)

            String dim = editor.addMakeIntegerInstruction("dim_" + (index + 1), index + 1);
            editor.addCallWithExistentOutputs("size", Arrays.asList(output), false, input, dim);
        } else if (totalDims != -1) {

            // A(1, end) -> end is last and ndims(A) != position of end
            // But number of dimensions is known

            if (numIndices > totalDims) {
                // end is in an index that comes *after* the number of dimensions.
                // e.g. A(1, 1, end), where ndims(A) == 2
                // end is necessarily 1.

                editor.addInstruction(AssignmentInstruction.fromInteger(output, 1));
            } else {
                // end is in the last index here as well
                // The number of dimensions is known, but the number of indexed dimensions is not equal to ndims(A)
                // So end refers to the end of more than one dimension
                // e.g. A(1, end) where ndims(A) == 4
                // end is size(A, 2) * size(A, 3) * size(A, 4).

                String currentResult = null;

                for (int i = index; i < totalDims; ++i) {
                    String dim = editor.addMakeIntegerInstruction("dim_" + (i + 1), i + 1);
                    String sizeTemp = editor.addSimpleCallToOutput("size", input, dim);

                    if (currentResult == null) {
                        currentResult = sizeTemp;
                    } else {
                        if (i == totalDims - 1) {
                            editor.addCallWithExistentOutputs("times", Arrays.asList(output), false, currentResult,
                                    sizeTemp);
                        } else {
                            editor.addSimpleCallToOutput("times", currentResult, sizeTemp);
                        }
                    }
                }
            }

        } else {
            // A(1, end). Number of dimensions is unknown.
            // Roughly equivalent to [~, end_value] = size(A);
            // We compute the result in a loop.

            String startPoint = editor.addMakeIntegerInstruction("start", index + 1);
            String iterPoint = editor.addMakeIntegerInstruction("step", 1);
            String ndims = editor.addSimpleCallToOutput("ndims", input);

            String initialSize = editor.addMakeIntegerInstruction("size", 1);

            ForLoopBuilderResult loop = editor.makeForLoop(startPoint, iterPoint, ndims);
            BlockEditorHelper loopEditor = loop.getLoopBuilder();
            BlockEditorHelper endEditor = loop.getEndBuilder();

            BlockEditorHelper startEditor = editor;

            String loopStartSize = startEditor.makeIntegerTemporary("size");
            String iter = startEditor.makeIntegerTemporary("iter");
            String sizeVar = startEditor.makeIntegerTemporary("dim");
            String loopEndSize = startEditor.makeIntegerTemporary("size");

            loopEditor.addInstruction(new PhiInstruction(loopStartSize, Arrays.asList(initialSize, loopEndSize), Arrays
                    .asList(editor.getBlockId(), loopEditor.getBlockId())));
            loopEditor.addInstruction(new IterInstruction(iter));

            loopEditor.addCallWithExistentOutputs("size", Arrays.asList(sizeVar), false, input, iter);
            loopEditor.addCallWithExistentOutputs("times", Arrays.asList(loopEndSize), false, loopStartSize, sizeVar);

            endEditor.addInstruction(new PhiInstruction(output, Arrays.asList(initialSize, loopEndSize), Arrays
                    .asList(editor.getBlockId(), loopEditor.getBlockId())));

            editor = endEditor;
        }

        editor.addInstructions(nextInstructions);
    }

    private static int getNumberOfDimensions(VariableType matrixType) {
        if (matrixType instanceof ScalarType) {
            return 1;
        } else if (matrixType instanceof MatrixType) {
            int numDims = ((MatrixType) matrixType).matrix().getShape().getNumDims();

            if (numDims == 0) {
                return 1;
            }
            return numDims == 1 ? 2 : numDims;
        }

        return -1;
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                // Explicitly invalidated
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
