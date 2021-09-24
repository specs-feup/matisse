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

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

/**
 * Removes horzcat call instructions, replacing them by lower-level instructions. This is similar to
 * {@link TableSimplificationPass}, but it only deals with horzcats and it is capable of combining multiple rows.
 * 
 * @author Luís Reis
 *
 */
public class HorzcatEliminationPass extends InstructionRemovalPass<FunctionCallInstruction> {
    public HorzcatEliminationPass() {
        super(FunctionCallInstruction.class);
    }

    @Override
    protected boolean canEliminate(TypedInstance instance, FunctionCallInstruction functionCallInstruction) {

        if (!functionCallInstruction.getFunctionName().equals("horzcat")) {
            return false;
        }

        List<String> outputs = functionCallInstruction.getOutputs();
        if (outputs.size() != 1) {
            return false;
        }

        String output = outputs.get(0);

        MatrixType outputType = (MatrixType) instance.getVariableType(output).get();
        TypeShape typeShape = outputType.getTypeShape();

        return typeShape.isKnownRow();
    }

    @Override
    protected void removeInstruction(TypedInstance instance,
            SsaBlock block,
            int blockId,
            int instructionId,
            FunctionCallInstruction instruction,
            DataStore passData) {

        assert canEliminate(instance, instruction);

        ProviderData providerData = instance.getProviderData();

        DataProviderService dataProvider = passData.get(ProjectPassServices.DATA_PROVIDER);
        dataProvider.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);
        dataProvider.invalidate(CompilerDataProviders.CONTROL_FLOW_GRAPH);

        String output = instruction.getOutputs().get(0);
        List<String> inputs = instruction.getInputVariables();

        MatrixType outputType = (MatrixType) instance.getVariableType(output).get();

        SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

        InstanceProvider plusProvider = systemFunctions.getSystemFunction("plus").get();
        InstanceProvider zerosProvider = systemFunctions.getSystemFunction("matisse_new_array_from_dims").get();
        InstanceProvider sizeProvider = systemFunctions.getSystemFunction("size").get();

        List<SsaInstruction> nextInstructions = new ArrayList<>(
                block.getInstructions().subList(instructionId + 1, block.getInstructions().size()));
        block.removeInstructionsFrom(instructionId);

        String allocVar = instance.makeTemporary("initial", outputType);
        int constantOutputSize = 0;
        List<String> unknownSizeFactors = new ArrayList<>();
        int currentUnknownSizeFactor = 0;
        VariableType intType = providerData.getNumerics().newInt();

        for (String input : inputs) {
            VariableType inputType = instance.getVariableType(input).get();
            if (inputType instanceof ScalarType) {
                ++constantOutputSize;
                continue;
            }

            if (inputType instanceof MatrixType) {
                MatrixType matrixType = (MatrixType) inputType;
                TypeShape inputShape = matrixType.getTypeShape();

                if (!inputShape.isKnownRow()) {
                    throw new NotImplementedException("Matrix might not be a row.");
                }

                int cols = inputShape.getDim(1);
                if (cols < 0) {
                    NumericTypeV2 twoType = providerData.getNumerics().newInt(2);
                    String dim2 = instance.makeTemporary("dim2", twoType);
                    block.addAssignment(dim2, 2);

                    ProviderData sizeProviderData = providerData.create(inputType, twoType);
                    FunctionType sizeFunctionType = sizeProvider.getType(sizeProviderData);
                    String computedSize = instance.makeTemporary("size", sizeFunctionType.getOutputTypes().get(0));
                    block.addInstruction(new TypedFunctionCallInstruction("size", sizeFunctionType, computedSize,
                            input,
                            dim2));

                    unknownSizeFactors.add(computedSize);
                } else {
                    constantOutputSize += cols;
                }

                continue;
            }

            // ?
            throw new NotImplementedException(inputType.getClass());
        }

        VariableType outputSizeType = providerData.getNumerics().newInt(constantOutputSize);
        String outputSizeVar = instance.makeTemporary("output_size", outputSizeType);
        block.addAssignment(outputSizeVar, constantOutputSize);
        for (String unknownSizeFactor : unknownSizeFactors) {
            String newSizeVar = instance.makeTemporary("new_size", intType);

            ProviderData plusProviderData = providerData.create(outputSizeType, intType);
            FunctionType plusFunctionType = plusProvider.getType(plusProviderData);
            outputSizeType = plusFunctionType.getOutputTypes().get(0);
            block.addInstruction(new TypedFunctionCallInstruction("plus", plusFunctionType, newSizeVar,
                    outputSizeVar,
                    unknownSizeFactor));

            outputSizeVar = newSizeVar;
        }

        VariableType oneType = providerData.getNumerics().newInt(1);
        String oneVar = instance.makeTemporary("one", oneType);

        block.addAssignment(oneVar, 1);

        List<VariableType> allocInputTypes = Arrays.asList(outputSizeType, oneType);
        List<String> allocInputs = Arrays.asList(oneVar, outputSizeVar);
        ProviderData zerosProviderData = providerData.create(allocInputTypes);
        zerosProviderData.setOutputType(outputType);
        FunctionType functionType = zerosProvider.getType(zerosProviderData);
        block.addInstruction(new TypedFunctionCallInstruction("matisse_new_array_from_dims", functionType, Arrays
                .asList(allocVar),
                allocInputs));

