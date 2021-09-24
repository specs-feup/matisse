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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.Utils.InstanceSet;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.PrecedenceLevel;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.utilities.StringLines;

/**
 * A function whose code is automatically generated from CTokens (i.e., by using the object CInstructionList).
 * 
 * <p>
 * Since the CInstructionList may contain literal tokens, this class has methods for defining custom information, such
 * as includes and instances.
 * 
 * <p>
 * This instance has implementation.
 * 
 * @author Joao Bispo
 * 
 */
public class InstructionsInstance extends FunctionInstance {

    private final String cFunctionName;
    private String cFilename;

    private List<String> comments;
    private final CInstructionList cBody;

    private Set<String> customImplementationIncludes;
    private InstanceSet customImplementationInstances;
    private Boolean isConstantSpecialized;

    /**
     * Helper method which uses the FunctionTypes inside CInstructionList.
     * 
     * @param cFunctionName
     * @param cFilename
     * @param cBody
     *            cannot be null
     */
    public InstructionsInstance(String cFunctionName, String cFilename, CInstructionList cBody) {
	this(cBody.getFunctionTypes(), cFunctionName, cFilename, cBody);
    }

    /**
     * 
     * @param functionTypes
     * @param cFunctionName
     * @param cFilename
     * @param cBody
     *            the instructions of the function. Cannot be null
     */
    public InstructionsInstance(FunctionType functionTypes, String cFunctionName,
	    String cFilename, CInstructionList cBody) {
	super(functionTypes);

	this.cFunctionName = cFunctionName;
	this.cFilename = cFilename;

	this.comments = null;

	if (cBody == null) {
	    throw new RuntimeException("The parameter CInstructionList 'body' cannot be null.");
	}
	this.cBody = cBody;

	customImplementationIncludes = null;
	customImplementationInstances = new InstanceSet();
	isConstantSpecialized = null;
    }

    /**
     * @param customImplementationIncludes
     *            the includesDeclaration to set
     */
    public void setCustomIncludes(String... includes) {
	Set<String> includesDeclaration = SpecsFactory.newHashSet(Arrays.asList(includes));
	setCustomIncludes(includesDeclaration);
    }

    public void setCustomIncludes(Set<String> includes) {
	this.customImplementationIncludes = includes;
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
	this.customImplementationInstances.add(dependentInstances);
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

    public void setComments(List<String> comments) {
	this.comments = SpecsFactory.newArrayList(comments);
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
     * @see org.specs.CIR.Functions.FunctionInstance#getInstructions()
     */
    @Override
    public CInstructionList getInstructions() {
	return cBody;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getFunctionComments()
     */
    @Override
    public List<String> getComments() {
	return comments;
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

	customImplementationInstances.add(instances);
	// FactoryUtils.addAll(instances, customImplementationInstances);

	return customImplementationInstances.getInstances();
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#isConstantSpecialized()
     */
    @Override
    public boolean isConstantSpecialized() {
	if (isConstantSpecialized == null) {
	    return super.isConstantSpecialized();
	}

	return isConstantSpecialized;
    }

    /**
     * @param isConstantSpecialized
     *            the isConstantSpecialized to set
     */
    public void setIsConstantSpecialized(Boolean isConstantSpecialized) {
	this.isConstantSpecialized = isConstantSpecialized;
    }

    /**
     * @param cFilename
     *            the cFilename to set
     */
    public void setcFilename(String cFilename) {
	this.cFilename = cFilename;
    }

    @Override
    public PrecedenceLevel getCallPrecedenceLevel() {
	return PrecedenceLevel.FunctionCall;
    }

}
