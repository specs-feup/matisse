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
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.VariableTypeFactory;
import org.specs.CIRFunctions.Utilities.TransformationUtils;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.Functions.BaseFunctions.Static.ConstantArrayDecInstance;

import pt.up.fe.specs.util.SpecsFactory;

public class MinMaxHigherDimIndexDecInstance extends AInstanceBuilder {

    private final MinMax minOrMax;

    /**
     * @param data
     */
    public MinMaxHigherDimIndexDecInstance(ProviderData data, MinMax minOrMax) {
	super(data);

	this.minOrMax = minOrMax;
    }

    // The names
    private final static String MM_INPUT_NAME = "input_matrix";
    private final static String MM_OUTPUT_NAME = "output_matrix";
    private final static String MM_INDEXES_NAME = "output_indexes";
    private final static String MM_BASE_FUNCTION_NAME = "_higher_dim_dec";

    /**
     * Creates and returns a new instance of {@link MinMaxHigherDimIndexDecInstance}.
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
		return new MinMaxHigherDimIndexDecInstance(data, minOrMax).create();
	    }
	};

    }

    @Override
    public FunctionInstance create() {

	// FIX [ MinMaxDimDecInstance.newInstance() ] : javadoc this function with the correct inputs

	List<VariableType> originalTypes = getData().getInputTypes();

	List<Integer> shape = MatrixUtils.getShapeDims(originalTypes.get(0));

	// Build the function types
	FunctionType functionTypes = buildFunctionTypes(originalTypes);

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
    private FunctionType buildFunctionTypes(List<VariableType> originalTypes) {

	VariableType input = originalTypes.get(0);

	// The input types and names
	List<VariableType> inputTypes = Arrays.asList(input);
	List<String> inputNames = Arrays.asList(MM_INPUT_NAME);

	// The output types and names ( the output has the same type as the input )
	List<Integer> shape = MatrixUtils.getShapeDims(input);
	VariableType intType = getNumerics().newInt();
	VariableType indexes = StaticMatrixType.newInstance(intType, shape);

	List<VariableType> outputTypes = Arrays.asList(input, indexes);
	List<String> outputNames = Arrays.asList(MM_OUTPUT_NAME, MM_INDEXES_NAME);

	FunctionType functionTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputNames,
		outputTypes);
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
	Variable indexesArray = functionTypes.getInputVar(MM_INDEXES_NAME);
	CNode inputVariable = CNodeFactory.newVariable(inputArray);
	CNode outputVariable = CNodeFactory.newVariable(outputArray);

	// Build the function call to ones and add it to the instructions ( indexes = ones(shape) )
	CNode onesFunctionCall = buildOnesCall(functionTypes, shape, indexesArray);
	instructions.addInstruction(onesFunctionCall, InstructionType.FunctionCall);

	// Build the function call to zeros and add it to the instructions ( output = zeros(shape); )
	CNode zerosFunctionCall = buildZerosCall(functionTypes, shape, outputArray);
	instructions.addInstruction(zerosFunctionCall, InstructionType.FunctionCall);

	// Copy the contents of the input array to the output array
	CNode copyFunctionCall = buildCopyCall(inputVariable, outputVariable);
	instructions.addInstruction(copyFunctionCall, InstructionType.FunctionCall);

	return instructions;
    }

    /**
     * Builds and returns the ones function call.
     * 
     * @return a {@link CNode} with the function call
     */
    private CNode buildOnesCall(FunctionType functionTypes, List<Integer> shape, Variable outputVariable) {

	// The inputs for the 'ones' implementation
	List<VariableType> onesTypes = SpecsFactory.newArrayList();
	for (Integer dim : shape) {
	    // onesTypes.add(VariableTypeFactoryOld.newNumeric(NumericDataFactory.newInstance(dim)));
	    onesTypes.add(getNumerics().newInt(dim));
	}

	// VariableTypeUtils.getNumericType(functionTypes.getCReturnType())).getMatlabString();
	VariableType numericVarType = ScalarUtils.toScalar(functionTypes.getInputVar(MM_INDEXES_NAME).getType());
	// NumericType numericType = VariableTypeUtilsOld.getNumericType(numericVarType);
	// onesTypes.add(VariableTypeFactory.newString(MatlabToCTypes.getNumericClass(numericType).getMatlabString()));
	onesTypes.add(VariableTypeFactory.newString(MatlabToCTypesUtils.getNumericClass(numericVarType).getMatlabString()));

	// Get the implementation of 'ones'
	InstanceProvider onesProvider = ConstantArrayDecInstance.newProvider("ones", 1);
	FunctionInstance onesImplementation = getInstance(onesProvider, onesTypes);
	// FunctionInstance onesImplementation = ConstantArrayDecInstance.newInstance(onesTypes,
	// "ones", 1);

	// The output token
	CNode outputToken = CNodeFactory.newVariable(outputVariable);

	// Get the function call to 'ones'
	CNode onesCallToken = onesImplementation.newFunctionCall(Arrays.asList(outputToken));

	return onesCallToken;
    }

    /**
     * Builds and returns the zeros function call.
     * 
     * @return a {@link CNode} with the function call
     */
    private CNode buildZerosCall(FunctionType functionTypes, List<Integer> shape, Variable outputVariable) {

	// The inputs for the 'zeros' implementation
	List<VariableType> zeroTypes = SpecsFactory.newArrayList();
	for (Integer dim : shape) {
	    zeroTypes.add(getNumerics().newInt(dim));
	}

	VariableType numericVarType = ScalarUtils.toScalar(functionTypes.getInputVar(MM_OUTPUT_NAME).getType());

	zeroTypes.add(VariableTypeFactory.newString(MatlabToCTypesUtils.getNumericClass(numericVarType).getMatlabString()));

	// Get the implementation of 'zeros'
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
