/**
 * Copyright 2014 SPeCS.
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

package org.specs.MatlabToC.Utilities;

import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Utilities.InputChecker.AInputsChecker;
import org.specs.CIR.Utilities.InputChecker.Check;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;

public class MatisseChecker extends AInputsChecker<MatisseChecker> {

    public MatisseChecker() {
        super();
    }

    public MatisseChecker(ProviderData data) {
        super(data);
    }

    private MatisseChecker(ProviderData data, List<Check> checks) {
        super(data, checks);
    }

    @Override
    public MatisseChecker create(ProviderData data) {
        return new MatisseChecker(data, getChecks());
    }

    /**
     * 
     * @return true if dynamic memory allocation is allowed
     */
    public MatisseChecker dynamicAllocationEnabled() {
        Check check = (ProviderData data) -> data.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION);
        addCheckPrivate(check);
        return this;
    }

    public MatisseChecker dynamicOutput() {
        Check check = (ProviderData data) -> {
            if (!data.getOutputTypes().isEmpty()) {
                VariableType outputType = data.getOutputTypes().get(0);
                if (outputType != null && outputType instanceof StaticMatrixType) {
                    return false;
                }
            }

            return data.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION);
        };
        addCheckPrivate(check);
        return this;
    }

    public MatisseChecker areConstant() {
        Check check = (ProviderData data) -> {
            for (VariableType input : data.getInputTypes()) {
                if (!ScalarUtils.hasConstant(input)) {
                    return false;
                }
            }

            return true;
        };
        addCheckPrivate(check);
        return this;
    }

    public MatisseChecker hasConstantValue(int index, int value) {
        Check check = (ProviderData data) -> {
            if (data.getNumInputs() <= index) {
                return false;
            }
            VariableType type = data.getInputTypes().get(index);
            if (!(type instanceof ScalarType)) {
                return false;
            }

            ScalarType scalarType = (ScalarType) type;
            Number number = scalarType.scalar().getConstant();
            if (number == null) {
                return false;
            }

            return number.intValue() == number.doubleValue() && number.intValue() == value;
        };

        addCheckPrivate(check);
        return this;
    }
}
