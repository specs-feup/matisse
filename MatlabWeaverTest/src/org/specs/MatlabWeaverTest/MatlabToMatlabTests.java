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

package org.specs.MatlabWeaverTest;

import static org.junit.Assert.*;

import org.junit.Test;
import org.specs.MatlabWeaverTest.Resources.AspectsResource;
import org.specs.MatlabWeaverTest.Resources.MatlabResource;
import org.specs.MatlabWeaverTest.utils.TestUtils;

import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.properties.SpecsProperty;

/**
 * @author Joao Bispo
 * 
 */
public class MatlabToMatlabTests {

    private final TestUtils testUtils;

    public MatlabToMatlabTests() {
	this.testUtils = new TestUtils();
    }

    /**
     * @throws java.lang.Exception
     */
    /*
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    ProcessUtils.programStandardInit();
    }
    */

    @Test
    public void harrisSecondaryConcerns() {
	boolean success = testUtils.runMatlabToMatlab(MatlabResource.HARRIS, AspectsResource.HARRIS_2ND_CONCERNS);
	assertTrue(success);

    }

    // @Test
    public void gridIterateCountLoopIterAndNumExec() {

	// TODO: Is not working
	boolean success = testUtils.runMatlabToMatlab(MatlabResource.GRID_ITERATE,
		AspectsResource.COUNT_LOOP_ITERATIONS);
	assertTrue(success);

    }

    // TODO: Is not working
    // @Test
    public void gridIterateCountLoopIterations() {

	boolean success = testUtils.runMatlabToMatlab(MatlabResource.GRID_ITERATE,
		AspectsResource.COUNT_LOOP_ITERATIONS);
	assertTrue(success);

    }

    // TODO: Is not working
    // @Test
    public void gridIterateMonitor() {

	boolean success = testUtils
		.runMatlabToMatlab(MatlabResource.GRID_ITERATE, AspectsResource.GRID_ITERATE_MONITOR);
	assertTrue(success);

    }

    @Test
    public void sectionNumOfExectuions() {

	boolean success = testUtils.runMatlabToMatlab(MatlabResource.GRID_ITERATE,
		AspectsResource.SECTION_NUM_OF_EXECUTIONS);
	assertTrue(success);

    }

    @Test
    public void sectionNumOfSpecificExecutions() {
	SpecsSystem.programStandardInit();
	SpecsProperty.ShowStackTrace.applyProperty("true");

	boolean success = testUtils.runMatlabToMatlab(MatlabResource.GRID_ITERATE,
		AspectsResource.SECTION_NUM_OF_SPECIFIC_EXECUTIONS);
	assertTrue(success);

    }
}
