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

package org.specs.MatlabToC.Functions.MathFunctions.Static.minmax;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.TypesOld.VariableTypeFactory;
import org.specs.MatlabIR.MatlabLanguage.NumericClassName;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.Functions.BaseFunctions.Static.ConstantArrayDecInstance;

import pt.up.fe.specs.util.SpecsFactory;

public class MinMaxScalarDecInstance extends AInstanceBuilder {

    private final MinMax minOrMax;
    private final boolean isMatrix;

    /**
     * @param data
     */
    public MinMaxScalarDecInstance(ProviderData data, MinMax minOrMax, boolean isMatrix) {
	super(data);

	this.minOrMax = minOrMax;
	this.isMatrix = isMatrix;
    }

    // The names
    private final static String MM_INPUT_NAME = "input_scalar";
    private final static String MM_OUTPUT_NAME_MATRIX = "output_matrix";
    private final static String MM_OUTPUT_NAME_SCALAR = "output_scalar";
    private final static String MM_BASE_FUNCTION_NAME = "_scalar_dec";

    /**
     * Creates and returns a new instance of {@link MinMaxScalarDecInstance}.
     * 
     * @param originalTypes
     * @param useLinearArrays
     * @param useSolver
     * @param minOrMax
     * @param isMatrix
     * @return
     */
    public static InstanceProvider newProvider(final MinMax minOrMax, final boolean isMatrix) {

	return new InstanceProvider() {

	    @Override
	    public FunctionInstance newCInstance(ProviderData data) {
		return new MinMaxScalarDecInstance(data, minOrMax, isMatrix).create();
	    }
	};
    }

    @Override
    public FunctionInstance create() {

	List<VariableType> originalTypes = getData().getInputTypes();

	// FIX [ MinMaxDimDecInstance.newInstance() ] : javadoc this function with the correct inputs

	// Build the function types
	FunctionType functionTypes = buildFunctionTypes(originalTypes, isMatrix);

	// return new MinMaxScalarDecInstance(functionTypes, minOrMax, willBeMatrix);

	String typesSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());
	String cFunctionName = minOrMax.getName() + MM_BASE_FUNCTION_NAME + typesSuffix;

	String cFilename = MatlabCFilename.MatlabGeneral.getCFilename();
	CInstructionList cBody = buildInstructions(functionTypes, isMatrix);

