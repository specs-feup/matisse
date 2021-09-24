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

package org.specs.CIRFunctions.LibraryFunctionsBase;

import java.util.List;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;

/**
 * @author Joao Bispo
 * 
 */
public interface CLibraryFunction extends InstanceProvider {

    String getFunctionName();

    SystemInclude getLibrary();

    /**
     * The types of the inputs.
     * 
     * <p>
     * A null value represents variadic inputs that will not be checked.
     * 
     * TODO: Replace null with Optional
     * 
     * @return
     */
    List<VariableType> getInputTypes(NumericFactory numerics);

    VariableType getOutputType(NumericFactory numerics);

    default boolean checkArgs(ProviderData data) {
	return CLibraryFunctionUtils.checkArgumentTypes(this, data);
    }

    @Override
    default Optional<InstanceProvider> accepts(ProviderData data) {
	if (!checkArgs(data)) {
	    return Optional.empty();
	}

	return Optional.of(this);
    }

    @Override
    default FunctionInstance newCInstance(ProviderData data) {
	return CLibraryFunctionUtils.newInstance(this, data);
    }

    default boolean hasSideEffects() {
	return false;
    }

}