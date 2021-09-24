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

package org.specs.MatlabToC.Functions.MathFunctions.Static.bitshift;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Utilities.InputChecker.Check;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabIR.MatlabLanguage.NumericClassName;
import org.specs.MatlabToC.Functions.MathFunctions.Static.BitShiftDecFunctions;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;
import org.specs.MatlabToC.Utilities.MatisseChecker;

import pt.up.fe.specs.util.SpecsLogs;

/**
 * This represents the implementation of the builtin Matlab function '<i>bitshift'</i>, when the input is double (
 * matrix or scalar ) and declared ( in the case of matrices ). This implementation uses the C shift operators:
 * <code><<</code> and <code>>></code>.
 * 
 * @author Pedro Pinto
 * 
 */
public class BitshiftDoubleDecBuilder implements MatlabInstanceProvider {

    private static final MatisseChecker CHECKER = create();

    @Override
    public FunctionInstance create(ProviderData builderData) {

	List<VariableType> inputTypes = builderData.getInputTypes();

	boolean isScalar = !MatrixUtils.isStaticMatrix(inputTypes.get(0));

	if (isScalar) {
	    return BitShiftDecFunctions.newBitshiftDoubleDecScalar(builderData, inputTypes);
	}
	return BitShiftDecFunctions.newBitshiftDoubleDecMatrix(builderData, inputTypes);

    }

    @Override
    public boolean checkRule(ProviderData builderData) {
	return CHECKER.create(builderData).check();
    }

    private static MatisseChecker create() {

	// Check: If first input is a scalar, needs to be a float type,
	// or a declared double matrix
	Check firstArgCheck = data -> {
	    VariableType inputType = data.getInputTypes().get(0);
	    if (ScalarUtils.isScalar(inputType)) {
		if (ScalarUtils.isInteger(inputType)) {
		    return false;
		}
	    }

	    if (MatrixUtils.isMatrix(inputType)) {
		if (!MatrixUtils.isStaticMatrix(inputType)) {
		    return false;
		}
	    }

	    return true;
	};

	Check thirdInputCheck = data -> {
	    // If there is a third input
	    if (data.getInputTypes().size() == 3) {
		VariableType thirdInput = data.getInputTypes().get(2);

		// It MUST be a string
		if (!StringTypeUtils.isString(thirdInput)) {
		    return false;
		}

		// It MUST be part of a set of valid strings

		if (!checkStringInput(thirdInput)) {
		    SpecsLogs
			    .msgInfo("Bitshift error: The third argument ( the class string ) is not a supported type: '"
				    + thirdInput.toString() + "'.");
		    return false;
		}

	    }

	    return true;
	};

	return new MatisseChecker().
		numOfInputsRange(2, 3).
		hasScalarType(0).
		// If first input is a scalar, needs to be a float type
		addCheck(firstArgCheck).
		isInteger(1).
		addCheck(thirdInputCheck);
	/*
		List<VariableType> inputTypes = builderData.getInputTypes();

		// See if we can use declared matrices
		/*
		if (!builderData.getSetupTable().useStaticAllocation()) {
		    return false;
		}
		*/
	/*
		// We need 2 or 3 inputs
		if (!isSizeValid(inputTypes, 2, 3)) {
		    return false;
		}

		// The first input needs to be either an double scalar or a declared double matrix
		VariableType firstInput = inputTypes.get(0);
		if (!ScalarUtils.hasScalarType(firstInput)) {
		    return false;
		}

		if (ScalarUtils.isScalar(firstInput)) {
		    if (ScalarUtils.isInteger(firstInput)) {
			return false;
		    }
		}

		if (MatrixUtilsV2.isMatrix(firstInput)) {
		    if (!MatrixUtilsV2.isStaticMatrix(firstInput)) {
			return false;
		    }
		}

		// The second input needs to be an integer
		VariableType secondInput = inputTypes.get(1);

		// Check if integer
		if (!ScalarUtils.isInteger(secondInput)) {
		    return false;
		}

		// If there is a third input
		if (inputTypes.size() == 3) {
		    VariableType thirdInput = inputTypes.get(2);

		    // It MUST be a string
		    if (!StringTypeUtils.isString(thirdInput)) {
			return false;
		    }

		    // It MUST be part of a set of valid strings

		    if (!checkStringInput(thirdInput)) {
			LoggingUtils
				.msgInfo("Bitshift error: The third argument ( the class string ) is not a supported type: '"
					+ thirdInput.toString() + "'.");
			return false;
		    }

		}

		return true;
		*/
    }

    /**
     * Checks if the string passed as the third input is valid.
     * 
     * @param thirdInputString
     *            - the string of the third input
     * @return true if valid, false otherwise
     */
    private static boolean checkStringInput(VariableType thirdInput) {

	String value = StringTypeUtils.getString(thirdInput);

	NumericClassName className = NumericClassName.getNumericClassName(value);
	if (className == null) {
	    return false;
	}

	// Check if classname refers to an integer type
	if (!className.isInteger()) {
	    return false;
	}

	return true;
    }

    @Override
    public InputsFilter getInputsFilter() {

	return new InputsFilter() {

	    @Override
	    public List<CNode> filterInputArguments(ProviderData data, List<CNode> inputArguments) {

		// We only need the first 2 inputs, the matrix / scalar and the number of bits to shift
		inputArguments = Arrays.asList(inputArguments.get(0), inputArguments.get(1));

		return inputArguments;
	    }
	};
    }
}
