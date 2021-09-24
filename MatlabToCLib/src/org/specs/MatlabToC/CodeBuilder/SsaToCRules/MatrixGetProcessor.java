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

package org.specs.MatlabToC.CodeBuilder.SsaToCRules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.TemporaryUtils;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.Functions.CustomFunctions.AccessMatrixValues;
import org.specs.MatlabToC.Functions.MatissePrimitives.MatissePrimitiveProviders;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.matisselib.ssa.instructions.MatrixGetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class MatrixGetProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof MatrixGetInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        MatrixGetInstruction get = (MatrixGetInstruction) instruction;

        String output = instruction.getOutputs().get(0);

        String matrix = get.getInputMatrix();
        List<String> indices = get.getIndices();

        VariableType inputType = builder.getInstance().getVariableType(matrix).get();
        if (inputType instanceof MatrixType) {

            // FIXME: Too many incorrect assumptions here.
            // MatrixType matrixType = (MatrixType) inputType;

            if (indices.stream()
                    .allMatch(index -> builder.getInstance().getVariableType(index).get() instanceof ScalarType)) {
                emitFastScalarImplementation(builder, currentBlock, output, matrix, indices);
                return;
            }

            if (indices.size() != 1) {
                emitMultipleMatrixGetImplementation(builder, currentBlock, output, matrix, indices);
                return;
            }

            emitSingleMatrixGetImplementation(builder, currentBlock, output, matrix, indices.get(0));

        } else if (inputType instanceof ScalarType) {
            emitScalarImplementation(builder, currentBlock, output, matrix, indices);
        } else {
            throw new NotImplementedException("Matrix access expression for type " + inputType);
        }
    }

    private static void emitScalarImplementation(SsaToCBuilderService builder,
            CInstructionList currentBlock,
            String output,
            String matrix,
            List<String> indices) {

        // FIXME: Are all indices scalars? Do they all have the value 1?

        builder.generateAssignmentForSsaNames(currentBlock, output, matrix);

    }

    private static void emitMultipleMatrixGetImplementation(SsaToCBuilderService builder,
            CInstructionList currentBlock,
            String output,
            String matrix,
            List<String> indices) {

        ProviderData providerData = builder.getCurrentProvider();

        AccessMatrixValues accessMatrixValues = new AccessMatrixValues(indices.size());

        List<CNode> args = SpecsFactory.newArrayList();
        args.add(builder.generateVariableExpressionForSsaName(currentBlock, matrix, false));
        for (String index : indices) {
            args.add(builder.generateVariableExpressionForSsaName(currentBlock, index, false));
        }

        VariableNode outputNode = builder.generateVariableNodeForSsaName(output);
        CNode fcall = MFileProvider.getFunctionCallWithOutputsAsInputs(accessMatrixValues, args,
                Arrays.asList(outputNode), providerData);

        currentBlock.addInstruction(fcall);
    }

    private static void emitSingleMatrixGetImplementation(SsaToCBuilderService builder,
            CInstructionList currentBlock,
            String output,
            String matrix,
            String index) {

        ProviderData providerData = builder.getCurrentProvider();

        CNode outputVariable = builder.generateVariableExpressionForSsaName(currentBlock, output, false);
        MatrixType outputType = (MatrixType) outputVariable.getVariableType();

        MatrixType inputMatrixType = (MatrixType) builder.getInstance().getVariableType(matrix).get();
        CNode inputMatrix = builder.generateVariableExpressionForSsaName(currentBlock, matrix, false);

        MatrixType indexMatrixType = (MatrixType) builder.getInstance().getVariableType(index).get();
        CNode indexMatrix = builder.generateVariableExpressionForSsaName(currentBlock, index, false);

        // We will first create a matrix with the appropriate size.

        ProviderData sizeProviderData = providerData.create(indexMatrixType);
        FunctionInstance sizeInstance = MatlabBuiltin.SIZE.getMatlabFunction().getCheckedInstance(sizeProviderData);

        MatrixType shapeType = (MatrixType) sizeInstance
                .getFunctionType()
                .getOutputAsInputTypes()
                .get(0)
                .pointer().getType(false);
        CNode shapeNode = builder.generateTemporaryNode("shape", shapeType);
        FunctionCallNode sizeFunctionCall = sizeInstance.newFunctionCall(indexMatrix);
        int outputIndex = sizeFunctionCall.getInputTokens().size() - 1;
        assert TemporaryUtils.isTemporaryName(((VariableNode) sizeFunctionCall.getInputTokens().get(outputIndex))
                .getVariableName());
        sizeFunctionCall.getFunctionInputs().setInput(outputIndex, shapeNode);
        currentBlock.addInstruction(sizeFunctionCall);

        ProviderData createProviderData = providerData.create(shapeType);
        createProviderData.setOutputType(Arrays.asList(outputType));
        FunctionInstance createInstance = MatissePrimitiveProviders.newArrayDynamic()
                .getCheckedInstance(createProviderData);

        currentBlock.addFunctionCall(createInstance, shapeNode, outputVariable);

        // Now that we have a matrix with the proper size, we'll populate it.

        VariableType intType = providerData.getNumerics().newInt();
        VariableType int1Type = providerData.getNumerics().newInt(1);

        VariableNode inductionVar = builder.generateTemporaryNode("i", intType);
        CNode numElems = indexMatrixType.matrix().functions().numel()
                .getCheckedInstance(providerData.create(indexMatrixType))
                .newFunctionCall(indexMatrix);

        List<CNode> bodyInstructions = new ArrayList<>();
        CNode indexToRetrieve = shapeType.matrix().functions().get()
                .getCheckedInstance(providerData.create(indexMatrixType, intType))
                .newFunctionCall(indexMatrix, inductionVar);
        CNode zeroBasedIndexExpression = COperator.Subtraction
                .newCInstance(providerData.create(indexToRetrieve.getVariableType(), int1Type))
                .newFunctionCall(indexToRetrieve, CNodeFactory.newCNumber(1));
        CNode zeroBasedIndex = builder.generateTemporaryNode("index", zeroBasedIndexExpression.getVariableType());
        bodyInstructions.add(CNodeFactory.newAssignment(zeroBasedIndex, zeroBasedIndexExpression));

        CNode value = inputMatrixType
                .matrix().functions().get()
                .getCheckedInstance(providerData.createFromNodes(inputMatrix, zeroBasedIndex))
                .newFunctionCall(inputMatrix, zeroBasedIndex);
        CNode set = outputType
                .matrix().functions().set()
                .getCheckedInstance(providerData.createFromNodes(outputVariable, inductionVar, value))
                .newFunctionCall(outputVariable, inductionVar, value);
        bodyInstructions.add(set);

        currentBlock.addInstruction(new ForNodes(providerData)
                .newForLoopBlock(inductionVar, numElems, bodyInstructions));
    }

    private static void emitFastScalarImplementation(SsaToCBuilderService builder,
            CInstructionList currentBlock,
            String output,
            String matrix,
            List<String> indices) {

        MatrixType matrixType = (MatrixType) builder.getInstance().getVariableType(matrix).get();
        InstanceProvider getProvider = matrixType.matrix().functions().get();

        CNode outputVariable = builder.generateVariableExpressionForSsaName(currentBlock, output, false);
        List<CNode> inputs = new ArrayList<>();
        inputs.add(builder.generateVariableExpressionForSsaName(currentBlock, matrix, false));

        CNode one = CNodeFactory.newCNumber(1);
        for (String index : indices) {
            CNode inputVariable = builder.generateVariableExpressionForSsaName(currentBlock, index, false);

            CNode inputNode = FunctionInstanceUtils.getFunctionCall(COperator.Subtraction,
                    builder.getCurrentProvider(), Arrays.asList(inputVariable, one));

            inputs.add(inputNode);
        }

        CNode functionCall = FunctionInstanceUtils.getFunctionCall(getProvider, builder.getCurrentProvider(), inputs);
        currentBlock.addAssignment(outputVariable, functionCall);
    }

}
