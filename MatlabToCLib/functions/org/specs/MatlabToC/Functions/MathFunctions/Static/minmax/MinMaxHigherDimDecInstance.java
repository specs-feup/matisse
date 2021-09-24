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
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.TypesOld.VariableTypeFactory;
import org.specs.CIRFunctions.Utilities.TransformationUtils;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.Functions.BaseFunctions.Static.ConstantArrayDecInstance;

import pt.up.fe.specs.util.SpecsFactory;

public class MinMaxHigherDimDecInstance extends AInstanceBuilder {

    private final MinMax minOrMax;

    /**
     * @param data
     */
    private MinMaxHigherDimDecInstance(ProviderData data, MinMax minOrMax) {
	super(data);

	this.minOrMax = minOrMax;
    }

    // The names
    private final static String MM_INPUT_NAME = "input_matrix";
    private final static String MM_OUTPUT_NAME = "output_matrix";
    private final static String MM_BASE_FUNCTION_NAME = "_higher_dim_dec";

    /**
     * Creates and returns a new instance of {@link MinMaxHigherDimDecInstance}.
     * 
     * @param originalTypes
     * @param useLinearArrays
     * @param useSolver
     * @param minOrMax
     * @return
     */
    public static InstanceProvider newProvider(final MinMax minOrMax) {
	return new InstanceProvider() {

	    @Override
	    public FunctionInstance newCInstance(ProviderData data) {
		return new MinMaxHigherDimDecInstance(data, minOrMax).create();
	    }
	};
    }

    @Override
    public FunctionInstance create() {

	List<VariableType> originalTypes = getData().getInputTypes();

	// FIX [ MinMaxDimDecInstance.newInstance() ] : javadoc this function with the correct inputs

	List<Integer> shape = MatrixUtils.getShapeDims(originalTypes.get(0));

	// Build the function types
	FunctionType functionTypes = buildFunctionTypes(originalTypes);

	// return new MinMaxHigherDimDecInstance(functionTypes, minOrMax, shape, settings);

	String typesSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());
	String cFunctionName = minOrMax.getName() + MM_BASE_FUNCTION_NAME + typesSuffix;
	String cFilename = MatlabCFilename.MatlabGeneral.getCFilename();
	CInstructionList cBody = buildInstructions(functionTypes, shape);

	return new InstructionsInstance(functionTypes, cFunctionName, cFilename, cBody);
    }

    /**
     * Builds the function types for this instance.
     * 
     * @param originalTypes
     * @param useLinearArrays
     * @return
     */
    private static FunctionType buildFunctionTypes(List<VariableType> originalTypes) {

	VariableType input = originalTypes.get(0);

	// The input types and names
	List<VariableType> inputTypes = Arrays.asList(input);
	List<String> inputNames = Arrays.asList(MM_INPUT_NAME);

	// The output types and names
	VariableType outputType = input;
	String outputName = MM_OUTPUT_NAME;

	FunctionType functionTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputName,
		outputType);
	return functionTypes;
    }

    /**
     * Builds the instructions needed for this instance.
     * 
     * @return an instance of {@link CInstructionList}
     */
    private CInstructionList buildInstructions(FunctionType functionTypes, List<Integer> shape) {

	CInstructionList instructions = new CInstructionList(functionTypes);

	// The input and output arrays and their tokens
	Variable inputArray = functionTypes.getInputVar(MM_INPUT_NAME);
	Variable outputArray = functionTypes.getInputVar(MM_OUTPUT_NAME);
	CNode inputVariable = CNodeFactory.newVariable(inputArray);
	CNode outputVariable = CNodeFactory.newVariable(outputArray);

	// Build the function call to zeros and add it to the instructions ( output = zeros(output_shape); )
	CNode zerosFunctionCall = buildZerosCall(functionTypes, shape, outputArray);
	instructions.addInstruction(zerosFunctionCall, InstructionType.FunctionCall);

	// Copy the contents of the input array to the output array
	CNode copyFunctionCall = buildCopyCall(inputVariable, outputVariable);
	instructions.addInstruction(copyFunctionCall, InstructionType.FunctionCall);

	// Build and add the return instruction
	CNode returnToken = CNodeFactory.newReturn(inputArray);
	instructions.addInstruction(returnToken, InstructionType.Return);

	return instructions;
    }

    /**
     * Builds and returns the zeros function call.
     * 
     * @param shape
     * @param functionTypes
     * 
     * @return a {@link CNode} with the function call
     */
    private CNode buildZerosCall(FunctionType functionTypes, List<Integer> shape, Variable outputVariable) {

	// The inputs for the 'zeros' implementation
	List<VariableType> zeroTypes = SpecsFactory.newArrayList();
	for (Integer dim : shape) {
	    // zeroTypes.add(VariableTypeFactoryOld.newNumeric(NumericDataFactory.newInstance(dim)));
	    zeroTypes.add(getNumerics().newInt(dim));
	}

	// Add the string with the element type of the matrix
	VariableType returnType = functionTypes.getCReturnType();
	// NumericType numericType = VariableTypeUtilsOld.getNumericType(returnType);
	// String classString = MatlabToCTypes.getNumericClass(numericType).getMatlabString();
	String classString = MatlabToCTypesUtils.getNumericClass(returnType).getMatlabString();
	zeroTypes.add(VariableTypeFactory.newString(classString));

	// Get the implementation of 'zeros'
	// FunctionInstance zerosImplementation = ConstantArrayDecInstance.newInstance(zeroTypes,
	// "zeros", 0);
	InstanceProvider zerosProvider = ConstantArrayDecInstance.newProvider("zeros", 0);
	FunctionInstance zerosImplementation = getInstance(zerosProvider, zeroTypes);

	// The output token
	CNode outputToken = CNodeFactory.newVariable(outputVariable);

	// Get the function call to 'zeros'
	CNode zerosCallToken = zerosImplementation.newFunctionCall(Arrays.asList(outputToken));

	return zerosCallToken;
    }

    /**
     * Builds and returns the copy function call.
     * 
     * @param inputVariable
     *            - the input array variable
     * @param outputVariable
     *            - the output array variable
     * @return a {@link CNode} with the function call
     */
    private CNode buildCopyCall(CNode inputVariable, CNode outputVariable) {

	CNode copyCall = new TransformationUtils(getData().newInstance()).parseMatrixCopy(inputVariable,
		outputVariable);

	return copyCall;
    }

}
