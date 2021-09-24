/**
 * Copyright 2012 SPeCS Research Group.
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
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Utilities.InputChecker.AInputsChecker;
import org.specs.MatlabToC.MatlabFunction.MatlabFunctionInstance;
import org.specs.MatlabToC.Utilities.InputsFilter;

/**
 * InstanceProvider for MATLAB functions that need InputsFilter. Also, encapsulates instances inside
 * MatlabFunctionInstance.
 * <p>
 * The main purpose of this class is to adapt existing InstanceProviders to be used as one of the possible MATLAB
 * providers in a MatlabFunction.
 * 
 * @author Joao Bispo
 * 
 */
public interface MatlabInstanceProvider extends InstanceProvider {

    /**
     * 
     * @param data
     * @return true if the ProviderData can be used to create a FunctionInstance
     */
    boolean checkRule(ProviderData data);

    /**
     * Returns an FunctionInstance for the given ProviderData, or null if the current builder could not implement the
     * instance.
     * 
     * @param providerData
     * @return
     */
    FunctionInstance create(ProviderData providerData);

    /**
     * Returns an InputsFilter, to be used to parse FunctionCall arguments.
     * 
     * <p>
     * As default, the method returns an InputParser which does not modify the input arguments.
     * 
     * @param functionTypes
     * 
     * @return
     */
    default InputsFilter getInputsFilter() {
	return InputsFilter.DEFAULT_FILTER;
    }

    @Override
    default Optional<InstanceProvider> accepts(ProviderData data) {
	if (!checkRule(data)) {
	    return Optional.empty();
	}

	return Optional.of(this);
	/*
	// Try to get an instance
	FunctionInstance instance = create(data);
	if (instance == null) {
	    return Optional.empty();
	}
	
	// return Optional.of(new MatlabChachedProvider(getInputsFilter(), instance));
	return Optional.of(new MatlabInstance.Builder(instance).filter(getInputsFilter()).create());
	*/
    }

    @Override
    default FunctionInstance newCInstance(ProviderData data) {
	// Try to get an instance

	FunctionInstance instance = create(data);
	if (instance == null) {
	    throw new RuntimeException("FunctionInstance could not be created in class '" + getClass()
		    + "', with data:\n" + data);
	}

	// return instance;
	// Add ProviderData, for additional information (e.g., output types)
	return new MatlabFunctionInstance.Builder(instance).filter(getInputsFilter()).providerData(data).create();
    }

    public static MatlabInstanceProvider create(AInputsChecker<?> checker, InstanceProvider provider,
	    InputsFilter filter) {

	return new MatlabInstanceProvider() {

	    @Override
	    public InputsFilter getInputsFilter() {
		return filter;
	    }

	    @Override
	    public FunctionInstance create(ProviderData providerData) {
		return provider.newCInstance(providerData);
	    }

	    @Override
	    public boolean checkRule(ProviderData data) {
		return checker.create(data).check();
	    }
	};

    }
}
