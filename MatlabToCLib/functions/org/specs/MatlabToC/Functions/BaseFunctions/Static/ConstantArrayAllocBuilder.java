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

package org.specs.MatlabToC.Functions.BaseFunctions.Static;

import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRFunctions.Utilities.UtilityInstances;
import org.specs.CIRTypes.Types.StdInd.StdIntFactory;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabToC.Functions.BaseFunctions.Dynamic.ArrayAllocFunctions;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Builder for 'zeros' function, when using declared matrixes.
 * 
 * <p>
 * It's a matrix creator function for declared implementation, which means that returns a matrix, which is given as an
 * input to the function.
 * 
 * @author Joao Bispo
 * 
 */
public class ConstantArrayAllocBuilder implements MatlabInstanceProvider {

    private final String functionBaseName;
    private final CNode setValue;

    /**
     * 
     * @param returnType
     */
    public ConstantArrayAllocBuilder(String functionName, CNode setValue) {
        functionBaseName = functionName;
        this.setValue = setValue;
    }

    /**
     * Rules for 'inputTypes': <br>
     * - All elements of 'inputTypes' but the last (optional) must be of type NumericType (constant or variable), from
     * which an integer is extracted;<br>
     * - If the last type is not a NumericType, it must be a constant String with a value as defined in
     * 'NumericClassName';<br>
     */
    /* (non-Javadoc)
     * @see org.specs.CIR.Function.FunctionBuilder#checkRule(java.util.List)
     */
    @Override
    public boolean checkRule(ProviderData fSig) {

        // Check if allocated arrays are permitted
        if (!fSig.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION)) {
            return false;
        }

        // Get input types
        List<VariableType> argumentTypes = fSig.getInputTypes();

        // Check that last argument is of type String
        VariableType lastArg = argumentTypes.get(argumentTypes.size() - 1);
        boolean hasString = false;
        if (StringTypeUtils.isString(lastArg)) {
            hasString = true;
        }

        // Determine last index to check for NumericTypes
        int lastIndex = argumentTypes.size() - 1;
        if (hasString) {
            lastIndex -= 1;
        }

        for (int i = 0; i <= lastIndex; i++) {
            VariableType type = argumentTypes.get(i);

            // Check if it has a numeric element
            if (!ScalarUtils.hasScalarType(type)) {
                return false;
            }

            // Check that is not a matrix
            if (MatrixUtils.isMatrix(type)) {
                return false;
            }

        }

        return true;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Function.FunctionBuilder#create(org.specs.CIR.Function.FunctionPrototype, java.util.List)
     */
    @Override
    public FunctionInstance create(ProviderData data) {

        // If input only has one numeric value, duplicate it
        // e.g., zeros(3) <==> zeros(3, 3)
        List<ScalarType> scalars = data.getInputTypesOf(ScalarType.class);

        if (scalars.size() == 1) {
            VariableType firstType = data.getInputTypes().get(0);
            // Adding after the first element, there can be a string as last argument
            data.getInputTypes().add(1, firstType.copy());
        }

        return ArrayAllocFunctions.newConstantHelper(functionBaseName, setValue).newCInstance(data);

    }

    @Override
    public InputsFilter getInputsFilter() {
        return new InputsFilter() {

            @Override
            public List<CNode> filterInputArguments(ProviderData data, List<CNode> inputArguments) {

                // Check if last argument is of type String
                CNode lastArg = inputArguments.get(inputArguments.size() - 1);
                VariableType lastArgType = lastArg.getVariableType();

                // Check if last argument is a String
                boolean hasString = false;
                if (StringTypeUtils.isString(lastArgType)) {
                    hasString = true;
                }

                // Create new list with updated arguments
                int lastIndexExclusive = inputArguments.size();
                if (hasString) {
                    lastIndexExclusive -= 1;
                }

                List<CNode> newArguments = SpecsFactory.newArrayList(inputArguments.subList(0, lastIndexExclusive));

                // If arguments has size one, add a copy of the token, to generate a square matrix
                if (newArguments.size() == 1) {
                    CNode copy = newArguments.get(0).copy();
                    newArguments.add(copy);
                }

                // Add casts if arguments are not of type integer
                for (int i = 0; i < newArguments.size(); i++) {
                    CNode arg = newArguments.get(i);
                    VariableType type = arg.getVariableType();

                    boolean isInteger = ScalarUtils.isInteger(type);
                    if (isInteger) {
                        continue;
                    }

                    // Add cast to integer
                    VariableType int32Type = StdIntFactory.newInt32();
                    FunctionInstance castFunction = UtilityInstances.newCastToScalar(type, int32Type);

                    CNode fcall = CNodeFactory.newFunctionCall(castFunction, arg);
                    newArguments.set(i, fcall);
                }

                // Add set value
                return newArguments;
            }
        };
    }
}
