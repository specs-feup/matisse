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
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;

/**
 * This implementation represents a call to the Matlab builtin function <b><i>'sum'</i></b> with a vector ( Mx1 or 1xN )
 * when the dimension ( the <i>DIM</i> input ) is not specified.
 * 
 * <br>
 * <br>
 * 
 * <b>Examples:</b> <br>
 * 
 * <pre>
 * {@code
 * s = sum(VEC, 'double');
 * s = sum(VEC);
 * s = sum(VEC, 'native');
 * }
 * </pre>
 * 
 * @author Pedro Pinto
 */
public class SumDecVectorBuilder implements MatlabInstanceProvider {

    @Override
    public FunctionInstance create(ProviderData builderData) {
        return SumDecVectorInstance.newInstance(builderData);
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

        // If we can't use declared matrices we can't use this builder and its instance
        // if (!builderData.getSetupTable().useStaticAllocation()) {
        if (builderData.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION)) {
            return false;
        }

        // Need 1 or 2 inputs ( sum(input, class) )
        if (!isSizeValid(inputTypes, 1, 2)) {
            return false;
        }

        // The first input needs to be a numeric vector ( a Mx1 or 1xN matrix )
        // VariableType firstInput = inputTypes.get(0);
        MatrixType firstInput = builderData.getInputType(MatrixType.class, 0);
        // if (!MatrixUtilsV2.isStaticMatrix(firstInput)) {
        if (firstInput.matrix().usesDynamicAllocation()) {
            return false;
        }

        // Check if it has a numeric element
        if (!ScalarUtils.hasScalarType(firstInput)) {
            return false;
        }

        // if (!MatrixUtils.isRowVector(firstInput) && !MatrixUtils.isColumnVector(firstInput)) {
        if (firstInput.matrix().getShape().getNumDims() != 1) {
            return false;
        }

        // If there is a second input
        if (inputTypes.size() == 2) {

            VariableType secondInput = inputTypes.get(1);

            // It must be a string
            if (!StringTypeUtils.isString(secondInput)) {
                return false;
            }

            // It needs to have one of to possible values, 'native' or 'double'
            if (!isStringValid(secondInput, "native", "double")) {
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

                // Leave only one argument, the vector that will be summed and that is the first input
                originalArguments = Arrays.asList(originalArguments.get(0));

                return originalArguments;
            }
        };
    }

}
