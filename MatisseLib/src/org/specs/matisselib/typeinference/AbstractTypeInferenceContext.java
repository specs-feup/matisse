/**
 * Copyright 2015 SPeCS.
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

package org.specs.matisselib.typeinference;

import java.util.Optional;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.services.InstructionReportingService;

public abstract class AbstractTypeInferenceContext implements TypeInferenceContext {
    @Override
    public ProviderData getProviderData() {
        return getInstance().getProviderData();
    }

    @Override
    public InstructionReportingService getInstructionReportService() {
        return getPassData().get(TypeInferencePass.INSTRUCTION_REPORT_SERVICE);
    }

    @Override
    public FunctionIdentification getFunctionIdentification() {
        return getInstance().getFunctionIdentification();
    }

    @Override
    public NumericFactory getNumerics() {
        return getProviderData().getNumerics();
    }

    private Optional<Number> getKnownScalarValue(String variableName) {
        return getVariableType(variableName).flatMap(variableType -> {
            if (variableType instanceof ScalarType) {
                ScalarType scalarType = (ScalarType) variableType;

                return Optional.ofNullable(scalarType.scalar().getConstant());
            }
            return Optional.empty();
        });
    }

    @Override
    public boolean isKnownAllTrue(String variableName) {
        return getKnownScalarValue(variableName)
                .map(value -> value.doubleValue() != 0)
                .orElse(false);
    }

    @Override
    public boolean isKnownAllFalse(String variableName) {
        return getKnownScalarValue(variableName)
                .map(value -> value.doubleValue() == 0)
                .orElse(false);
    }

    @Override
    public int getSourceBlock() {
        return -1;
    }

    @Override
    public void reachEndOfBlock(int blockId) {
    }

    @Override
    public Optional<String> getForLoopStartName() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getForLoopIntervalName() {
        return Optional.empty();
    }
}
