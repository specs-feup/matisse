/**
 * Copyright 2017 SPeCS.
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
import java.util.Optional;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.ForLoopBuilderResult;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * The multi-set elimination pass only works for matrix_set instructions when *all* the following conditions are true:
 * <ul>
 * <li>The function does NOT have the %!disable simple_multi_set_elimination directive
 * <li>The set instruction has a single index dimension, which is a matrix.
 * <li>The value of the set instruction is a scalar
 * </ul>
 * 
 * It converts the matrix_set instructions into the equivalent for loop.
 * 
 * @author Luís Reis
 *
 */
public class SimpleMultiSetEliminationPass extends InstructionRemovalPass<MatrixSetInstruction> {

    public SimpleMultiSetEliminationPass() {
        super(MatrixSetInstruction.class);
    }

    @Override
    protected boolean canEliminate(TypedInstance instance, MatrixSetInstruction instruction) {
        if (PassUtils.skipPass(instance, "simple_multi_set_elimination")) {
            return false;
        }

        if (instruction.getIndices().size() != 1) {
            return false;
        }

        String index = instruction.getIndices().get(0);
        if (!MatrixUtils.isMatrix(instance.getVariableType(index))) {
            return false;
        }

        String value = instruction.getValue();
        return ScalarUtils.isScalar(instance.getVariableType(value));
    }

    @Override
    protected void removeInstruction(TypedInstance instance,
            SsaBlock block,
            int blockId,
            int instructionId,
            MatrixSetInstruction instruction,
            DataStore passData) {

        SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

        List<SsaInstruction> nextInstructions = new ArrayList<>(
                block.getInstructions().subList(instructionId + 1, block.getInstructions().size()));
        block.removeInstructionsFrom(instructionId);
        BlockEditorHelper editor = new BlockEditorHelper(instance, systemFunctions, blockId);

        String sourceMatrix = instruction.getInputMatrix();
        String output = instruction.getOutput();
        String value = instruction.getValue();

        Optional<VariableType> matrixType = instance.getVariableType(output);
        String suggestedDerivedName = NameUtils.getSuggestedName(output);

        String index = instruction.getIndices().get(0);
        ScalarType indexUnderlyingType = MatrixUtils.getElementType(instance.getVariableType(index).get());
        String one = editor.addMakeIntegerInstruction("one", 1);
        String indexNumel = editor.addSimpleCallToOutput("numel", index);

        ForLoopBuilderResult forLoop = editor.makeForLoop(one, one, indexNumel);
        BlockEditorHelper loopEditor = forLoop.getLoopBuilder();
        String loopStartMatrix = editor.makeTemporary(suggestedDerivedName, matrixType);
        String loopEndMatrix = editor.makeTemporary(suggestedDerivedName, matrixType);
        List<Integer> sourceBlocks = Arrays.asList(editor.getBlockId(), loopEditor.getBlockId());
        loopEditor.addInstruction(
                new PhiInstruction(loopStartMatrix, Arrays.asList(sourceMatrix, loopEndMatrix), sourceBlocks));
        String iter = loopEditor.addIntItersInstruction("iter");
        String currentIndex = loopEditor.addSimpleGet(index, iter, indexUnderlyingType);
        loopEditor.addInstruction(
                new MatrixSetInstruction(loopEndMatrix, loopStartMatrix, Arrays.asList(currentIndex), value));

        BlockEditorHelper endEditor = forLoop.getEndBuilder();
        endEditor.addInstruction(
                new PhiInstruction(output, Arrays.asList(sourceMatrix, loopEndMatrix), sourceBlocks));

        endEditor.addInstructions(nextInstructions);
    }

}
