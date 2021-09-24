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

package org.specs.CIR.Utilities.InstanceSelector;

import java.util.List;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;

import com.google.common.collect.Lists;

import pt.up.fe.specs.util.SpecsLogs;

/**
 * Helper class that maps several InstanceProviders to the same ProviderData.
 * 
 * @author Joao Bispo
 *
 */
public abstract class MultiInstanceProvider implements InstanceProvider {

    private final List<InstanceProvider> filters;

    public MultiInstanceProvider() {
	this.filters = Lists.newLinkedList();
    }

    /**
     * Returns a FunctionImplementation corresponding to a specialization of this function to the given types.
     * 
     * <p>
     * If a builder for the given types is not found, returns an empty optional.
     * 
     * @param data
     * @return
     */
    @Override
    // public Optional<InstanceProvider> accepts(ProviderData data) {
    public Optional<InstanceProvider> accepts(ProviderData data) {
	for (InstanceProvider filter : filters) {

	    Optional<InstanceProvider> provider = filter.accepts(data);
	    if (provider.isPresent()) {
		return provider;
	    }

	}

	return Optional.empty();
	// return OptionalUtils.findFirstNonEmpty(filters.
	// stream().
	// Create a stream that returns Optionals that have been tested with the input data
	// map(f -> f.accepts(data)));
    }

    @Override
    public FunctionType getType(ProviderData data) {
	return accepts(data)
		.orElseThrow(
			() -> new RuntimeException("Could not instantiate " + getReadableName() + ", with " + data))
		.getType(data);
    }

    /**
     * Adds a filter to the end of the filter list.
     * 
     * @param builder
     */
    public final void addFilter(InstanceProvider filter) {
	filters.add(filter);
    }

    /**
     * Adds a filter to the beginning of the filter list.
     * 
     * @param builder
     */
    public final void addFilterFirst(InstanceProvider filter) {
	filters.add(0, filter);
    }

    /**
     * 
     */
    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.InstanceProvider#getInstance(org.specs.CIR.Functions.ProviderData)
     */
    @Override
    public FunctionInstance newCInstance(ProviderData data) {
	SpecsLogs
		.msgWarn("Calling newCInstance directly on " + getReadableName()
			+ ". use getCheckedInstance of accepts instead.");
	return getCheckedInstance(data);
    }
    /* (non-Javadoc)
     * @see org.specs.CIR.FunctionInstance.InstanceProvider#newFunctionCall(org.specs.CIR.FunctionInstance.ProviderData, java.util.List)
     */
    // @Override
    // public FunctionCallToken newFunctionCall(ProviderData data, List<CToken> arguments) {
    // return accepts(data).get().newFunctionCall(data, arguments);
    // return accepts(data).get().newFunctionCall(arguments);
    // }
}
