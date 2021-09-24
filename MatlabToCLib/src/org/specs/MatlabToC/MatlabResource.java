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

package org.specs.MatlabToC;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * @author Joao Bispo
 * 
 */
public enum MatlabResource implements ResourceProvider {

    COLON_OP_ALLOC("colonop.m"),
    COLON_OP_DEC("colonop_const.m");

    private final static String RESOURCE_FOLDER = "mfiles";

    // private final String functionName;
    private final String resourceFilename;

    // private final List<VariableType> defaultTypes;

    // private MatlabResource(String functionName, String resource, List<VariableType> defaultTypes) {
    private MatlabResource(String resource) {
	// this.functionName = functionName;
	this.resourceFilename = resource;
	// this.defaultTypes = defaultTypes;
    }

    /**
     * @return the resource
     */
    @Override
    public String getResource() {
	return RESOURCE_FOLDER + "/" + resourceFilename;
    }

    public String getFunctionName() {
	// return functionName;
	return SpecsIo.removeExtension(resourceFilename);
    }

    public MatlabNode getFunction() {
	return new MatlabParser().parse(this);
	// /String functionCode = IoUtils.getResource(getResource());
	// FileNode fileToken = MatlabProcessorUtils.fromMFile(functionCode, getFunctionName());

	// return fileToken.getMainFunction();
    }
    /**
     * @return the defaultTypes
     */
    /*
    public List<VariableType> getDefaultTypes() {
    return defaultTypes;
    }
    */
}
