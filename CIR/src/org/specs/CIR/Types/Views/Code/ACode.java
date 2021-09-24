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

package org.specs.CIR.Types.Views.Code;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.Portability.RestrictDefinitionInstance;
import org.specs.CIR.Types.VariableType;

import pt.up.fe.specs.util.SpecsLogs;

public abstract class ACode implements Code {

    private final VariableType type;

    public ACode(VariableType type) {
	this.type = type;
    }

    @Override
    public String getSimpleType() {
	throw new UnsupportedOperationException();
    }

    /*
        @Override
        public String getSmallId() {
    	throw new UnsupportedOperationException();
        }
    */
    /**
     * As default, returns getSimpleType(), suffixed by '*' if type is pointer.
     */
    @Override
    public String getType() {
	StringBuilder builder = new StringBuilder();

	builder.append(getSimpleType());
	// If pointer, add a '*'
	if (type.pointer().isByReference()) {
	    builder.append("* restrict");
	}

	return builder.toString();
    }

    /**
     * As default, returns getType().
     */
    @Override
    public String getReturnType() {
	return getType();
    }

    @Override
    public String getDeclaration(String variableName) {
	StringBuilder builder = new StringBuilder();

	builder.append(getType());
	builder.append(" ");
	builder.append(variableName);

	return builder.toString();

    }

    /**
     * The default version of this method returns a declaration of the variable, followed by an assignment with the
     * first value. If the list has more than one value, issues a warning. If given type is a pointer, issues a warning.
     * 
     * <p>
     * Example: int a = 34
     */
    @Override
    public String getDeclarationWithInputs(String variableName, List<String> values) {

	// Check if pointer type
	if (type.pointer().isByReference()) {
	    SpecsLogs.warn("Using declaration initialization in a type marked as pointer ('"
		    + getDeclaration(variableName) + "'). Check if correct.");
	}

	StringBuilder builder = new StringBuilder();

	builder.append(getDeclaration(variableName));

	// If no values, return
	if (values.isEmpty()) {
	    return builder.toString();
	}

	if (values.size() != 1) {
	    SpecsLogs.warn("List of values have more than one element, using only the first element.");
	}

	builder.append(" = ");

	String value = values.get(0);
	builder.append(value);

	return builder.toString();
    }

    @Override
    public Set<FunctionInstance> getInstances() {
	if (!type.pointer().isByReference()) {
	    return Collections.emptySet();
	}

	Set<FunctionInstance> instances = new HashSet<>();

	instances.add(new RestrictDefinitionInstance());

	return instances;
    }

    @Override
    public Set<String> getIncludes() {
	if (!type.pointer().isByReference()) {
	    return Collections.emptySet();
	}

	Set<String> includes = new HashSet<>();

	includes.add(RestrictDefinitionInstance.HEADER);

	return includes;
    }

}
