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

package org.specs.CIRv2.Types;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.specs.CIR.Language.Types.CTypeSizes;
import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.Views.Conversion.ConversionUtils;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.StdInd.StdIntCategory;
import org.specs.CIRTypes.Types.StdInd.StdIntFactory;
import org.specs.CIRTypes.Types.StdInd.StdIntType;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.properties.SpecsProperty;

public class ConversionUtilsTest {

    // private static final NumericFactoryV2 TYPES = new NumericFactoryV2(CTypeSizes.DEFAULT_SIZES);
    private static final NumericFactory TYPES = new NumericFactory(CTypeSizes.DEFAULT_SIZES);

    @Test
    public void testIsAssignable() {

	VariableType doubleType = TYPES.newNumeric(CTypeV2.DOUBLE);
	VariableType floatType = TYPES.newNumeric(CTypeV2.FLOAT);
	VariableType intType = TYPES.newNumeric(CTypeV2.INT);

	StdIntType unsignedInt32 = StdIntType.newInstance(StdIntCategory.EXACT_WIDTH, 32, true);
	StdIntType unsignedInt16 = StdIntType.newInstance(StdIntCategory.EXACT_WIDTH, 16, true);
	StdIntType signedInt64 = StdIntType.newInstance(StdIntCategory.EXACT_WIDTH, 64, false);

	assertTrue(ConversionUtils.isAssignable(unsignedInt32, signedInt64));
	assertFalse(ConversionUtils.isAssignable(signedInt64, unsignedInt32));
	assertTrue(ConversionUtils.isAssignable(signedInt64, signedInt64));

	// Since the default size of int is 32 bits, it should return false
	// uint32 cannot hold all the values of intType and vice-versa, when the types have the same width
	assertFalse(ConversionUtils.isAssignable(intType, unsignedInt32));
	assertFalse(ConversionUtils.isAssignable(unsignedInt32, intType));
	assertTrue(ConversionUtils.isAssignable(unsignedInt16, intType));

	assertTrue(ConversionUtils.isAssignable(intType, floatType));
	assertFalse(ConversionUtils.isAssignable(floatType, intType));
	assertTrue(ConversionUtils.isAssignable(floatType, floatType));
	assertTrue(ConversionUtils.isAssignable(floatType, doubleType));
	assertFalse(ConversionUtils.isAssignable(doubleType, floatType));

    }

    @Test
    public void testSortScalars() {
	SpecsSystem.programStandardInit();
	SpecsProperty.ShowStackTrace.applyProperty("true");

	ScalarType doubleType = TYPES.newNumeric(CTypeV2.DOUBLE);
	ScalarType floatType = TYPES.newNumeric(CTypeV2.FLOAT);
	ScalarType intType = TYPES.newNumeric(CTypeV2.INT);

	StdIntType int64Type = StdIntFactory.newInt64();
	StdIntType int32Type = StdIntFactory.newInt32();
	StdIntType int8Type = StdIntFactory.newInt8();
	StdIntType int16Type = StdIntFactory.newInt16();

	List<ScalarType> types = SpecsFactory.newArrayList();
	types.add(doubleType);
	types.add(floatType);
	types.add(intType);
	types.add(int64Type);
	types.add(int32Type);
	types.add(int8Type);
	types.add(int16Type);

	Collections.sort(types);

	List<ScalarType> sortedTypes = SpecsFactory.newArrayList();
	sortedTypes.add(int8Type);
	sortedTypes.add(int16Type);
	sortedTypes.add(intType);
	sortedTypes.add(int32Type);
	sortedTypes.add(int64Type);
	sortedTypes.add(floatType);
	sortedTypes.add(doubleType);

	assertEquals(types, sortedTypes);

	// Test when types are not compatible
	// This changed, not it just informs the user
	/*
	boolean exceptionOccurred = false;
	try {
	    StdIntFactory.newUInt32().compareTo(int32Type);
	} catch (RuntimeException e) {
	    exceptionOccurred = true;
	}

	assertTrue(exceptionOccurred);
	*/
    }

}
