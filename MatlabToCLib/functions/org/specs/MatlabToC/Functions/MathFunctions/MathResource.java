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

package org.specs.MatlabToC.Functions.MathFunctions;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * @author Joao Bispo
 * 
 */
public enum MathResource implements ResourceProvider {

    FIX_GENERAL("fix_general.m"),
    MEAN_STATIC("mean_static.m"),
    MEAN_DYNAMIC("mean_dynamic.m"),
    MEAN_DYNAMIC_ONEDIM("mean_dynamic_onedim.m"),
    MIN_DYNAMIC("min_dynamic.m"),
    MIN2("min2.m"),
    MIN3("min3.m"),
    PROD_VECTOR("prod_vector.m"),
    SUM_DYNAMIC("sum_dynamic.m"),
    SUM_ONE_DIM("sum_one_dim.m"),
    SUM_MATRIX("sum_matrix.m"),
    DOT1D("dot_1d.m");
    // FIND_STATIC("find_static.m");

    private final static String RESOURCE_FOLDER = "mfiles/math/";

    private final String resourceFilename;

    private MathResource(String resource) {
	this.resourceFilename = MathResource.RESOURCE_FOLDER + resource;
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
