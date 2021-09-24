/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.MatlabToC.Functions.BaseFunctions.General;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.specs.CIR.CodeGenerator.MatrixCode;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRFunctions.CirFilename;
import org.specs.CIRFunctions.CirFunctionsUtils;
import org.specs.CIRFunctions.MatrixFunction;
import org.specs.CIRFunctions.MatrixDec.SizeStatic;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixStruct;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.Functions.BaseFunctions.BaseResource;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;
import org.specs.MatlabToC.Utilities.MatisseChecker;
import org.specs.matisselib.PassMessage;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.specs.util.exceptions.OverflowException;

/**
 * @author Joao Bispo
 * 
 */
public class Size extends AInstanceBuilder {

    private final boolean isScalar;

    private Size(ProviderData data, boolean isScalar) {
        super(data);

        this.isScalar = isScalar;
    }

    @Override
    public FunctionInstance create() {

        if (isScalar) {
            return newScalarBuilder().create(getData());
        }

        return newMatrixBuilder().create(getData());
    }

    /**
     * @return
     */
    public static MatlabInstanceProvider newMatrixBuilder() {
        MatisseChecker checker = new MatisseChecker().
        // Check if 1 or 2 inputs
                numOfInputsRange(1, 2).numOfOutputsAtMost(1).
                // Check if input 1 is matrix
                isMatrix(0).
                // Check if input 2 is scalar
                isScalar(1);

        return new MatlabInstanceProvider() {

            /* (non-Javadoc)
             * @see org.specs.MatlabToCLib.MatlabFunction.AFunctionBuilder#checkRule(org.specs.CIR.FunctionInstance.ProviderData)
             */
            @Override
            public boolean checkRule(ProviderData data) {
                return checker.create(data).check();
            }

            @Override
            public FunctionInstance create(ProviderData builderData) {
                return getProvider(builderData, true).getCheckedInstance(builderData);
            }

            @Override
            public FunctionType getType(ProviderData builderData) {
                return getProvider(builderData, false).getType(builderData);
            }

            private InstanceProvider getProvider(ProviderData builderData, boolean isCodegen) {
                List<VariableType> inputTypes = builderData.getInputTypes();

                // If two arguments, use .M file
                if (inputTypes.size() == 2) {
                    return new Size(builderData, false).getTwoArgsProvider(isCodegen);
                }

                return MatrixFunction.SIZE;
            }

            /* (non-Javadoc)
             * @see org.specs.MatlabToC.CirInterface.MatlabToCProvider#getInputsParser(org.specs.CIR.Functions.FunctionTypes)
             */
            @Override
            public InputsFilter getInputsFilter() {
                return Size.getInputsParser();
            }

        };
    }

    public static InstanceProvider newMultiOutputsBuilder() {
        MatisseChecker checker = new MatisseChecker()
                .numOfInputs(1)
                .isMatrix(0)
                .isScalar(1)
                .numOfOutputsAtLeast(2);

        return new MatlabInstanceProvider() {

            @Override
            public FunctionType getType(ProviderData providerData) {
                MatrixType inType = providerData.getInputType(MatrixType.class, 0);
                TypeShape inShape = inType.getTypeShape();

                int numDims = inShape.getRawNumDims();
                // System.out.println("numDims=" + numDims);

                int nargouts = providerData.getNargouts().get();
                FunctionTypeBuilder functionTypeBuilder = FunctionTypeBuilder
                        .newWithOutputsAsInputs()
                        .addInput("in", inType);
                for (int i = 1; i <= nargouts; ++i) {
                    String outputName = "out_" + i;

                    ScalarType outputType = providerData.getNumerics().newInt();
                    if (numDims != -1) {
                        // Try to find out if this dimension of the size is constant

                        if (i > numDims) {
                            // In [~, ~, A, B] = size(mat2D);
                            // A and B are 1
                            outputType = outputType.scalar().setConstant(1);
                        } else if (i != nargouts) {
                            int dim = inShape.getDim(i - 1);
                            if (dim >= 0) {
                                outputType = outputType.scalar().setConstant(dim);
                            }
                        } else {
                            Integer size = 1;
                            for (int j = i - 1; j < numDims; ++j) {
                                int dim = inShape.getDim(j);
                                if (dim < 0) {
                                    size = null;
                                    break;
                                }

                                size *= dim;
                            }
                            if (size != null) {
                                outputType = outputType.scalar().setConstant(size);
                            }
                        }
                    }

                    functionTypeBuilder.addOutputAsInput(outputName, outputType);
                }

                return functionTypeBuilder.build();
            }

            @Override
            public FunctionInstance create(ProviderData providerData) {
                FunctionType functionType = getType(providerData);
                return new Size(providerData, false).getMultiOutputsInstance(functionType);
            }

            @Override
            public boolean checkRule(ProviderData data) {
                return checker.create(data).check();
            }
        };
    }

