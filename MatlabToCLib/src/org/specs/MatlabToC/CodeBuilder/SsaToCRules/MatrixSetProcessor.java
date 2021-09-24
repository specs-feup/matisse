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
import java.util.Optional;

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
import org.specs.CIR.Tree.CNodes.FunctionInputsNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.MatlabToC.Functions.MatissePrimitive;
import org.specs.MatlabToC.Functions.CustomFunctions.SetMultiple;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class MatrixSetProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof MatrixSetInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        MatrixSetInstruction set = (MatrixSetInstruction) instruction;
        String ssaValue = set.getValue();
        List<String> ssaIndices = set.getIndices();
        String ssaInputMatrix = set.getInputMatrix();
        String ssaOutputMatrix = set.getOutputs().get(0);

        CNode one = CNodeFactory.newCNumber(1);

        // From here on, we'll just use the output matrix.

        boolean allIndicesScalar = ssaIndices.stream().allMatch(
                index -> builder.getInstance().getVariableType(index).get() instanceof ScalarType);

        VariableType valueType = builder.getInstance().getVariableType(ssaValue).get();
        if (allIndicesScalar) {
            handleAllIndicesScalar(builder, currentBlock, ssaValue, ssaIndices, ssaOutputMatrix, ssaInputMatrix, one,
                    valueType);
        } else {
            if (!builder.getVariableTypeFromSsaName(ssaInputMatrix).isPresent()) {
                throw new NotImplementedException("Undefined output, in matrix range set");
            }
            builder.generateAssignmentForSsaNames(currentBlock, ssaOutputMatrix, ssaInputMatrix);

            // TODO: Validate shape?
            // TODO: Assuming shape of value is 1x1 if and only if it is a scalar type.

            boolean isScalarValue = !MatrixUtils.isMatrix(valueType);
            int totalIndexes = ssaIndices.size();
            SetMultiple setMultiple = new SetMultiple(totalIndexes, isScalarValue, builder.getEngine());

            List<CNode> inputs = new ArrayList<>();
            inputs.add(builder.generateVariableExpressionForSsaName(currentBlock, ssaOutputMatrix, false));
            for (String index : ssaIndices) {
                inputs.add(builder.generateVariableExpressionForSsaName(currentBlock, index, false));
            }
            inputs.add(builder.generateVariableExpressionForSsaName(currentBlock, ssaValue, false));

            List<CNode> outputs = Arrays.asList(builder.generateVariableExpressionForSsaName(currentBlock,
                    ssaOutputMatrix, false));

            CNode functionCall = MFileProvider.getFunctionCallWithOutputsAsInputs(setMultiple, inputs, outputs,
                    builder.getCurrentProvider());

            currentBlock.addInstruction(functionCall);
        }
    }

    private static void handleAllIndicesScalar(SsaToCBuilderService builder,
            CInstructionList currentBlock,
            String ssaValue,
            List<String> ssaIndices, String ssaOutputMatrix, String ssaInputMatrix, CNode one,
            VariableType valueType) {

        MatrixType matrixType = (MatrixType) builder.getInstance().getVariableType(ssaOutputMatrix).get();

        List<CNode> indices = new ArrayList<>();
        for (String index : ssaIndices) {
            CNode inputVariable = builder.generateVariableNodeForSsaName(index);

            indices.add(inputVariable);
        }

        Optional<VariableNode> rightHand = builder.tryGenerateVariableNodeForSsaName(ssaInputMatrix);
        VariableNode outputMatrixNode = builder.generateVariableNodeForSsaName(ssaOutputMatrix);
        if (!rightHand.isPresent()) {

            List<VariableType> indexTypes = new ArrayList<>();
            for (String index : ssaIndices) {
                VariableType variableType = builder.getVariableTypeFromSsaName(index).get();
                indexTypes.add(variableType);
            }

            if (!ScalarUtils.areScalar(indexTypes)) {
                throw new NotImplementedException("Undefined matrix set with matrix indices.");
            }

            InstanceProvider zerosProvider = builder.getSystemFunction("zeros").get();

            ProviderData providerData = builder.getCurrentProvider()
                    .createFromNodes(one, indices.get(0));
            VariableNode outputNode = outputMatrixNode;
            providerData.setOutputType(outputNode.getVariableType());

            FunctionInstance instance = zerosProvider.getCheckedInstance(providerData);
            CNode zerosCall;
            int outIndex;
            if (indices.size() == 1) {
                zerosCall = instance.newFunctionCall(one, indices.get(0));
                outIndex = 2;
            } else {
                zerosCall = instance.newFunctionCall(indices);
                outIndex = indices.size();
            }
            FunctionInputsNode zeroInputs = ((FunctionCallNode) zerosCall).getFunctionInputs();

            assert TemporaryUtils.isTemporaryName(((VariableNode) zeroInputs.getInputs().get(outIndex))
                    .getVariableName());
            zeroInputs.setInput(outIndex, outputNode);

            currentBlock.addInstruction(zerosCall);
        } else if (outputMatrixNode.getCode().equals(rightHand.get().getCode())
                && outputMatrixNode.getVariableType() instanceof StaticMatrixType) {
            // Do nothing.
        } else {
            builder.generateAssignmentForSsaNames(currentBlock, ssaOutputMatrix, ssaInputMatrix);

            builder.getReporter().emitMessage(PassMessage.OPTIMIZATION_OPPORTUNITY,
                    "MATISSE could not prove that matrix set doesn't resize matrix.");

            InstanceProvider reserveCapacityProvider = MatissePrimitive.RESERVE_CAPACITY.getMatlabFunction();

            List<CNode> reserveCapacityInputs = new ArrayList<>();
            reserveCapacityInputs.add(builder.generateVariableNodeForSsaName(ssaInputMatrix));
            for (CNode index : indices) {
                reserveCapacityInputs.add(index);
            }

            ProviderData reserveCapacityData = builder.getCurrentProvider()
                    .createFromNodes(reserveCapacityInputs);
            reserveCapacityData.setOutputType(matrixType);

            FunctionCallNode reserveCapacityCall = reserveCapacityProvider.getCheckedInstance(reserveCapacityData)
                    .newFunctionCall(reserveCapacityInputs);
            reserveCapacityCall.getFunctionInputs().setInput(reserveCapacityInputs.size(), outputMatrixNode);

            currentBlock.addInstruction(reserveCapacityCall);
        }

        List<CNode> inputs = new ArrayList<>();
        inputs.add(outputMatrixNode);

        InstanceProvider setProvider = matrixType.matrix().functions().set();

        // TODO: Validate indices to ensure there's nothing like A(1.5)

        CNode valueNode;
        if (valueType instanceof MatrixType) {
            // A(1, 2, 3) = MatrixType
            MatrixType matrixValueType = (MatrixType) valueType;
            TypeShape typeShape = matrixValueType.getTypeShape();

            boolean performRuntimeValidation;

            if (typeShape.isFullyDefined()) {
                if (typeShape.getNumElements() == 1) {
                    performRuntimeValidation = false;
                } else {
                    throw builder.getReporter().emitError(PassMessage.CORRECTNESS_ERROR,
                            "In A(I) = B;, where A(I) refers to a single position, B must be a scalar, instead got matrix of size "
                                    + typeShape.getNumElements());
                }
            } else {
                performRuntimeValidation = true;
            }

            if (performRuntimeValidation) {
                // TODO
            }

            List<CNode> getArguments = new ArrayList<>();
            getArguments.add(builder.generateVariableExpressionForSsaName(currentBlock, ssaValue));
            getArguments.add(CNodeFactory.newCNumber(0));
            ProviderData getData = builder.getCurrentProvider().createFromNodes(getArguments);

            valueNode = ((MatrixType) valueType).functions().get().getCheckedInstance(getData)
                    .newFunctionCall(getArguments);
        } else if (valueType instanceof ScalarType) {
            valueNode = builder.generateVariableExpressionForSsaName(currentBlock, ssaValue);
        } else {
            throw new NotImplementedException(valueType.getClass());
        }

        for (CNode index : indices) {
            CNode inputNode = FunctionInstanceUtils.getFunctionCall(COperator.Subtraction,
                    builder.getCurrentProvider(), Arrays.asList(index, one));

            inputs.add(inputNode);
        }

        inputs.add(valueNode);

        CNode functionCall = FunctionInstanceUtils.getFunctionCall(setProvider, builder.getCurrentProvider(),
                inputs);
        currentBlock.addInstruction(functionCall);
    }
}
