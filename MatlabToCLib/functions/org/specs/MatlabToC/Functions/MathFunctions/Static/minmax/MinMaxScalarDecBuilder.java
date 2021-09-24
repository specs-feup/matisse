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

import static org.specs.CIR.TypesOld.TypeVerification.*;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;

public class MinMaxScalarDecBuilder implements MatlabInstanceProvider {

    private final MinMax minOrMax;

    public MinMaxScalarDecBuilder(MinMax minOrMax) {
	this.minOrMax = minOrMax;
    }

    @Override
    public FunctionInstance create(ProviderData builderData) {

	if (!checkRule(builderData)) {
	    return null;
	}

	// List<VariableType> originalTypes = builderData.getInputTypes();

	boolean isMatrix = false;
	/*
		if (originalTypes.size() == 4) {
		    isMatrix = true;
		}
	*/
	// Remove the useless types ( all but the scalar input )
	// List<VariableType> newTypes = Arrays.asList(originalTypes.get(0));

	// ProviderData newData = ProviderData.newInstance(builderData, newTypes);
	// return MinMaxScalarDecInstance.newProvider(minOrMax, isMatrix).create(newData);
	return MinMaxScalarDecInstance.newProvider(minOrMax, isMatrix).newCInstance(builderData);
	// return MinMaxScalarDecInstance.newInstance(newTypes, minOrMax, isMatrix);
    }

    /**
     * Checks if this builder can be used for the given inputs.
     * 
     * @param builderData
     * @return
     */
    @Override
    public boolean checkRule(ProviderData builderData) {

	List<VariableType> inputTypes = builderData.getInputTypes();

	// See if we can use declared matrices
	// if (!builderData.getSetupTable().useStaticAllocation()) {
	// if (builderData.getSetupTable().isDynamicAllocationAllowed()) {
	// / return false;
	// }

	// We need 2 or 4 inputs
	// if (!isSizeValid(inputTypes, 2, 4)) {
	// 1 input
	if (!isSizeValid(inputTypes, 1)) {
	    return false;
	}

	// The first input needs to be a numeric scalar
	VariableType firstInput = inputTypes.get(0);
	// if (firstInput.getType() != CType.Numeric) {
	if (!ScalarUtils.isScalar(firstInput)) {
	    return false;
	}

	// The second can't be a non_empty declared matrix ( or this would be a call: A = max(s,M), which belong in
	// another builder )
	/*
	VariableType secondInput = inputTypes.get(1);
	if (MatrixUtilsV2.isStaticMatrix(secondInput)) {
	    if (!MatrixUtils.isEmptyMatrix(secondInput)) {
		return false;
	    }
	}
	*/

	return true;
    }

    @Override
    public InputsFilter getInputsFilter() {
	return new InputsFilter() {

	    @Override
	    public List<CNode> filterInputArguments(ProviderData data, List<CNode> originalArguments) {

		// Leave only one argument, the scalar
		originalArguments = Arrays.asList(originalArguments.get(0));

		return originalArguments;
	    }
	};
    }

}
