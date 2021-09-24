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

package org.specs.matlabtocl.v2.types.kernel;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.Types.CNumberParser;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.Scalar;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.Views.Code.Code;
import org.specs.CIR.Types.Views.Conversion.Conversion;
import org.specs.CIR.Types.Views.Pointer.Reference;

public final class CLNativeType extends ScalarType implements CNumberParser {
	public static final CLNativeType BOOL = new CLNativeType("bool", "u1", null, true, true, 1, false);

	public static final CLNativeType UCHAR = new CLNativeType("uchar", "u8", null, true, true, 8, false);
	public static final CLNativeType CHAR = new CLNativeType("char", "s8", null, true, false, 8, false);
	public static final CLNativeType USHORT = new CLNativeType("ushort", "u16", null, true, true, 16, false);
	public static final CLNativeType SHORT = new CLNativeType("short", "s16", null, true, false, 16, false);
	public static final CLNativeType UINT = new CLNativeType("uint", "u32", "U", true, true, 32, false);
	public static final CLNativeType INT = new CLNativeType("int", "s32", "", true, false, 32, false);
	public static final CLNativeType ULONG = new CLNativeType("ulong", "u64", "UL", true, true, 64, false);
	public static final CLNativeType LONG = new CLNativeType("long", "s64", "L", true, false, 64, false);
	public static final CLNativeType SIZE_T = new CLNativeType("size_t", "sn", null, true, true, null, false);
	public static final CLNativeType HALF = new CLNativeType("half", "f16", "h", false, false, 16, false);
	public static final CLNativeType FLOAT = new CLNativeType("float", "f32", "f", false, false, 32, false);
	public static final CLNativeType DOUBLE = new CLNativeType("double", "f64", null, false, false, 64, false);

	private final String typeCode;
	private final String smallId;
	private final String suffix;
	private final boolean isInteger;
	private final boolean isUnsigned;
	private final boolean isReference;
	private final Optional<Integer> nbits;
	private Number constant;

	private CLNativeType(String typeCode,
			String smallId,
			String suffix,
			boolean isInteger,
			boolean isUnsigned,
			Integer nbits,
			boolean isReference) {

		this.typeCode = typeCode;
		this.suffix = suffix;
		this.smallId = smallId;
		this.isInteger = isInteger;
		this.isUnsigned = isUnsigned;
		this.nbits = Optional.ofNullable(nbits);
		this.isReference = isReference;
		this.constant = null;
	}

	@Override
	public Scalar scalar() {
		return new CLNativeScalar(this);
	}

	public boolean isInteger() {
		return this.isInteger;
	}

	public boolean isUnsigned() {
		return this.isUnsigned;
	}

	public String getTypeCode() {
		return this.typeCode;
	}

	public Optional<String> getSuffix() {
		return Optional.ofNullable(suffix);
	}

	@Override
	public String toString() {
		return this.typeCode + "(" + constant + ")";
	}

	@Override
	public Code code() {
		return new CLNativeTypeCode(this);
	}

	@Override
	public Conversion conversion() {
		return new CLNativeConversion(this);
	}

	@Override
	public Reference pointer() {
		return new Reference() {

			@Override
			public boolean supportsReference() {
				return true;
			}

			@Override
			public boolean isByReference() {
				return CLNativeType.this.isReference;
			}

			@Override
			public VariableType getType(boolean isByReference) {
				if (isByReference == this.isByReference()) {
					return CLNativeType.this;
				}

				return new CLNativeType(typeCode,
						smallId,
						suffix,
						isInteger, isUnsigned,
						nbits.orElse(null),
						isByReference);
			}
		};
	}

	public Optional<Integer> getBits() {
		return this.nbits;
	}

	@Override
	public String getSmallId() {
		return this.smallId;
	}

	@Override
	protected CLNativeType copyPrivate() {
		CLNativeType copiedType = new CLNativeType(typeCode, smallId, suffix,
				isInteger, isUnsigned,
				nbits.orElse(null),
				isReference);
		copiedType.constant = constant;

		return copiedType;
	}

	@Override
	public boolean strictEquals(VariableType type) {
		if (!equals(type)) {
			return false;
		}

		CLNativeType otherType = (CLNativeType) type;
		if (constant == null) {
			return otherType.constant == null;
		}

		return constant.equals(otherType.constant);
	}

	public Number getConstant() {
		return constant;
	}

	public CLNativeType setConstant(Number constant) {
		CLNativeType newType = copyPrivate();
		newType.constant = constant;
		return newType;
	}

	CLNativeType setConstant(String constant) {
		if (constant == null) {
			return setConstant((Number) null);
		}

		return setConstant(Double.valueOf(constant));
	}

	public static List<CLNativeType> values() {
		return Arrays.asList(CLNativeType.BOOL,
				CLNativeType.UCHAR, CLNativeType.CHAR,
				CLNativeType.USHORT, CLNativeType.SHORT,
				CLNativeType.UINT, CLNativeType.INT,
				CLNativeType.ULONG, CLNativeType.LONG,
				CLNativeType.SIZE_T,
				CLNativeType.HALF, CLNativeType.FLOAT, CLNativeType.DOUBLE);
	}

	@Override
	public String parseNumber(String number) {
		if (nbits.equals(Optional.of(1))) {
			return number.matches("^0(\\.0*)??$") ? "false" : "true";
		}

		if (suffix != null) {
			if (isInteger()) {
				number = number.replaceAll("\\.[0-9]*$", "");
			} else if (!number.contains(".")) {
				number += ".";
			}
			return number + suffix;
		}

		return "((" + typeCode + ")" + number + ")";
	}
}
