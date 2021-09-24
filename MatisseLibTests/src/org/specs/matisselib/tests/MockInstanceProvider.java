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

package org.specs.matisselib.tests;

import java.util.function.Function;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;

public class MockInstanceProvider implements InstanceProvider {

    private final Function<ProviderData, VariableType> outputTypeGetter;

    public MockInstanceProvider(Function<ProviderData, VariableType> outputTypeGetter) {
	this.outputTypeGetter = outputTypeGetter;
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
	throw new UnsupportedOperationException();
    }

    @Override
    public FunctionType getType(ProviderData data) {
	return FunctionTypeBuilder
		.newInline()
		.addInputs(data.getInputTypes())
		.returning(this.outputTypeGetter.apply(data))
		.build();
    }

}
