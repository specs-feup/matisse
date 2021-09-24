/**
 * Copyright 2016 SPeCS.
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

package org.specs.MatlabToC.Functions.MatisseInternalFunctions;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.MatlabToC.MatlabToCUtils;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProviderHelper;
import org.specs.MatlabToC.Utilities.MatisseChecker;
import org.specs.MatlabToC.jOptions.MatisseOptimization;

public class MatisseIsOptimizationEnabled extends AInstanceBuilder {

    private static final MatisseChecker CHECKER = new MatisseChecker()
            .numOfInputs(1)
            .isString(0)
            .numOfOutputsAtMost(1);

    public MatisseIsOptimizationEnabled(ProviderData data) {
        super(data);
    }

    public static InstanceProvider getProvider() {
        return new MatlabInstanceProviderHelper(MatisseIsOptimizationEnabled.CHECKER,
                data -> new MatisseIsOptimizationEnabled(data).create());
    }

    @Override
    public FunctionInstance create() {
        StringType argument = getData().getInputType(StringType.class, 0);

        String value = argument.getString();
        if (value == null) {
            throw new UnsupportedOperationException("MATISSE_is_optimization_enabled requires a constant argument");
        }

        MatisseOptimization opt = MatisseOptimization.valueOf(value);

        boolean isActive = MatlabToCUtils.isActive(getData().getSettings(), opt);
        VariableType returnType = getNumerics().newInt(isActive ? 1 : 0);

        FunctionType functionType = FunctionTypeBuilder
                .newInline()
                .addInput(argument)
                .returning(returnType)
                .build();

        InlineCode code = tokens -> {
            return isActive ? "1" : "0";
        };
        InlinedInstance instance = new InlinedInstance(
                functionType,
                "$MATISSE_is_optimization_enabled$" + isActive,
                code);
        instance.setCallPrecedenceLevel(PrecedenceLevel.Atom);
        return instance;
    }
}
