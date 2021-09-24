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

package org.specs.MatlabToCTester.Auxiliary;

/**
 * @author Joao Bispo
 * 
 */
public class MatlabOptions {

    // The absolute and relative error tolerance values used in the
    // comparison of the C outputs and Matlab outputs
    private final double absEpsilon;
    private final double relEpsilon;
    private final boolean testWithMatlab;

    public MatlabOptions(double absEpsilon, double relEpsilon, boolean testWithMatlab) {
	this.absEpsilon = absEpsilon;
	this.relEpsilon = relEpsilon;
	this.testWithMatlab = testWithMatlab;
    }

    /**
     * @return the absEpsilon
     */
    public double getAbsEpsilon() {
	return absEpsilon;
    }

    /**
     * @return the relEpsilon
     */
    public double getRelEpsilon() {
	return relEpsilon;
    }

    public boolean testWithMatlab() {
	return testWithMatlab;
    }
}
