/**
 * Copyright 2017 SPeCS.
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

package org.specs.matisselib;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.OutputData;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.matisselib.helpers.InputProcessor;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class InferenceResult {
    public TypedInstance instance;
    public DataStore instancedPassData;
    public boolean inPass;

    private int currentPass = 0;
    private List<OutputData> outputData;

    public InferenceResult(TypedInstance instance, DataStore passData, List<OutputData> outputData) {
        this.instance = instance;
        this.instancedPassData = passData;
        this.outputData = outputData;
    }

    public boolean accepts(ProviderData data) {
        FunctionType functionType = instance.getFunctionType();
        if (functionType.getArgumentsTypes().size() != data.getNumInputs()) {
            return false;
        }

        if (outputData == null) {
            if (data.getOutputTypes() != null) {
                return false;
            }
        } else {
            if (data.getOutputTypes() == null || outputData.size() != data.getOutputTypes().size()) {
                return false;
            }
        }
        // TODO: Deal with output types and unused output information.

        List<VariableType> argumentsTypes = functionType.getArgumentsTypes();
        for (int index = 0; index < argumentsTypes.size(); index++) {
            boolean specializeConstant = instance.isScalarConstantSpecialized(index);

            VariableType variableType = argumentsTypes.get(index);

            if (!variableType.equals(data.getInputTypes().get(index))) {
                return false;
            }

            if (variableType instanceof ScalarType && specializeConstant) {
                String variableConstant = ScalarUtils.getConstantString(variableType);
                String candidateConstant = ScalarUtils.getConstantString(data.getInputTypes().get(index));

                if (variableConstant == null) {
                    if (candidateConstant != null) {
                        return false;
                    }
                } else if (!variableConstant.equals(candidateConstant)) {
                    return false;
                }
            }

            if (variableType instanceof StringType) {
                String variableString = StringTypeUtils.getString(variableType);
                String candidateString = StringTypeUtils.getString(data.getInputTypes().get(index));

                if (!variableString.equals(candidateString)) {
                    return false;
                }
            }

            if (variableType instanceof DynamicMatrixType) {
                DynamicMatrixType matrix = (DynamicMatrixType) variableType;
                TypeShape instanceShape = InputProcessor.processDynamicMatrixInputShape(matrix);
                TypeShape newShape = InputProcessor.processDynamicMatrixInputShape(
                        data.getInputType(DynamicMatrixType.class, index));
                if (!instanceShape
                        .equals(newShape)) {

                    return false;
                }
            }
        }

        // TODO: Should we also check the outputs?
        return true;
    }

    public int getCurrentPass() {
        return currentPass;
    }

    public void nextPass() {
        ++currentPass;
    }

    public boolean isInPass() {
        return inPass;
    }

    public void setInPass(boolean inPass) {
        this.inPass = inPass;
    }
}
