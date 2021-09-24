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

package org.specs.MatlabToC.Functions.MatlabOpsV2;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * @author Joao Bispo
 * 
 */
public enum OpsResource implements ResourceProvider {

    // MATRIX_MUL("matrix_mul.m"),
    // MATRIX_MULV2("matrix_mulv2.m");
    MATRIX_MULV3_WITH_ACC("matrix_mulv3_with_acc.m"),
    MATRIX_MULV3("matrix_mulv3.m");

    private final static String RESOURCE_FOLDER = "mfiles/ops/";

    private final String resourceFilename;

    private OpsResource(String resource) {
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
