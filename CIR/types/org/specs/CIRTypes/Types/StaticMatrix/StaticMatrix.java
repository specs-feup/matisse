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

package org.specs.CIRTypes.Types.StaticMatrix;

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.ATypes.Matrix.AMatrix;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;

public class StaticMatrix extends AMatrix {

    private final StaticMatrixType type;

    public StaticMatrix(StaticMatrixType staticMatrix) {
	this.type = staticMatrix;
    }

    @Override
    public StaticMatrixFunctions functions() {
	return new StaticMatrixFunctions();
    }

    @Override
    public Integer getNumElements() {
	return type.getTypeShape().getNumElements();
    }

    @Override
    public ScalarType getElementType() {
	return type.getElementType();
    }

    @Override
    public TypeShape getShape() {
	return type.getShape();
    }

    @Override
    public boolean isView() {
	return type.isView();
    }

    @Override
    public MatrixType getView() {
	if (type.isView()) {
	    return type;
	}

	return StaticMatrixType.newInstanceView(type, true);
    }

    @Override
    public MatrixType setElementType(ScalarType elementType) {
	return type.setElementType(elementType);
    }

    @Override
    public boolean usesDynamicAllocation() {
	return false;
    }

    /* (non-Javadoc)
     * @see org.specs.CIRv2.Types.ATypes.Matrix.AMatrix#setShape(org.specs.CIRv2.Types.MatrixShape)
     */
    @Override
    public MatrixType setShape(TypeShape shape) {
	return type.setShape(shape);
    }

}
