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

package org.specs.CIRFunctions.Utilities;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;

/**
 * @author Joao Bispo
 * 
 */
public enum UtilityProvider implements InstanceProvider {

    /**
     * Creates a new instance of the function 'sign', which returns an integer representing the sign of the given
     * number. Returns 1 if the number is positive, -1 if the number is negative and 0, if the number is zero.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A numeric type with the number to be tested;
     */
    SIGN {
	@Override
	public FunctionInstance newCInstance(ProviderData data) {

	    return new UtilityInstances(data).newSignInstance();
	}

    };

    /**
     * Creates a function instance of a matrix function based on the given list of input types.
     * 
     * @param variableType
     * 
     * @return
     */
    @Override
    public abstract FunctionInstance newCInstance(ProviderData data);

}
