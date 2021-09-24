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

package org.specs.CIR.Types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * Represents the shape of a type.
 * 
 * <p>
 * - The shape can be defined (number of dimensions > 1) or undefined (number of dimensions == 0). <br>
 * - The sizes of the dimensions themselves can be defined (>0), empty (0) or undefined (-1) <br>
 * 
 * @author Joao Bispo
 * 
 */
public class TypeShape {

    private final List<Integer> shape;
    private final List<Number> values;
    private final ShapeType type;
    private final boolean force1D;

    enum ShapeType {
        UNDEFINED,
        SCALAR,
        ROW,
        COLUMN,
        MATRIX
    }

    private static final int UNDEFINED_SQUARE_VALUE = -2;

    // Cached Shapes
    private static final TypeShape UNDEFINED_SHAPE = newInstance(Collections.emptyList());
    private static final TypeShape SCALAR_SHAPE = newInstance(1, 1);

    /**
     * Builds a new MatrixShape. Internally, shape does not have null values, undefined dimensions are represented
     * internally as -1.
     * 
     * @param shape
     * @param values
     */
    private TypeShape(List<Integer> shape, List<Number> values, boolean force1D) {
        if (shape.size() == 1) {
            SpecsLogs.msgInfo(" - Creating shape with a single dimension, assuming column vector");
            // LoggingUtils.msgWarn("1D matrix shape");
        }

        this.shape = processShape(shape);
        this.values = values;
        this.type = getType(shape);
        this.force1D = force1D;

        // If values is not empty, the number of values has to be the same as the number of elements given by the shape
        if (!values.isEmpty()) {
            Preconditions.checkArgument(getNumElements().intValue() == values.size());
        }
    }

    private ShapeType getType(List<Integer> shape2) {
        if (isUndefined()) {
            return ShapeType.UNDEFINED;
        }

        // Special case of row vector, where first dimension is 1
        if (this.shape.size() == 2 && this.shape.get(0) == 1 && this.shape.get(1) != 1) {
            return ShapeType.ROW;
        }

        // Special case of column vector, where second dimension is 1
        if (this.shape.size() == 2 && this.shape.get(0) != 1 && this.shape.get(1) == 1) {
            return ShapeType.COLUMN;
        }

        // Check if all of the dimensions are equal to 1 (scalar)
        boolean allOnes = true;
        for (Integer dim : this.shape) {
            if (!dim.equals(1)) {
                allOnes = false;
                break;
            }
        }

        if (allOnes) {
            return ShapeType.SCALAR;
        }

        return ShapeType.MATRIX;

    }

