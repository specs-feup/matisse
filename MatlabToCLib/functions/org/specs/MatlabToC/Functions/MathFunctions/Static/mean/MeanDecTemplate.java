/**
 *  Copyright 2013 SPeCS Research Group.
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

package org.specs.MatlabToC.Functions.MathFunctions.Static.mean;

import pt.up.fe.specs.util.SpecsStrings;

/**
 * This class contains the template for the declared, numeric version of {@link MeanDecFunctions}.
 * @author Pedro Pinto
 *
 */
public enum MeanDecTemplate {
    
    // vector
    MV_SCALAR_DIVISION_CALL,
    MV_VECTOR_SUM_CALL,
    MV_VECTOR_LENGTH,
    
    // matrix
    MM_RIGHT_DIVISION_CALL,
    MM_MATRIX_SUM_CALL,
    MM_DIM_SIZE;
    
    // vector
    public final static String VECTOR_INPUT_NAME = "vector";
    public final static String VECTOR_OUTPUT_NAME = "output";
    
    // matrix
    public final static String MATRIX_INPUT_NAME = "matrix";
    public final static String MATRIX_OUTPUT_NAME = "output";
    
    /**
     * 
     * @return
     * 		a {@link String} with the name, in upper case, surrounded by <>.
     */
    public String getTag() {
	return "<" + name().toUpperCase() + ">";
    }

    /**
     * Parses the template and replaces the tags with the provided values.
     * 
     * @param template
     * 		- the template string
     * @param tagsAndValues
     * 		- the <tag, value> pairs
     * @return
     * 		a {@link String} with the parsed template
     */
    public static String parseTemplate(String template, String... tagsAndValues) {
	
	// No default tags and values ( the null parameter )
	return SpecsStrings.parseTemplate(template, null, tagsAndValues);

    }
}
