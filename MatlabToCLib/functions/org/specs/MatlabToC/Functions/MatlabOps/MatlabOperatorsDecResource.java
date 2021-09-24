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

package org.specs.MatlabToC.Functions.MatlabOps;

import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * Contains the resources needed by {@link MatlabOperatorsDec}.
 * 
 * @author Pedro Pinto
 *
 */
public enum MatlabOperatorsDecResource implements ResourceProvider {

    // For matrix power
    MATRIX_POW_POSITIVE_INT("matrix_power_positive_integer.c"),
    MATRIX_POW_POSITIVE_INT_ITER("matrix_power_positive_integer_iter.c"),

    // For colon
    SCALAR_COLON_DEC_CONSEC_INTS("scalar_colon_dec_ints.c"),
    SCALAR_COLON_DEC_SPACED_INTS("scalar_colon_dec_ints.c"),
    SCALAR_COLON_DEC_GENERAL("scalar_colon_dec_general.c");
    
    private final static String RESOURCE_FOLDER = "templates/operators_dec";
    private final String resource;
    
    
    private MatlabOperatorsDecResource(String resource){
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
