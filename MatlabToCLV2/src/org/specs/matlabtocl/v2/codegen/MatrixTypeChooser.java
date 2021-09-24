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

package org.specs.matlabtocl.v2.codegen;

import java.util.HashMap;
import java.util.Map;

import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.matlabtocl.v2.types.kernel.AddressSpace;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;
import org.specs.matlabtocl.v2.types.kernel.RawBufferMatrixType;
import org.specs.matlabtocl.v2.types.kernel.SizedMatrixType;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class MatrixTypeChooser {
    private final Map<String, MatrixInformation> matrixInformation = new HashMap<>();

    private MatrixInformation requireInformationFor(String name) {
	MatrixInformation info = this.matrixInformation.get(name);
	if (info != null) {
	    return info;
	}

	info = new MatrixInformation();
	this.matrixInformation.put(name, info);
	return info;
    }

    public void requireNumel(String name) {
	requireInformationFor(name).requireNumel();
    }

    public void requireFullShape(String name) {
	requireInformationFor(name).requireFullShape();
    }

    public void requireAtLeastShape(String name, int numDims) {
	requireInformationFor(name).requireAtLeastShape(numDims);
    }

    public MatrixType buildMatrixType(String name, AddressSpace addressSpace, CLNativeType underlyingCLType) {

	MatrixInformation info = requireInformationFor(name);

	if (info.needsFullShape()) {
	    throw new NotImplementedException("Matrix type with full shape");
	}

	if (info.needsNumel() || info.needsAtLeastShape() > 0) {
	    return new SizedMatrixType(AddressSpace.GLOBAL, underlyingCLType, info.needsNumel(),
		    info.needsAtLeastShape());
	}

	return new RawBufferMatrixType(AddressSpace.GLOBAL, underlyingCLType);
    }
}
