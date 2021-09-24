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

package org.specs.CIR.FunctionInstance;

import java.util.Optional;

import org.specs.CIR.Utilities.InputChecker.Checker;

public class GenericInstanceProvider implements InstanceProvider {

    private final Checker checker;
    private final InstanceProvider provider;

    public GenericInstanceProvider(Checker checker, InstanceProvider provider) {
	this.checker = checker;
	this.provider = provider;
    }

    @Override
    public Optional<InstanceProvider> accepts(ProviderData data) {
	// Create a checker with the new data
	Checker tempChecker = checker.create(data);

	// If does not pass checker, return empty
	if (!tempChecker.check()) {
	    return Optional.empty();
	}

	// FIXME: Check if this really works
	return provider.accepts(data);

	/*FunctionInstance instance = provider.newCInstance(data);
	if (instance == null) {
	    LoggingUtils.msgWarn("Internal problem: InstanceProvider returned null for " + provider);
	}

	return instance != null;*/
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
	return provider.newCInstance(data);
    }

    @Override
    public String getReadableName() {
	return provider.getReadableName();
    }
}
