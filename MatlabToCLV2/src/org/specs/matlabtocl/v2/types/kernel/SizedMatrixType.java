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

package org.specs.matlabtocl.v2.types.kernel;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.Matrix;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.Views.Code.Code;

public class SizedMatrixType extends MatrixType {
    private final RawBufferMatrixType underlyingRawMatrixType;
    private final boolean storeNumel;
    private final int storedDims;

    public SizedMatrixType(AddressSpace addressSpace, ScalarType elementType, boolean storeNumel, int storedDims) {
	this.underlyingRawMatrixType = new RawBufferMatrixType(addressSpace, elementType);
	this.storeNumel = storeNumel;
	this.storedDims = storedDims;
    }

    @Override
    public Matrix matrix() {
	return new SizedMatrix(this);
    }

    public ScalarType getElementType() {
	return this.underlyingRawMatrixType.getElementType();
    }

    public AddressSpace getAddressSpace() {
	return this.underlyingRawMatrixType.getAddressSpace();
    }

    public RawBufferMatrixType getUnderlyingRawMatrixType() {
	return this.underlyingRawMatrixType;
    }

    public boolean containsNumel() {
	return this.storeNumel;
    }

    public int containedDims() {
	return this.storedDims;
    }

    @Override
    public Code code() {
	return new SizedMatrixCode(this);
    }

    @Override
    public String getSmallId() {
	return "s" + this.underlyingRawMatrixType.getSmallId() + "_" + (this.storeNumel ? "n" : "") + this.storedDims;
    }

    @Override
    public boolean strictEquals(VariableType type) {
	return equals(type);
    }
}
