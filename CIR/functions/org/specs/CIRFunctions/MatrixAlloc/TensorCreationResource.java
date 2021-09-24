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

package org.specs.CIRFunctions.MatrixAlloc;

import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * @author Joao Bispo
 * 
 */
public enum TensorCreationResource implements ResourceProvider {

    NEW_ARRAY_BODY("new_array_body.c"),
    NEW_ARRAY_FROM_MATRIX_BODY("new_array_from_matrix_body.c"),
    NEW_ARRAY_HELPER_BODY("new_array_helper_body.c"),
    NEW_CONST_ARRAY_BODY("new_const_array_body.c"),
    NEW_ARRAY_VIEW_BODY("new_array_view_body.c"),
    NEW_ARRAY_POINTER_VIEW_BODY("new_array_pointer_view_body.c"),
    EYE_BODY("eye_body.c"),
    FREE_BODY("free_body.c"),
    FREE_VIEW_BODY("free_view_body.c"),
    FREE_COMMENTS("free_comments.c");

    private final static String RESOURCE_FOLDER = "cirlib/tensor_creation";

    private final String resource;

    /**
     * @param resource
     */
    private TensorCreationResource(String resource) {
	this.resource = resource;
    }

    @Override
    public String getResource() {
	return RESOURCE_FOLDER + "/" + resource;
    }

}
