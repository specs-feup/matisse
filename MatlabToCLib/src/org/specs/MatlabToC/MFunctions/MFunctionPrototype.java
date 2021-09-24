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

package org.specs.MatlabToC.MFunctions;

import java.util.ArrayList;
import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabToC.SystemInfo.ImplementationData;
import org.specs.matisselib.providers.MatlabFunction;

/**
 * @author Joao Bispo
 * 
 */
public class MFunctionPrototype extends MatlabFunction {

    private final String mFunctionName;
    private final List<String> inputNames;
    private final List<String> outputNames;
    private final String parentFunctionName;

    private final FunctionNode functionToken;

    /**
     * 
     */
    private MFunctionPrototype(boolean isInternal, String fileName, String mFunctionName, String parentFunctionName,
	    FunctionNode mFunction, ImplementationData implementationData) {

	super();
	this.mFunctionName = mFunctionName;
	this.parentFunctionName = parentFunctionName;
	functionToken = mFunction;

	// Add input names
	// TODO replace by methods of MatlabTokenUtils
	inputNames = mFunction.getInputNames();
	outputNames = mFunction.getOutputNames();

	MFunctionProvider mFunctionBuilder = new MFunctionProvider(isInternal, fileName, mFunctionName,
		parentFunctionName, getScope(), mFunction, implementationData);

	// Add builder
	addFilter(mFunctionBuilder);
    }

    public static MFunctionPrototype newMainFunction(boolean isInternal, String fileName, String mFunctionName,
	    FunctionNode mFunction, ImplementationData implementationData) {

	return new MFunctionPrototype(isInternal, fileName, mFunctionName, null, mFunction, implementationData);
    }

    public static MFunctionPrototype newSubFunction(boolean isInternal, String fileName, String mFunctionName,
	    String parentFunctionName, FunctionNode mFunction, ImplementationData implementationData) {

	return new MFunctionPrototype(isInternal, fileName, mFunctionName, parentFunctionName, mFunction,
		implementationData);
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Function.GeneralFunction#getFunctionName()
     */
    @Override
    public String getFunctionName() {
	return mFunctionName;
    }

    /**
     * @return the functionToken
     */
    public MatlabNode getFunctionToken() {
	return functionToken;
    }

    /**
     * @return the inputNames
     */
    public List<String> getInputNames() {
	return inputNames;
    }

    /**
     * @return the outputNames
     */
    public List<String> getOutputNames() {
	return outputNames;
    }

    /**
     * The scope this function is associated to.
     * 
     * Usually, the scope of a function is defined by the general name of the function.
     * 
     * @return
     */
    public List<String> getScope() {
	List<String> scope = new ArrayList<>();

	if (parentFunctionName != null) {

	    // Add sub-function to the scope
	    scope.add(parentFunctionName);
	}

	scope.add(getFunctionName());

	return scope;
    }

}