    /**
     * @return
     */
    public static MatlabInstanceProvider newScalarBuilder() {
        MatisseChecker checker = new MatisseChecker().
        // Check if one or two inputs
                numOfInputs(2).numOfOutputsAtMost(1).
                // Check if scalar
                areScalar();

        return new MatlabInstanceProvider() {
            /* (non-Javadoc)
             * @see org.specs.MatlabToCLib.MatlabFunction.AFunctionBuilder#checkRule(org.specs.CIR.FunctionInstance.ProviderData)
             */
            @Override
            public boolean checkRule(ProviderData data) {
                return checker.create(data).check();
            }

            @Override
            public FunctionInstance create(ProviderData builderData) {
                return new SizeStatic(builderData).newSizeScalar();

            }

            /* (non-Javadoc)
             * @see org.specs.MatlabToC.CirInterface.MatlabToCProvider#getInputsParser(org.specs.CIR.Functions.FunctionTypes)
             */
            @Override
            public InputsFilter getInputsFilter() {
                return Size.getInputsParser();
            }

        };
    }

    public static MatlabInstanceProvider newScalarMatrixBuilder() {
        MatisseChecker checker = new MatisseChecker().
        // Check if one or two inputs
                numOfInputs(1).numOfOutputsAtMost(1).
                // Check if scalar
                areScalar();

        return new MatlabInstanceProvider() {
            /* (non-Javadoc)
             * @see org.specs.MatlabToCLib.MatlabFunction.AFunctionBuilder#checkRule(org.specs.CIR.FunctionInstance.ProviderData)
             */
            @Override
            public boolean checkRule(ProviderData data) {
                return checker.create(data).check();
            }

            @Override
            public FunctionInstance create(ProviderData builderData) {
                return new SizeStatic(builderData).newSizeScalarMatrix();

            }

            /* (non-Javadoc)
             * @see org.specs.MatlabToC.CirInterface.MatlabToCProvider#getInputsParser(org.specs.CIR.Functions.FunctionTypes)
             */
            @Override
            public InputsFilter getInputsFilter() {
                return Size.getInputsParser();
            }

        };
    }

    /**
     * Inputs parser for Size function
     * 
     * @param fTypes
     * @return
     */
    private static InputsFilter getInputsParser() {

        return new InputsFilter() {

            @Override
            public List<CNode> filterInputArguments(ProviderData data, List<CNode> originalArguments) {
                // Get type of first argument
                VariableType type = originalArguments.get(0).getVariableType();

                if (MatrixUtils.usesDynamicAllocation(type)) {
                    return originalArguments;
                }

                // When the given input type is a scalar, no inputs are needed
                // if (type.getType() == CType.Numeric) {
                if (ScalarUtils.isScalar(type)) {
                    return Collections.emptyList();
                }

                if (originalArguments.size() >= 2) {
                    return originalArguments;
                }

                // When the given input type is a declared matrix, the first input is not needed
                // Remove the first argument
                return originalArguments.subList(1, originalArguments.size());

            }
        };
    }

