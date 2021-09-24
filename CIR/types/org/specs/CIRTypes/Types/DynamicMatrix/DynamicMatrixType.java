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

package org.specs.CIRTypes.Types.DynamicMatrix;

import static com.google.common.base.Preconditions.*;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.Types.AVariableType;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Code.Code;
import org.specs.CIR.Types.Views.Conversion.Conversion;
import org.specs.CIR.Types.Views.Pointer.Reference;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixStruct;

/**
 * TODO: Implement builder pattern (http://www.javacodegeeks.com/2013/01/the-builder-pattern-in-practice.html)
 * 
 * @author Joao Bispo
 * 
 */
public class DynamicMatrixType extends MatrixType {

    private final ScalarType elementType;
    private final boolean isReference;
    private final boolean isView;
    private TypeShape shape;

    private FunctionInstance structInstance;

    private DynamicMatrixType(ScalarType elementType, boolean isPointer, boolean isView, TypeShape shape) {
	this.elementType = elementType;
	this.isReference = isPointer;
	this.isView = isView;
	this.shape = shape;
    }

    public static DynamicMatrixType newInstance(DynamicMatrixType type, boolean isPointer) {
	return new DynamicMatrixType(type.elementType, isPointer, type.isView, type.shape);
    }

    public static DynamicMatrixType newInstanceView(DynamicMatrixType type, boolean isView) {
	return new DynamicMatrixType(type.elementType, type.isReference, isView, type.shape);
    }

    // public static DynamicMatrixType newInstance(VariableType elementType, List<Integer> shape) {
    public static DynamicMatrixType newInstance(VariableType elementType, List<Integer> shape) {
	return DynamicMatrixType.newInstance(elementType, TypeShape.newInstance(shape));
    }

    public static DynamicMatrixType newInstance(VariableType elementType, TypeShape shape) {
	checkArgument(shape != null, "shape must not be null");

	if (!ScalarUtils.isScalar(elementType)) {
	    throw new RuntimeException("Cannot create a dynamic matrix with element type '" + elementType
		    + "' because it is not a Scalar");
	}

	ScalarType scalarType = ScalarUtils.cast(elementType);
	return new DynamicMatrixType(scalarType, false, false, shape);
    }

    // public static Dynamic

    public ScalarType getElementType() {
	return this.elementType;
    }

    public TypeShape getShape() {
	return this.shape;
    }

    public DynamicMatrixType setShape(TypeShape shape) {
	DynamicMatrixType newType = (DynamicMatrixType) copy();
	newType.setShapePrivate(shape);
	return newType;
    }

    public DynamicMatrixType setElementType(ScalarType elementType) {
	return new DynamicMatrixType(elementType, isByReference(), isView(), getShape());
    }

    private void setShapePrivate(TypeShape shape) {
	this.shape = shape;
    }

    public boolean isByReference() {
	return this.isReference;
    }

    public boolean isView() {
	return this.isView;
    }

    public synchronized FunctionInstance getStructInstance() {
	if (this.structInstance == null) {
	    this.structInstance = DynamicMatrixStruct.newInstance(this.elementType);
	}
	return this.structInstance;
    }

    /**
     * Returns an id in the format ["p"]t[element_id], where "p" is put if variable is a pointer, and element_id is the
     * id of the element type.
     */
    @Override
    public String getSmallId() {
	StringBuilder builder = new StringBuilder();

	if (isByReference()) {
	    builder.append("p");
	}

	builder.append("t");
	builder.append(getElementType().getSmallId());

	return builder.toString();
    }

    /* (non-Javadoc)
     * @see org.specs.CIRv2.Types.Code.TypeCode#getTypeCode()
     */
    @Override
    public Code code() {
	return new DynamicMatrixCode(this);
    }

    @Override
    public Reference pointer() {
	return new DynamicMatrixReference(this);
    }

    @Override
    public DynamicMatrix matrix() {
	return new DynamicMatrix(this);
    }

    @Override
    public Conversion conversion() {
	return new DynamicMatrixConversion(this);
    }

    @Override
    public boolean usesDynamicAllocation() {
	return true;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();

	if (this.isReference) {
	    builder.append("P");
	}

	builder.append(super.toString())
		.append("(" + this.elementType + ", shape=" + getShape() + ")");

	return builder.toString();
    }

    public static DynamicMatrixType newInstance(VariableType elementType) {
	return newInstance(elementType, TypeShape.newUndefinedShape());
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Types.AVariableType#copyPrivate()
     */
    @Override
    protected AVariableType copyPrivate() {
	return new DynamicMatrixType(this.elementType, this.isReference, this.isView, this.shape);
    }

    @Override
    public boolean strictEquals(VariableType type) {
	if (type instanceof DynamicMatrixType) {
	    return strictEquals((DynamicMatrixType) type);
	}

	return false;
    }

    public boolean strictEquals(DynamicMatrixType type) {
	/*    private final ScalarType elementType;
	    private final boolean isReference;
	    private final boolean isView;
	    private TypeShape shape;*/

	if (type == null) {
	    return false;
	}

	if (!this.elementType.strictEquals(type.elementType)) {
	    return false;
	}

	if (this.isReference != type.isReference) {
	    return false;
	}

	if (this.isView != type.isView) {
	    return false;
	}

	return this.shape.equals(type.shape);
    }

}
