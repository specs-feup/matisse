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
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.TypeVerification;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;

import pt.up.fe.specs.util.SpecsLogs;

public class MinMaxDimDecBuilder implements MatlabInstanceProvider {

    private final MinMax minOrMax;

    public MinMaxDimDecBuilder(MinMax minOrMax) {
	this.minOrMax = minOrMax;
    }

    @Override
    public FunctionInstance create(ProviderData builderData) {

	if (!checkRule(builderData)) {
	    return null;
	}

	List<VariableType> originalTypes = builderData.getInputTypes();

	// Get the number of outputs
	// int numberOutputs = VariableTypeContent.getNumeric(originalTypes.get(3)).getIntValue();
	int numberOutputs = ScalarUtils.getConstant(originalTypes.get(3)).intValue();

	// Remove the useless types ( the empty matrix and the number of outputs )
	List<VariableType> newTypes = Arrays.asList(originalTypes.get(0), originalTypes.get(2));

	ProviderData newData = ProviderData.newInstance(builderData, newTypes);

	if (numberOutputs == 1) {
	    // return MinMaxDimDecInstance.newInstance(newTypes, minOrMax);
	    return MinMaxDimDecInstance.newInstance(newData, minOrMax);
	}

	return MinMaxDimIndexDecInstance.newInstance(newData, minOrMax);

    }

    @Override
    public boolean checkRule(ProviderData builderData) {

	// See if we can use declared matrices
	/*
	if (!builderData.getSetup().useStaticAllocation()) {
	    return false;
	}
	*/

	List<VariableType> originalTypes = builderData.getInputTypes();

	// We need 4 inputs ( matrix, [], dim, #outputs )
	if (originalTypes.size() != 4) {
	    return false;
	}

	// The first input is a numeric declared matrix
	VariableType firstInput = originalTypes.get(0);
	if (!MatrixUtils.isStaticMatrix(firstInput)) {
	    return false;
	}

	// Check if it has a numeric element
	if (!ScalarUtils.hasScalarType(firstInput)) {
	    return false;
	}

	// The second input is an empty matrix
	MatrixType secondInput = builderData.getInputType(MatrixType.class, 1);
	// if (!MatrixUtils.isEmptyMatrix(secondInput)) {
	if (!secondInput.getTypeShape().isKnownEmpty()) {
	    return false;
	}

	// The third input is an integer scalar constant > 0
	VariableType thirdInput = originalTypes.get(2);
	if (!TypeVerification.isIntegerConstant(thirdInput)) {
	    SpecsLogs.msgInfo("Error using " + minOrMax.getName()
		    + "\nDimension argument must be a positive integer scalar within indexing range.");
	    return false;
	}
	if (!TypeVerification.isIntegerConstantInRange(thirdInput, 0, null)) {
	    SpecsLogs.msgInfo("Error using " + minOrMax.getName()
		    + "\nDimension argument must be a positive integer scalar within indexing range.");
	    return false;
	}

	// The fourth input is the number of outputs of the original function call ( needs to be 1 or 2 )
	VariableType fourthInput = originalTypes.get(3);
	if (!TypeVerification.isIntegerConstant(fourthInput)) {
	    return false;
	}
	if (!TypeVerification.isIntegerConstantValid(fourthInput, 1, 2)) {
	    SpecsLogs.msgInfo("Error using " + minOrMax.getName() + "\nToo many output arguments.");
	    return false;
	}

	return true;
    }

    @Override
    public InputsFilter getInputsFilter() {
	return new InputsFilter() {

	    @Override
	    public List<CNode> filterInputArguments(ProviderData data, List<CNode> inputArguments) {

		/* We only need the first input, the matrix. The empty matrix is useless, the DIM will be 
		 * coded in the instance and the number of outputs is only useful to call the correct instance. */
		inputArguments = Arrays.asList(inputArguments.get(0));

		return inputArguments;
	    }
	};
    }
}
