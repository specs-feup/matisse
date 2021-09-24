/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.MatlabToC.Functions.MatlabOps;

import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * Contains the resources needed by {@link MatlabOperatorsAlloc}.
 * 
 * @author Pedro Pinto
 * 
 */
public enum MatlabOperatorsAllocResource implements ResourceProvider {

    // For allocated scalar colon
    ELEMENT_WISE_ALLOC_CHECK1("element_wise_alloc_check1.c"),
    ELEMENT_WISE_ALLOC_CHECK2("element_wise_alloc_check2.c"),
    SCALAR_COLON_ALLOC("scalar_colon_alloc.c");

    private final static String RESOURCE_FOLDER = "templates/operators_alloc";
    private final String resource;

    private MatlabOperatorsAllocResource(String resource) {
	this.resource = resource;
    }

    /**
     * @return the resource
     */
    @Override
    public String getResource() {
	return RESOURCE_FOLDER + "/" + resource;
    }
}
