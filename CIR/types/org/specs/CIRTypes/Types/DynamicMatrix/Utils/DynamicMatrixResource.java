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

import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * @author Joao Bispo
 * 
 */
public enum DynamicMatrixResource implements ResourceProvider {

    CHANGE_SHAPE_BODY("change_shape.c"),
    COPY_BODY("copy_body.c"),
    CREATE_BODY("create_body.c"),
    LENGTH_BODY("length_body.c"),
    DIM_SIZE_BODY("dim_size_body.c"),
    NUMEL_BODY("numel_body.c"),
    NUMEL_COMMENTS("numel_comments.c"),
    PRINT_BODY("print_body.c"),
    IS_SAME_SHAPE_BODY("is_same_shape_body.c"),
    SET_MATRIX_VALUES_BODY("set_matrix_values_body.c"),
    GET_VALUE_BODY("get_value_body.c"),
    SET_VALUE_BODY("set_value_body.c"),
    TRANSPOSE_BODY("transpose_body.c");

    private final static String RESOURCE_FOLDER = "cirlib/tensor_functions";

    private final String resource;

    /**
     * @param resource
     */
    private DynamicMatrixResource(String resource) {
	this.resource = resource;
    }

    @Override
    public String getResource() {
	return RESOURCE_FOLDER + "/" + resource;
    }

}
