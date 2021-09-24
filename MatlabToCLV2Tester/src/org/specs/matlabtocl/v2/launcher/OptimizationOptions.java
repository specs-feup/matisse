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

package org.specs.matlabtocl.v2.launcher;

public final class OptimizationOptions {
    public final boolean assumeMatrixIndicesInRange;
    public final boolean assumeMatrixSizesMatch;
    public final boolean enableZ3;

    public OptimizationOptions(boolean assumeMatrixIndicesInRange,
	    boolean assumeMatrixSizesMatch,
	    boolean enableZ3) {

	this.assumeMatrixIndicesInRange = assumeMatrixIndicesInRange;
	this.assumeMatrixSizesMatch = assumeMatrixSizesMatch;
	this.enableZ3 = enableZ3;
    }
}
