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

package org.specs.MatlabToC.Functions.BaseFunctions.Static;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixTypeBuilder;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.jOptions.AMatlabInstanceBuilder;
import org.specs.MatlabToC.jOptions.MatlabInlinable;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.SpecsFactory;

/**
 * Implementation of function 'row' for declared matrixes.
 * 
 * TODO: This instance does not respect the rule that the number of input types to create the instance should be the
 * same as the number of input arguments for the function call.
 * 
 * @author Joao Bispo
 *
 */
public class RowDecNumericInstance extends AMatlabInstanceBuilder {

    /**
     * @param data
     */
    public RowDecNumericInstance(ProviderData data) {
        super(data);
    }

    private static final String BASE_NAME = "new_row";

    public static InstanceProvider getProvider() {
        return data -> new RowDecNumericInstance(data).create();
    }

    @Override
    public FunctionInstance create() {
        // Check if should inline. This function can only be inlined if it is not called inside other function, since it
        // spans several statements.
        // if (mSetup().getInlining().inline(MatlabInlinable.NEW_ROW) && getData().getFunctionCallLevel() < 2) {
        if (getSettings().get(CirKeys.INLINE).inline(MatlabInlinable.NEW_ROW) && getData().getFunctionCallLevel() < 2) {
            return newInlinedInstance();
        }

        return newFunctionInstance();
    }

    private FunctionInstance newInlinedInstance() {
        VariableType outputType = null;
        if (!getData().getOutputTypes().isEmpty()) {
            outputType = getData().getOutputTypes().get(0);
        }

        FunctionType functionTypes = newFunctionTypes(getData().getInputTypes(), outputType);

        // Make sure return type is a matrix
        assert functionTypes.getCReturnType() instanceof MatrixType;

        String cFunctionName = getFunctionName(functionTypes);
        // String cFilename = MatlabCFilename.ArrayCreatorsDec.getCFilename();
        // CInstructionList cBody = buildBodyInstructions(functionTypes);

        // Set<FunctionInstance> callInstances = Sets.newHashSet();
        // Create set function which receives the array that is being written, an a single index
        VariableType elementType = MatrixUtils.getElementType(functionTypes.getCReturnType());
        List<VariableType> setInputs = Arrays.asList(functionTypes.getCReturnType(), getNumerics().newInt(),
                elementType);

        InstanceProvider setProvider = ((MatrixType) functionTypes.getCReturnType()).matrix().functions().set();
        FunctionInstance set = getInstance(setProvider, setInputs);

        InlineCode code = buildInlinedBody(functionTypes, set);

        InlinedInstance instance = new InlinedInstance(functionTypes, cFunctionName, code);

        instance.setCallInstances(set);

        return instance;

    }

    // private InlineCode buildInlinedBody(final FunctionTypes functionTypes, final Set<FunctionInstance> callInstances)
    // {
    private static InlineCode buildInlinedBody(final FunctionType functionTypes, final FunctionInstance setInstance) {
        return arguments -> {

            CInstructionList insts = new CInstructionList(functionTypes);

            // StringBuilder builder = new StringBuilder();

            // Build array variable
            CNode arrayVar = SpecsCollections.last(arguments);
            // Variable arrayVar = CTokenContent.getVariable(arrayVarToken);

            List<CNode> values = arguments.subList(0, arguments.size() - 1);

            // Build assignment instructions
            for (int i = 0; i < values.size(); i++) {

                // Array access index
                CNode index = CNodeFactory.newCNumber(i);

                // Build set
                CNode set = FunctionInstanceUtils.getFunctionCall(setInstance, arrayVar, index, values.get(i));
                // CToken set = new MatrixUtils(getProviderSetup()).newSet(arrayVar, index, values.get(i));

                // Build instruction unless it is last instruction
                /*
                if (i != values.size() - 1) {
                set = CTokenFactory.newInstruction(InstructionType.FunctionCall, set);
                }
                 */

                // Add instruction
                insts.addInstruction(set);
                // builder.append(CodeGeneratorUtils.tokenCode(set));

                // Add dependencies
                // CTokenUtils.collectInstances(set, callInstances);
            }

            return insts.toString();

            // return builder.toString();
            // System.out.println("CODE:" + builder.toString());

            // System.out.println("ARGS:" + arguments);
            // throw new RuntimeException("STOP!");

        };
    }

