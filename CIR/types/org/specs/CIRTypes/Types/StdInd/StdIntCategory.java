/**
 * Copyright 2014 SPeCS.
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

package org.specs.CIRTypes.Types.StdInd;

import static org.specs.CIRTypes.Types.StdInd.StdIntTypeUtils.*;

/**
 * 
 * 
 * @author Joao Bispo
 * 
 */
public enum StdIntCategory {

    EXACT_WIDTH("int" + N_BITS + "_t", "INT" + N_BITS + "_MIN", "INT" + N_BITS + "_MAX"),
    LEAST_WIDTH("int_least" + N_BITS + "_t", "INT_LEAST" + N_BITS + "_MIN", "INT_LEAST" + N_BITS + "_MAX"),
    FASTEST("int_fast" + N_BITS + "_t", "INT_FAST" + N_BITS + "_MIN", "INT_FAST" + N_BITS + "_MAX"),
    POINTER("intptr_t", "INTPTR_MIN", "INTPTR_MAX"),
    MAXIMUM_WIDTH("intmax_t", "INTMAX_MIN", "INTMAX_MAX");

    private final String typeTemplate;
    private final String minValueTemplate;
    private final String maxValueTemplate;

    private StdIntCategory(String typeTemplate, String minValueTemplate, String maxValueTemplate) {
	this.typeTemplate = typeTemplate;
	this.minValueTemplate = minValueTemplate;
	this.maxValueTemplate = maxValueTemplate;
    }

    public String getType(int bits, boolean isUnsigned) {
	// Replace the number of bits in type template
	String type = typeTemplate.replace(N_BITS, Integer.toString(bits));

	// Append 'u', if unsigned
	if (isUnsigned) {
	    return "u" + type;
	}

	return type;
    }

    public String getMinimumValue(int bits, boolean isUnsigned) {
	// If unsigned, return 0 as minimum value
	if (isUnsigned) {
	    return "0";
	}

	// Replace the number of bits in minimum template
	String minValue = minValueTemplate.replace(N_BITS, Integer.toString(bits));

	return minValue;
    }

    public String getMaximumValue(int bits, boolean isUnsigned) {
	// Replace the number of bits in maximum value template
	String maxValue = maxValueTemplate.replace(N_BITS, Integer.toString(bits));

	// Append 'U', if unsigned
	if (isUnsigned) {
	    return "U" + maxValue;
	}

	return maxValue;
    }

}
