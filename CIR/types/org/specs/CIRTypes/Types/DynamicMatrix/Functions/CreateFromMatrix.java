/**
 * Copyright 2014 SPeCS.
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

package org.specs.CIRTypes.Types.DynamicMatrix.Functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.CodeGenerator.CodeGeneratorUtils;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Utilities.InputChecker.CirInputsChecker;
import org.specs.CIRFunctions.MatrixAlloc.TensorCreationFunctions;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.Functions.LowLevel.CreateHelper;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixStruct;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;

public class CreateFromMatrix extends AInstanceBuilder {

    private final Optional<ScalarType> customElementType;

    public CreateFromMatrix(ProviderData data, ScalarType elementType) {
        super(data);

        this.customElementType = Optional.ofNullable(elementType);
    }

    public CreateFromMatrix(ProviderData data) {
        // super(data);
        this(data, null);
    }

    public static InstanceProvider getProvider() {
        CirInputsChecker checker = new CirInputsChecker()
                .numOfInputs(1)
                .isMatrix(0);

        return new GenericInstanceProvider(checker, data -> new CreateFromMatrix(data).create());
    }

    @Override
    public FunctionInstance create() {

        MatrixType inputMatrix = getTypeAtIndex(MatrixType.class, 0);

        ScalarType elementType = getElementType(inputMatrix);

        // However, if output type is defined in ProviderData, use that type instead
        if (getData().getOutputType() != null) {
            VariableType outputType = getData().getOutputType();
            if (ScalarUtils.hasScalarType(outputType)) {
                elementType = ScalarUtils.toScalar(outputType);
            }

        }

        // VariableType elementType = InputTypesUtils.getElementType(getData());

        final FunctionInstance arrayHelper = CreateHelper.getProvider(elementType).newCInstance(getData());

        DynamicMatrixType outputType = DynamicMatrixType.newInstance(elementType, inputMatrix.matrix().getShape());
        FunctionType fType = FunctionType.newInstanceWithOutputsAsInputs(
                Arrays.asList("in"), Arrays.asList(inputMatrix),
                "out", outputType);

        if (inputMatrix instanceof DynamicMatrixType) {

            // Inlined code
            InlineCode code = args -> {

                CNode matrixNode = args.get(0);
                if (!(matrixNode instanceof VariableNode)) {
                    throw new RuntimeException("CNode should be a VariableNode: " + matrixNode);
                }

                Variable var = ((VariableNode) matrixNode).getVariable();
                MatrixType matrixType = (MatrixType) matrixNode.getVariableType();

                CNode shapeNode = CNodeFactory.newLiteral(DynamicMatrixStruct.getShapeCode(var.getName(), matrixType));
                CNode dimsNode = getFunctionCall(matrixType.matrix().functions().numDims(), matrixNode);

                List<CNode> inputArgs = new ArrayList<>();
                inputArgs.add(shapeNode);
                inputArgs.add(dimsNode);
                inputArgs.add(args.get(1));

                return CodeGeneratorUtils.functionCallCode(arrayHelper.getCName(),
                        arrayHelper.getFunctionType().getCInputTypes(), inputArgs);

            };

            String functionName = "create_from_matrix$" + arrayHelper.getCName();
            InlinedInstance instance = new InlinedInstance(fType, functionName, code);

            instance.setCallInstances(arrayHelper);

            return instance;

        }

        CInstructionList body = new CInstructionList(fType);

        TypeShape inputShape = inputMatrix.matrix().getShape();
        int numDims = inputShape.getRawNumDims();

        StaticMatrixType shapeType = StaticMatrixType.newInstance(getNumerics().newInt(), 1, numDims);
        Variable shape = new Variable("shape", shapeType);
        VariableNode shapeNode = CNodeFactory.newVariable(shape);

        InstanceProvider shapeSet = shapeType.matrix().functions().set();
        for (int dim = 0; dim < numDims; ++dim) {
            body.addInstruction(getFunctionCall(shapeSet,
                    shapeNode,
                    CNodeFactory.newCNumber(dim),
                    CNodeFactory.newCNumber(inputShape.getRawDim(dim))));
        }

        CNode output = CNodeFactory.newVariable("out", outputType);

        ProviderData tensorCreationData = getData().create(shapeType, getNumerics().newInt());
        tensorCreationData.setOutputType(outputType);

        FunctionInstance newArrayHelperInstance = new TensorCreationFunctions(tensorCreationData)
                .newArrayHelper(elementType);

        body.addLiteralInstruction(newArrayHelperInstance.getCName()
                + "(" + shapeNode.getCode()
                + ", "
                + numDims
                + ", "
                + output.getCode()
                + ");");

        body.addReturn(output);

        String functionName = "create_from_matrix_" + inputMatrix.getSmallId() + "_" + elementType.getSmallId();
        InstructionsInstance instance = new InstructionsInstance(functionName, "lib/matrix", body);

        instance.setCustomImplementationInstances(newArrayHelperInstance);

        return instance;
    }

    private ScalarType getElementType(MatrixType inputMatrix) {
        if (this.customElementType.isPresent()) {
            return this.customElementType.get();
        }

        // As default, use the type of the input matrix
        return inputMatrix.matrix().getElementType();
    }
}
