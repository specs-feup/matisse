/**
 *  Copyright 2012 SPeCS Research Group.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.specs.MatlabToC.Functions.BaseFunctions.Dynamic;

import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * @author Joao Bispo
 *
 */
enum ArrayCreatorsResource implements ResourceProvider {

    ARRAY_CREATOR_BODY("array_creator_alloc_body.c");
//    ZEROS_ALLOC_BODY("zeros_alloc_body");
 
    private final static String RESOURCE_FOLDER = "templates/arraycreator";
    
    private final String resource;

    /**
     * @param resource
     */
    private ArrayCreatorsResource(String resource) {
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
