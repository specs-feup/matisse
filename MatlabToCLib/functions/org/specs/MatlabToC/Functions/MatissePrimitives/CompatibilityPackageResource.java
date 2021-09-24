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

package org.specs.MatlabToC.Functions.MatissePrimitives;

import org.specs.MatlabToC.Utilities.MatlabResourceProvider;

/**
 * @author Joao Bispo
 * 
 */
public enum CompatibilityPackageResource implements MatlabResourceProvider {

    NEW_ARRAY("matisse_new_array.m"),
    NEW_ARRAY_FROM_DIMS("matisse_new_array_from_dims.m"),
    NEW_ARRAY_FROM_MATRIX("matisse_new_array_from_matrix.m"),
    NEW_ARRAY_FROM_VALUES("matisse_new_array_from_values.m"),
    TO_REAL("matisse_to_real.m"),
    CHANGE_SHAPE("matisse_change_shape.m"),
    IDIVIDE("matisse_idivide.m"),
    MAX_OR_ZERO("matisse_max_or_zero.m"),
    RESERVE_CAPACITY("MATISSE_reserve_capacity.m");

    private final static String RESOURCE_FOLDER = "mfiles/compatibility_package/";

    private final String resourceFilename;

    private CompatibilityPackageResource(String resource) {
        this.resourceFilename = RESOURCE_FOLDER + resource;
    }

    /**
     * @return the resource
     */
    @Override
    public String getResource() {
        return resourceFilename;
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
