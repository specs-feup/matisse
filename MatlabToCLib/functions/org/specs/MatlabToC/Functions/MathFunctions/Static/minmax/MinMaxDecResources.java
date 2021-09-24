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

package org.specs.MatlabToC.Functions.MathFunctions.Static.minmax;



/**
 * Contains the resources needed by {@link MinMaxFunctions}.
 * 
 * @author Pedro Pinto
 *
 */
public enum MinMaxDecResources {

    MIN_MAX_VECTOR_DEC("min_max_vector_dec.c"),
    MIN_MAX_VECTOR_INDEX_DEC("min_max_vector_index_dec.c"),
    MIN_MAX_MATRICES_DEC("min_max_matrices_dec.c"),
    MIN_MAX_SCALARS_DEC("min_max_scalars_dec.c"),
    MIN_MAX_MATRIX_SCALAR_DEC("min_max_matrix_scalar_dec.c");
    
    private final static String RESOURCE_FOLDER = "templates/min_max_dec";
    private final String resource;
    
    
    private MinMaxDecResources(String resource){
	this.resource = resource;
    }


    /**
     * @return the resource
     */
    public String getResource() {
        return RESOURCE_FOLDER + "/" + resource;
    }
}
