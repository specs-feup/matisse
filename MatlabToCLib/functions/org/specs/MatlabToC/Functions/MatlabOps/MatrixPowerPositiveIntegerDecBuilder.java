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

package org.specs.MatlabToC.Functions.MatlabOps;

import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;

public class MatrixPowerPositiveIntegerDecBuilder implements MatlabInstanceProvider {

    @Override
    public FunctionInstance create(ProviderData builderData) {

        if (!checkRule(builderData)) {
            return null;
        }

        return new MatlabOperatorsDec(builderData).newMatrixPowerPositiveIntegerIter();

    }

    @Override
    public boolean checkRule(ProviderData builderData) {

        // The input types
        List<VariableType> inputTypes = builderData.getInputTypes();

        // See if we can use declared matrices
        // if (!builderData.getSetupTable().useStaticAllocation()) {
        if (builderData.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION)) {
            return false;
        }

        // We must get exactly 2 arguments
        if (inputTypes.size() != 2) {
            return false;
        }

        // The first argument needs to be a numeric declared square matrix
        MatrixType firstInput = builderData.getInputType(MatrixType.class, 0);
        // if (!MatrixUtilsV2.isStaticMatrix(firstInput)) {
        if (firstInput.matrix().usesDynamicAllocation()) {
            return false;
        }

        // Matrices always have scalars as elements
        /*
        if (!ScalarUtils.hasScalarType(firstInput)) {
            return false;
        }
        */
        // if (!MatrixUtilsV2.isSquare(firstInput)) {
        if (!firstInput.getTypeShape().isSquare()) {
            return false;
        }

        // The second argument needs to be a numeric
        VariableType secondInput = inputTypes.get(1);
        // if (secondInput.getType() != CType.Numeric) {
        if (!ScalarUtils.isScalar(secondInput)) {
            return false;
        }
        /*
        	NumericType secondInputType = VariableTypeContent.getNumericType(secondInput);
        	
        	if (!TypeVerification.isValidNumericType(secondInputType, NumericType.getAllIntegers())) {
        	    return false;
        	}
        */
        return true;
    }
}
