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

package org.specs.CIRTypes.Types.StaticMatrix;

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;

import com.google.common.base.Preconditions;

public class StaticMatrixTypeBuilder {
    private final ScalarType elementType;
    private final TypeShape shape;
    private Number valueUpperBound;
    private Number valueLowerBound;

    private StaticMatrixTypeBuilder(ScalarType elementType, TypeShape shape) {
	Preconditions.checkArgument(elementType != null);
	Preconditions.checkArgument(shape != null);
	Preconditions.checkArgument(shape.isFullyDefined());

	this.elementType = elementType;
	this.shape = shape;
    }

    public StaticMatrixTypeBuilder inRange(Number valueLowerBound, Number valueUpperBound) {
	Preconditions.checkState(this.valueUpperBound == null);
	Preconditions.checkState(this.valueLowerBound == null);
	Preconditions.checkArgument(valueUpperBound != null);
	Preconditions.checkArgument(valueLowerBound != null);

	this.valueUpperBound = valueUpperBound;
	this.valueLowerBound = valueLowerBound;

	return this;
    }

    public static StaticMatrixTypeBuilder fromElementTypeAndShape(ScalarType elementType, TypeShape shape) {
	return new StaticMatrixTypeBuilder(elementType, shape);
    }

    public static StaticMatrixTypeBuilder fromSameTypeAndShape(MatrixType type) {
	return new StaticMatrixTypeBuilder(type.matrix().getElementType(), type.getTypeShape());
    }

    public StaticMatrixType build() {
	return StaticMatrixType.newInstance(this.elementType, this.shape,
		this.valueLowerBound, this.valueLowerBound);
    }
}
