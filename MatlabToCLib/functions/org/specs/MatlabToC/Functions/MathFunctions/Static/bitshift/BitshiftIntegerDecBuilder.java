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
 * specific language governing permissions and limitations under the License.
 */

package org.specs.MatlabToC.Functions.MathFunctions.Static.bitshift;

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
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.InputsFilter;

import pt.up.fe.specs.util.SpecsLogs;

/**
 * This represents the implementation of the builtin Matlab function '<i>bitshift'</i>, when the input is integer (
 * matrix or scalar ) and declared ( in the case of matrices ). This implementation uses the C shift operators:
 * <code><<</code> and <code>>></code>.
 * 
 * @author Pedro Pinto
 * 
 */
public class BitshiftIntegerDecBuilder implements MatlabInstanceProvider {

    @Override
    public FunctionInstance create(ProviderData builderData) {

        return BitshiftIntegerDecInstance.newInstance(builderData);
    }

    @Override
    public boolean checkRule(ProviderData builderData) {

        List<VariableType> inputTypes = builderData.getInputTypes();

        // See if we can use declared matrices
        if (builderData.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION)) {
            return false;
        }

        // We need 2 or 3 inputs
        if (!isSizeValid(inputTypes, 2, 3)) {
            return false;
        }

        // The first input needs to be either an integer scalar or a declared integer matrix
        VariableType firstInput = inputTypes.get(0);
        if (!ScalarUtils.hasScalarType(firstInput)) {
            return false;
        }

        if (ScalarUtils.isScalar(firstInput) && !ScalarUtils.isInteger(firstInput)) {
            return false;
        }

        if (MatrixUtils.isMatrix(firstInput)) {
            if (!MatrixUtils.isStaticMatrix(firstInput)) {
                return false;
            }

            VariableType elementType = MatrixUtils.getElementType(firstInput);
            if (!ScalarUtils.isInteger(elementType)) {
                return false;
            }
        }

        // The second input needs to be an integer
        VariableType secondInput = inputTypes.get(1);

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

            // It MUST represent the same type the input has
            String thirdInputString = StringTypeUtils.getString(thirdInput);

            if (!checkStringInput(firstInput, thirdInputString)) {
                SpecsLogs
                        .msgInfo(
                                "Bitshift error: The third argument ( the class string ) needs to agree with the type of the first argument.");
                return false;
            }

        }

        return true;
    }

    /**
     * Checks if the string passed as the third input represents the same type the first input has.
     * 
     * @param firstInputNumericType
     *            - the numeric type of the first input
     * @param thirdInputString
     *            - the string of the third input
     * @return true if valid, false otherwise
     */
    private static boolean checkStringInput(VariableType firstInputNumericType, String thirdInputString) {

        if (MatlabToCTypesUtils.getNumericType(firstInputNumericType).equals(thirdInputString)) {
            return true;
        }

        return false;
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