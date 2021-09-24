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

package org.specs.CIRTypes.Types.String.Functions;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.String.StringType;

public class Copy extends AInstanceBuilder {
    public Copy(ProviderData data) {
	super(data);
    }

    @Override
    public FunctionInstance create() {
	List<VariableType> inputTypes = getData().getInputTypes();
	StringType stringType = (StringType) inputTypes.get(0);

	FunctionType functionType = FunctionType.newInstanceNotImplementable(inputTypes, stringType);
	String functionName = "$string_copy$" + stringType.getSmallId();

	InlineCode inlineCode = arguments -> {
	    return arguments.get(1).getCode() + " = " + arguments.get(0).getCode();
	};

	return new InlinedInstance(functionType, functionName, inlineCode);
    }
}
