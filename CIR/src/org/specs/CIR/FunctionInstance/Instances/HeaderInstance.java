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

import java.util.Collections;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Instance to create C header files.
 * 
 * @author Joao Bispo
 * 
 */
public class HeaderInstance extends FunctionInstance {

    private final String cFilename;
    private final String declarationCode;

    private Set<FunctionInstance> customImplementationInstances;

    // private Set<String> declarationIncludes;
    // private Set<String> customUseIncludes;

    public HeaderInstance(String cFilename, String declarationCode) {
	super(null);

	this.cFilename = cFilename;
	this.declarationCode = declarationCode;

	customImplementationInstances = Collections.emptySet();
    }

    /**
     * @param declarationIncludes
     *            the declarationIncludes to set
     */
    /*
    public void setDeclarationIncludes(Set<String> declarationIncludes) {
    this.declarationIncludes = declarationIncludes;
    }
    */

    /**
     * @param customUseIncludes
     *            the customUseIncludes to set
     */
    /*
    public void setCustomUseIncludes(Set<String> customUseIncludes) {
    this.customUseIncludes = customUseIncludes;
    }
    */

    /**
     * Returns the includes of the NumericType of the tensor.
     */
    /*
    @Override
    public Set<String> getDeclarationIncludes() {
    Set<String> includes = super.getDeclarationIncludes();

    if (!includes.isEmpty()) {
        LoggingUtils.msgWarn("Base includes of structure are not empty. Check if this is ok.");
    }

    // FactoryUtils.addAll(includes, declarationIncludes);

    return includes;
    }
    */
    @Override
    public String getDeclarationCode() {
	return declarationCode;
    }

    @Override
    public String getCName() {
	int index = cFilename.lastIndexOf('/');
	if (index == -1) {
	    return cFilename;
	}

	return cFilename.substring(index + 1);
    }

    @Override
    public String getCFilename() {
	return cFilename;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getCallIncludes()
     */
    /*
    @Override
    public Set<String> getCallIncludes() {
    Set<String> includes = super.getCallIncludes();

    //FactoryUtils.addAll(includes, customUseIncludes);

    return includes;
    }
    */

    @Override
    public boolean hasImplementation() {
	return false;
    }

    /**
     * Helper method with variadic inputs.
     * 
     * @param dependentInstances
     */
    public void setCustomImplementationInstances(FunctionInstance... dependentInstances) {
	Set<FunctionInstance> instances = SpecsFactory.newHashSet();
	for (FunctionInstance instance : dependentInstances) {
	    instances.add(instance);
	}

	setCustomImplementationInstances(instances);
    }

    /**
     * @param dependentInstances
     *            the dependentInstances to set
     */
    public void setCustomImplementationInstances(Set<FunctionInstance> dependentInstances) {
	this.customImplementationInstances = dependentInstances;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getImplementationInstances()
     */
    @Override
    public Set<FunctionInstance> getImplementationInstances() {
	Set<FunctionInstance> instances = super.getImplementationInstances();

	instances.addAll(customImplementationInstances);

	return instances;
    }

}
