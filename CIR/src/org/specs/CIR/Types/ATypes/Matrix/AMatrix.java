/**
 * Copyright 2014 SPeCS.
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

package org.specs.CIR.Types.ATypes.Matrix;

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;

public abstract class AMatrix implements Matrix {

    /* (non-Javadoc)
     * @see org.specs.CIRv2.Types.ATypes.Matrix.Matrix#functions()
     */
    @Override
    public MatrixFunctions functions() {
	throw new UnsupportedOperationException();
    }

    @Override
    public Integer getNumElements() {
	// return -1;
	return null;
    }

    @Override
    // public VariableType getElementType() {
    public ScalarType getElementType() {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean isView() {
	throw new UnsupportedOperationException();
    }

    @Override
    public MatrixType getView() {
	throw new UnsupportedOperationException();
    }

    @Override
    public TypeShape getShape() {
	throw new UnsupportedOperationException();
    }

    @Override
    public MatrixType setShape(TypeShape shape) {
	throw new UnsupportedOperationException("Unsupported to '" + getClass() + "'");
    }

    @Override
    public MatrixType setElementType(ScalarType elementType) {
	throw new UnsupportedOperationException("Unsupported to '" + getClass() + "'");
    }
}