	return new InstructionsInstance(functionTypes, cFunctionName, cFilename, cBody);
    }

    /**
     * Builds the function types for this instance.
     * 
     * @param originalTypes
     * @param isMatrix
     * @param useLinearArrays
     * @return
     */
    private static FunctionType buildFunctionTypes(List<VariableType> originalTypes, boolean isMatrix) {

	VariableType input = originalTypes.get(0);

	// The input types and names
	List<VariableType> inputTypes = Arrays.asList(input);
	List<String> inputNames = Arrays.asList(MM_INPUT_NAME);

	// If the output will be a 1x1 matrix
	if (isMatrix) {

	    // The output types and names
	    VariableType outputType = VariableTypeFactory.newDeclaredMatrix(input, 1, 1);
	    String outputName = MM_OUTPUT_NAME_MATRIX;

	    FunctionType functionTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes,
		    outputName, outputType);
	    return functionTypes;
	}

	// The output types and names
	VariableType outputType = input;
	String outputName = MM_OUTPUT_NAME_SCALAR;

	FunctionType functionTypes = FunctionType.newInstance(inputNames, inputTypes, outputName, outputType);
	return functionTypes;
    }

    /**
     * Builds the instructions needed for this instance.
     * 
     * @return an instance of {@link CInstructionList}
     */
    private CInstructionList buildInstructions(FunctionType functionTypes, boolean isMatrix) {

	CInstructionList instructions = new CInstructionList(functionTypes);

	if (isMatrix) {
	    buildMatrixInstructions(instructions);
	} else {
	    buildScalarInstructions(instructions);
	}

	return instructions;
    }

    /**
     * Builds the instructions for this instance when the output will be a matrix.
     * 
     * @param instructions
     *            - the {@link CInstructionList} that this method will change
     */
    private void buildMatrixInstructions(CInstructionList instructions) {

	FunctionType functionTypes = instructions.getFunctionTypes();

	// The output and input variables and their tokens
	Variable inputVar = functionTypes.getInputVar(MM_INPUT_NAME);
	Variable outputVar = functionTypes.getInputVar(MM_OUTPUT_NAME_MATRIX);

	// CNode inputToken = CNodeFactory.newVariable(inputVar);
	// CNode outputToken = CNodeFactory.newVariable(outputVar);

	// Call zeros on the output ( zeros_1_1(output)) )
	CNode zerosFunctionCallT = buildZerosInstruction(outputVar);
	instructions.addInstruction(zerosFunctionCallT, InstructionType.FunctionCall);

	// Create the assignment ( output_matrix[0] = input_scalar; )
	// List<CNode> inputTokens = Arrays.asList(outputToken, CNodeFactory.newCNumber(0), inputToken);

	// CNode setCall = getFunctionCall(DeclaredProvider.SET, inputTokens);
	// instructions.addInstruction(setCall);

	CNode setCall = getNodes().matrix().set(outputVar, CNodeFactory.newVariable(inputVar), "0");
	instructions.addInstruction(setCall);

	// Return the output
	CNode returnT = CNodeFactory.newReturn(outputVar);
	instructions.addInstruction(returnT, InstructionType.Return);
    }

    /**
     * Builds the instructions for this instance when the output will be a scalar.
     * 
     * @param instructions
     *            - the {@link CInstructionList} that this method will change
     */
    private static void buildScalarInstructions(CInstructionList instructions) {

	FunctionType functionTypes = instructions.getFunctionTypes();

	// The output and input variables and their tokens
	Variable inputVar = functionTypes.getInputVar(MM_INPUT_NAME);
	Variable outputVar = functionTypes.getReturnVar();

	CNode inputToken = CNodeFactory.newVariable(inputVar);
	CNode outputToken = CNodeFactory.newVariable(outputVar);

	// Create the assignment
	CNode assignment = CNodeFactory.newAssignment(outputToken, inputToken);
	instructions.addAssignment(assignment);

	// Create the return
	CNode returnT = CNodeFactory.newReturn(outputToken);
	instructions.addInstruction(returnT, InstructionType.Return);
    }

    /**
     * Builds and returns the zeros function call.
     * 
     * @return a {@link CNode} with the function call
     */
    private CNode buildZerosInstruction(Variable outputVariable) {

	// The inputs for the 'zeros' implementation
	List<VariableType> zeroTypes = SpecsFactory.newArrayList();
	// zeroTypes.add(VariableTypeFactoryOld.newNumeric(NumericDataFactory.newInstance(1)));
	// zeroTypes.add(VariableTypeFactoryOld.newNumeric(NumericDataFactory.newInstance(1)));
	zeroTypes.add(getNumerics().newInt(1));
	zeroTypes.add(getNumerics().newInt(1));

	// NumericType outputInnerType = VariableTypeUtilsOld.getNumericType(outputVariable.getType());
	// NumericClassName numericClassName = MatlabToCTypes.getNumericClass(outputInnerType);
	NumericClassName numericClassName = MatlabToCTypesUtils.getNumericClass(outputVariable.getType());
	String numericString = numericClassName.getMatlabString();

	zeroTypes.add(VariableTypeFactory.newString(numericString));

	// Get the implementation of 'zeros'
	InstanceProvider zerosProvider = ConstantArrayDecInstance.newProvider("zeros", 0);
	FunctionInstance zerosImplementation = getInstance(zerosProvider, zeroTypes);
	// FunctionInstance zerosImplementation = ConstantArrayDecInstance.newInstance(zeroTypes,
	// "zeros", 0);

	// The output token
	CNode outputToken = CNodeFactory.newVariable(outputVariable);

	// Get the function call to 'zeros'
	CNode zerosCallToken = zerosImplementation.newFunctionCall(Arrays.asList(outputToken));

	return zerosCallToken;
    }

}
