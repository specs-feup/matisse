/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.MatlabToC.Functions.MathFunctions.Static.mean;

import pt.up.fe.specs.util.SpecsIo;

/**
 * Contains the resources needed by {@link MeanDecFunctions}.
 * 
 * @author Pedro Pinto
 * 
 */
public enum MeanDecResources {

    MEAN_DEC_MATRIX("mean_dec_matrix.c"),
    MEAN_DEC_MATRIX_N("mean_dec_matrix_n.c");

    private MeanDecResources(String resource) {
	this.resource = resource;
    }

    /**
     * @return a {@link String} with the resource path
     */
    private String getResourcePath() {

	return MeanDecResources.RESOURCE_FOLDER + "/" + this.resource;
    }

    /**
     * @return a {@link String} with the template
     */
    public String getTemplate() {

	return SpecsIo.getResource(getResourcePath());
    }

    private final String resource;
    private final static String RESOURCE_FOLDER = "templates/mean_dec";
}