        String currentMatrix = allocVar;

        VariableType currentIndexType = oneType;
        String currentIndex = instance.makeTemporary("index", oneType);
        block.addAssignment(currentIndex, 1);

        for (int i = 0; i < inputs.size(); ++i) {
            String input = inputs.get(i);
            VariableType inputType = instance.getVariableType(input).get();

            if (inputType instanceof ScalarType) {
                String newMatrix = instance.makeTemporary("matrix", outputType);

                block.addInstruction(new SimpleSetInstruction(newMatrix, currentMatrix, Arrays.asList(currentIndex),
                        input));

                currentMatrix = newMatrix;

                ProviderData plusProviderData = providerData.create(currentIndexType, oneType);
                FunctionType plusFunctionType = plusProvider.getType(plusProviderData);
                currentIndexType = plusFunctionType.getOutputTypes().get(0);

                String newIndex;
                Number constant = ScalarUtils.getConstant(instance.getVariableType(currentIndex).get());
                if (constant != null) {
                    int newValue = constant.intValue() + 1;
                    newIndex = instance.makeTemporary("index", providerData.getNumerics().newInt(newValue));
                    block.addAssignment(newIndex, newValue);
                } else {
                    newIndex = instance.makeTemporary("index", currentIndexType);
                    block.addInstruction(new TypedFunctionCallInstruction("plus", plusFunctionType, newIndex,
                            currentIndex,
                            oneVar));
                }

                currentIndex = newIndex;

            } else if (inputType instanceof MatrixType) {

                MatrixType matrixType = (MatrixType) inputType;

                int loopSize = matrixType.getTypeShape().getDim(1);
                String loopSizeVar;

                if (loopSize < 0) {
                    loopSizeVar = unknownSizeFactors.get(currentUnknownSizeFactor++);
                } else {
                    VariableType loopType = providerData.getNumerics().newInt(loopSize);
                    loopSizeVar = instance.makeTemporary("loop_size", loopType);
                    block.addAssignment(loopSizeVar, loopSize);
                }

                SsaBlock loopBlock = new SsaBlock();
                int loopBlockId = instance.addBlock(loopBlock);

                SsaBlock nextBlock = new SsaBlock();
                int nextBlockId = instance.addBlock(nextBlock);

                instance.breakBlock(blockId, blockId, nextBlockId);

                block.addInstruction(new ForInstruction(oneVar, oneVar, loopSizeVar, loopBlockId, nextBlockId));

                // Now build loop
                String startMatrixVar = instance.makeTemporary("matrix", outputType);
                String nextMatrixVar = instance.makeTemporary("matrix", outputType);
                String startIndexVar = instance.makeTemporary("index", intType);
                String nextIndexVar = instance.makeTemporary("index", intType);
                String iteration = instance.makeTemporary("iter", intType);
                VariableType elementType = outputType.matrix().getElementType();
                String matrixValue = instance.makeTemporary("value", elementType);

                loopBlock.addInstruction(new PhiInstruction(startMatrixVar,
                        Arrays.asList(currentMatrix, nextMatrixVar), Arrays.asList(blockId, loopBlockId)));
                loopBlock.addInstruction(new PhiInstruction(startIndexVar, Arrays.asList(currentIndex, nextIndexVar),
                        Arrays.asList(blockId, loopBlockId)));
                loopBlock.addInstruction(new IterInstruction(iteration));
                loopBlock.addInstruction(new SimpleGetInstruction(matrixValue, input, iteration));
                loopBlock.addInstruction(new SimpleSetInstruction(nextMatrixVar, startMatrixVar, Arrays
                        .asList(startIndexVar), matrixValue));

                ProviderData plusProviderData = providerData.create(intType, oneType);
                FunctionType plusFunctionType = plusProvider.getType(plusProviderData);
                currentIndexType = plusFunctionType.getOutputTypes().get(0);
                loopBlock.addInstruction(new TypedFunctionCallInstruction("plus", plusFunctionType, nextIndexVar,
                        startIndexVar,
                        oneVar));

                // Build code after loop
                String finalMatrix = instance.makeTemporary("matrix", outputType);
                nextBlock.addInstruction(new PhiInstruction(finalMatrix,
                        Arrays.asList(currentMatrix, nextMatrixVar), Arrays.asList(blockId, loopBlockId)));
                currentMatrix = finalMatrix;

                String finalIndex = instance.makeTemporary("index", intType);
                nextBlock.addInstruction(new PhiInstruction(finalIndex,
                        Arrays.asList(currentIndex, nextIndexVar), Arrays.asList(blockId, loopBlockId)));
                currentIndex = finalIndex;

                block = nextBlock;
                blockId = nextBlockId;
            } else {
                throw new NotImplementedException("Member type: " + inputType);
            }

        }

        block.addInstruction(AssignmentInstruction.fromVariable(output, currentMatrix));
        block.addInstructions(nextInstructions);
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                // Explicitly invalidated if needed
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
