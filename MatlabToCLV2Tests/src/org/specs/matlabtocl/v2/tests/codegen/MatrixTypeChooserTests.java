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

package org.specs.matlabtocl.v2.tests.codegen;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.matlabtocl.v2.codegen.MatrixTypeChooser;
import org.specs.matlabtocl.v2.types.kernel.AddressSpace;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;
import org.specs.matlabtocl.v2.types.kernel.RawBufferMatrixType;
import org.specs.matlabtocl.v2.types.kernel.SizedMatrixType;

public class MatrixTypeChooserTests {
    @Test
    public void testRaw() {
	MatrixTypeChooser typeChooser = new MatrixTypeChooser();
	MatrixType type = typeChooser.buildMatrixType("hello", AddressSpace.GLOBAL, CLNativeType.INT);

	Assert.assertEquals(RawBufferMatrixType.class, type.getClass());
	Assert.assertEquals(CLNativeType.INT, type.matrix().getElementType());
	Assert.assertEquals("global int*", type.code().getSimpleType());
    }

    @Test
    public void testRaw2() {
	MatrixTypeChooser typeChooser = new MatrixTypeChooser();
	typeChooser.requireNumel("otherName");
	MatrixType type = typeChooser.buildMatrixType("hello", AddressSpace.GLOBAL, CLNativeType.INT);

	Assert.assertEquals(RawBufferMatrixType.class, type.getClass());
	Assert.assertEquals(CLNativeType.INT, type.matrix().getElementType());
	Assert.assertEquals("global int*", type.code().getSimpleType());
    }

    @Test
    public void testWithNumel() {
	MatrixTypeChooser typeChooser = new MatrixTypeChooser();
	typeChooser.requireNumel("hello");
	SizedMatrixType type = (SizedMatrixType) typeChooser.buildMatrixType("hello", AddressSpace.GLOBAL,
		CLNativeType.INT);

	Assert.assertEquals(AddressSpace.GLOBAL, type.getAddressSpace());
	Assert.assertEquals(CLNativeType.INT, type.getElementType());
	Assert.assertTrue(type.containsNumel());
	Assert.assertEquals(0, type.containedDims());
    }

    @Test
    public void testWithTwoDims() {
	MatrixTypeChooser typeChooser = new MatrixTypeChooser();
	typeChooser.requireAtLeastShape("hello", 1);
	typeChooser.requireAtLeastShape("hello", 2);
	typeChooser.requireAtLeastShape("hello", 1);

	SizedMatrixType type = (SizedMatrixType) typeChooser.buildMatrixType("hello", AddressSpace.GLOBAL,
		CLNativeType.INT);

	Assert.assertEquals(AddressSpace.GLOBAL, type.getAddressSpace());
	Assert.assertEquals(CLNativeType.INT, type.getElementType());
	Assert.assertFalse(type.containsNumel());
	Assert.assertEquals(2, type.containedDims());
    }
}
