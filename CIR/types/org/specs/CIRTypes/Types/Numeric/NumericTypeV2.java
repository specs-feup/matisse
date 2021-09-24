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

package org.specs.CIRTypes.Types.Numeric;

import java.math.BigDecimal;

import org.specs.CIR.Language.Types.CTypeSizes;
import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Types.AVariableType;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.CNative.CNative;
import org.specs.CIR.Types.ATypes.CNative.CNativeType;
import org.specs.CIR.Types.ATypes.Scalar.Scalar;
import org.specs.CIR.Types.Views.Code.Code;
import org.specs.CIR.Types.Views.Conversion.Conversion;
import org.specs.CIR.Types.Views.Pointer.Reference;
import org.specs.CIRTypes.Types.Numeric.SubTypes.NumericCNative;
import org.specs.CIRTypes.Types.Numeric.SubTypes.NumericScalar;
import org.specs.CIRTypes.Types.Numeric.Views.NumericCode;
import org.specs.CIRTypes.Types.Numeric.Views.NumericConversion;
import org.specs.CIRTypes.Types.Numeric.Views.NumericReference;

/**
 * A built-in type of C (int, double, etc...)
 * 
 * @author Joao Bispo
 * 
 */
// public class NumericTypeV2 extends CNativeType<NumericTypeV2> {
public final class NumericTypeV2 extends CNativeType {

    private CTypeV2 type;
    private int bits;
    private Number constant;
    private boolean isReference;
    private final BigDecimal maxValue;
    private final BigDecimal minValue;
    private boolean isLiteral;

    private NumericTypeV2(CTypeV2 type, int bits, Number constant, boolean isPointer, BigDecimal maxValue,
	    BigDecimal minValue, boolean isLiteral) {

	this.type = type;
	this.bits = bits;
	this.constant = constant;
	this.isReference = isPointer;
	this.maxValue = maxValue;
	this.minValue = minValue;
	this.isLiteral = isLiteral;

    }

    public NumericTypeV2(CTypeV2 type, int bits, Number constant, boolean isPointer, BigDecimal maxValue,
	    BigDecimal minValue) {
	this(type, bits, constant, isPointer, maxValue, minValue, false);

    }

    public static NumericTypeV2 newInstance(CTypeV2 type, int bits, Number constant, boolean isPointer) {
	CTypeSizes sizes = CTypeSizes.newInstance();
	sizes.set(type, bits);

	return new NumericTypeV2(type, bits, constant, isPointer, sizes.getMaxValue(type), sizes.getMinValue(type));
    }

    @Override
    protected AVariableType copyPrivate() {
	// All fields are immutable (unless AtomicInteger and such is used as Number)
	return new NumericTypeV2(this.type, this.bits, this.constant, this.isReference, this.maxValue, this.minValue,
		this.isLiteral);
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Types.AVariableType#copy()
     */
    @Override
    public NumericTypeV2 copy() {
	return (NumericTypeV2) super.copy();
    }

    public NumericTypeV2 setLiteral(boolean isLiteral) {
	NumericTypeV2 newType = this.copy();
	newType.setLiteralPrivate(isLiteral);

	return newType;
    }

    public boolean isLiteral() {
	return this.isLiteral;
    }

    private void setLiteralPrivate(boolean isLiteral) {
	this.isLiteral = isLiteral;
    }

    private void setPointerPrivate(boolean isPointer) {
	this.isReference = isPointer;
    }

    /**
     * Builds a NumericType without constant information and that is not a pointer (e.g., int).
     * 
     * @param type
     * @param nBits
     * @return
     */
    public static NumericTypeV2 newInstance(CTypeV2 type, int nBits) {
	return newInstance(type, nBits, null, false);
    }

    /**
     * Returns a copy of the NumericType, but with the given pointer status.
     * 
     * @param type
     * @return
     */
    public NumericTypeV2 setPointer(boolean isPointer) {
	NumericTypeV2 newType = this.copy();
	newType.setPointerPrivate(isPointer);

	return newType;
    }

    public NumericTypeV2 setConstant(Number constant) {
	// return new NumericTypeV2()
	NumericTypeV2 newType = this.copy();
	// NumericTypeV2 newType = new NumericTypeV2(this);
	newType.setConstantPrivate(constant);

	return newType;
    }

    private void setConstantPrivate(Number constant) {
	this.constant = constant;
    }

    public NumericTypeV2 setCType(CTypeV2 ctype) {

	NumericTypeV2 newType = this.copy();
	newType.setCTypePrivate(ctype);

	return newType;
    }

    private void setCTypePrivate(CTypeV2 type) {
	this.type = type;
    }

    public NumericTypeV2 setBits(int bits) {
	NumericTypeV2 newType = this.copy();
	newType.setBitsPrivate(bits);

	return newType;
    }

    public void setBitsPrivate(int bits) {
	this.bits = bits;
    }

    public Number getConstant() {
	return this.constant;
    }

    public CTypeV2 getCtype() {
	return this.type;
    }

    public boolean isByReference() {
	return this.isReference;
    }

    public int getBits() {
	return this.bits;
    }

    public BigDecimal getMaxValue() {
	return this.maxValue;
    }

    public BigDecimal getMinValue() {
	return this.minValue;
    }

    @Override
    public String getSmallId() {

	// If type is not pointer, just return small id
	if (!isByReference()) {
	    return getCtype().getSmallId();
	}

	// Is pointer, append 'p'
	StringBuilder builder = new StringBuilder();

	builder.append("p");
	builder.append(getCtype().getSmallId());

	return builder.toString();
    }

    @Override
    public Code code() {
	// return new NumericCode(ctype, isPointer);
	return new NumericCode(this);
    }

    @Override
    public Reference pointer() {
	return new NumericReference(this);
    }

    @Override
    public Scalar scalar() {
	return new NumericScalar(this);
    }

    @Override
    public Conversion conversion() {
	return new NumericConversion(this);
    }

    @Override
    public CNative cnative() {
	return new NumericCNative(this);
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();

	builder.append(this.type.toString());
	if (isByReference()) {
	    builder.append("*");
	}

	if (this.constant != null) {
	    builder.append(" (");
	    builder.append(this.constant);

	    if (this.isLiteral) {
		builder.append(" - literal");
	    }
	    builder.append(")");
	}

	if (isWeakType()) {
	    builder.append(" weak");
	}

	return builder.toString();
    }

    @Override
    public boolean strictEquals(VariableType type) {
	if (type instanceof NumericTypeV2) {
	    return strictEquals((NumericTypeV2) type);
	}

	return false;
    }

    public boolean strictEquals(NumericTypeV2 type) {
	if (type == null) {
	    return false;
	}

	if (this.type != type.type) {
	    return false;
	}

	if (this.bits != type.bits) {
	    return false;
	}

	if ((this.constant == null) != (type.constant == null)) {
	    return false;
	}

	if (this.constant != null && !this.constant.equals(type.constant)) {
	    return false;
	}

	if (this.isReference != type.isReference) {
	    return false;
	}

	if ((this.maxValue == null) != (type.maxValue == null)) {
	    return false;
	}

	if (this.maxValue != null && !this.maxValue.equals(type.maxValue)) {
	    return false;
	}

	if ((this.minValue == null) != (type.minValue == null)) {
	    return false;
	}

	if (this.minValue != null && !this.minValue.equals(type.minValue)) {
	    return false;
	}

	return this.isLiteral == type.isLiteral;
    }
}
