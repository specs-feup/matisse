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

package org.specs.matlabtocl.v2.functions.matlab.math;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;

public class CastIntegerToIntegerFunction implements MatlabInstanceProvider {
    private final CLNativeType type;

    public CastIntegerToIntegerFunction(CLNativeType type) {
	this.type = type;
    }

    @Override
    public boolean checkRule(ProviderData data) {
	if (data.getNumInputs() != 1) {
	    return false;
	}

	if (data.getNargouts().orElse(1) != 1) {
	    return false;
	}

	VariableType inputType = data.getInputTypes().get(0);
	if (!(inputType instanceof CLNativeType)) {
	    return false;
	}

	return true;
    }

    @Override
    public FunctionType getType(ProviderData data) {
	return FunctionTypeBuilder.newInline()
		.addInput(data.getInputTypes().get(0))
		.returning(this.type)
		.build();
    }

    @Override
    public FunctionInstance create(ProviderData providerData) {
	FunctionType functionType = getType(providerData);

	InlineCode code = tokens -> {
	    return "(" + this.type.code().getSimpleType() + ") "
		    + tokens.get(0).getCodeForContent(PrecedenceLevel.Cast);
	};

	String functionName = "cast_to$" + FunctionInstanceUtils.getTypesSuffix(functionType);
	InlinedInstance instance = new InlinedInstance(functionType, functionName, code);
	instance.setCallPrecedenceLevel(PrecedenceLevel.Cast);

	return instance;
    }
}
