package org.specs.MatlabToC.Functions.BaseFunctions.Static;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.SpecsFactory;

/**
 * 
 * @author Pedro Pinto
 * 
 */
public class EyeDecBuilder implements MatlabInstanceProvider {

    public EyeDecBuilder() {
        super();
    }

    @Override
    public boolean checkRule(ProviderData builderData) {

        List<VariableType> inputTypes = builderData.getInputTypes();

        // Check if we can use declared arrays
        // if (!builderData.getSetupTable().useStaticAllocation()) {
        if (builderData.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION)) {
            return false;
        }

        // We need one to three inputs ( if only one scalar input, it will be repeated and we get a square matrix )
        if (inputTypes.size() < 1 || inputTypes.size() > 3) {
            // if (!(inputTypes.size() == 2 || inputTypes.size() == 3)) {
            return false;
        }

        // The last input must be a string
        // VariableType lastInput = inputTypes.get(inputTypes.size() - 1);
        // if (!StringTypeUtils.isString(lastInput)) {
        // return false;
        // }

        // Create a copy of the inputs without the last argument
        List<VariableType> newInputTypes = new ArrayList<>(inputTypes);
        // Remove last if string
        if (StringTypeUtils.isString(SpecsCollections.last(inputTypes))) {
            newInputTypes.remove(newInputTypes.size() - 1);
        }
        // List<VariableType> newInputTypes = inputTypes.subList(0, inputTypes.size() - 1);

        // All inputs ( but the last ) must be numeric and int
        for (VariableType input : newInputTypes) {

            // Check if it has a numeric element
            if (!ScalarUtils.hasScalarType(input)) {
                return false;
            }

            if (!ScalarUtils.isInteger(input)) {
                return false;
            }

        }

        // All inputs ( but the last ) must be constant and greater than zero
        for (VariableType input : newInputTypes) {

            // NumericData data = VariableTypeContent.getNumeric(input);
            if (!ScalarUtils.hasConstant(input)) {
                return false;
            }

            if (ScalarUtils.getConstant(input).intValue() < 1) {
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

        List<VariableType> inputTypes = builderData.getInputTypes();

        // If only one numeric input is provided, duplicate it
        int numScalars = inputTypes.size();
        if (StringTypeUtils.isString(SpecsCollections.last(inputTypes))) {
            numScalars--;
        }

        // if (inputTypes.size() == 2) {
        if (numScalars == 1) {
            inputTypes.add(1, inputTypes.get(0));
        }

        return EyeDecInstance.newInstance(builderData);
    }

    @Override
    public InputsFilter getInputsFilter() {
        return new InputsFilter() {

            /**
             * Discards all elements in 'inputArguments' and adds a declared matrix (that will be used as the return
             * argument) with the size and shape of the return type of this function.
             */
            @Override
            public List<CNode> filterInputArguments(ProviderData data, List<CNode> originalArguments) {
                // Do not use any of the arguments of the list, and add the
                // output-as-input matrix
                List<CNode> newArguments = SpecsFactory.newArrayList();

                // Update new arguments with temporary outs-as-ins
                return newArguments;
            }
        };
    }

}
