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

package org.specs.MatlabToC.Functions.CustomFunctions;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * @author Joao Bispo
 * 
 */
public enum BuiltinTemplate implements ResourceProvider {

    GET_WILDCARD("get_wildcard.m"),
    MATRIX_INDEXES("matrix_indexes.m"),
    MATRIX_VALUES("matrix_values.m"),
    SET_MULTIPLE("set_multiple.m"),
    SET_WILDCARD("set_wildcard.m"),
    WILDCARD_LAST("wildcard_last.m"),
    DELETE_SINGLE_INDEX_MATRIX("delete_single_index_matrix.m"),
    DELETE_SINGLE_INDEX_SCALAR("delete_single_index_scalar.m");

    private final static String RESOURCE_FOLDER = "templates/builtin/";

    private final String resourceFilename;

    private BuiltinTemplate(String resource) {
        this.resourceFilename = RESOURCE_FOLDER + resource;
    }

    /**
     * @return the resource
     */
    @Override
    public String getResource() {
        return resourceFilename;
    }

    public String getFunctionName() {
        return SpecsIo.removeExtension(resourceFilename);
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
