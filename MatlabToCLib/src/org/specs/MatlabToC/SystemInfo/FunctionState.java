/**
 *  Copyright 2013 SPeCS Research Group.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.specs.MatlabToC.SystemInfo;

import java.util.List;

/**
 * Captures the general current state of a MATLAB function while being translated.
 * 
 * @author Joao Bispo
 *
 */
public class FunctionState {

    private final List<String> scope;
    private int lineNumber;
    
    public FunctionState(List<String> scope, int lineNumber) {
	this.scope = scope;
	this.lineNumber = lineNumber;
    }
    
    /**
     * @return the scope
     */
    public List<String> getScope() {
	return scope;
    }
    
    /**
     * @return the lineNumber
     */
    public int getLineNumber() {
	return lineNumber;
    }
    
    
}
