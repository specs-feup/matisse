/**
 * Copyright 2015 SPeCS.
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

package org.specs.MatlabWeaverTest.base;

import org.junit.Test;
import org.specs.MatlabWeaverTest.utils.TestUtils;

import pt.up.fe.specs.util.SpecsSystem;

/**
 * "Disabled" (ends with Tester) because is not passing on GitHub action, but is passing locally
 */
public class BaseTester {

    // private final TestUtils testUtils;

    public BaseTester() {
        // this.testUtils = new TestUtils();
        SpecsSystem.programStandardInit();
    }

    @Test
    public void test() {
        for (JoinpointResource resource : JoinpointResource.values()) {
            TestUtils.test(resource);
        }
        // boolean result = TestUtils.runMWeaverGui(BaseLaraResource.FUNCTION, BaseMatlabResource.TEST_CASE_1);
        // assertTrue(result);
    }

}
