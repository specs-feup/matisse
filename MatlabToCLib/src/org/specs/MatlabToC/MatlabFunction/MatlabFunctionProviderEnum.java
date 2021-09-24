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

package org.specs.MatlabToC.MatlabFunction;

import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.matisselib.providers.GenericMatlabFunction;
import org.specs.matisselib.providers.MatlabFunction;
import org.specs.matisselib.providers.MatlabFunctionProvider;

/**
 * @author Joao Bispo
 *
 */
public interface MatlabFunctionProviderEnum extends MatlabFunctionProvider {

    /**
     * <p>
     * If you declare 'getProviders' abstract inside the enum, you can then implement the method in each enumeration
     * field.
     * 
     * @return
     */
    List<InstanceProvider> getProviders();

    /**
     * 
     * @return the name of the MATLAB function
     */
    String getName();

    @Override
    default MatlabFunction getMatlabFunction() {
	return new GenericMatlabFunction(getName(), getProviders(), isOutputTypeEqualToInput());
    }

}
