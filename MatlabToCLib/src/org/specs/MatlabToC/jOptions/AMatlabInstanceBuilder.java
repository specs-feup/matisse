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

package org.specs.MatlabToC.jOptions;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;

/**
 * TODO: Evaluate if this class is still needed, or if it can be replaced with AInstanceBuilder
 * 
 * @author João Bispo
 *
 */
public abstract class AMatlabInstanceBuilder extends AInstanceBuilder {

    // private final MatisseSetup mSetup;

    public AMatlabInstanceBuilder(ProviderData data) {
        super(data);

        // mSetup = new MatisseSetup(getSetup());
    }
    /*
    public MatisseSetup mSetup() {
    	return mSetup;
    }
    */
}
