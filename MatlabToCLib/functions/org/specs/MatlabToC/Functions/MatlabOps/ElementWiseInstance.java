/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.MatlabToC.Functions.MatlabOps;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRFunctions.MatrixFunction;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.Functions.MatlabOps.ElementWise.ElementWiseAllocCode;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsCollections;

/**
 * Supposed to work with both declared and allocated matrices.
 * 
 * @author Pedro Pinto
 * 
 */
public class ElementWiseInstance extends AInstanceBuilder {

    private final FunctionInstance function;

    /**
     * @param data
     */
    public ElementWiseInstance(ProviderData data, FunctionInstance function) {
        super(data);

        this.function = function;
    }

    // Names
    private static final String BASE_FUNCTION_NAME = "element_wise_";
    public static final String INPUT_NAME_PREFIX = "input_matrix_";
    public static final String OUTPUT_NAME = "output_matrix";

    public static InstanceProvider newProvider(final FunctionInstance function) {
        return new InstanceProvider() {

            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                return new ElementWiseInstance(data, function).create();
            }

            @Override
            public FunctionType getType(ProviderData data) {
                return newFunctionTypes(function, data.getInputTypes(), data.getOutputTypes());
            }
        };
    }

    @Override
    public FunctionInstance create() {

        List<VariableType> inputTypes = getData().getInputTypes();

        FunctionType functionTypes = newFunctionTypes(this.function, inputTypes, getData().getOutputTypes());

        // Get input names
        List<String> inputNames = getInputNames(inputTypes.size());
        String cFunctionName = getFunctionName(functionTypes, this.function);

        ElementWiseAllocCode allocCode = ElementWiseAllocCode.newInstance(inputTypes, inputNames,
                ElementWiseInstance.OUTPUT_NAME,
                inputTypes.size(), functionTypes.getCReturnType(), getData().newInstance());

        String cFilename = MatlabCFilename.MatrixMath.getCFilename();
        CInstructionList cBody = buildInstructions(functionTypes, this.function, allocCode);

        InstructionsInstance instance = new InstructionsInstance(functionTypes, cFunctionName, cFilename, cBody);

        if (allocCode != null) {
            instance.setCustomImplementationInstances(allocCode.getInstances());
            instance.setCustomIncludes(allocCode.getIncludes());
        }

        return instance;
    }

    private static FunctionType newFunctionTypes(FunctionInstance function, List<VariableType> originalTypes,
            List<VariableType> outputTypes) {

        String outputName = ElementWiseInstance.OUTPUT_NAME;
        VariableType outputType;

        if (outputTypes != null && outputTypes.size() == 1 && outputTypes.get(0) instanceof MatrixType) {
            outputType = outputTypes.get(0);
        } else {
            VariableType outputElementType = ScalarUtils.toScalar(function.getFunctionType().getCReturnType());

            if (outputElementType == null) {
                List<ScalarType> elementTypes = MatrixUtils.getElementTypes(originalTypes);

                outputElementType = ScalarUtils.getMaxRank(elementTypes);
                SpecsLogs.warn("CHECK IF THIS IS CORRECT");
                // The type of the matrix with the lowest priority
                // NumericType lowestPriorityNumeric = VariableTypeUtils.getLowestNumericPriority(originalTypes);
                // lowestPriorityVariable = VariableTypeFactoryOld.newNumeric(lowestPriorityNumeric);
            }

            TypeShape shape = getElementShape(originalTypes);

            // The output has the same type as the inputs, and the same shape
            // MatrixType firstInput = MatrixUtils.getFirstMatrix(originalTypes);
            MatrixType firstInput = SpecsCollections.getFirst(originalTypes, MatrixType.class);
            if (MatrixUtils.isStaticMatrix(firstInput)) {
                outputType = StaticMatrixType.newInstance(outputElementType, shape, null, null);
            } else {
                outputType = DynamicMatrixType.newInstance(outputElementType, shape);
            }

        }

        List<String> inputNames = getInputNames(originalTypes.size());
        List<VariableType> inputTypes = SpecsFactory.newArrayList(originalTypes);

        return FunctionTypeBuilder.newWithSingleOutputAsInput()
                .addInputs(inputNames, inputTypes)
                .addOutputAsInput(outputName, outputType)
                .elementWise()
                .withSideEffectsIf(function.getFunctionType().canHaveSideEffects())
                .build();
    }

    private static TypeShape getElementShape(List<VariableType> originalTypes) {
        TypeShape baseShape = TypeShape.newInstance(1, 1);

        for (VariableType type : originalTypes) {
            if (type instanceof ScalarType) {
                continue;
            }

            TypeShape shape = type.getTypeShape();
            if (shape.isFullyDefined() && shape.getNumElements() != 1) {
                return shape;
            }

            baseShape = shape;
        }

        // [1, 1] if all inputs are scalars, undefined otherwise.
        return baseShape;
    }

    private static List<String> getInputNames(int numInputs) {
        // The inputs names and types
        List<String> inputNames = SpecsFactory.newArrayList();
        for (int i = 0; i < numInputs; i++) {
            inputNames.add(ElementWiseInstance.INPUT_NAME_PREFIX + (i + 1));
        }
        return inputNames;
    }

    private CInstructionList buildInstructions(FunctionType functionTypes, FunctionInstance function,
            ElementWiseAllocCode allocCode) {

        CInstructionList instructions = new CInstructionList(functionTypes);

        // If allocCode is not null, insert check code
        if (allocCode != null) {
            instructions.addLiteralInstruction(allocCode.getCheckCode());
        }

        // The induction variable
        Variable inductionVar = new Variable("i", getNumerics().newInt());
        CNode inductionToken = CNodeFactory.newVariable(inductionVar);

        // The first input variable and its token
        Variable firstMatrixVar = getFirstMatrix(functionTypes);
        CNode firstInputMatrix = CNodeFactory.newVariable(firstMatrixVar);

        // The output variable and its token
        Variable outputVar = functionTypes.getInputVar(ElementWiseInstance.OUTPUT_NAME);
        CNode outputT = CNodeFactory.newVariable(outputVar);

        // Build the FOR loop data
        CNode startValue = CNodeFactory.newCNumber(0);
        COperator stopOp = COperator.LessThan;

        MatrixType firstMatrixType = (MatrixType) firstMatrixVar.getType();
        // List<VariableType> numelTypes = Arrays.asList(firstMatrixVar.getType());
        // List<VariableType> numelTypes = CollectionUtils.asList(firstMatrixType);

        // MatrixImplementation firstInputImplementation = MatrixUtilsV2.getImplementation(numelTypes.get(0));
        // InstanceProvider numelProvider = MatrixFunction.NUMEL.getProvider(firstInputImplementation);
        InstanceProvider numelProvider = firstMatrixType.matrix().functions().numel();
        CNode endValue = getFunctionCall(numelProvider, firstInputMatrix);

        COperator incrementOp = COperator.Addition;

        // Build the arguments for the operator
        List<CNode> opArguments = SpecsFactory.newArrayList();
        for (int i = 0; i < functionTypes.getArgumentsNames().size(); i++) {

            // The input name
            String inputName = functionTypes.getArgumentsNames().get(i);
            // The input type
            VariableType inputType = functionTypes.getArgumentsTypes().get(i);
            // The input variable
            Variable inputVariable = new Variable(inputName, inputType);
            // The input token
            CNode inputT = CNodeFactory.newVariable(inputVariable);

            // If matrix type, use a GET
            if (MatrixUtils.isMatrix(inputType)) {
                // The get for this argument
                // List<CToken> getArguments = Arrays.asList(inputT, inductionToken);
                // List<VariableType> getTypes = CTokenUtils.getVariableTypes(getArguments);
                // FunctionInstance getInstance = MatrixProvider.GET.getInstance(firstInputImplementation, getTypes);
                // FunctionInstance getInstance = MatrixProvider.GET.getInstance(
                // firstInputImplementation, data);
                // CToken getCall = getInstance.newFunctionCall(getArguments);

                InstanceProvider getProvider = MatrixFunction.GET.getProvider();
                CNode getCall = getFunctionCall(getProvider, inputT, inductionToken);

                opArguments.add(getCall);
            }
            // Just add the variable
            else {
                opArguments.add(inputT);
            }

        }

        // The call to the operator
        CNode opCallT = function.newFunctionCall(opArguments);

        // The call to the set
        // List<CToken> setArguments = Arrays.asList(outputT, inductionToken, opCallT);
        // List<VariableType> setTypes = CTokenUtils.getVariableTypes(setArguments);

        // FunctionInstance setInstance = MatrixProvider.SET.getInstance(firstInputImplementation,
        // data);
        // CToken setCallT = setInstance.newFunctionCall(setArguments);

        InstanceProvider setProvider = MatrixFunction.SET.getProvider();
        CNode setCallT = getFunctionCall(setProvider, outputT, inductionToken, opCallT);

        // Create a block with the FOR loop instruction and the set call
        CNode forLoopBlockT = new ForNodes(getData()).newForLoopBlock(inductionVar, startValue, stopOp, endValue,
                incrementOp, CNodeFactory.newCNumber(1), setCallT);
        instructions.addInstruction(forLoopBlockT, InstructionType.Block);

        // Finally add the return instruction
        instructions.addInstruction(CNodeFactory.newReturn(outputVar), InstructionType.Return);

        return instructions;
    }

    /**
     * @param functionTypes
     * @return
     */
    private static Variable getFirstMatrix(FunctionType functionTypes) {
        for (String argName : functionTypes.getArgumentsNames()) {
            Variable var = functionTypes.getInputVar(argName);
            if (MatrixUtils.isMatrix(var.getType())) {
                return var;
            }
        }

        throw new RuntimeException("Could not find a matrix type inside the input types:"
                + functionTypes.getArgumentsTypes());
    }

    private static String getFunctionName(FunctionType functionTypes, FunctionInstance function) {

        StringBuilder builder = new StringBuilder();

        builder.append(ElementWiseInstance.BASE_FUNCTION_NAME);

        builder.append(function.getCName().toLowerCase());

        VariableType returnType = functionTypes.getCReturnType();

        // If return type is declares, name needs the elementTypes and the number of elements
        if (MatrixUtils.isStaticMatrix(returnType)) {

            // Output as in is not needed
            int numInputs = functionTypes.getCInputTypes().size() - functionTypes.getNumOutsAsIns();
            for (int i = 0; i < numInputs; i++) {

                VariableType type = functionTypes.getCInputTypes().get(i);

                VariableType numericType = ScalarUtils.toScalar(type);
                if (numericType == null) {
                    throw new RuntimeException("Case not supported:" + type);
                }
                builder.append(type.getSmallId());
            }

            builder.append("_");

            builder.append(MatrixUtils.getShape(returnType).getNumElements());

        } else {
            builder.append(FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes()));
        }

        return builder.toString();
    }

}
