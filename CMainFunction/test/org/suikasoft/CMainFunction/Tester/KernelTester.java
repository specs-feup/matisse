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

package org.suikasoft.CMainFunction.Tester;

import java.io.File;

import org.junit.BeforeClass;

import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.utilities.AverageType;

/**
 * @author Joao Bispo
 * 
 */
public class KernelTester {

    @BeforeClass
    public static void setUpClass() throws Exception {
	SpecsSystem.programStandardInit();
    }

    // @Test
    public void test() {
	String executablePath = "C:\\Users\\Joao Bispo\\Dropbox\\Research\\Work\\2013-06-21 Using .o when compiling with O2\\Exe-auto\\All-O2.exe";
	double minimumRuntime = 0.1; // seconds
	int numberOfRepeats = 5;

	File executable = new File(executablePath);

	// int numberOfRuns = KernelLauncher.discoverNumberOfRuns(executable, minimumRuntime);

	ExecutionResults results = KernelUtils.measureExecution(executable, 0, minimumRuntime,
		numberOfRepeats, AverageType.GEOMETRIC_MEAN);

	// Run
	// System.out.println("NUMBER OF RUNS:" + numberOfRuns);
	System.out.println("Results:" + results);
    }
}
