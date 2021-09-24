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

package org.specs.CIRFunctions.Common;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;

public class IsDefinedProvider implements InstanceProvider {

    private IsDefinedProvider() {
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        FunctionType functionType = getType(data);

        InlineCode inlineCode = new InlineCode() {
            @Override
            public String getInlineCode(List<CNode> arguments) {
                return arguments.get(0).getCodeForLeftSideOf(PrecedenceLevel.NotEqual) + " != NULL";
            }
        };
        InlinedInstance instance = new InlinedInstance(functionType, "is_defined$", inlineCode);
        instance.setCallPrecedenceLevel(PrecedenceLevel.NotEqual);

        return instance;
    }

    @Override
    public FunctionType getType(ProviderData data) {
        VariableType outputType = data.getNumerics().newInt();

        return FunctionType.newInstanceNotImplementable(data.getInputTypes(), outputType);
    }

    public static InstanceProvider create() {
        return new IsDefinedProvider();
    }
}
