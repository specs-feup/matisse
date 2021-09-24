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

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.Matrix;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.Views.Code.Code;

public class RawBufferMatrixType extends MatrixType {

    private final AddressSpace addressSpace;
    private final ScalarType elementType;

    public RawBufferMatrixType(AddressSpace addressSpace, ScalarType elementType) {
	this.addressSpace = addressSpace;
	this.elementType = elementType;
    }

    @Override
    public Matrix matrix() {
	return new RawBufferMatrix(this);
    }

    public ScalarType getElementType() {
	return this.elementType;
    }

    public AddressSpace getAddressSpace() {
	return this.addressSpace;
    }

    @Override
    public Code code() {
	return new RawBufferMatrixCode(this);
    }

    @Override
    public String getSmallId() {
	return "b" + this.addressSpace.getSmallId() + this.elementType.getSmallId();
    }

    @Override
    public boolean strictEquals(VariableType type) {
	return equals(type);
    }
}
