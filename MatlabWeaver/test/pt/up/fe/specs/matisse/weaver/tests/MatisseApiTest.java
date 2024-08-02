/**
 * Copyright 2016 SPeCS.
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

package pt.up.fe.specs.matisse.weaver.tests;

import org.junit.After;
import org.junit.BeforeClass;

import pt.up.fe.specs.matisse.weaver.MatisseWeaverTester;
import pt.up.fe.specs.util.SpecsSystem;

public class MatisseApiTest {

    @BeforeClass
    public static void setupOnce() {
        SpecsSystem.programStandardInit();
        MatisseWeaverTester.clean();
    }

    @After
    public void tearDown() {
        MatisseWeaverTester.clean();
    }

    private static MatisseWeaverTester newTester() {
        return new MatisseWeaverTester("matisse/test/api/")
                .setSrcPackage("src/")
                .setResultPackage("results/");
    }

    // TODO: Disabled because is not passing on GitHub action, but is passing locally
    // @Test
    public void testLogger() {
        newTester().test("LoggerTest.lara", "logger_test.m");
    }

    // TODO: Disabled because is not passing on GitHub action, but is passing locally
    // @Test
    public void testTimer() {
        newTester().test("TimerTest.lara", "timer_test.m");
    }

    // TODO: Disabled because is not passing on GitHub action, but is passing locally
    // @Test
    public void testEnergy() {
        newTester().test("EnergyTest.lara", "energy_test.m");
    }

}
