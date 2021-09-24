package org.specs.MatlabToC.Functions.MathFunctions.Static.sum;

import static org.specs.CIR.TypesOld.TypeVerification.*;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;

/**
 * This implementation represents a call to the Matlab builtin function <b><i>'sum'</i></b> with a scalar input. When
 * the dimension ( the <i>DIM</i> parameter ) is not specified, this implementation returns a scalar ( the same as the
 * input ). When the dimension is specified this implementation returns a 1x1 matrix with the input in the first
 * position. To use this declared matrices version the <i>DIM</i> parameter, if provided, must be a constant.
 * 
 * <br>
 * <br>
 * 
 * <b>Examples:</b> <br>
 * 
 * <pre>
 * {@code
 * s = sum(v, 1, 'double');
 * s = sum(v, 3);
 * s = sum(v, 'native');
 * s = sum(v);
 * }
 * </pre>
 * 
 * @author Pedro Pinto
 * 
 */
public class SumDecScalarBuilder implements MatlabInstanceProvider {

    @Override
    public FunctionInstance create(ProviderData builderData) {

	if (!checkRule(builderData)) {
	    return null;
	}
	/*
		List<VariableType> inputTypes = builderData.getInputTypes();

		// Indicates if the DIM parameter was specified
		boolean hasDim = false;
		// Check it
		if (inputTypes.size() == 3) {
		    hasDim = true;
		} else {
		    if (inputTypes.size() == 2) {
			if (!isString(inputTypes.get(1))) {
			    hasDim = true;
			}
		    }
		}
	*/
	// return SumDecScalarInstance.newInstance(inputTypes, hasDim);
	return SumDecScalarInstance.newProvider().newCInstance(builderData);
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
	/*
	if (builderData.getSetupTable().isDynamicAllocationAllowed()) {
	    return false;
	}
	*/

	// We need 1, 2 or 3 inputs
	if (!isSizeValid(inputTypes, 1, 2, 3)) {
	    return false;
	}

	// The first input needs to be a numeric scalar
	VariableType firstInput = inputTypes.get(0);
	// if (firstInput.getType() != CType.Numeric) {
	if (!ScalarUtils.isScalar(firstInput)) {
	    return false;
	}

	// If there is a second input it needs to be either a constant numeric integer or a string
	if (inputTypes.size() == 2) {

	    VariableType secondInput = inputTypes.get(1);

	    // If it is a string it needs to have one of to possible values, 'native' or 'double'
	    if (StringTypeUtils.isString(secondInput)) {

		if (!isStringValid(secondInput, "native", "double")) {
		    return false;
		}
	    } else {
		// If not, it must be a constant numeric integer
		if (!isIntegerConstant(secondInput)) {
		    return false;
		}
	    }

	}

	// If there are three inputs the second must be a constant numeric integer and the third a string
	if (inputTypes.size() == 3) {

	    VariableType secondInput = inputTypes.get(1);
	    VariableType thirdInput = inputTypes.get(2);

	    if (!isIntegerConstant(secondInput) || !StringTypeUtils.isString(thirdInput)) {
		return false;
	    }

	    // The string needs to have one of to possible values, 'native' or 'double'
	    if (!isStringValid(thirdInput, "native", "double")) {
		return false;
	    }
	}

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
