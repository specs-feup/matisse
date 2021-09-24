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

package org.specs.MatlabToC.Functions;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.MatlabToC.Functions.MatisseHelperFunctions.HelperFunctionResource;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.MatlabFunction.MatlabFunctionProviderEnum;

public enum MatisseHelperFunction implements MatlabFunctionProviderEnum {
    /**
     * <p>
     * <code>y = MATISSE_raw_ind2sub(sizes, indices);</code>
     * </p>
     *
     * Similar to ind2sub(sizes, indices), except that it returns a matrix with the same number of elements as sizes,
     * rather than having multiple outputs.
     */
    MATISSE_raw_ind2sub("MATISSE_raw_ind2sub") {
	@Override
	public List<InstanceProvider> getProviders() {
	    List<InstanceProvider> providers = new ArrayList<>();

	    providers.add(MFileProvider.getProvider(HelperFunctionResource.RAW_IND2SUB));

	    return providers;
	}

    },

    /**
     * <p>
     * <code>y = MATISSE_raw_sub2ind(sizes, indices);</code>
     * </p>
     *
     * Similar to sub2ind, except indices is a single matrix, instead of a list of multiple arguments.<br/>
     * <code>sub2ind(sizes, index1, index2, ..., indexN)</code> is equivalent to
     * <code>MATISSE_raw_sub2ind(sizes, [index1, index2, ..., indexN])</code>.
     */
    MATISSE_raw_sub2ind("MATISSE_raw_sub2ind") {
	@Override
	public List<InstanceProvider> getProviders() {
	    List<InstanceProvider> providers = new ArrayList<>();

	    providers.add(MFileProvider.getProvider(HelperFunctionResource.RAW_SUB2IND));

	    return providers;
	}

    };

    private final String name;

    private MatisseHelperFunction(String name) {
	this.name = name;
    }

    @Override
    public abstract List<InstanceProvider> getProviders();

    @Override
    public String getName() {
	return this.name;
    }
}
