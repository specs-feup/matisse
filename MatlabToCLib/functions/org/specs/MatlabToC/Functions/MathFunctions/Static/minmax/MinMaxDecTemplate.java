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

package org.specs.MatlabToC.Functions.MathFunctions.Static.minmax;

import pt.up.fe.specs.util.SpecsStrings;

/**
 * This class contains the template information used by {@link MinMaxFunctions}.
 * 
 * @author Pedro Pinto
 * 
 */
public enum MinMaxDecTemplate {

    MM_OPERATOR,
    MM_LENGTH,
    MM_COPY_CALL,
    MM_TEMP_TYPE,
    MM_MAX_TYPE,
    MM_VECTOR_GET_0,
    MM_VECTOR_GET_I;

    /* The names of the inputs */

    // General
    public final static String MM_OUTPUT_MAX_NAME = "max";

    // For vectors
    public final static String MM_VECTOR_INPUT_NAME = "vector";
    public final static String MM_VECTOR_OUTPUT_INDEX_NAME = "index";

    // For matrices
    public final static String MM_MATRICES_INPUT_1_NAME = "matrix1";
    public final static String MM_MATRICES_INPUT_2_NAME = "matrix2";

    // For scalars
    public final static String MM_SCALARS_INPUT_1_NAME = "scalar1";
    public final static String MM_SCALARS_INPUT_2_NAME = "scalar2";

    // For matrix / scalar
    public final static String MM_MATRIX_INPUT_NAME = "matrix";
    public final static String MM_SCALAR_INPUT_NAME = "scalar";

    /**
     * It's the name, in upper case, surrounded by <>.
     * 
     * @return
     */
    public String getTag() {
	return "<" + name().toUpperCase() + ">";
    }

    public static String parseTemplate(String template, String... tagsAndValues) {

	// No default tags and values ( the null parameter )
	return SpecsStrings.parseTemplate(template, null, tagsAndValues);

    }
}
