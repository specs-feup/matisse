package org.specs.MatlabToC.Functions.MathFunctions.Static;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * 
 * Builder for the Matlab built-in function 'linspace' when the argument 'n' is not specified or when it is a constant.
 * In both cases the array 'vec' created inside the implementation can be declared statically.
 * 
 * @author Pedro Pinto
 * 
 */
public class LinspaceDecBuilder implements MatlabInstanceProvider {

    // private static final boolean IS_UNROLLED = true;

    static final String VAR_START = "start";
    static final String VAR_END = "end";
    static final String VAR_ARRAY = "array";

    public LinspaceDecBuilder() {

    }

    @Override
    public boolean checkRule(ProviderData fSig) {

        List<VariableType> argumentTypes = fSig.getInputTypes();

        // Check if it has either 2 or 3 inputs
        if (argumentTypes.size() != 2 && argumentTypes.size() != 3) {
            return false;
        }

        // Check if all of the inputs are numeric
        for (VariableType input : argumentTypes) {

            // Check if it has a numeric element
            if (!ScalarUtils.hasScalarType(input)) {
                return false;
            }

        }

        // When this function is called with the N argument
        if (argumentTypes.size() == 3) {

            // See if it is a constant
            // NumericData data = VariableTypeContent.getNumeric(argumentTypes.get(2));
            // if (!data.hasConstant()) {
            if (!ScalarUtils.hasConstant(argumentTypes.get(2))) {
                return false;
            }

            /**
             * When the argument 'n' is less than 1 this implementation will not be used. The Matlab function 'linspace'
             * returns and empty 1x0 matrix of double precision floats which is something that is yet to be considered.
             */
            // if (data.getDoubleValue() < 1.0) {
            if (ScalarUtils.getConstant(argumentTypes.get(2)).doubleValue() < 1.0) {

                SpecsLogs
                        .msgInfo(
                                "The argument 'n' with a value less than 1 is not supported by this declared implementation of 'linspace'.");
                return false;
            }
        }

        return true;
    }

    @Override
    public FunctionInstance create(ProviderData iSig) {

        if (!checkRule(iSig)) {
            return null;
        }

        List<VariableType> argumentTypes = iSig.getInputTypes();

        int n_int = getN(argumentTypes);

        return new GeneralDecFunctions(iSig.getSettings()).newLinspaceDec(n_int);
    }

    /**
     * @param argumentTypes
     * @return
     */
    private static int getN(List<VariableType> argumentTypes) {
        // The size of the array returned by 'linspace'
        double n = 100.0;

        if (argumentTypes.size() == 3) {
            // n = VariableTypeContent.getNumeric(argumentTypes.get(2)).getDoubleValue();
            n = ScalarUtils.getConstant(argumentTypes.get(2)).doubleValue();
        }

        int n_int = (int) Math.floor(n);
        return n_int;
    }

    @Override
    public InputsFilter getInputsFilter() {
        return new InputsFilter() {

            @Override
            public List<CNode> filterInputArguments(ProviderData data, List<CNode> originalArguments) {
                List<CNode> newArguments = SpecsFactory.newArrayList(originalArguments);

                /**
                 * Remove the 'n' argument. This is a specialized function implementation as we know exactly which value
                 * 'n' will have ( it will be either 100 or another constant ).
                 */
                boolean hasN = false;
                if (newArguments.size() == 3) {
                    hasN = true;
                }

                /**
                 * If 'n' equals 1, then we will also remove the 'start' argument as it is not used in the function
                 * body.
                 */
                List<VariableType> types = CNodeUtils.getVariableTypes(originalArguments);
                boolean nIsOne = getN(types) == 1;

                if (hasN) {
                    int lastIndex = newArguments.size() - 1;
                    newArguments.remove(lastIndex);
                }

                if (nIsOne) {
                    newArguments.remove(0);
                }

                return newArguments;
            }
        };
    }
}
