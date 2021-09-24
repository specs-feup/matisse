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

package org.specs.matlabtocl.v2.functions.matlab.math;

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
import org.specs.CIR.Types.Views.Conversion.ConversionUtils;
import org.specs.matisselib.PassMessage;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;

public class DivideOperation extends AInstanceBuilder {

    private DivideOperation(ProviderData data) {
        super(data);
    }

    public static InstanceProvider getProvider() {
        return data -> new DivideOperation(data).create();
    }

    @Override
    public FunctionInstance create() {
        if (getData().getNumInputs() != 2) {
            throw getData().getReportService()
                    .emitError(PassMessage.CORRECTNESS_ERROR, "mrdivide requires 2 arguments");
        }

        // STUB
        VariableType returnType = CLNativeType.DOUBLE;

        FunctionType functionType = FunctionTypeBuilder.newInline()
                .addInputs(getData().getInputTypes())
                .returning(returnType)
                .build();

        InlineCode inlineCode = tokens -> {
            String leftCode = ConversionUtils.to(tokens.get(0), returnType)
                    .getCodeForLeftSideOf(PrecedenceLevel.Division);
            String rightCode = ConversionUtils.to(tokens.get(1), returnType)
                    .getCodeForRightSideOf(PrecedenceLevel.Division);
            return leftCode + " / " + rightCode;
        };
        FunctionInstance instance = new InlinedInstance(functionType, "$mrdivide$" + getData().getInputTypes(),
                inlineCode);
        return instance;
    }

}
