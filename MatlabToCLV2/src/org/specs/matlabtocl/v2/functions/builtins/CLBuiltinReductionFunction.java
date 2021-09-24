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

package org.specs.matlabtocl.v2.functions.builtins;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.CodeGenerator.CodeGeneratorUtils;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Types.VariableType;

public enum CLBuiltinReductionFunction implements InstanceProvider {
    WORK_GROUP_REDUCE_ADD("work_group_reduce_add");

    String functionName;

    private CLBuiltinReductionFunction(String functionName) {
        this.functionName = functionName;
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        List<String> inputNames = Arrays.asList("value");
        VariableType inputType = data.getInputTypes().get(0);
        List<VariableType> inputTypes = Arrays.asList(inputType);

        InlineCode inlineCode = arguments -> CodeGeneratorUtils.functionCallCode(
                this.functionName,
                inputTypes,
                arguments);

        FunctionType functionType = FunctionType.newInstance(inputNames, inputTypes, null, inputType);

        return new InlinedInstance(functionType, this.functionName, inlineCode);
    }

}
