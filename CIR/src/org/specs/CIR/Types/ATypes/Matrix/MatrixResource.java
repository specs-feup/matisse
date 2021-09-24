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

package org.specs.CIR.Types.ATypes.Matrix;

import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * @author Joao Bispo
 * 
 */
public enum MatrixResource implements ResourceProvider {

    SET_ROW_BODY("set_row_body.c");

    private final static String RESOURCE_FOLDER = "cirlib/matrix_functions";

    private final String resource;

    /**
     * @param resource
     */
    private MatrixResource(String resource) {
	this.resource = resource;
    }

    @Override
    public String getResource() {
	return RESOURCE_FOLDER + "/" + resource;
    }

}
