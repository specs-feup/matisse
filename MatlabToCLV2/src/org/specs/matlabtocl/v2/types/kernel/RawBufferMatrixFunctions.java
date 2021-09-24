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

package org.specs.matlabtocl.v2.types.kernel;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Types.ATypes.Matrix.MatrixFunctions;
import org.specs.matlabtocl.v2.functions.matrix.RawMatrixGet;
import org.specs.matlabtocl.v2.functions.matrix.RawMatrixSet;

public class RawBufferMatrixFunctions implements MatrixFunctions {
    RawBufferMatrixFunctions() {
    }

    @Override
    public InstanceProvider get() {
	return new RawMatrixGet();
    }

    @Override
    public InstanceProvider set() {
	return new RawMatrixSet();
    }
}
