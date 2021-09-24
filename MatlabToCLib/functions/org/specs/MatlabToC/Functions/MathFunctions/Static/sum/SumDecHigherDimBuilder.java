package org.specs.MatlabToC.Functions.MathFunctions.Static.sum;

import static org.specs.CIR.TypesOld.TypeVerification.*;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;

/**
 * This implementation represents a call to the Matlab builtin function <b><i>'sum'</i></b> with a <i>'DIM</i>'
 * parameter that is greater than the number of dimensions of the input. To use this declared matrices version the
 * <i>DIM</i> parameter must be a constant.
 * 
 * <br>
 * <br>
 * 
 * <b>Examples:</b> <br>
 * 
 * <pre>
 * {@code
 * s = sum(v, 3, 'double');
 * s = sum(v, 6);
 * s = sum(v, 4, 'native');
 * }
 * </pre>
 * 
 * @author Pedro Pinto
 * 
 */
public class SumDecHigherDimBuilder implements MatlabInstanceProvider {

    @Override
    public boolean checkRule(ProviderData builderData) {

        // If we can't use declared matrices we can't use this builder and its instance
        // if (!builderData.getSetupTable().useStaticAllocation()) {
        if (builderData.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION)) {
            return false;
        }

        // Need 2 or 3 inputs
        List<VariableType> inputTypes = builderData.getInputTypes();
        if (!isSizeValid(inputTypes, 2, 3)) {
            return false;
        }

        // The first input needs to be a declared numeric matrix
        MatrixType firstInput = builderData.getInputType(MatrixType.class, 0);

        // It must be a declared matrix
        // if (!MatrixUtilsV2.isStaticMatrix(firstInput)) {
        if (firstInput.matrix().usesDynamicAllocation()) {
            return false;
        }

        // Check if it has a numeric element
        if (!ScalarUtils.hasScalarType(firstInput)) {
            return false;
        }

        // The second input needs to be a constant numeric int
        VariableType secondInput = inputTypes.get(1);

        if (!isIntegerConstant(secondInput)) {
            return false;
        }

        // The second input needs to have a value greater than the number of dimensions
        int numDims = MatlabToCTypesUtils.getMatlabNumDims(firstInput);
        // int secondInputInt = VariableTypeContent.getNumeric(secondInput).getIntValue();
        int secondInputInt = ScalarUtils.getConstant(secondInput).intValue();

        if (secondInputInt <= numDims) {
            return false;
        }

        // If there is a third input
        if (inputTypes.size() == 3) {

            VariableType thirdInput = inputTypes.get(2);

            // It must be a string
            if (!StringTypeUtils.isString(thirdInput)) {
                return false;
            }

            // It needs to have one of to possible values, 'native' or 'double'
            if (!isStringValid(thirdInput, "native", "double")) {
                return false;
            }
        }

        return true;
    }

    @Override
    public FunctionInstance create(ProviderData builderData) {

        if (!checkRule(builderData)) {
            return null;
        }

        return SumDecHigherDimInstance.newInstance(builderData);
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
