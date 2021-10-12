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
public enum AspectsResource implements ResourceProvider {

    COUNT_LOOP_ITERATIONS("count_loop_iterations.lara"),
    GRID_ITERATE_SPECIALIZATION("grid_iterate_specialization.lara"),
    HARRIS_2ND_CONCERNS("harris_2ndConcerns.lara"),
    GRID_ITERATE_MONITOR("monitor_rangevalues_grid_iterate.lara"),
    SECTION_NUM_OF_SPECIFIC_EXECUTIONS("section_num_of specific_executions.lara"),
    SECTION_NUM_OF_EXECUTIONS("section_num_of_executions.lara"),
    TYPES_FIR_SINGLE("types_fir_single.lara"),
    TYPES_GRIDITERATE_SINGLE("types_griditerate_single.lara"),
    TYPES_GRIDITERATE_DOUBLE("types_griditerate_double.lara"),
    TYPES_LATNRM_SINGLE("types_latnrm_single.lara"),
    TYPES_LATNRM_STATIC_SPECIALIZATION("types_latnrm_static_specialization.lara"),
    TYPES_MULT("types_mult.lara"),
    TYPES_SUBBAND("types_subband.lara"),
    MATISSE_OPTIONS("matisse_options_test.lara");

    private final String resource;

    private static final String basePackage = "lara/";

    /**
     * @param resource
     */
    private AspectsResource(String resource) {
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
