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

/**
 * @author Joao Bispo
 * 
 */
public class MatlabWithCAspectsTests {

    private final TestUtils testUtils;

    public MatlabWithCAspectsTests() {
	this.testUtils = new TestUtils();
    }

    @Test
    public void firSingleType() {

	// Dynamic
	boolean success = testUtils.runMatlabToMatlab(MatlabResource.FIR, AspectsResource.TYPES_FIR_SINGLE);
	assertTrue(success);

	// Static
	success = testUtils.runMatlabToMatlab(MatlabResource.FIR, AspectsResource.TYPES_FIR_SINGLE);
	assertTrue(success);
    }

    @Test
    public void gridIterateSingleType() {

	// Dynamic
	boolean success = testUtils.runMatlabToMatlab(MatlabResource.GRID_ITERATE,
		AspectsResource.TYPES_GRIDITERATE_SINGLE);
	assertTrue(success);

	// Static
	success = testUtils.runMatlabToMatlab(MatlabResource.GRID_ITERATE, AspectsResource.TYPES_GRIDITERATE_SINGLE);
	assertTrue(success);
    }

    @Test
    public void gridIterateDoubleType() {

	// Dynamic
	boolean success = testUtils.runMatlabToMatlab(MatlabResource.GRID_ITERATE,
		AspectsResource.TYPES_GRIDITERATE_DOUBLE);
	assertTrue(success);

	// Static
	success = testUtils.runMatlabToMatlab(MatlabResource.GRID_ITERATE, AspectsResource.TYPES_GRIDITERATE_DOUBLE);
	assertTrue(success);
    }

    @Test
    public void latnrmSingleTypeDynamic() {

	// Dynamic
	boolean success = testUtils.runMatlabToMatlab(MatlabResource.LATNRM, AspectsResource.TYPES_LATNRM_SINGLE);
	assertTrue(success);
    }

    @Test
    public void latnrmSingleTypeStaticAndSpecialization() {

	// Dynamic
	boolean success = testUtils.runMatlabToMatlab(MatlabResource.LATNRM,
		AspectsResource.TYPES_LATNRM_STATIC_SPECIALIZATION);
	assertTrue(success);

	// Static
	success = testUtils
		.runMatlabToMatlab(MatlabResource.LATNRM, AspectsResource.TYPES_LATNRM_STATIC_SPECIALIZATION);
	assertTrue(success);
    }

    @Test
    public void subbandType() {

	// Dynamic
	boolean success = testUtils.runMatlabToMatlab(MatlabResource.SUBBAND, AspectsResource.TYPES_SUBBAND);
	assertTrue(success);

	// Static
	success = testUtils.runMatlabToMatlab(MatlabResource.SUBBAND, AspectsResource.TYPES_SUBBAND);
	assertTrue(success);
    }

    @Test
    public void multType() {

	// Dynamic
	boolean success = testUtils.runMatlabToMatlab(MatlabResource.MULT, AspectsResource.TYPES_MULT);
	assertTrue(success);

	// Static
	success = testUtils.runMatlabToMatlab(MatlabResource.MULT, AspectsResource.TYPES_MULT);
	assertTrue(success);
    }

    @Test
    public void matisseOptionsTest() {
	SpecsSystem.programStandardInit();
	boolean success = testUtils.runMatlabToMatlab(MatlabResource.FIR, AspectsResource.MATISSE_OPTIONS);
	assertTrue(success);

    }

}
