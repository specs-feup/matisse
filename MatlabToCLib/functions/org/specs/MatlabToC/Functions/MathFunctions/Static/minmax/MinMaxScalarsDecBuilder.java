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
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;

public class MinMaxScalarsDecBuilder implements MatlabInstanceProvider {

    private final MinMax minOrMax;

    public MinMaxScalarsDecBuilder(MinMax minOrMax) {
	this.minOrMax = minOrMax;
    }

    @Override
    public FunctionInstance create(ProviderData builderData) {

	if (!checkRule(builderData)) {
	    return null;
	}

	// Process the original types to remove the last input ( the number of outputs )
	// List<VariableType> originalTypes = builderData.getInputTypes();
	// originalTypes.remove(originalTypes.size() - 1);

	// return MinMaxDecFunctions.newMinMaxScalarsDecInstance(originalTypes, minOrMax);
	return MinMaxFunctions.newMinMaxScalarsInstance(builderData.getInputTypes(), minOrMax);
    }

    @Override
    public boolean checkRule(ProviderData builderData) {

	List<VariableType> originalTypes = builderData.getInputTypes();

	// We need three inputs ( the last is the number of outputs and will be removed )
	// if (originalTypes.size() != 3) {
	// return false;
	// }

	// We need two inputs
	if (originalTypes.size() != 2) {
	    return false;
	}
	/*	
		// The number of outputs must be exactly 1
		VariableType thirdInput = originalTypes.get(2);
		// int numberOutputs = VariableTypeContent.getNumeric(thirdInput).getIntValue();
		int numberOutputs = ScalarUtils.getConstant(thirdInput).intValue();

		if (numberOutputs != 1) {
		    LoggingUtils.msgInfo("Error using " + minOrMax.getName() + "\n" + minOrMax.getName().toUpperCase()
			    + " with two matrices to compare and two output arguments is not supported.");
		    return false;
		}
	*/
	VariableType firstInput = originalTypes.get(0);
	VariableType secondInput = originalTypes.get(1);

	// Both numeric
	// if (firstInput.getType() != CType.Numeric || secondInput.getType() != CType.Numeric) {
	if (!ScalarUtils.isScalar(firstInput) || !ScalarUtils.isScalar(secondInput)) {
	    return false;
	}

	// And of the same type ( considering double = float )
	/*
	if (!firstInput.equals(secondInput)) {
	    // These two IFs deal with the exception of this test ( double = float )
	    if (!ScalarUtils.isInteger(firstInput) && !ScalarUtils.isInteger(secondInput)) {
		return true;
	    }

	    return false;
	}
	*/
	/*
	// And of the same type ( considering double = float )
	NumericType firstNumericType = VariableTypeContent.getNumericType(firstInput);
	NumericType secondNumericType = VariableTypeContent.getNumericType(secondInput);

	if (firstNumericType != secondNumericType) {

	    // These two IFs deal with the exception of this test ( double = float )
	    if (firstNumericType == NumericType.Float && secondNumericType == NumericType.Double) {
		return true;
	    }

	    if (firstNumericType == NumericType.Double && secondNumericType == NumericType.Float) {
		return true;
	    }

	    return false;
	}
	*/

	return true;
    }

    @Override
    public InputsFilter getInputsFilter() {
	return new InputsFilter() {

	    @Override
	    public List<CNode> filterInputArguments(ProviderData data, List<CNode> inputArguments) {

		// We only need the first two inputs, the scalars
		inputArguments = Arrays.asList(inputArguments.get(0), inputArguments.get(1));

		return inputArguments;
	    }
	};
    }
}
