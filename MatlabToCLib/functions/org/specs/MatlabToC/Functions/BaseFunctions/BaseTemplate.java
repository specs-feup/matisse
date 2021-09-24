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

package org.specs.MatlabToC.Functions.BaseFunctions;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * @author Joao Bispo
 * 
 */
public enum BaseTemplate implements ResourceProvider {

    /**
     * Receives one input, a matrix with the shape.
     */
    // ACCESS_MATRIX_INDEX("access_matrix_index.m"),
    NEW_FROM_MATRIX("new_from_matrix.m"),
    // SUB2IND_SCALAR_INDEXES("sub2ind_scalar_indexes.m");
    SUB2IND_SCALAR_INDEXES_ROW_MAJOR("sub2ind_scalar_indexes_rowmajor.m"),
    SUB2IND_SCALAR_INDEXES_COLUMN_MAJOR("sub2ind_scalar_indexes_colmajor.m"),
    PADARRAY_SPECIALIZED("padarray_specialized.m");

    private final static String RESOURCE_FOLDER = "templates/base/";

    private final String resourceFilename;

    private BaseTemplate(String resource) {
	this.resourceFilename = BaseTemplate.RESOURCE_FOLDER + resource;
    }

    /**
     * @return the resource
     */
    @Override
    public String getResource() {
	return this.resourceFilename;
    }

    public String getFunctionName() {
	return SpecsIo.removeExtension(this.resourceFilename);
    }

    /**
     * Returns the value of 'getResource()'
     */
    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
	return getResource();
    }

}