    private static List<Integer> processShape(List<Integer> shape) {
        // If shape is null, return empty list
        if (shape == null) {
            return Collections.emptyList();
        }

        if (shape.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> newShape = new ArrayList<>();

        // Replace null values with '-1'
        for (int i = 0; i < shape.size(); i++) {
            Integer shapeValue = shape.get(i);

            if (shapeValue == null) {
                shapeValue = -1;
            }

            newShape.add(shapeValue);
        }

        for (int i = newShape.size(); i < 2; ++i) {
            newShape.add(1);
        }

        int lastNot1 = 1;
        for (int i = 2; i < newShape.size(); ++i) {
            if (newShape.get(i) != 1) {
                lastNot1 = i;
            }
        }

        newShape.subList(lastNot1 + 1, newShape.size()).clear();

        return Collections.unmodifiableList(newShape);
    }

    private TypeShape(List<Integer> shape) {
        this(shape, Collections.emptyList(), false);
        // this.shape = shape;
    }

    public static TypeShape newDimsShape(int numDims) {
        // A value of -1 for a dimension means it is undefined
        List<Integer> dims = new ArrayList<>(numDims);
        for (int i = 0; i < numDims; i++) {
            dims.add(-1);
        }

        return newInstance(dims);
        // return newInstance(new Integer[numDims]);
    }

    public static TypeShape newInstance(Integer... dims) {
        List<Integer> dimsList = SpecsFactory.newArrayList(dims.length);
        for (Integer dim : dims) {
            dimsList.add(dim);
        }

        return new TypeShape(dimsList);
    }

    public static TypeShape newInstance(List<Integer> shape) {
        return new TypeShape(shape);
    }

    public static TypeShape newInstanceFromStrings(List<String> shape) {
        List<Integer> indexes = shape.stream()
                .map(index -> Integer.decode(index))
                .collect(Collectors.toList());

        return newInstance(indexes);
    }

    public static <T extends Number> TypeShape newShapeWithValues(List<Integer> shape, List<T> values) {
        List<Number> numberValues = new ArrayList<>(values);
        return new TypeShape(shape, numberValues, false);
    }

    public static TypeShape newUndefinedShape() {
        // return newInstance((List<Integer>) null);
        // return newInstance(Collections.emptyList());
        return TypeShape.UNDEFINED_SHAPE;
    }

    public static TypeShape newUndefinedSquare() {
        return newInstance(TypeShape.UNDEFINED_SQUARE_VALUE, TypeShape.UNDEFINED_SQUARE_VALUE);
    }

    /**
     * A Row shape with undefined size.
     * 
     * @return
     */
    public static TypeShape newRow() {
        return newInstance(1, -1);
    }

    public static TypeShape newRow(int numCols) {
        return newInstance(1, numCols);
    }

    /**
     * A column shape with undefined size.
     * 
     * @return
     */
    public static TypeShape newColumn() {
        return newInstance(-1, 1);
    }

    public static TypeShape newColumn(int numRows) {
        return newInstance(numRows, 1);
    }

    public static TypeShape new1D() {
        return new TypeShape(Arrays.asList(-1, -1), Collections.emptyList(), true);
    }

    public static TypeShape newEmpty() {
        return newInstance(0, 0);
    }

    /**
     * Returns the number of elements represented by this shape. If the shape is not fully defined, the number of
     * elements cannot be calculated and throws an exception.
     * 
     * 
     * @param shape
     * @return
     */
    public Integer getNumElements() {

        // Check if shape is undefined
        if (isUndefined()) {
            throw new RuntimeException("Shape of the matrix undefined: " + this);
        }

        // Check if the shape is null
        /*
        	if (shape == null) {
        	    LoggingUtils.msgWarn("1. Returning -1, check if ok.");
        	    // return null;
        	    return -1;
        	}
         */
        // Check if the shape is empty
        // if (shape.isEmpty()) {
        // No need, during calculation it will be zero
        // if (isEmpty()) {
        // return 0;
        // }
        /*
        	Integer acc = shape.get(0);
        
        	// If an undefined dimension is found, return -1
        	if (acc == -1) {
        	    LoggingUtils.msgWarn("2. Returning -1, check if ok.");
        	    // return null;
        	    return -1;
        	}
         */
        Integer acc = 1;
        for (int i = 0; i < this.shape.size(); i++) {
            Integer dim = this.shape.get(i);
            if (dim < 0) {
                throw new RuntimeException("Dimension '" + i + "' of the matrix shape is undefined: " + this);
                // LoggingUtils.msgWarn("3. Returning -1, check if ok.");
                // return null;
                // return -1;
            }
            acc = acc * dim;
        }

        return acc;
    }

    /**
     * The number of dimensions of the shape. Column and row vectors are treated as a special case and return a
     * dimension size of 1. Scalars return 0.
     * 
     * <p>
     * If no shape is defined, returns -1.
     * 
     * @return
     */
    public int getNumDims() {

        switch (this.type) {
        case UNDEFINED:
            return -1;
        case SCALAR:
            return 0;
        case ROW:
            return 1;
        case COLUMN:
            return 1;
        case MATRIX:
            return this.shape.size();
        default:
            throw new RuntimeException("ShapeType not defined:" + this.type);

        }
        /*
        if (isUndefined()) {
        return -1;
        }
        
        // Special case of row vector, where first dimension is 1
        if (shape.size() == 2 && shape.get(0) == 1 && shape.get(1) != 1) {
        return 1;
        }
        
        // Special case of column vector, where second dimension is 1
        if (shape.size() == 2 && shape.get(0) != 1 && shape.get(1) == 1) {
        return 1;
        }
        
        // Check if all of the dimensions are different than zero
        for (Integer dim : shape) {
        if (dim == null) {
        	continue;
        }
        
        if (dim == 0) {
        	return 0;
        }
        
        // Dimension is undefined, but counts toward dimension count
        if (dim < 0) {
        	continue;
        	// throw new RuntimeException("Do not know what to do when dimension is less than 0");
        }
        
        }
        
        return shape.size();
         */
    }

    public boolean isScalar() {
        return this.type == ShapeType.SCALAR;
    }

    /**
     * Gets the number of dimensions of the matrix, or -1 if it is not known.
     */
    public int getRawNumDims() {
        return this.shape.size() == 0 ? -1 : this.shape.size();
    }

    /**
     * A matrix is empty if any of its dimensions has the value 0.
     * 
     * <p>
     * - If the shape is not defined, returns false. <br>
     * 
     * @return
     */
    public boolean isKnownEmpty() {
        // It is possible for a matrix to be partially undefined
        // and still be known to be empty. Example: shape [-1, 0].

        if (isUndefined()) {
            return false;
        }

        for (int dim : getDims()) {
            if (dim == 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * 
     * @return true if the shape is undefined (has no information about the number of dimensions)
     */
    public boolean isUndefined() {
        return this.shape.isEmpty();
    }

    /**
     * Similar to {@link #getDim(int)} but:
     * <ul>
     * <li>accessing non-zero dimensions of empty matrices may return a non-zero value (as opposed to throw an error);
     * <li>positions after the end return 1.
     * 
     * @param dim
     *            The index of the dimension
     * @return The relevant dimension index
     */
    public int getRawDim(int dim) {
        Preconditions.checkArgument(dim >= 0);
        if (dim >= this.shape.size()) {
            return 1;
        }

        return this.shape.get(dim);
    }

    /**
     * Returns the size of the specified dimension, or -1 if the dimension is not defined. Use zero-based indexing.
     * 
     * @param dim
     * @return
     */
    public int getDim(int dim) {
        if (isKnownEmpty()) {
            if (dim < this.shape.size() && this.shape.get(dim) == 0) {
                // This case is safe. The matrix is empty, but it *is* correctly identified as empty.
                return 0;
            }
            throw new RuntimeException("Matrix is empty, check how to proceed in this case");
        }

        if (dim >= this.shape.size()) {
            throw new RuntimeException("[CHECK] Dim " + dim + " exceeds dimensions. should return 1 in this case?");
        }

        return this.shape.get(dim);
        // Integer value = shape.get(dim);

        // return value == null ? -1 : value;
    }

    /**
     * If no shape is defined, returns empty list.
     * 
     * @return
     */
    public List<Integer> getDims() {
        return this.shape;
    }

    /**
     * 
     * @return if the matrix is empty, returns 0. Otherwise, returns the maximum dimension of the matrix
     */
    public Integer getMaxDim() {
        if (isKnownEmpty()) {
            return 0;
        }

        // Check if any of the dimensions is undefined.
        // If any of the dimensions is undefined, return null
        for (Integer dim : this.shape) {
            if (dim == null) {
                SpecsLogs.warn("SHOULD RETURN NULL HERE OR -1 OR EXCEPTION?");
                return null;
            }
        }
        return Collections.max(this.shape);
    }

    /**
     * Creates a string representation of the shape.
     * 
     * <p>
     * If given the shape {1, 3}, returns 1x3. If shape is empty, returns empty string.
     * 
     * @param shape
     * @return
     */
    public String getString() {
        if (isKnownEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        List<Integer> dims = getDims();

        builder.append(dims.get(0));

        for (int i = 1; i < dims.size(); i++) {
            builder.append("x");
            builder.append(dims.get(i));
        }

        return builder.toString();
    }

    /**
     * Creates another string representation of the shape.
     * 
     * <p>
     * If given the shape {1, 3}, returns [1][3]
     * 
     * @return
     */
    public String getStringV2() {
        StringBuilder builder = new StringBuilder();

        if (isUndefined()) {
            return "undefined-shape";
        }

        for (Integer dim : getDims()) {
            builder.append("[");
            builder.append(dim);
            builder.append("]");
        }

        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("[Matrix Shape: ");
        builder.append(this.shape);

        if (hasValues()) {
            builder.append(" - ").append(getValues());
        }

        int numDims = getNumDims();

        builder.append(", Dims: ");
        builder.append(numDims);

        if (this.force1D) {
            builder.append(" 1d");
        }

        builder.append("]");

        return builder.toString();
        // return "[Matrix Shape: " + shape + "]";
    }

    /**
     * A shape is considered fully defined if it contains information about the number of dimensions, and all dimensions
     * are defined (are equal or greater than 0).
     * 
     * @return
     */
    public boolean isFullyDefined() {
        // Check if shape is defined
        if (isUndefined()) {
            return false;
        }

        // return shape.stream().allMatch(i -> i != null);
        // Check if all dimensions are greater than -1
        return this.shape.stream().allMatch(i -> i > -1);
    }

    public boolean hasValues() {
        return !this.values.isEmpty();
    }

    public List<Number> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    /**
     * 
     * @return the value of the first dimension that is not a singleton (size 1). If all dimensions are singletons,
     *         returns the value 1
     */
    /*
    public Integer getFirstNonSingletonDim() {
    for (Integer dim : getDims()) {
        if (dim > 1) {
    	return dim;
        }
    }
    
    // No dim greater than 1 found, return 1
    return 1;
    }
     */

    /**
     * Returns true if the shape has two dimensions, and their values are greater than 0, and equal.
     * 
     * @param matrix
     *            - the matrix we will test
     * @return true if the matrix is square, false otherwise
     */
    public boolean isSquare() {

        // Has two dimensions?
        if (getNumDims() != 2) {
            return false;
        }

        // Are dimensions equal?
        if (getDim(0) != getDim(1)) {
            return false;
        }

        // Special case of undefined square matrix
        if (getDim(0) == TypeShape.UNDEFINED_SQUARE_VALUE) {
            return true;
        }

        // Are greater than 0?
        if (!(getDim(0) > 0)) {
            return false;
        }

        return true;
    }

    /**
     * Return the zero-based index of the first non-singleton dimension. If all dimensions have size 1,
     * 
     * <p>
     * TODO: This is the code that was originaly copied from MatrixUtils, check if it needs to be revised
     * 
     * @param matrix
     *            - the input matrix
     */
    public Integer getFirstNonSingletonDimension() {
        List<Integer> dims = getDims();

        // Return the index of the first dimension whose size is not 1
        for (int i = 0; i < dims.size(); i++) {
            if (!dims.get(i).equals(1)) {
                return i;
            }
        }
        /*
        for (Integer integer : dims) {
        if (!integer.equals(1)) {
        	return dims.indexOf(integer);
        	//return new Integer(dims.indexOf(integer));
        }
        }
         */

        // Return the first dimension if all dimensions as singleton
        // return new Integer(0);
        return 0;
    }

    public static TypeShape newScalarShape() {
        return TypeShape.SCALAR_SHAPE;
    }

    /**
     * Indicates whether the TypeShape is known to be a row.
     * 
     */
    public boolean isKnownRow() {
        return this.shape.size() == 2 &&
                this.shape.get(0) == 1;
    }

    /**
     * Indicates whether the TypeShape is known to be a column.
     */
    public boolean isKnownColumn() {
        if (getNumDims() < 0) {
            return false;
        }

        for (int i = 1; i < this.shape.size(); ++i) {
            if (this.shape.get(i) != 1) {
                return false;
            }
        }

        return true;
    }

    public boolean isKnown1D() {
        if (this.force1D) {
            return true;
        }

        return isKnownRow() || isKnownColumn();
    }

    /**
     * Indicates whether the TypeShape is known to not be empty, scalar, row or column.
     */
    public boolean isKnownMultiDimensional() {
        for (int dim : this.shape) {
            if (dim == 0) {
                // Known empty
                return false;
            }
            if (dim > 1) {
                return true;
            }
        }

        return false;
    }

    /**
     * Creates a new shape instance that can represent:
     * 
     * <li>Any shape representable by the current instance
     * <li>Any shape representable by typeShape
     * 
     * @return The combined shape
     */
    public TypeShape combineWith(TypeShape typeShape) {
        Preconditions.checkArgument(typeShape != null);

        if (isUndefined() || typeShape.isUndefined()) {
            return TypeShape.newUndefinedShape();
        }

        if (equals(typeShape)) {
            return this;
        }

        if (isScalar() && typeShape.isScalar()) {
            return TypeShape.newScalarShape();
        }

        if (isKnownRow() && typeShape.isKnownRow()) {
            return TypeShape.newRow();
        }

        if (isKnownColumn() && typeShape.isKnownColumn()) {
            return TypeShape.newColumn();
        }

        if (isKnown1D() && typeShape.isKnown1D()) {
            return TypeShape.new1D();
        }

        if (getRawNumDims() == typeShape.getRawNumDims() && getRawNumDims() > 0) {
            return TypeShape.newDimsShape(getRawNumDims());
        }

        return TypeShape.newUndefinedShape();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TypeShape) {
            return equals((TypeShape) obj);
        }

        return false;
    }

    public boolean equals(TypeShape obj) {
        if (obj == null) {
            return false;
        }

        return this.type == obj.type &&
                this.shape.equals(obj.shape) &&
                this.values.equals(obj.values);
    }

    @Override
    public int hashCode() {
        return this.type.hashCode() ^ this.shape.hashCode() ^ this.values.hashCode();
    }

    public boolean isNumElementsKnown() {
        // If we ever add the capability for separate numel tracking, change this function.
        return isFullyDefined();
    }
}
