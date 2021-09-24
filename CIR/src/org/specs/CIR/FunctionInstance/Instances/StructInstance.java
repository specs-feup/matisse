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

package org.specs.CIR.FunctionInstance.Instances;

import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * Instance to create C structs.
 * 
 * @author Joao Bispo
 * 
 */
public class StructInstance extends FunctionInstance {

    private final String structName;
    private final String cFilename;
    private final String declarationCode;

    private Set<String> declarationIncludes;
    private Set<String> customUseIncludes;

    public StructInstance(String structName, String cFilename, String declarationCode) {
	super(null);

	this.structName = structName;
	this.cFilename = cFilename;
	this.declarationCode = declarationCode;
    }

    /**
     * @param declarationIncludes
     *            the declarationIncludes to set
     */
    public void setDeclarationIncludes(Set<String> declarationIncludes) {
	this.declarationIncludes = declarationIncludes;
    }

    /**
     * @param customUseIncludes
     *            the customUseIncludes to set
     */
    public void setCustomUseIncludes(Set<String> customUseIncludes) {
	this.customUseIncludes = customUseIncludes;
    }

    /**
     * Returns the includes of the NumericType of the tensor.
     */
    @Override
    public Set<String> getDeclarationIncludes() {
	Set<String> includes = super.getDeclarationIncludes();

	if (!includes.isEmpty()) {
	    SpecsLogs.warn("Base includes of structure are not empty. Check if this is ok.");
	}

	SpecsFactory.addAll(includes, declarationIncludes);

	return includes;
    }

    @Override
    public String getDeclarationCode() {
	return declarationCode;
    }

    @Override
    public String getCName() {
	return structName;
    }

    @Override
    public String getCFilename() {
	return cFilename;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getCallIncludes()
     */
    @Override
    public Set<String> getCallIncludes() {
	Set<String> includes = super.getCallIncludes();

	SpecsFactory.addAll(includes, customUseIncludes);

	return includes;
    }

    @Override
    public boolean hasImplementation() {
	return false;
    }

}
