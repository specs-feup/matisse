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
import org.specs.matlabtocl.v2.functions.matrix.SizedMatrixData;
import org.specs.matlabtocl.v2.functions.matrix.SizedMatrixDim;
import org.specs.matlabtocl.v2.functions.matrix.SizedMatrixGet;
import org.specs.matlabtocl.v2.functions.matrix.SizedMatrixNumel;
import org.specs.matlabtocl.v2.functions.matrix.SizedMatrixSet;

public class SizedMatrixFunctions implements MatrixFunctions {
    SizedMatrixFunctions() {
    }

    @Override
    public InstanceProvider get() {
	return new SizedMatrixGet();
    }

    @Override
    public InstanceProvider set() {
	return new SizedMatrixSet();
    }

    @Override
    public InstanceProvider getDim() {
	return new SizedMatrixDim();
    }

    @Override
    public InstanceProvider numel() {
	return new SizedMatrixNumel();
    }

    @Override
    public InstanceProvider data() {
	return new SizedMatrixData();
    }
}
