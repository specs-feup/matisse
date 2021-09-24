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

package org.specs.MatlabToC.MFileInstance;

/**
 * @author Joao Bispo
 * 
 */
public abstract class MatlabTemplate {

    /**
     * @param totalIndexes
     * @param engine
     * @return
     */
    public String addToEngine(MatlabToCEngine engine) {
	String functionName = getName();

	// Create M-code if not present, and add it
	if (!engine.hasMatlabFunction(functionName)) {
	    String functionCode = getMCode();
	    engine.addWithCheck(functionName, functionCode);
	}

	return functionName;
    }

    /**
     * @param totalIndexes
     * @return
     */
    public abstract String getMCode();

    /**
     * @param totalIndexes
     * @return
     */
    public abstract String getName();

}
