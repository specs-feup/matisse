/**
 * Copyright 2016 SPeCS.
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
import java.util.function.BiFunction;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.functionproperties.AssumeMatrixIndicesInRangeProperty;
import org.specs.matisselib.functionproperties.AssumeMatrixSizesMatchProperty;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.ComputationBuilderResult;
import org.specs.matisselib.helpers.ForLoopBuilderResult;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.RangeGetInstruction;
import org.specs.matisselib.ssa.instructions.RangeInstruction;
import org.specs.matisselib.ssa.instructions.RangeInstruction.Index;
import org.specs.matisselib.ssa.instructions.RangeInstruction.PartialRangeIndex;
import org.specs.matisselib.ssa.instructions.RangeSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.ValidateLooseMatchInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class FullRangeEliminationPass extends SizeAwareInstructionRemovalPass<RangeInstruction> {

    public static final String PASS_NAME = "full_range_elimination";

    public FullRangeEliminationPass() {
        super(RangeInstruction.class);
    }

    @Override
    protected boolean canEliminate(FunctionBody body,
            RangeInstruction instruction,
            Function<String, Optional<VariableType>> typeGetter,
            SizeGroupInformation sizeInfo) {

        return true;
    }

    @Override
    protected void removeInstruction(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            SsaBlock block,
            int blockId,
            int instructionId,
            RangeInstruction instruction,
            SizeGroupInformation sizeInfo,
            DataStore passData) {

        Logger logger = PassUtils.getLogger(passData, PASS_NAME);

        logger.log("Found instruction: " + instruction);

        SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

        List<SsaInstruction> nextInstructions = new ArrayList<>(
                block.getInstructions().subList(instructionId + 1, block.getInstructions().size()));
        block.removeInstructionsFrom(instructionId);

        BlockEditorHelper editor = new BlockEditorHelper(body,
                providerData,
                systemFunctions,
                typeGetter,
                makeTemporary,
                blockId);

        String inputMatrix = instruction.getInputMatrix();
        String output = instruction.getOutput();

        String inputMatrixSuggestedName = NameUtils.getSuggestedName(inputMatrix);

        String one = editor.addMakeIntegerInstruction("one", 1);

        List<String> offsets = new ArrayList<>();
        List<String> sizes = new ArrayList<>();
        List<Index> indices = instruction.getIndices();
        boolean validate = instruction instanceof RangeGetInstruction
                && !body.hasProperty(AssumeMatrixIndicesInRangeProperty.class);

        for (int i = 0; i < indices.size(); i++) {
            Index index = indices.get(i);
            if (index instanceof RangeInstruction.NormalIndex) {

                offsets.add(null);

                String variable = ((RangeInstruction.NormalIndex) index).getIndex();
                VariableType type = typeGetter.apply(variable).get();
                if (ScalarUtils.isScalar(type)) {
                    sizes.add(one);

                    if (validate) {
                        addValidationCheck(editor, variable, inputMatrix, i, indices.size());
                    }
                } else {
                    String suggestedName = NameUtils.getSuggestedName(variable) + "_numel";
                    String numel = editor.addSimpleCallToOutputWithSemantics("numel", suggestedName, variable);
                    sizes.add(numel);

                    if (validate) {
                        String flatMatrix = editor.addVerticalFlatten(variable);
                        String max = editor.addSimpleCallToOutputWithSemantics("matisse_max_or_zero", "max_index",
                                flatMatrix);
                        addValidationCheck(editor, max, inputMatrix, i, indices.size());
                    }
                }

            } else if (index instanceof RangeInstruction.FullRangeIndex) {
                offsets.add(null);

                String suggestedName = inputMatrixSuggestedName + "_end";

                String end = editor.addMakeEnd(suggestedName, inputMatrix, i, indices.size());

                sizes.add(end);

                sizeInfo.buildSize(inputMatrix, i, end);
            } else if (index instanceof RangeInstruction.PartialRangeIndex) {
                PartialRangeIndex partialRange = (RangeInstruction.PartialRangeIndex) index;

                String start = partialRange.getStart();
                String end = partialRange.getEnd();

                String diff = editor.addSimpleCallToOutputWithSemantics("minus", "range_diff", end, start);
                String size = editor.addSimpleCallToOutputWithSemantics("plus", "range_size", diff, one);
                sizes.add(size);
                offsets.add(start);

                if (validate) {
                    addValidationCheck(editor, end, inputMatrix, i, indices.size());
                }
            } else {
                throw new NotImplementedException(index.getClass());
            }
        }

        boolean isPerfectSizeMatch = false;
        boolean inAccessRange = false;

        if (instruction instanceof RangeSetInstruction) {
            RangeSetInstruction setInstruction = (RangeSetInstruction) instruction;
            String value = setInstruction.getValue();
            if (ScalarUtils.isScalar(typeGetter.apply(value))) {
                logger.log("Setting to scalar");

                isPerfectSizeMatch = true;
                inAccessRange = true;
            } else {

                isPerfectSizeMatch = sizeInfo.inRangeOfMatrix(sizes, value);
                logger.log("Is perfect size match? " + sizes + ": " + isPerfectSizeMatch);
                if (!isPerfectSizeMatch) {
                    logger.log(sizeInfo.toString());
                }

                inAccessRange = true; // FIXME
            }
        }

        String suggestedMatrixName = NameUtils.getSuggestedName(output);
        VariableType outputType = typeGetter.apply(output).get();

        String initialAllocation;
        if (instruction instanceof RangeGetInstruction) {
            initialAllocation = editor.addTypedOutputCall("matisse_new_array_from_dims", suggestedMatrixName,
                    outputType, sizes);
        } else if (instruction instanceof RangeSetInstruction) {
            initialAllocation = inputMatrix;
        } else {
            throw new NotImplementedException(instruction.getClass());
        }

        String sizeMatrix = null;
        if (instruction instanceof RangeSetInstruction && !isPerfectSizeMatch) {
            RangeSetInstruction set = (RangeSetInstruction) instruction;

            String valueSuggestedName = NameUtils.getSuggestedName(set.getValue());

            sizeMatrix = editor.addSimpleCallToOutputWithSemantics("horzcat",
                    inputMatrixSuggestedName + "_dims",
                    sizes);

            if (!body.hasProperty(AssumeMatrixSizesMatchProperty.class)) {
                String valueSize = editor.addSimpleCallToOutputWithSemantics("size",
                        valueSuggestedName + "_size",
                        set.getValue());
                editor.addInstruction(new ValidateLooseMatchInstruction(sizeMatrix, valueSize));
            }
        }

        ComputationBuilderResult computation = generateLoops(editor,
                instruction,
                typeGetter, makeTemporary,
                inputMatrix, indices,
                offsets, sizes, sizeMatrix, one,
                new ArrayList<>(), new ArrayList<>(),
                initialAllocation,
                suggestedMatrixName, outputType,
                isPerfectSizeMatch, inAccessRange,
                indices.size() - 1);

        editor = computation.getEndBuilder();
        editor.addAssignment(output, computation.getResultVariable());

        editor.addInstructions(nextInstructions);
    }

    private static void addValidationCheck(BlockEditorHelper editor,
            String variable,
            String inputMatrix,
            int i,
            int numIndices) {

        String semantics = NameUtils.getSuggestedName(inputMatrix);

        String end = editor.addMakeEnd(semantics + "_end" + i, inputMatrix, i, numIndices);

        String condition = editor.addSimpleCallToOutput("le", variable, end);

        editor.addValidateTrue(condition);
    }

    private ComputationBuilderResult generateLoops(BlockEditorHelper editor,
            RangeInstruction instruction,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            String inputMatrix,
            List<Index> indices,
            List<String> offsets,
            List<String> sizes,
            String sizeMatrix,
            String one,
            List<String> getIndexVariables,
            List<String> setIndexVariables,
            String parentVariable,
            String suggestedMatrixName,
            VariableType outputType,
            boolean isPerfectSizeMatch,
            boolean inAccessRange,
            int i) {

        assert i < indices.size() : "Expected i < " + indices.size() + ", got i = " + i;

        if (i < 0) {
            VariableType elementType = MatrixUtils.getElementType(outputType);

            if (instruction instanceof RangeGetInstruction) {
                String value = editor.addSimpleGet(inputMatrix, getIndexVariables, elementType);
                String output = editor.addSimpleSet(parentVariable, setIndexVariables, value);

                return new ComputationBuilderResult(output, editor);
            }
            if (instruction instanceof RangeSetInstruction) {
                RangeSetInstruction set = (RangeSetInstruction) instruction;

                String valueMatrix = set.getValue();
                Optional<VariableType> valueType = typeGetter.apply(valueMatrix);

                String value;
                if (ScalarUtils.isScalar(valueType)) {
                    value = valueMatrix;
                } else if (isPerfectSizeMatch) {
                    value = editor.addSimpleGet(valueMatrix, getIndexVariables, elementType);
                } else {
                    value = editor.addRelativeGet(valueMatrix, sizeMatrix, getIndexVariables, elementType);
                }

                String output;
                if (inAccessRange) {
                    output = editor.addSimpleSet(parentVariable, setIndexVariables, value);
                } else {
                    output = editor.addSet(parentVariable, setIndexVariables, value);
                }

                return new ComputationBuilderResult(output, editor);
            }
            throw new NotImplementedException(instruction.getClass());
        }

        Index index = indices.get(i);
        if (index instanceof RangeInstruction.NormalIndex) {

            String variable = ((RangeInstruction.NormalIndex) index).getIndex();
            VariableType type = typeGetter.apply(variable).get();
            if (ScalarUtils.isScalar(type)) {
                if (instruction instanceof RangeGetInstruction) {
                    getIndexVariables.add(0, variable);
                    setIndexVariables.add(0, one);
                } else if (instruction instanceof RangeSetInstruction) {
                    getIndexVariables.add(0, one);
                    setIndexVariables.add(0, variable);
                } else {
                    throw new NotImplementedException(instruction.getClass());
                }

                return generateLoops(editor,
                        instruction,
                        typeGetter, makeTemporary,
                        inputMatrix, indices,
                        offsets, sizes, sizeMatrix,
                        one,
                        getIndexVariables, setIndexVariables,
                        parentVariable,
                        suggestedMatrixName, outputType,
                        isPerfectSizeMatch, inAccessRange,
                        i - 1);
            }
        }

        ForLoopBuilderResult loop = editor.makeForLoop(one, one, sizes.get(i));
        BlockEditorHelper loopEditor = loop.getLoopBuilder();
        BlockEditorHelper endEditor = loop.getEndBuilder();

        String inLoopVariable = makeTemporary.apply(suggestedMatrixName, outputType);
        String afterLoopVariable = makeTemporary.apply(suggestedMatrixName, outputType);

        String iter = loopEditor.addIntItersInstruction("iter");

        if (index instanceof RangeInstruction.NormalIndex) {

            String variable = ((RangeInstruction.NormalIndex) index).getIndex();
            VariableType type = typeGetter.apply(variable).get();
            if (!ScalarUtils.isScalar(type)) {
                MatrixType matrixType = (MatrixType) type;
                String targetPosition = loopEditor.addSimpleGet(variable, iter, matrixType.matrix().getElementType());

                if (instruction instanceof RangeGetInstruction) {
                    getIndexVariables.add(0, targetPosition);
                    setIndexVariables.add(0, iter);
                } else if (instruction instanceof RangeSetInstruction) {
                    getIndexVariables.add(0, iter);
                    setIndexVariables.add(0, targetPosition);
                } else {
                    throw new NotImplementedException(instruction.getClass());
                }
            }
        } else if (index instanceof RangeInstruction.FullRangeIndex) {
            getIndexVariables.add(0, iter);
            setIndexVariables.add(0, iter);
        } else if (index instanceof RangeInstruction.PartialRangeIndex) {
            String relativeIndex = loopEditor.addSimpleCallToOutput("plus", iter, offsets.get(i));
            String getName = NameUtils.getSuggestedName(inputMatrix) + "_index";
            String getIndex = loopEditor.addSimpleCallToOutputWithSemantics("minus", getName, relativeIndex, one);

            if (instruction instanceof RangeGetInstruction) {
                getIndexVariables.add(0, getIndex);
                setIndexVariables.add(0, iter);
            } else if (instruction instanceof RangeSetInstruction) {
                getIndexVariables.add(0, iter);
                setIndexVariables.add(0, getIndex);
            } else {
                throw new NotImplementedException(instruction.getClass());
            }

        } else {
            throw new NotImplementedException(index.getClass());
        }

        ComputationBuilderResult computation = generateLoops(loopEditor,
                instruction,
                typeGetter, makeTemporary,
                inputMatrix, indices,
                offsets, sizes, sizeMatrix,
                one,
                getIndexVariables, setIndexVariables,
                inLoopVariable,
                suggestedMatrixName, outputType,
                isPerfectSizeMatch, inAccessRange,
                i - 1);
        String nestedVariable = computation.getResultVariable();
        BlockEditorHelper computationEnd = computation.getEndBuilder();

        PhiInstruction loopPhi = new PhiInstruction(inLoopVariable,
                Arrays.asList(parentVariable, nestedVariable),
                Arrays.asList(editor.getBlockId(), computationEnd.getBlockId()));
        loopEditor.prependInstruction(loopPhi);
        PhiInstruction endPhi = new PhiInstruction(afterLoopVariable,
                Arrays.asList(parentVariable, nestedVariable),
                Arrays.asList(editor.getBlockId(), computationEnd.getBlockId()));
        endEditor.addInstruction(endPhi);

        return new ComputationBuilderResult(afterLoopVariable, endEditor);
    }

    @Override
    protected void afterPass(FunctionBody body, DataStore dataStore, boolean performedElimination) {
        if (performedElimination) {
            dataStore.get(ProjectPassServices.DATA_PROVIDER).invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION // Explicitly invalidated
        );
    }

}
