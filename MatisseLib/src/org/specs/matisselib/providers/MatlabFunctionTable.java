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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.specs.CIR.FunctionInstance.InstanceProvider;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * Class which maps Prototype objects to their function names.
 * 
 * <p>
 * Includes methods for adding prototypes.
 * 
 * @author Joao Bispo
 * 
 */
public class MatlabFunctionTable {

    private final Map<String, MatlabFunction> prototypes;

    /**
     * @param primitives
     */
    public MatlabFunctionTable() {
	this.prototypes = SpecsFactory.newHashMap();
    }

    public MatlabFunctionTable(Map<String, MatlabFunction> prototypes) {
	this.prototypes = prototypes;
    }

    /**
     * @param defaultPrototypeTable
     */
    public MatlabFunctionTable(MatlabFunctionTable table) {
	this();

	this.prototypes.putAll(table.getPrototypes());
    }

    /**
     * @return the prototypes
     */
    public Map<String, MatlabFunction> getPrototypes() {
	return Collections.unmodifiableMap(prototypes);
    }

    public void addPrototype(MatlabFunction newPrototype) {

	MatlabFunction previousPrototype = prototypes.put(newPrototype.getFunctionName(), newPrototype);
	if (previousPrototype != null) {
	    SpecsLogs.warn("Duplicated prototype for function " + newPrototype.getFunctionName());
	}
    }

    /**
     * Builds a Map of FunctionPrototypes, mapped to their function name, from an enumeration implementing
     * MatlabFunctionProvider.
     * 
     * @param enumProvider
     * @param primitivesData
     * @return
     */
    public <T extends Enum<T> & MatlabFunctionProvider> void addPrototypes(Class<T> prototypeBuilderEnum) {

	// Map the providers to the function name
	for (T enumConstant : prototypeBuilderEnum.getEnumConstants()) {
	    addPrototype(enumConstant.getMatlabFunction());
	}
    }

    /**
     * @param functionsDec2
     * @return
     */
    @Override
    public String toString() {
	// Get function names
	List<String> functions = SpecsFactory.newArrayList(prototypes.keySet());

	// Sort
	Collections.sort(functions);

	StringBuilder builder = new StringBuilder();

	for (String functionName : functions) {
	    builder.append(functionName).append("\n");
	}

	return builder.toString();
    }

    /**
     * 
     * @param prototypes
     */
    public void addPrototypes(List<MatlabFunction> prototypes) {
	for (MatlabFunction functionPrototype : prototypes) {
	    addPrototype(functionPrototype);
	}
    }

    /**
     * Adds a builder to the end of the list of builders of the MATLAB function with the given name.
     * 
     * @param functionName
     * @param builder
     */
    public void addBuilder(String functionName, InstanceProvider builder) {
	MatlabFunction function = getFunction(functionName);
	function.addFilter(builder);
    }

    /**
     * Adds a builder to the beginning of the list of builders of the MATLAB function with the given name.
     * 
     * @param functionName
     * @param builder
     */
    public void addBuilderFirst(String functionName, InstanceProvider builder) {
	MatlabFunction function = getFunction(functionName);
	function.addFilterFirst(builder);
    }

    private MatlabFunction getFunction(String functionName) {

	// Get MATLAB function
	MatlabFunction function = prototypes.get(functionName);

	// If not function with that name, create a generic MATLAB function
	if (function == null) {
	    List<InstanceProvider> emptyList = SpecsFactory.newArrayList();
	    function = new GenericMatlabFunction(functionName, emptyList);

	    // Add to table
	    prototypes.put(functionName, function);
	}
	return function;
    }

    /**
     * Adds a list of builders to the beginning of the list of builders of the MATLAB function with the given name.
     * 
     * <p>
     * The order of the list is preserved, i.e. the first elements of the table for the given name will correspond to
     * the first elements of the list.
     * 
     * @param functionName
     * @param builders
     */
    public void addBuilderFirst(String functionName, List<InstanceProvider> builders) {
	// Add builders in reserve order, to maintain original order of given list
	List<InstanceProvider> reversedBuilders = new ArrayList<>(builders);
	Collections.reverse(reversedBuilders);

	for (InstanceProvider builder : reversedBuilders) {
	    addBuilderFirst(functionName, builder);
	}
    }
}
