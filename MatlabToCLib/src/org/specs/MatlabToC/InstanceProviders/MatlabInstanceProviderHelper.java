/**
 * Copyright 2014 SPeCS.
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

package org.specs.MatlabToC.InstanceProviders;

import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Utilities.InputChecker.Checker;
import org.specs.MatlabToC.Utilities.InputsFilter;

public class MatlabInstanceProviderHelper implements MatlabInstanceProvider {

    private final Checker checker;
    private final InstanceProvider provider;
    private final Optional<InputsFilter> filter;

    public MatlabInstanceProviderHelper(Checker checker, InstanceProvider provider,
	    InputsFilter filter) {

	this.checker = checker;
	this.provider = provider;
	this.filter = Optional.ofNullable(filter);
    }

    public MatlabInstanceProviderHelper(Checker checker, InstanceProvider provider) {
	this(checker, provider, null);
    }

    @Override
    public boolean checkRule(ProviderData data) {
	return checker.create(data).check();
    }

    @Override
    public FunctionInstance create(ProviderData providerData) {
	return provider.getCheckedInstance(providerData);
    }

    @Override
    public FunctionType getType(ProviderData data) {
	return provider.getType(data);
    }

    @Override
    public InputsFilter getInputsFilter() {
	if (filter.isPresent()) {
	    return filter.get();
	}

	return MatlabInstanceProvider.super.getInputsFilter();
    }

}
