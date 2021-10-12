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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.specs.MatlabWeaverTest.utils.TestUtils;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.properties.SpecsProperty;

/**
 * @author Joao Bispo
 * 
 */
@RunWith(Suite.class)
@SuiteClasses({ MatlabToMatlabTests.class, MatlabWithCAspectsTests.class

})
public class MWeaverSuite {

    @BeforeClass
    public static void setUp() {
	SpecsSystem.programStandardInit();

	SpecsProperty.ShowStackTrace.applyProperty("true");

	SpecsLogs.msgInfo("Setting up suite");

    }

    @AfterClass
    public static void tearDown() {
	SpecsLogs.msgInfo("Tearing down suite");

	TestUtils.deleteTestFolder();

    }

}
