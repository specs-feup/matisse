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

package org.specs.CIRTypes.Types.StdInd;

import org.specs.CIR.Types.AVariableType;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.CNative.CNative;
import org.specs.CIR.Types.ATypes.CNative.CNativeType;
import org.specs.CIR.Types.ATypes.Scalar.Scalar;
import org.specs.CIR.Types.Views.Code.Code;
import org.specs.CIR.Types.Views.Conversion.Conversion;
import org.specs.CIR.Types.Views.Pointer.Reference;
import org.specs.CIRTypes.Types.StdInd.SubTypes.StdIntCNative;
import org.specs.CIRTypes.Types.StdInd.SubTypes.StdIntScalar;
import org.specs.CIRTypes.Types.StdInd.Views.StdIntCode;
import org.specs.CIRTypes.Types.StdInd.Views.StdIntConversion;
import org.specs.CIRTypes.Types.StdInd.Views.StdIntReference;

/**
 * A built-in type of C (int, double, etc...)
 * 
 * @author Joao Bispo
 * 
 */
// public class StdIntType extends AVariableType implements CodeType, ScalarType {
public final class StdIntType extends CNativeType {

    private final StdIntCategory type;
    private int nBits;
    private final boolean isUnsigned;
    private Number constant;
    private boolean isByReference;
    private boolean isLiteral;

    /*
    public static class Builder {
    private StdIntCategory type;
    private int nBits;
    private boolean isUnsigned;
    private Number constant = null;
    private boolean isPointer = false;
    
    public Builder(StdIntType type) {
        this(type.type, type.nBits, type.isUnsigned);
        this.constant = type.constant;
        this.isPointer = type.isPointer;
    }
    
    public Builder(StdIntCategory type, int nBits, boolean isUnsigned) {
        this.type = type;
        this.nBits = nBits;
        this.isUnsigned = isUnsigned;
    }
    
    public Builder constant(Number constant) {
        this.constant = constant;
        return this;
    }
    
    public Builder isPointer(boolean isPointer) {
        this.isPointer = isPointer;
        return this;
    }
    
    public StdIntType build() {
        return new StdIntType(type, nBits, isUnsigned, constant, isPointer);
    }
    
    }
    */
    private StdIntType(StdIntCategory type, int nBits, boolean isUnsigned, Number constant, boolean isPointer,
	    boolean isLiteral) {
	// private StdIntType(StdIntCategory type, int nBits, boolean isUnsigned, Number constant, boolean isPointer) {
	this.type = type;
	this.nBits = nBits;
	this.isUnsigned = isUnsigned;
	this.constant = constant;
	this.isByReference = isPointer;
	this.isLiteral = isLiteral;
    }

    // private StdIntType(StdIntType type) {
    // this(type.type, type.nBits, type.isUnsigned, type.constant, type.isPointer, type.isLiteral);
    // }

    private StdIntType(StdIntCategory type, int nBits, boolean isUnsigned, Number constant, boolean isPointer) {
	this(type, nBits, isUnsigned, constant, isPointer, false);
    }

    @Override
    protected AVariableType copyPrivate() {
	// All fields are immutable (unless AtomicInteger and such is used as Number)
	return new StdIntType(this.type, this.nBits, this.isUnsigned, this.constant, this.isByReference);
    }

    public static StdIntType newInstance(StdIntCategory type, int nBits, boolean isUnsigned) {
	return new StdIntType(type, nBits, isUnsigned, null, false);
    }

    /**
     * Helper method for types with exact width.
     * 
     * @param nBits
     * @param isUnsigned
     * @return
     */
    public static StdIntType newInstance(int nBits, boolean isUnsigned) {
	return new StdIntType(StdIntCategory.EXACT_WIDTH, nBits, isUnsigned, null, false);
    }

