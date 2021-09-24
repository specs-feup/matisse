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

package org.specs.CIR.Language.Types;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import org.specs.CIR.Utilities.BigDecimalFactory;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

public class CTypeSizes {

    public static final CTypeSizes DEFAULT_SIZES;
    static {
	DEFAULT_SIZES = new CTypeSizes();

	DEFAULT_SIZES.setChar(8);
	DEFAULT_SIZES.setShort(16);
	DEFAULT_SIZES.setInt(32);
	DEFAULT_SIZES.setLong(32);
	DEFAULT_SIZES.setLongLong(64);
	DEFAULT_SIZES.setFloat(32);
	DEFAULT_SIZES.setDouble(64);
	DEFAULT_SIZES.setLongDouble(80);

	Collections.unmodifiableMap(DEFAULT_SIZES.bitSizes);

	DEFAULT_SIZES.setMaxValue(CTypeV2.FLOAT, new BigDecimal("3.4e+38"));
	DEFAULT_SIZES.setMaxValue(CTypeV2.DOUBLE, new BigDecimal("1.8E+308"));
	DEFAULT_SIZES.setMaxValue(CTypeV2.DOUBLE_LONG, new BigDecimal("1.2E4932"));

	Collections.unmodifiableMap(DEFAULT_SIZES.maxValues);
    }

    private final Map<CTypeV2, Integer> bitSizes;
    private final Map<CTypeV2, BigDecimal> maxValues;

    /*
    private CTypeSizes(Map<CTypeV2, Integer> bitSizes) {
    this.bitSizes = bitSizes;
    }
    */

    private CTypeSizes() {
	this.bitSizes = SpecsFactory.newHashMap();
	this.maxValues = SpecsFactory.newHashMap();
    }

    private CTypeSizes(CTypeSizes sizes) {
	this.bitSizes = SpecsFactory.newHashMap(sizes.bitSizes);
	this.maxValues = SpecsFactory.newHashMap(sizes.maxValues);
    }

    /**
     * Creates a new instance with the following default values: <br>
     * - Char: 8 bits; <br>
     * - Short: 16 bits; <br>
     * - Int: 32 bits; <br>
     * - Long: 32 bits; <br>
     * - Long Long: 64 bits; <br>
     * - Float: 32 bits; <br>
     * - Double: 64 bits; <br>
     * - Long Double: 80 bits; <br>
     * 
     * @return a new instance with the default values
     */
    public static CTypeSizes newInstance() {
	// return new CTypeSizes(FactoryUtils.newHashMap(DEFAULT_SIZES.bitSizes));
	return new CTypeSizes(DEFAULT_SIZES);
    }

    public int getSize(CTypeV2 type) {
	Integer size = bitSizes.get(type);

	if (size == null) {
	    SpecsLogs.warn("Bit size for C type '" + type + "' not defined, returning size '"
		    + type.getAtLeastBits() + "'");

	    return type.getAtLeastBits();
	}

	return size;
    }

    public static BigDecimal getMaxValue(CTypeV2 type, int bits, Map<CTypeV2, BigDecimal> maxValues) {
	if (type.isInteger()) {
	    return BigDecimalFactory.instance().getIntegerMax(bits, type.isUnsigned());
	}

	return maxValues.get(type);
    }

    public BigDecimal getMaxValue(CTypeV2 type) {
	return getMaxValue(type, bitSizes.get(type), maxValues);
	/*
		if (type.isInteger()) {
		    int numBits = bitSizes.get(type);
		    return BigDecimalFactory.instance().getIntegerMax(numBits, type.isUnsigned());
		}

		return maxValues.get(type);
		*/
    }

    public BigDecimal getMinValue(CTypeV2 type) {
	return getMinValue(type, bitSizes.get(type), maxValues);
    }

    public static BigDecimal getMinValue(CTypeV2 type, int bits, Map<CTypeV2, BigDecimal> maxValues) {
	if (type.isInteger()) {
	    // int numBits = bitSizes.get(type);
	    return BigDecimalFactory.instance().getIntegerMin(bits, type.isUnsigned());
	}

	BigDecimal value = maxValues.get(type);

	if (value == null) {
	    System.out.println("VALUE IS NULL FOR:" + type);
	    return null;
	}

	return value.negate();
    }

    public void setMaxValue(CTypeV2 type, BigDecimal value) {
	maxValues.put(type, value);
    }

    public int getCharSize() {
	return bitSizes.get(CTypeV2.CHAR);
    }

    public void setChar(int nBits) {
	bitSizes.put(CTypeV2.CHAR, nBits);
	bitSizes.put(CTypeV2.CHAR_SIGNED, nBits);
	bitSizes.put(CTypeV2.CHAR_UNSIGNED, nBits);
    }

    public void setShort(int nBits) {
	bitSizes.put(CTypeV2.SHORT, nBits);
	bitSizes.put(CTypeV2.SHORT_UNSIGNED, nBits);
    }

    public void setInt(int nBits) {
	bitSizes.put(CTypeV2.INT, nBits);
	bitSizes.put(CTypeV2.INT_UNSIGNED, nBits);
    }

    public void setLong(int nBits) {
	bitSizes.put(CTypeV2.LONG, nBits);
	bitSizes.put(CTypeV2.LONG_UNSIGNED, nBits);
    }

    public void setLongLong(int nBits) {
	bitSizes.put(CTypeV2.LONG_LONG, nBits);
	bitSizes.put(CTypeV2.LONG_LONG_UNSIGNED, nBits);
    }

    public void setFloat(int nBits) {
	bitSizes.put(CTypeV2.FLOAT, nBits);
    }

    public void setDouble(int nBits) {
	bitSizes.put(CTypeV2.DOUBLE, nBits);
    }

    public void setLongDouble(int nBits) {
	bitSizes.put(CTypeV2.DOUBLE_LONG, nBits);
    }

    public void set(CTypeV2 type, int nBits) {
	bitSizes.put(type, nBits);
    }

}
