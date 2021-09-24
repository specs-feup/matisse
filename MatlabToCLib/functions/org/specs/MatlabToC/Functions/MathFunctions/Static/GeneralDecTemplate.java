/**
 * Copyright 2012 SPeCS Research Group.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License. under the License.
 */

package org.specs.MatlabToC.Functions.MathFunctions.Static;

import java.util.List;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsStrings;

/**
 * @author Joao Bispo
 * 
 */
public enum GeneralDecTemplate {

    /* Input/Output parameters */
    
    // For linspace
    V_START,
    V_END,
    V_OUTPUT,
    
    // For bitshift
    FIRST_INPUT,
    SECOND_INPUT,
    OUTPUT,
    
    
    /* General Tags */
    
    // For linspace
    N_STEPS,
    
    // For bitshift
    IN_RANGE_NAME,
    MATRIX_LENGTH,
    MIN_MACRO,
    MAX_MACRO,
    TYPE,
    IS_INTEGER_CALL,
    IN_RANGE_CALL;
    
    
    // For linspace
    public final static String VAR_NAME_START = "start";
    public final static String VAR_NAME_END = "end";
    public final static String VAR_NAME_OUTPUT = "output";

    // For bitshift
    public final static String FIRST_INPUT_NAME = "input";
    public final static String SECOND_INPUT_NAME = "bits_to_shift";
    public final static String OUTPUT_NAME = "output";
    
    private static final List<String> DEFAULT_TAGS_AND_VALUES;
    static {
	DEFAULT_TAGS_AND_VALUES = SpecsFactory.newArrayList();

	// For linspace
	DEFAULT_TAGS_AND_VALUES.add(V_START.getTag());
	DEFAULT_TAGS_AND_VALUES.add(VAR_NAME_START);

	DEFAULT_TAGS_AND_VALUES.add(V_END.getTag());
	DEFAULT_TAGS_AND_VALUES.add(VAR_NAME_END);

	DEFAULT_TAGS_AND_VALUES.add(V_OUTPUT.getTag());
	DEFAULT_TAGS_AND_VALUES.add(VAR_NAME_OUTPUT);
	
	// For bitshift
	DEFAULT_TAGS_AND_VALUES.add(FIRST_INPUT.getTag());
	DEFAULT_TAGS_AND_VALUES.add(FIRST_INPUT_NAME);

	DEFAULT_TAGS_AND_VALUES.add(SECOND_INPUT.getTag());
	DEFAULT_TAGS_AND_VALUES.add(SECOND_INPUT_NAME);
	
	DEFAULT_TAGS_AND_VALUES.add(OUTPUT.getTag());
	DEFAULT_TAGS_AND_VALUES.add(OUTPUT_NAME);
	
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
