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

package org.specs.CIRBase.Types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.specs.CIR.CirTestUtils;
import org.specs.CIR.Types.TypeShape;

public class MatrixShapeTest {

    @Test
    public void test() {
	// Undefined shape
	TypeShape undefinedShape = TypeShape.newUndefinedShape();

	assertEquals(-1, undefinedShape.getNumDims());
	CirTestUtils.testException(() -> undefinedShape.getNumElements());
	assertFalse(undefinedShape.isKnownEmpty());
	assertFalse(undefinedShape.isFullyDefined());
	assertTrue(undefinedShape.isUndefined());
	assertFalse(undefinedShape.isSquare());

	// Empty shape
	TypeShape emptyShape = TypeShape.newInstance(3, 0, 2);

	assertEquals(3, emptyShape.getNumDims());
	assertEquals(0, emptyShape.getNumElements().intValue());
	assertTrue(emptyShape.isKnownEmpty());
	assertTrue(emptyShape.isFullyDefined());
	assertFalse(emptyShape.isUndefined());
	assertFalse(emptyShape.isSquare());

	// Shape with undefined dimensions
	TypeShape undefinedDimShape = TypeShape.newInstance(1, -1);

	assertEquals(1, undefinedDimShape.getNumDims());
	CirTestUtils.testException(() -> undefinedDimShape.getNumElements());
	assertFalse(undefinedDimShape.isKnownEmpty());
	assertFalse(undefinedDimShape.isFullyDefined());
	assertFalse(undefinedDimShape.isUndefined());
	assertFalse(undefinedDimShape.isSquare());

	// Scalar
	TypeShape scalarShape = TypeShape.newInstance(1, 1);

	assertEquals(0, scalarShape.getNumDims());
	assertEquals(1, scalarShape.getNumElements().intValue());
	assertFalse(scalarShape.isKnownEmpty());
	assertTrue(scalarShape.isFullyDefined());
	assertFalse(scalarShape.isUndefined());
	assertFalse(scalarShape.isSquare());

	// Row
	TypeShape rowShape = TypeShape.newInstance(1, 5);

	assertEquals(1, rowShape.getNumDims());
	assertEquals(5, rowShape.getNumElements().intValue());
	assertFalse(rowShape.isKnownEmpty());
	assertTrue(rowShape.isFullyDefined());
	assertFalse(rowShape.isUndefined());
	assertFalse(rowShape.isSquare());

	// Column
	TypeShape colShape = TypeShape.newInstance(1, 5);

	assertEquals(1, colShape.getNumDims());
	assertEquals(5, colShape.getNumElements().intValue());
	assertFalse(colShape.isKnownEmpty());
	assertTrue(colShape.isFullyDefined());
	assertFalse(colShape.isUndefined());
	assertFalse(colShape.isSquare());

	// 2D Matrix
	TypeShape matrixShape = TypeShape.newInstance(3, 5);

	assertEquals(2, matrixShape.getNumDims());
	assertEquals(15, matrixShape.getNumElements().intValue());
	assertFalse(matrixShape.isKnownEmpty());
	assertTrue(matrixShape.isFullyDefined());
	assertFalse(matrixShape.isUndefined());
	assertFalse(matrixShape.isSquare());

	// 2D Square Matrix
	TypeShape squareMatrixShape = TypeShape.newInstance(7, 7);

	assertTrue(squareMatrixShape.isSquare());

	// 2D Square Undefined Matrix
	TypeShape squareUndefinedMatrixShape = TypeShape.newUndefinedSquare();
	assertTrue(squareUndefinedMatrixShape.isSquare());
    }
}
