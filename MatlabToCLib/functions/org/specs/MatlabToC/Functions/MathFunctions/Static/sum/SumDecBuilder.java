package org.specs.MatlabToC.Functions.MathFunctions.Static.sum;

import static org.specs.CIR.TypesOld.TypeVerification.*;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;

/**
 * This implementation represents a call to the Matlab builtin function <b><i>'sum'</i></b> with a N-D matrix. To use
 * this declared matrices version the <i>DIM</i> parameter, if provided, must be a constant.
 * 
 * 
 * <b>Examples:</b> <br>
 * 
 * <pre>
 * {@code
 * s = sum(MAT, 1, 'double');
 * s = sum(MAT, 3);
 * s = sum(MAT, 2, 'native');
 * }
 * </pre>
 * 
 * @author Pedro Pinto
 * 
 */
public class SumDecBuilder implements MatlabInstanceProvider {

    @Override
    public FunctionInstance create(ProviderData builderData) {

        if (!checkRule(builderData)) {
            return null;
        }

        // Get the input types and the dimension
        // List<VariableType> inputTypes = builderData.getInputTypes();

        // Return a new instance of SumDec
        return SumDecInstance.newInstance(builderData);
    }

    @Override
    public boolean checkRule(ProviderData builderData) {

        // If we can't use declared matrices we can't use this builder and its instance
        // if (!builderData.getSetupTable().useStaticAllocation()) {
        if (builderData.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION)) {
            return false;
        }

        List<VariableType> inputTypes = builderData.getInputTypes();

        // Need 1, 2 or 3 inputs
        if (!isSizeValid(inputTypes, 1, 2, 3)) {
            return false;
        }

        // The first input needs to be a declared numeric matrix
        VariableType firstInput = inputTypes.get(0);

        if (!MatrixUtils.isStaticMatrix(firstInput)) {
            return false;
        }

        // Check if it has a numeric element
        if (!ScalarUtils.hasScalarType(firstInput)) {
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

                // And needs to have a value not greater than the number of dimensions
                int numDims = MatrixUtils.getShapeDims(firstInput).size();
                // int secondInputInt = VariableTypeContent.getNumeric(secondInput).getIntValue();
                int secondInputInt = ScalarUtils.getConstant(secondInput).intValue();

                if (secondInputInt > numDims) {
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

            // The DIM needs to have a value not greater than the number of dimensions
            int numDims = MatrixUtils.getShapeDims(firstInput).size();
            // int secondInputInt = VariableTypeContent.getNumeric(secondInput).getIntValue();
            int secondInputInt = ScalarUtils.getConstant(secondInput).intValue();

            if (secondInputInt > numDims) {
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
            public List<CNode> filterInputArguments(ProviderData data, List<CNode> inputArguments) {

                // Even if there are 3 inputs (input, DIM, class string), we only need the
                // first (the input)
                inputArguments = Arrays.asList(inputArguments.get(0));

                return inputArguments;
            }
        };
    }
}
