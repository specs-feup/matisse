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

package org.specs.MatlabToC.Functions.MatisseHelperFunctions;

import org.specs.MatlabToC.Utilities.MatlabResourceProvider;

/**
 * @author Joao Bispo
 * 
 */
public enum HelperFunctionResource implements MatlabResourceProvider {

    RAW_IND2SUB("MATISSE_raw_ind2sub.m"),
    RAW_SUB2IND("MATISSE_raw_sub2ind.m");

    private final static String RESOURCE_FOLDER = "mfiles/helpers/";

    private final String resourceFilename;

    private HelperFunctionResource(String resource) {
	this.resourceFilename = HelperFunctionResource.RESOURCE_FOLDER + resource;
    }

    /**
     * @return the resource
     */
    @Override
    public String getResource() {
	return this.resourceFilename;
    }

    /**
     * Returns the value of 'getResource()'
     */
    @Override
    public String toString() {
	return getResource();
    }

}
