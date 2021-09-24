/**
 * Copyright 2013 SPeCS.
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

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Code.Code;
import org.specs.CIR.Types.Views.Conversion.Conversion;

import com.google.common.base.Preconditions;

/**
 * TODO: Implement BuilderPattern (http://www.javacodegeeks.com/2013/01/the-builder-pattern-in-practice.html)
 * 
 * @author Joao Bispo
 * 
 */
public final class StaticMatrixType extends MatrixType {

    private final ScalarType elementType;
    private TypeShape shape;
    private final Number lowerBound, upperBound;
    private final boolean isView;

    private StaticMatrixType(ScalarType elementType, TypeShape shape,
	    Number lowerBound, Number upperBound,
	    boolean isView) {

	this.elementType = elementType;
	this.shape = shape;
	this.lowerBound = lowerBound;
	this.upperBound = upperBound;
	this.isView = isView;

	if (elementType.scalar().hasConstant()) {
	    // Most likely a bug.
	    // If we later decide to take advantage of this behavior, we can remove this.
	    throw new RuntimeException("Underlying matrix type has constant value.");
	}

	if (shape.getNumElements() == null) {
	    throw new RuntimeException("Shape of a Static Matrix cannot be null.");
	}
    }

    public static StaticMatrixType newInstanceView(StaticMatrixType type, boolean isView) {
	return new StaticMatrixType(type.elementType, type.shape, null, null, isView);
    }

    public static StaticMatrixType newInstance(VariableType elementType, Integer... shape) {
	return newInstance(elementType, Arrays.asList(shape));
    }

    @Override
    protected StaticMatrixType copyPrivate() {
	return new StaticMatrixType(this.elementType, this.shape,
		this.lowerBound, this.upperBound,
		this.isView);
    }

    /**
     * @param shape
     *            the shape to set
     */
    public StaticMatrixType setShape(TypeShape shape) {
	StaticMatrixType newType = (StaticMatrixType) this.copy();
	newType.setShapePrivate(shape);

	return newType;
    }

    /**
     * @param shape
     *            the shape to set
     */
    private void setShapePrivate(TypeShape shape) {
	this.shape = shape;

	// if (shape.getNumElements() == null) {
	if (!shape.isFullyDefined()) {
	    throw new RuntimeException("Shape of a Static Matrix must be fully defined. Shape: " + shape);
	}
    }

    public StaticMatrixType setElementType(ScalarType elementType) {
	return new StaticMatrixType(elementType, getShape(),
		this.lowerBound, this.upperBound,
		isView());
    }

    public ScalarType getElementType() {
	return this.elementType;
    }

    public TypeShape getShape() {
	return this.shape;
    }

    public boolean isView() {
	return this.isView;
    }

    @Override
    public String getSmallId() {
	StringBuilder builder = new StringBuilder();

	// Append element type
	builder.append(getElementType().getSmallId());
	// Append dimension
	builder.append(getTypeShape().getString());

	return builder.toString();
    }

    /* (non-Javadoc)
     * @see org.specs.CIRv2.Types.Code.TypeCode#getTypeCode()
     */
    @Override
    public Code code() {
	return new StaticMatrixCode(this);
    }

    @Override
    public StaticMatrix matrix() {
	return new StaticMatrix(this);
    }

    @Override
    public Conversion conversion() {
	return new StaticMatrixConversion(this);
    }

    /* (non-Javadoc)
     * @see org.specs.CIRv2.Types.AVariableType#toString()
     */
    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();

	builder.append(super.toString());
	builder.append(" - ");
	builder.append(this.elementType.toString());
	builder.append(" (");
	builder.append(getShape());
	builder.append(")");

	if (this.lowerBound != null || this.upperBound != null) {
	    builder.append(", in [");
	    builder.append(this.lowerBound);
	    builder.append(", ");
	    builder.append(this.upperBound);
	    builder.append("]");
	}

	return builder.toString();
    }

    /**
     * @param varType
     * @param shape
     * @return
     */
    public static StaticMatrixType newInstance(VariableType elementType, List<Integer> shape) {
	return newInstance(elementType, TypeShape.newInstance(shape), null, null);
	/*
	if (!ScalarUtils.isScalar(elementType)) {
	    throw new RuntimeException("Cannot create a static matrix with element type '" + elementType
		    + "' because it is not a Scalar");
	}
	
	ScalarType scalarType = ScalarUtils.cast(elementType);
	return new StaticMatrixType(scalarType, MatrixShape.newInstance(shape), false);
	 */
    }

    public static StaticMatrixType newInstance(VariableType elementType, TypeShape shape, Number lowerBound,
	    Number upperBound) {
	Preconditions.checkArgument(elementType != null);
	Preconditions.checkArgument(shape != null);

	// Shape must be defined
	if (!shape.isFullyDefined()) {
	    throw new RuntimeException(
		    "Cannot create a static matrix with a shape that is not fully defined: " + shape);
	}

	if (!ScalarUtils.isScalar(elementType)) {
	    throw new RuntimeException("Cannot create a static matrix with element type '" + elementType
		    + "' because it is not a Scalar");
	}

	ScalarType scalarType = ScalarUtils.cast(elementType);
	return new StaticMatrixType(scalarType, shape, lowerBound, upperBound, false);
    }

    public StaticMatrixType asVerticalFlat() {
	StaticMatrixType newType = copyPrivate();
	newType.setShapePrivate(TypeShape.newInstance(this.shape.getNumElements(), 1));

	return newType;
    }

    public Number getLowerBound() {
	return this.lowerBound;
    }

    public Number getUpperBound() {
	return this.upperBound;
    }

    @Override
    public boolean strictEquals(VariableType type) {
	if (type instanceof StaticMatrixType) {
	    return strictEquals((StaticMatrixType) type);
	}

	return false;
    }

    public boolean strictEquals(StaticMatrixType type) {
	if (type == null) {
	    return false;
	}

	if (!this.elementType.strictEquals(type.elementType)) {
	    return false;
	}

	if (!this.shape.equals(type.shape)) {
	    return false;
	}

	if (this.lowerBound == null) {
	    if (type.lowerBound != null) {
		return false;
	    }
	} else if (!this.lowerBound.equals(type.lowerBound)) {
	    return false;
	}

	if (this.upperBound == null) {
	    if (type.upperBound != null) {
		return false;
	    }
	} else if (!this.upperBound.equals(type.upperBound)) {
	    return false;
	}

	return this.isView == type.isView;
    }
}
