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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.specs.CIR.CodeGenerator.CodeGeneratorUtils;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.Utils.InstanceSet;
import org.specs.CIR.Language.IncludesUtils;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Utilities.CodeReplacer;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.providers.KeyStringProvider;
import pt.up.fe.specs.util.utilities.StringLines;

/**
 * A function whose code is generated from literal code (ex.: templates).
 * 
 * <p>
 * If a body is defined for this instance, it has implementation.
 * 
 * @author Joao Bispo
 * 
 */
public class LiteralInstance extends FunctionInstance {

    private final String cFunctionName;
    private final String cFilename;

    private List<String> comments;
    private final String cBody;

    private final InstanceSet customImplementationInstances;

    private Set<String> customImplementationIncludes;

    /**
     * 
     * @param cBody
     * @param hCode
     * @param functionTypes
     */
    public LiteralInstance(FunctionType functionTypes, String cFunctionName, String cFilename, String cBody) {
	super(functionTypes);

	this.cFunctionName = cFunctionName;
	this.cFilename = cFilename;

	this.comments = null;
	this.cBody = cBody;

	customImplementationIncludes = null;
	// customImplementationInstances = null;
	customImplementationInstances = new InstanceSet();
    }

    /**
     * Helper constructor which accepts a CodeReplacer that automatically adds the CNodes in the CodeReplacer.
     * 
     * @param functionTypes
     * @param cFunctionName
     * @param cFilename
     * @param cBody
     */
    public LiteralInstance(FunctionType functionTypes, String cFunctionName, String cFilename, CodeReplacer cBody) {
	this(functionTypes, cFunctionName, cFilename, cBody.toString());

	getCustomImplementationInstances().add(cBody.getCNodes());
    }

    /**
     * @param customImplementationIncludes
     *            the includesDeclaration to set
     */
    /*
    public void setCustomImplementationIncludes(KeyProvider<String>... includes) {
    String[] includeStrings = new String[includes.length];
    for(int i=0; i<includes.length; i++) {
        includeStrings[i] = includes[i].getKey();
    }
    
    setCustomImplementationIncludes(includeStrings);
    }
    */

    /**
     * Helper method which uses the .toString method of the given objects to obtain the name of the include.
     * 
     * @param asList
     */
    // public <T extends KeyProvider<String>> void setCustomImplementationIncludes(List<T> includes) {
    /*
    public void setCustomImplementationIncludes(Object... includes) {
    setCustomImplementationIncludes(Arrays.asList(includes));
    }
    */
    /*
    public void setCustomImplementationIncludes(List<Object> includes) {
    List<String> includesString = FactoryUtils.newArrayList();
    
    for (Object include : includes) {
        includesString.add(include.toString());
    }
    
    setCustomImplementationIncludes(includesString);
    }
    */

    // public void setCustomImplementationIncludes(KeyProvider<String>... includes) {
    public void setCustomImplementationIncludes(KeyStringProvider... includes) {
	List<String> stringIncludes = SpecsFactory.newArrayList(includes.length);
	for (KeyStringProvider include : includes) {
	    stringIncludes.add(include.getKey());
	}

	setCustomImplementationIncludes(stringIncludes);
    }

    /**
     * @param customImplementationIncludes
     *            the includesDeclaration to set
     */

    public void setCustomImplementationIncludes(String... includes) {
	setCustomImplementationIncludes(Arrays.asList(includes));
	// Set<String> includesDeclaration = FactoryUtils.newHashSet(Arrays.asList(includes));
	// this.customImplementationIncludes = includesDeclaration;
    }

    /**
     * @param customImplementationIncludes
     *            the includesDeclaration to set
     */
    public void setCustomImplementationIncludes(Collection<String> includes) {
	Set<String> includesDeclaration = SpecsFactory.newHashSet(includes);
	this.customImplementationIncludes = includesDeclaration;
    }

    public void setImplementationIncludesFromName(String... functionNames) {
	Collection<SystemInclude> includes = IncludesUtils.getIncludes(functionNames);

	List<String> includeNames = new ArrayList<>();
	includes.forEach(include -> includeNames.add(include.getIncludeName()));

	setCustomImplementationIncludes(includeNames);
    }

    /**
     * @param comments
     *            the comments to set
     */
    public void setComments(String comments) {
	this.comments = SpecsFactory.newArrayList();

	for (String line : StringLines.newInstance(comments)) {
	    this.comments.add(line);
	}

    }

    /**
     * Besides the includes collection originally by FunctionInstance implementation, it also collects:<br>
     * - the includes defined by setCustomImplementationIncludes();<br>
     */
    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getImplementationIncludes()
     */
    @Override
    public Set<String> getImplementationIncludes() {
	Set<String> includes = super.getImplementationIncludes();

	SpecsFactory.addAll(includes, customImplementationIncludes);

	return includes;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getImplementationCode()
     */
    @Override
    public String getImplementationCode() {
	// Check if implementable
	if (!hasImplementation()) {
	    throw new UnsupportedOperationException("LiteralInstance '" + getCName() + "' is not implementable.");
	}

	String functionDeclaration = CodeGeneratorUtils.functionDeclarationCode(this);

	return CodeGeneratorUtils.functionImplementation(comments, functionDeclaration, cBody);
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getFunctionComments()
     */
    @Override
    public List<String> getComments() {
	return comments;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#hasImplementation()
     */
    @Override
    public boolean hasImplementation() {
	return cBody != null;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#hasDeclaration()
     */
    @Override
    public boolean hasDeclaration() {
	return true;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getCName()
     */
    @Override
    public String getCName() {
	return cFunctionName;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getCFileName()
     */
    @Override
    public String getCFilename() {
	return cFilename;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getImplementationInstances()
     */
    @Override
    public Set<FunctionInstance> getImplementationInstances() {
	Set<FunctionInstance> instances = super.getImplementationInstances();

	// Add all instances needed to call the custom instances
	for (FunctionInstance instance : customImplementationInstances.getInstances()) {
	    SpecsFactory.addAll(instances, instance.getCallInstances());
	}

	return instances;
    }

    /**
     * @return the customImplementationInstances
     */
    public InstanceSet getCustomImplementationInstances() {
	return customImplementationInstances;
    }

    public void addInstance(FunctionInstance... instances) {
	customImplementationInstances.add(instances);

    }
}