    private FunctionInstance getMultiOutputsInstance(FunctionType functionType) {
        MatrixType inputType = getData().getInputType(MatrixType.class, 0);

        CInstructionList body = new CInstructionList(functionType);
        List<String> outputAsInputNames = functionType.getOutputAsInputNames();
        List<VariableType> outputAsInputTypes = functionType.getOutputAsInputTypes();
        int numOutputs = outputAsInputTypes.size();

        MatrixType processedType;
        if (inputType instanceof DynamicMatrixType) {
            processedType = inputType.matrix().setShape(TypeShape.newUndefinedShape());
        } else {
            processedType = inputType;
        }

        CNode inputNode = CNodeFactory.newVariable(
                functionType.getArgumentsNames().get(0),
                processedType);

        int rawNumDims = inputType.matrix().getShape().getRawNumDims();
        if (numOutputs != rawNumDims) {
            int firstDim = numOutputs;

            for (int i = 0; i < firstDim - 1; ++i) {
                CNode dimNode = getFunctionCall(inputType.matrix().functions().getDim(),
                        inputNode,
                        CNodeFactory.newCNumber(i));

                body.addAssignment(CNodeFactory.newVariable(outputAsInputNames.get(i), dimNode.getVariableType()),
                        dimNode);
            }

            CNode size = CNodeFactory.newVariable("result", getNumerics().newInt());
            body.addAssignment(size, CNodeFactory.newCNumber(1));

            CNode ndimsNode = getFunctionCall(MatlabBuiltin.NDIMS.getMatlabFunction(), inputNode);

            Variable inductionVar = new Variable("i", getNumerics().newInt());

            List<CNode> bodyInstructions = new ArrayList<>();
            FunctionCallNode dimNode = getFunctionCall(
                    inputType.matrix().functions().getDim(),
                    inputNode,
                    CNodeFactory.newVariable(inductionVar));
            FunctionCallNode multiplication = getFunctionCall(COperator.Multiplication, size, dimNode);
            bodyInstructions.add(CNodeFactory.newAssignment(size, multiplication));

            body.addInstruction(new ForNodes(getData()).newForLoopBlock(inductionVar,
                    CNodeFactory.newCNumber(firstDim - 1),
                    COperator.LessThan,
                    ndimsNode,
                    COperator.Addition,
                    CNodeFactory.newCNumber(1),
                    bodyInstructions));

            body.addAssignment(
                    new Variable(SpecsCollections.last(outputAsInputNames), SpecsCollections.last(outputAsInputTypes)),
                    size);

            String functionName = "size_multiargs_" + inputType.getSmallId() + "_"
                    + numOutputs + "_generic";
            InstructionsInstance instance = new InstructionsInstance(functionName,
                    CirFilename.MATRIX.getFilename(), body);

            return instance;
        }

        for (int outputIndex = 0; outputIndex < numOutputs; ++outputIndex) {
            String outputName = functionType.getOutputAsInputNames().get(outputIndex);
            ScalarType outputType = (ScalarType) outputAsInputTypes.get(outputIndex);

            CNode leftHand = CNodeFactory.newVariable(outputName, outputType);
            CNode position = CNodeFactory.newCNumber(outputIndex + 1);
            List<CNode> arguments = Arrays.asList(inputNode, position);
            CNode rightHand = getFunctionCall(newMatrixBuilder(), arguments);
            body.addAssignment(leftHand, rightHand);
        }

        String functionName = "size_multiargs_" + inputType.getSmallId() + "_"
                + numOutputs + "_of_" + rawNumDims;
        InstructionsInstance instance = new InstructionsInstance(functionName,
                CirFilename.MATRIX.getFilename(), body);

        return instance;
    }

    public InstanceProvider getTwoArgsProvider(boolean isCodegen) {
        ProviderData builderData = getData();

        List<VariableType> inputTypes = builderData.getInputTypes();
        VariableType matrixType = inputTypes.get(0);

        // If input is static matrix, return function with static sizes
        // TODO this is not implemented yet, using previous method
        if (MatrixUtils.isStaticMatrix(matrixType)) {
            // Check if second type as a constant associated
            VariableType indexType = inputTypes.get(1);
            String constant = ScalarUtils.getConstantString(indexType);

            if (constant != null) {
                return newSizeTwoArgsStaticInlined();
            }

            if (isCodegen) {
                getData().getReportService().emitMessage(PassMessage.OPTIMIZATION_OPPORTUNITY,
                        "Unable to discover size of matrix at compile-time in call to size function, because dimension isn't constant.\nWith input types: "
                                + inputTypes);
            }
            return MFileProvider.getProvider(BaseResource.SIZE_TWO_ARGS);

        }

        // If input is dynamic matrix, return field access to the structure
        if (MatrixUtils.usesDynamicAllocation(matrixType)) {
            return newSizeTwoArgsDynamicInlined();
        }

        SpecsLogs.warn("Case not defined:" + matrixType);
        return null;
    }

    /**
     * @param data
     * @return
     */
    private InstanceProvider newSizeTwoArgsStaticInlined() {
        return this::newSizeTwoArgsStaticInlined;
    }

