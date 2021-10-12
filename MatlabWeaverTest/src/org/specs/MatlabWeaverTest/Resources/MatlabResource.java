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

package org.specs.MatlabWeaverTest.Resources;

import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * @author Joao Bispo
 *
 */
public enum MatlabResource implements ResourceProvider {
    CLOSURE("closure.m"),
    FIR("fir.m"),
    GRID_ITERATE("grid_iterate.m"),
    HARRIS("harris.m"),
    LATNRM("latnrm.m"),
    MULT("mult.m"),
    SUBBAND("subband.m");

    private final String resource;

    private static final String basePackage = "matlab/";

    /**
     * @param resource
     */
    private MatlabResource(String resource) {
	this.resource = basePackage + resource;
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.util.Interfaces.ResourceProvider#getResource()
     */
    @Override
    public String getResource() {
	return resource;
    }

}
