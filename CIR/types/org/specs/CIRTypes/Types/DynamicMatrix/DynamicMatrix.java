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

package org.specs.CIRTypes.Types.DynamicMatrix;

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.ATypes.Matrix.AMatrix;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;

public class DynamicMatrix extends AMatrix {

    private final DynamicMatrixType type;

    public DynamicMatrix(DynamicMatrixType dynamicMatrix) {
	this.type = dynamicMatrix;
    }

    /* (non-Javadoc)
     * @see org.specs.CIRv2.Types.ATypes.Matrix.AMatrix#functions()
     */
    @Override
    public DynamicMatrixFunctions functions() {
	return new DynamicMatrixFunctions();
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
    public MatrixType setShape(TypeShape shape) {
	return type.setShape(shape);
    }

    @Override
    public MatrixType setElementType(ScalarType elementType) {
	return type.setElementType(elementType);
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

	return DynamicMatrixType.newInstance(type, true);
    }

    @Override
    public boolean usesDynamicAllocation() {
	return true;
    }
}
