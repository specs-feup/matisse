/**
 * Copyright 2013 SPeCS Research Group.
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

/**
 * InstaceProviders are the bridge between ProviderData and FunctionInstances. They connect one set of input types (and
 * configuration) to one or more possible FunctionInstances.
 * 
 * @author Joao Bispo
 *
 */
public interface InstanceProvider {

    /**
     * Returns the most specific InstanceProvider if the given data is apt. Otherwise returns an empty Optional.
     * 
     * <p>
     * The default method is just a wrapper that calls newCInstance, this implementation exists mostly to avoid breaking
     * compatibility.
     * 
     * @param data
     * @return
     */
    default Optional<InstanceProvider> accepts(ProviderData data) {
	return Optional.of(this);
    }

    /**
     * 
     * @param data
     *            the data necessary to create a new FunctionInstance
     * 
     * @return a FunctionInstance according to the given ProviderData. Throws an exception if the instance could not be
     *         created
     */
    FunctionInstance newCInstance(ProviderData data);

    /**
     * If true, it means that if there is a hint about the output type for this provider, it applies to the type of the
     * inputs (e.g., most operators). It should be true when the output of a function should be the same as the inputs.
     * 
     * <p>
     * As default, returns false.
     * 
     * @return
     */
    default boolean propagateOutputToInputs() {
	return false;
    }

    default FunctionInstance getCheckedInstance(ProviderData data) {
	Optional<InstanceProvider> provider = accepts(data);
	if (!provider.isPresent()) {
	    throw data.getReportService().error(
		    "At function " + getReadableName() + ": Input types not supported for " + getReadableName() + ": "
			    + data.getInputTypes()
			    + ", num outputs=" + data.getNargouts());
	}
	return accepts(data).get().newCInstance(data);
    }

    /**
     * 
     * @param data
     * @return the FunctionType of this provider for the given data
     */
    default FunctionType getType(ProviderData data) {
	InstanceProvider finalProvider = accepts(data).orElseThrow(
		() -> new UnsupportedOperationException(getClass().getName() + ": " + toString()));

	if (finalProvider != this) {
	    return finalProvider.getType(data);
	}

	return newCInstance(data).getFunctionType();
    }

    default String getReadableName() {
	return getClass().getName();
    }
}