    private FunctionInstance newFunctionInstance() {

        VariableType outputType = null;
        // if (!getData().getOutputTypes().isEmpty()) {
        if (getData().hasOutputTypes()) {
            outputType = getData().getOutputTypes().get(0);
        }

        FunctionType functionTypes = newFunctionTypes(getData().getInputTypes(), outputType);
        String cFunctionName = getFunctionName(functionTypes);
        String cFilename = MatlabCFilename.ArrayCreatorsDec.getCFilename();
        CInstructionList cBody = buildBodyInstructions(functionTypes);

        return new InstructionsInstance(functionTypes, cFunctionName, cFilename, cBody);

        // return new RowDecNumericInstance(newFunctionTypes(argumentTypes, useLinearMatrices));
    }

    private FunctionType newFunctionTypes(List<VariableType> argumentTypes, VariableType outputType) {

        List<Integer> shape = SpecsFactory.newArrayList();
        if (argumentTypes.isEmpty()) {
            // argumentTypes is empty, create a 0x0 matrix
            shape.add(0);
            shape.add(0);

        } else {
            // Dimension is 1 row x #arguments columns
            shape.add(1);
            shape.add(argumentTypes.size());
        }

        // Extract constant values, if present
        List<Number> values = new ArrayList<>();
        boolean allConstants = true;
        for (VariableType arg : argumentTypes) {
            Number value = ((ScalarType) arg).scalar().getConstant();

            if (value == null) {
                allConstants = false;
                break;
            }

            values.add(value);
        }

        TypeShape matrixShape = null;
        if (allConstants) {
            matrixShape = TypeShape.newShapeWithValues(shape, values);
        } else {
            matrixShape = TypeShape.newInstance(shape);
        }

        // VariableType type = VariableTypeFactoryG.newDouble();
        ScalarType type = getNumerics().newDouble();
        if (!argumentTypes.isEmpty()) {
            // Return type is a Matrix with of the numeric type with higher
            // priority,
            // type = VariableTypeUtilsG.getMaximalFit(argumentTypes);
            type = ScalarUtils.getMaxRank(ScalarUtils.cast(argumentTypes));
        }

        // Input types are in the same number as argumentTypes and equal to the
        // numeric type with highest priority
        List<VariableType> inputTypes = SpecsFactory.newArrayList();
        List<String> inputNames = SpecsFactory.newArrayList();
        for (int i = 0; i < argumentTypes.size(); i++) {
            inputTypes.add(type);
            inputNames.add("arg" + i);
        }

        String returnName = "output_array";

        // VariableType returnType = VariableTypeFactory.newDeclaredMatrix(type, shape);
        VariableType returnType = null;

        // Check if type was already defined
        if (outputType != null && MatrixUtils.isStaticMatrix(outputType)) {
            // if (outputType != null && MatrixUtils.isDeclaredMatrix(outputType)) {
            returnType = outputType;
        } else {
            returnType = StaticMatrixTypeBuilder
                    .fromElementTypeAndShape(type, matrixShape)
                    .build();
        }

        // Expecting a matrix to be passed as output as input
        FunctionType fTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, returnName,
                returnType);

        return fTypes;
    }

    /**
     * 
     */
    private CInstructionList buildBodyInstructions(FunctionType functionTypes) {

        CInstructionList instructions = new CInstructionList(functionTypes);

        // Build array variable
        String arrayName = functionTypes.getOutputAsInputNames().get(0);
        VariableType arrayType = functionTypes.getOutputAsInputTypesNormalized().get(0);
        Variable arrayVar = new Variable(arrayName, arrayType);

        // Build assignment instructions
        for (int i = 0; i < functionTypes.getArgumentsNames().size(); i++) {
            // Build right hand variable token
            String varName = functionTypes.getArgumentsNames().get(i);
            VariableType varType = functionTypes.getArgumentsTypes().get(i);
            CNode rhVar = CNodeFactory.newVariable(varName, varType);

            // Array access index
            // CNode index = CNodeFactory.newCNumber(i);

            // Build set
            // CNode set = new MatrixUtils(getProviderSetup()).newSet(arrayVar, index, rhVar);
            CNode set = getNodes().matrix().set(arrayVar, rhVar, Integer.toString(i));

            // Add instruction
            instructions.addInstruction(set);
        }

        // Add return
        CNode returnVar = CNodeFactory.newVariable(arrayVar);
        instructions.addInstruction(CNodeFactory.newReturn(returnVar), InstructionType.Return);

        return instructions;
    }

    private static String getFunctionName(FunctionType functionTypes) {
        StringBuilder builder = new StringBuilder();

        builder.append(RowDecNumericInstance.BASE_NAME);

        // Append information about the numeric type and the number of arguments
        VariableType arrayType = functionTypes.getOutputAsInputTypesNormalized().get(0);
        VariableType numericType = ScalarUtils.toScalar(arrayType);
        builder.append("_");
        builder.append(numericType.getSmallId());

        builder.append("_");
        builder.append(functionTypes.getArgumentsNames().size());

        return builder.toString();

    }

}
