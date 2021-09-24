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

package org.specs.MatlabToC.Functions.CustomFunctions;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * @author Joao Bispo
 * 
 */
public enum BuiltinResource implements ResourceProvider {

    // NEW_ARRAY_DYNAMIC("new_array_dynamic.c");
    ;
    private final static String RESOURCE_FOLDER = "mfiles/builtin/";

    private final String resourceFilename;

    private BuiltinResource(String resource) {
	this.resourceFilename = RESOURCE_FOLDER + resource;
    }

    /**
     * @return the resource
     */
    @Override
    public String getResource() {
	return resourceFilename;
    }

    public String getFunctionName() {
	return SpecsIo.removeExtension(resourceFilename);
    }

    /*
    public FileNode getParsedMFile() {
    String functionCode = IoUtils.getResourceString(getResource());
    return MatlabProcessorUtils.fromMFile(functionCode, getFunctionName());
    }
    */

    /**
     * Returns the value of 'getResource()'
     */
    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
	return getResource();
    }

}
