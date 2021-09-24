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

package org.specs.CIR.Tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * Represents variable initializations.
 * 
 * @author Joao Bispo
 * 
 */
public class Initializations {

    private final Map<String, List<String>> variableInitialization;
    private Set<String> rewrittenValues;

    public Initializations() {
	this.variableInitialization = SpecsFactory.newHashMap();
	rewrittenValues = SpecsFactory.newHashSet();
    }

    public void add(String varName, String... values) {
	add(varName, Arrays.asList(values));
    }

    public void add(String varName, List<String> values) {
	List<String> previousVar = variableInitialization.put(varName, values);
	if (previousVar != null && !rewrittenValues.contains(varName)) {
	    rewrittenValues.add(varName);
	    SpecsLogs.msgInfo("Redefining the original value of variable '" + varName + "'");
	    // LoggingUtils.msgWarn("Replacing initialization values for variable '" + varName
	    // + "'. Check if ok.");
	}

    }

    public void append(String varName, String value) {
	List<String> values = variableInitialization.get(varName);

	// If value does not exist, add normally
	if (values == null) {
	    values = SpecsFactory.newArrayList();
	    values.add(value);
	    add(varName, values);
	    return;
	}

	// Else, add value to list
	values.add(value);
    }

    /*
    public List<String> getVariableInitialization(String varName) {
    return variableInitialization.get(varName);
    }
    */

    /**
     * @param variableName
     * @return
     */
    public List<String> getValues(String variableName) {
	return variableInitialization.get(variableName);
    }

    /**
     * @param inputInstructions
     */
    public void add(Initializations initializations) {
	for (String key : initializations.getVariables()) {
	    List<String> values = initializations.getValues(key);

	    this.add(key, values);
	}

    }

    public Collection<String> getVariables() {
	return variableInitialization.keySet();
    }
}