    /**
     * Returns a copy of the given StdIntType, but with the given pointer status.
     * 
     * @param isPointer
     * @return
     */
    public StdIntType setPointer(boolean isPointer) {
	StdIntType newType = (StdIntType) this.copy();
	newType.setPointerPrivate(isPointer);

	return newType;
    }

    private void setPointerPrivate(boolean isPointer) {
	this.isByReference = isPointer;
    }

    public StdIntType setConstant(Number constant) {
	// StdIntType newType = new StdIntType(this);
	StdIntType newType = (StdIntType) this.copy();
	newType.setConstantPrivate(constant);

	return newType;
    }

    private void setConstantPrivate(Number constant) {
	this.constant = constant;
    }

    public StdIntType setBits(int bits) {
	StdIntType newType = (StdIntType) this.copy();
	newType.setBitsPrivate(this.nBits);

	return newType;
    }

    private void setBitsPrivate(int bits) {
	this.nBits = bits;
    }

    public StdIntType setLiteral(boolean isLiteral) {
	StdIntType newType = (StdIntType) this.copy();
	newType.setLiteralPrivate(isLiteral);

	return newType;
    }

    public Number getConstant() {
	return this.constant;
    }

    public StdIntCategory getStdIntType() {
	return this.type;
    }

    public boolean isByReference() {
	return this.isByReference;
    }

    public int getnBits() {
	return this.nBits;
    }

    public boolean isUnsigned() {
	return this.isUnsigned;
    }

    public boolean isLiteral() {
	return this.isLiteral;
    }

    private void setLiteralPrivate(boolean isLiteral) {
	this.isLiteral = isLiteral;
    }

    @Override
    public String getSmallId() {

	if (this.type != StdIntCategory.EXACT_WIDTH) {
	    throw new UnsupportedOperationException("Not implemented yet");
	}

	StringBuilder builder = new StringBuilder();

	if (this.isByReference) {
	    builder.append("p");
	}

	if (this.isUnsigned) {
	    builder.append("u");
	}

	builder.append("i");
	builder.append(this.nBits);

	return builder.toString();
    }

    /**
     * Currently, it is considered long when the number of bits are higher than 32.
     * 
     * 
     * @return
     */
    public boolean isLong() {
	return getnBits() > 32;
    }

    @Override
    public Code code() {
	return new StdIntCode(this);
    }

    @Override
    public Reference pointer() {
	return new StdIntReference(this);
    }

    @Override
    public Scalar scalar() {
	return new StdIntScalar(this);
    }

    @Override
    public Conversion conversion() {
	return new StdIntConversion(this);
    }

    @Override
    public CNative cnative() {
	return new StdIntCNative(this);
    }

    @Override
    public String toString() {
	/*
		String pointer = "";
		if (isPointer()) {
		    pointer = "*";
		}
	*/
	StringBuilder builder = new StringBuilder();

	if (this.isUnsigned) {
	    builder.append("u");
	}

	builder.append("int");
	builder.append(this.nBits);

	if (isByReference()) {
	    builder.append("*");
	}

	if (getConstant() != null) {
	    builder.append(" (").append(getConstant().toString()).append(")");
	}

	return builder.toString();

	// return "int" + nBits + pointer;
    }

    @Override
    public boolean strictEquals(VariableType type) {
	if (type instanceof StdIntType) {
	    return strictEquals((StdIntType) type);
	}

	return false;
    }

    public boolean strictEquals(StdIntType type) {
	if (type == null) {
	    return false;
	}

	if (this.type != type.type) {
	    return false;
	}

	if (this.nBits != type.nBits) {
	    return false;
	}

	if (this.isUnsigned != type.isUnsigned) {
	    return false;
	}

	if (this.constant == null) {
	    if (type.constant != null) {
		return false;
	    }
	} else if (!this.constant.equals(type.constant)) {
	    return false;
	}

	if (this.isByReference != type.isByReference) {
	    return false;
	}

	return this.isLiteral == type.isLiteral;
    }

}