    private InlinedInstance newSizeTwoArgsStaticInlined(ProviderData data) {

        final List<VariableType> inputs = data.getInputTypes();

        // Should have only one argument, of type matrix
        final VariableType matrixType = CirFunctionsUtils.getMatrixTypeByIndex("size", inputs, 0);
        VariableType elementType = MatrixUtils.getElementType(matrixType);

        // Name of the function
        String functionName = "size_static_" + elementType.getSmallId();

        // Determine the value of size

        // Get matrix shape
        List<Integer> shape = MatrixUtils.getShapeDims(inputs.get(0));

        // Get index
        String constant = ScalarUtils.getConstantString(inputs.get(1));

        int integerConstant;
        try {
            integerConstant = SpecsStrings.parseIntegerRelaxed(constant);
        } catch (OverflowException e) {
            throw data.getReportService().error(e.getMessage());
        }
        // Correct integer to C index
        int cIndex = integerConstant - 1;

        final int value;
        if (cIndex >= shape.size()) {
            value = 1;
        } else {
            value = shape.get(cIndex);
        }

        FunctionType fTypes = FunctionType.newInstanceNotImplementable(inputs, getNumerics().newInt(value));

        InlineCode inlineCode = new InlineCode() {

            @Override
            public String getInlineCode(List<CNode> arguments) {
                if (arguments.size() != 2) {
                    SpecsLogs.warn("Calling this version of 'size' "
                            + "with a number of arguments different than two. ");
                }

                // return shape.get(cIndex).toString();
                return Integer.toString(value);
            }
        };

        InlinedInstance instance = new InlinedInstance(fTypes, functionName, inlineCode);

        return instance;

    }

    /**
     * @param data
     * @return
     */
    private InstanceProvider newSizeTwoArgsDynamicInlined() {
        return this::newSizeTwoArgsDynamicInlined;
    }

    private InlinedInstance newSizeTwoArgsDynamicInlined(ProviderData data) {

        List<VariableType> inputs = data.getInputTypes();

        // Should have only one argument, of type matrix
        MatrixType matrixType = (MatrixType) CirFunctionsUtils.getMatrixTypeByIndex("size", inputs, 0);
        VariableType elementType = MatrixUtils.getElementType(matrixType);
        ScalarType dimType = (ScalarType) inputs.get(1);

        // Get the type of the output, with constant if possible
        VariableType outputType = getSizeType(inputs.get(0), inputs.get(1));

        // Name of the function
        String functionName = "size_alloc_" + elementType.getSmallId();

        boolean shouldCheckSize = !dimType.scalar().hasConstant()
                || dimType.scalar().getConstant().doubleValue() > 2;

        FunctionType fTypes = FunctionType.newInstanceNotImplementable(inputs, outputType);
        InlineCode inlineCode = new InlineCode() {

            @Override
            public String getInlineCode(List<CNode> arguments) {
                if (arguments.size() != 2) {
                    SpecsLogs.warn("Calling this version of 'size' "
                            + "with a number of arguments different than two. ");
                }

                CNode strut = arguments.get(0);

                CNode index = arguments.get(1);

                // Get string and adjust index
                String indexString = index.getCodeForLeftSideOf(PrecedenceLevel.Subtraction) + " - 1";

                String inRangeCode = MatrixCode.getStructField(strut, DynamicMatrixStruct.TENSOR_SHAPE,
                        indexString);
                if (!shouldCheckSize) {
                    return inRangeCode;
                }

                String dims = MatrixCode.getStructField(strut, DynamicMatrixStruct.TENSOR_DIMS, null);

                return dims + " < " + index.getCodeForRightSideOf(PrecedenceLevel.GreaterThan) + " ? 1 : "
                        + inRangeCode;

            }
        };

        InlinedInstance instance = new InlinedInstance(fTypes, functionName, inlineCode);
        instance.setCallPrecedenceLevel(
                shouldCheckSize ? PrecedenceLevel.TernaryConditional : PrecedenceLevel.ArrayAccess);

        return instance;

    }

    /**
     * If matrix has shape, and second argument has a constant, returns an integer with constant. Otherwise, return a
     * simple integer.
     * 
     * @param variableType
     * @param indexType
     *            type that represents a MATLAB index
     * @return
     */
    private VariableType getSizeType(VariableType matrixType, VariableType indexType) {

        // Check if index has constant
        // Number indexNumber = ScalarUtils.getConstant(indexType);
        Number indexNumber = ScalarUtils.getConstant(indexType);
        if (indexNumber == null) {
            return getNumerics().newInt();
        }

        // Check if matrix has shape
        TypeShape shape = MatrixUtils.getShape(matrixType);
        if (shape == null) {
            return getNumerics().newInt();
        }

        int index = indexNumber.intValue();

        if (shape.getRawNumDims() > 0 && index - 1 >= shape.getRawNumDims()) {
            // Out of range
            return getNumerics().newInt(1);
        }

        // TODO: Have a method getDim in MatrixShape that does all this (getDim(int) that returns an Integer)
        // Get dimension
        if (shape.getDims() != null && index <= shape.getDims().size()) {
            int size = shape.getDims().get(index - 1);

            if (size >= 0) {
                return getNumerics().newInt(size);
            }
        }

        return getNumerics().newInt();
    }
}
