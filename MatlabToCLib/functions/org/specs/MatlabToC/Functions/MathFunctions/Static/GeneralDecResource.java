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

package org.specs.MatlabToC.Functions.MathFunctions.Static;

/**
 * @author Joao Bispo
 *
 */
public enum GeneralDecResource {

    // For linspace
    LINSPACE_DEC_EQUAL_1("linspace_DEC_n_equal_1.c"),
    LINSPACE_DEC_GREATER_1("linspace_DEC_n_greater_1.c"),
    
    // For bitshift
    BITSHIFT_DOUBLE_DEC_MATRIX("bitshift_double_dec_matrix.c"),
    BITSHIFT_DOUBLE_DEC_SCALAR("bitshift_double_dec_scalar.c"),
    BITSHIFT_DOUBLE_DEC_IN_RANGE("bitshift_double_dec_in_range.c"),
    BITSHIFT_DOUBLE_DEC_IS_INTEGER("bitshift_double_dec_is_integer.c");
    
    
    private final static String RESOURCE_FOLDER = "templates/function_dec";
    
    private final String resource;

    /**
     * @param resource
     */
    private GeneralDecResource(String resource) {
	this.resource = resource;
    }
    
    /**
     * @return the resource
     */
    public String getResource() {
	return RESOURCE_FOLDER + "/" + resource;
    }
    
}
