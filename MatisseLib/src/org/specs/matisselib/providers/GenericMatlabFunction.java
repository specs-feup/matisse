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

package org.specs.matisselib.providers;

import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;

/**
 * Simple concrete implementation of class MatlabFunction, which receives a name and a list of InstanceProviders.
 * 
 * <p>
 * This class should be adequate for most common cases.
 * 
 * @author Joao Bispo
 * 
 */
public class GenericMatlabFunction extends MatlabFunction {

    private final String functionName;
    private final boolean isOutputTypeEqualToInputs;

    /**
     * @param functionName
     */
    public GenericMatlabFunction(String functionName, List<InstanceProvider> builders) {
	this(functionName, builders, false);
    }

    public GenericMatlabFunction(String functionName, List<InstanceProvider> builders, boolean isOutputTypeEqualToInputs) {
	this.functionName = functionName;
	this.isOutputTypeEqualToInputs = isOutputTypeEqualToInputs;

	// Add builders
	for (InstanceProvider builder : builders) {
	    addFilter(builder);
	}
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Function.FunctionPrototype#getFunctionName()
     */
    @Override
    public String getFunctionName() {
	return functionName;
    }

    @Override
    public boolean propagateOutputToInputs() {
	return isOutputTypeEqualToInputs;
    }

    @Override
    public String getReadableName() {
	return functionName;
    }
}
