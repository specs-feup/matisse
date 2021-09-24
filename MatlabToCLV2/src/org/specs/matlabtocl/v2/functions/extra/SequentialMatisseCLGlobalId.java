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

package org.specs.matlabtocl.v2.functions.extra;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matisselib.PassMessage;

public class SequentialMatisseCLGlobalId implements InstanceProvider {

    @Override
    public FunctionType getType(ProviderData data) {
        if (data.getNumInputs() != 1) {
            throw data.getReportService().emitError(PassMessage.CORRECTNESS_ERROR,
                    "MATISSE_cl_global_id takes a single argument.");
        }
        if (data.getNargouts().orElse(1) > 1) {
            throw data.getReportService().emitError(PassMessage.CORRECTNESS_ERROR,
                    "MATISSE_cl_global_id takes a single output.");
        }

        if (!ScalarUtils.isInteger(data.getInputTypes().get(0))) {
            throw data.getReportService().emitError(PassMessage.CORRECTNESS_ERROR,
                    "MATISSE_cl_global_id takes an integer as argument.");
        }

        VariableType outputType = data.getOutputType();
        if (outputType == null) {
            outputType = data.getNumerics().newInt();
        } else if (!ScalarUtils.isInteger(outputType)) {
            throw data.getReportService().emitError(PassMessage.CORRECTNESS_ERROR, "Output must be an integer.");
        }

        return FunctionTypeBuilder.newInline()
                .addInput(data.getInputTypes().get(0))
                .returning(outputType)
                .withGlobalStateDependency() // Prevent loop invariant code motion
                .build();
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        throw data.getReportService().emitError(PassMessage.CORRECTNESS_ERROR,
                "MATISSE_cl_global_id can only be called in parallelized code.");
    }

}
