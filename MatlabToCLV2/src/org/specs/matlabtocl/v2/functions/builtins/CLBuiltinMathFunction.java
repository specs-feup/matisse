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

package org.specs.matlabtocl.v2.functions.builtins;

import java.util.Locale;
import java.util.stream.Collectors;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;

public enum CLBuiltinMathFunction implements MatlabInstanceProvider {
    LOG(1),
    EXP(1),
    SIN(1),
    COS(1),
    SQRT(1),
    MIN(2),
    MIN_SIZE_TYPE(2, "min", CLNativeType.SIZE_T);

    private final int numInputs;
    private final String overrideName;
    private final CLNativeType type;

    private CLBuiltinMathFunction(int numInputs, String name, CLNativeType type) {
        this.numInputs = numInputs;
        this.type = type;
        this.overrideName = name == null ? name().toLowerCase(Locale.UK) : name;
    }

    private CLBuiltinMathFunction(int numInputs) {
        this(numInputs, null, null);
    }

    @Override
    public boolean checkRule(ProviderData data) {

        if (data.getNumInputs() != numInputs) {
            return false;
        }

        if (type != null) {
            // STUB
            return true;
        } else {
            if (data.getInputTypes().stream().distinct().count() != 1) {
                return false;
            }

            VariableType input = data.getInputTypes().get(0);
            return input instanceof CLNativeType;
        }
    }

    @Override
    public FunctionType getType(ProviderData data) {
        CLNativeType inputType = data.getInputType(CLNativeType.class, 0);

        if (type != null) {
            return FunctionTypeBuilder.newInline()
                    .addInputs(type, data.getInputTypes().size())
                    .returning(type)
                    .build();
        } else {
            return FunctionTypeBuilder.newInline()
                    .addInputs(data.getInputTypes())
                    .returning(inputType.isInteger() ? CLNativeType.DOUBLE : inputType)
                    .build();
        }
    }

    @Override
    public FunctionInstance create(ProviderData data) {
        String name = overrideName;

        FunctionType functionType = getType(data);
        InlinedInstance instance = new InlinedInstance(functionType,
                name + "$" + FunctionInstanceUtils.getTypesSuffix(functionType),
                tokens -> {
                    return name + tokens.stream()
                            .map(token -> token.getCode())
                            .collect(Collectors.joining(", ", "(", ")"));
                });

        instance.setCallPrecedenceLevel(PrecedenceLevel.FunctionCall);
        return instance;
    }
}
