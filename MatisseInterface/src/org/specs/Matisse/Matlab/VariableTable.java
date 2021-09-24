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

package org.specs.Matisse.Matlab;

import java.util.Arrays;
import java.util.List;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.collections.ScopedMap;

/**
 * Maps variable names to a list of strings (scope).
 * 
 * <p>
 * If variable is inside a sub-function, its key will be <MAIN_FUNCTION>,<SUB_FUNCTION>.
 * 
 * @author Joao Bispo
 * 
 */
public class VariableTable {

    // public final static List<String> SCRIPT_KEY = Arrays.asList("_script");

    /**
     * 
     */
    private final ScopedMap<Boolean> variableMap;

    /**
     * 
     */
    public VariableTable() {
	this.variableMap = new ScopedMap<>();

    }

    public void addVariable(List<String> functionChain, String variableName) {
	this.variableMap.addSymbol(functionChain, variableName, Boolean.TRUE);
	// System.out.println("ADD:"+functionChain+variableName);
    }

    public void addVariable(String functionName, String variableName) {
	addVariable(Arrays.asList(functionName), variableName);
    }

    /**
     * Removes a variable from the table
     * 
     * @param functionName
     * @param name
     */
    public void removeVariable(List<String> functionName, String name) {
	// Check if it exists
	Boolean variableExists = this.variableMap.getSymbol(functionName, name);
	if (variableExists == null) {
	    return;
	}

	if (!variableExists) {
	    return;
	}

	// Set boolean to false
	this.variableMap.addSymbol(functionName, name, Boolean.FALSE);
    }

    public Boolean hasVariable(List<String> key) {
	if (key.size() < 2) {
	    SpecsLogs.warn("NOT IMPLEMENTED");
	    return false;
	}
	return hasVariable(key.subList(0, key.size() - 1), key.get(key.size() - 1));
    }

    public Boolean hasVariable(List<String> functionName, String variableName) {
	Boolean hasVariable = this.variableMap.getSymbol(functionName, variableName);
	if (hasVariable == null) {
	    return Boolean.FALSE;
	}

	return hasVariable;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return variableMap.toString();
    }

    public List<List<String>> getKeys() {
	return variableMap.getKeys();
    }
}
