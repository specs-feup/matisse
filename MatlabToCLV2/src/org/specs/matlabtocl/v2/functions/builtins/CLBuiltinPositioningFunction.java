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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.specs.CIR.CodeGenerator.CodeGeneratorUtils;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matlabtocl.v2.MatisseCLSettingsKeys;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;

public enum CLBuiltinPositioningFunction implements InstanceProvider {
    GET_GLOBAL_ID("get_global_id"),
    GET_GLOBAL_SIZE("get_global_size"),
    GET_LOCAL_ID("get_local_id"),
    GET_LOCAL_SIZE("get_local_size", data -> {
        List<VariableType> inputs = data.getInputTypes();
        CLNativeType baseType = CLNativeType.SIZE_T;
        if (inputs.size() != 1) {
            return baseType;
        }

        VariableType input = inputs.get(0);
        Number number = ScalarUtils.getConstant(input);
        if (number == null) {
            return baseType;
        }

        int dim = number.intValue();

        Integer[] groupSizes = data.getSettings().get(MatisseCLSettingsKeys.GROUP_SIZES);
        if (groupSizes == null) {
            return baseType;
        }

        if (groupSizes.length > dim) {
            Integer part = groupSizes[dim];
            if (part != null) {
                return baseType.scalar().setConstant(part);
            }
        } else {
            return baseType.scalar().setConstant(1);
        }
        return baseType;
    }),
    GET_GROUP_ID("get_group_id"),
    GET_SUB_GROUP_LOCAL_ID("get_sub_group_local_id", false),
    GET_SUB_GROUP_ID("get_sub_group_id", false),
    GET_NUM_SUB_GROUPS("get_num_sub_groups", false),
    GET_SUB_GROUP_SIZE("get_sub_group_size", false);

    final String functionName;
    final boolean hasArgument;
    final Function<ProviderData, VariableType> processReturnType;

    private CLBuiltinPositioningFunction(String functionName,
            Function<ProviderData, VariableType> processReturnType) {
        this(functionName, true, processReturnType);
    }

    private CLBuiltinPositioningFunction(String functionName) {
        this(functionName, true, null);
    }

    private CLBuiltinPositioningFunction(String functionName, boolean hasArgument) {
        this(functionName, hasArgument, null);
    }

    private CLBuiltinPositioningFunction(String functionName, boolean hasArgument,
            Function<ProviderData, VariableType> processReturnType) {

        if (processReturnType == null) {
            processReturnType = providerData -> null;
        }

        this.functionName = functionName;
        this.hasArgument = hasArgument;
        this.processReturnType = processReturnType;
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        final List<String> inputNames = hasArgument ? Arrays.asList("dimidx") : Collections.emptyList();
        final List<VariableType> inputTypes = hasArgument ? Arrays.asList(CLNativeType.UINT) : Collections.emptyList();
        VariableType returnType = CLNativeType.SIZE_T;

        VariableType overrideReturnType = processReturnType.apply(data);
        if (overrideReturnType != null) {
            returnType = overrideReturnType;
        }

        InlineCode inlineCode = arguments -> CodeGeneratorUtils.functionCallCode(
                this.functionName,
                inputTypes,
                arguments);

        FunctionType functionType = FunctionType.newInstance(inputNames, inputTypes, null, returnType);

        InlinedInstance instance = new InlinedInstance(functionType, this.functionName, inlineCode);
        instance.setCallPrecedenceLevel(PrecedenceLevel.FunctionCall);
        return instance;
    }
}
