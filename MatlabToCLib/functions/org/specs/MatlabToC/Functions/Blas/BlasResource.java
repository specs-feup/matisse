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

package org.specs.MatlabToC.Functions.Blas;

import org.specs.MatlabToC.Utilities.MatlabResourceProvider;

/**
 * @author Joao Bispo
 * 
 */
public enum BlasResource implements MatlabResourceProvider {

    MULT("blas_mult.c");

    private final static String RESOURCE_FOLDER = "templates/blas/";

    private final String resourceFilename;

    private BlasResource(String resource) {
	this.resourceFilename = RESOURCE_FOLDER + resource;
    }

    /**
     * @return the resource
     */
    @Override
    public String getResource() {
	return resourceFilename;
    }

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
