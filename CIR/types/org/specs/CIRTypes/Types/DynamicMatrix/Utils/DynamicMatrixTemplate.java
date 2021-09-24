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

package org.specs.CIRTypes.Types.DynamicMatrix.Utils;

import java.util.List;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsStrings;

/**
 * @author Joao Bispo
 * 
 */
public enum DynamicMatrixTemplate {
    // Fixed parameters (Input/Output, field names...)
    TENSOR_SHAPE,
    TENSOR_DIMS,
    TENSOR_LENGTH,
    VAR_LENGTH,
    VAR_T,
    VAR_INDEX,
    VAR_INDEXES,
    VAR_VALUE,

    // Function Calls
    CALL_NEW_EMPTY,
    CALL_NUMEL,
    CALL_SET,
    
    
    // General Tags
    /**
     * General name of the tensor structure
     */
    TENSOR_NAME,
    /**
     * Name of a specific tensor structure
     */
    TENSOR_STRUCT,
    /**
     * Type of the data in the tensor
     */
    DATA_TYPE,
    SMALL_ID,
    NUM_DIMS,
    /**
     * E.g., 'x, y, z'
     */
    DIM_NAMES;

    public final static String VAR_NAME_SHAPE = "shape";
    public final static String VAR_NAME_DIMS = "dims";
    public final static String VAR_NAME_LENGTH = "length";
    public final static String VAR_NAME_T = "t";
    public final static String VAR_NAME_INDEX = "index";
    public final static String VAR_NAME_INDEXES = "indexes";
    public final static String VAR_NAME_VALUE = "value";

    private static final List<String> DEFAULT_TAGS_AND_VALUES;
    static {
	DEFAULT_TAGS_AND_VALUES = SpecsFactory.newArrayList();

	DEFAULT_TAGS_AND_VALUES.add(TENSOR_SHAPE.getTag());
	DEFAULT_TAGS_AND_VALUES.add(VAR_NAME_SHAPE);

	DEFAULT_TAGS_AND_VALUES.add(TENSOR_DIMS.getTag());
	DEFAULT_TAGS_AND_VALUES.add(VAR_NAME_DIMS);

	DEFAULT_TAGS_AND_VALUES.add(TENSOR_LENGTH.getTag());
	DEFAULT_TAGS_AND_VALUES.add(VAR_NAME_LENGTH);

	DEFAULT_TAGS_AND_VALUES.add(VAR_LENGTH.getTag());
	DEFAULT_TAGS_AND_VALUES.add(VAR_NAME_LENGTH);
	
	DEFAULT_TAGS_AND_VALUES.add(VAR_T.getTag());
	DEFAULT_TAGS_AND_VALUES.add(VAR_NAME_T);

	DEFAULT_TAGS_AND_VALUES.add(VAR_INDEX.getTag());
	DEFAULT_TAGS_AND_VALUES.add(VAR_NAME_INDEX);

	DEFAULT_TAGS_AND_VALUES.add(VAR_INDEXES.getTag());
	DEFAULT_TAGS_AND_VALUES.add(VAR_NAME_INDEXES);

	DEFAULT_TAGS_AND_VALUES.add(VAR_VALUE.getTag());
	DEFAULT_TAGS_AND_VALUES.add(VAR_NAME_VALUE);
    }

    /**
     * It's the name, in upper case, surrounded by <>.
     * 
     * @return
     */
    public String getTag() {
	// return "<"+getName().toUpperCase()+">";
	return "<" + name().toUpperCase() + ">";
    }

    public static String parseTemplate(String template, String... tagsAndValues) {
	return SpecsStrings.parseTemplate(template, DEFAULT_TAGS_AND_VALUES, tagsAndValues);
    }

    
    public static List<String> getDefaultTagsAndValues() {
	return DEFAULT_TAGS_AND_VALUES;
    }

}
