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
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;

import pt.up.fe.specs.util.SpecsLogs;

public class MinMaxMatrixScalarDecBuilder implements MatlabInstanceProvider {

    private final MinMax minOrMax;

    public MinMaxMatrixScalarDecBuilder(MinMax minOrMax) {
	this.minOrMax = minOrMax;
    }

    @Override
    public FunctionInstance create(ProviderData builderData) {

	if (!checkRule(builderData)) {
	    return null;
	}

	// Process the original types to remove the last input ( the number of outputs )
	List<VariableType> originalTypes = builderData.getInputTypes();
	originalTypes.remove(originalTypes.size() - 1);

	return new MinMaxFunctions(builderData, minOrMax).newMinMaxMatrixScalarDecInstance();
    }

    @Override
    public boolean checkRule(ProviderData builderData) {

	// boolean firstMatrix = false;

	// We need two inputs, one numeric declared matrix, a numeric scalar

	// We need three inputs, one numeric declared matrix, a numeric scalar and the number of outputs ( that will be
	// removed )
	List<VariableType> originalTypes = builderData.getInputTypes();

	if (originalTypes.size() != 3) {
	    // if (originalTypes.size() != 2) {
	    return false;
	}

	VariableType firstInput = originalTypes.get(0);
	VariableType secondInput = originalTypes.get(1);

	if (MatrixUtils.isStaticMatrix(firstInput)) {

	    // firstMatrix = true;

	    if (!ScalarUtils.isScalar(secondInput)) {
		return false;
	    }
	} else {
	    if (!ScalarUtils.isScalar(firstInput)) {
		return false;
	    }
	    if (!MatrixUtils.isStaticMatrix(secondInput)) {
		return false;
	    }
	}

	// The number of outputs must be exactly 1
	VariableType thirdInput = originalTypes.get(2);
	int numberOutputs = ScalarUtils.getConstant(thirdInput).intValue();

	if (numberOutputs != 1) {
	    SpecsLogs.msgInfo("Error using " + minOrMax.getName() + "\n" + minOrMax.getName().toUpperCase()
		    + " with two matrices to compare and two output arguments is not supported.");
	    return false;
	}

	// Check if matrixes element types are of type numeric
	if (!ScalarUtils.hasScalarType(firstInput)) {
	    return false;
	}

	if (!ScalarUtils.hasScalarType(secondInput)) {
	    return false;
	}

	return true;
    }

    @Override
    public InputsFilter getInputsFilter() {
	return new InputsFilter() {

	    @Override
	    public List<CNode> filterInputArguments(ProviderData data, List<CNode> inputArguments) {

		// We only need the first two inputs, the matrix and the scalar
		inputArguments = Arrays.asList(inputArguments.get(0), inputArguments.get(1));

		return inputArguments;
	    }
	};
    }
}
