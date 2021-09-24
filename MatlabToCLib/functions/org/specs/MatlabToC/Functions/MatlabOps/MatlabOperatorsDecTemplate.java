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

package org.specs.MatlabToC.Functions.MatlabOps;

import java.util.List;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsStrings;

/**
 * This class contains the template information used by {@link MatlabOperatorsDec}.
 * 
 * @author Pedro Pinto
 * 
 */
public enum MatlabOperatorsDecTemplate {

    // For matrix power
    MP_FIRST_INPUT,
    MP_SECOND_INPUT,
    MP_OUTPUT,
    MP_SELF_CALL,
    MP_MAT_MULT_CALL,
    MP_EYE_CALL,
    MP_LENGTH,
    MP_INPUT_NUM_TYPE,
    MP_COPY_CALL,

    // For colon
    CD_MAX_CALL,
    CD_SIGN,
    CD_TOL,
    CD_N;
    
    // For matrix power
    public final static String MP_FIRST_INPUT_NAME = "base";
    public final static String MP_SECOND_INPUT_NAME = "exponent";
    public final static String MP_OUTPUT_NAME = "power";
    
    // For colon
    public final static String CD_FIRST_INPUT_NAME = "start";
    public final static String CD_SECOND_INPUT_NAME = "step";
    public final static String CD_THIRD_INPUT_NAME = "end";
    public final static String CD_OUTPUT_NAME = "output";
    
    private static final List<String> DEFAULT_TAGS_AND_VALUES;
    static {
	DEFAULT_TAGS_AND_VALUES = SpecsFactory.newArrayList();
    }

    /**
     * It's the name, in upper case, surrounded by <>.
     * 
     * @return
     */
    public String getTag() {
	return "<" + name().toUpperCase() + ">";
    }

    public static String parseTemplate(String template, String... tagsAndValues) {
	return SpecsStrings.parseTemplate(template, DEFAULT_TAGS_AND_VALUES, tagsAndValues);

    }
}