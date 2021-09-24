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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Represents a type in C.
 * 
 * @author Joao Bispo
 * 
 */
public enum CTypeV2 {

    CHAR(CDataType.CHAR, 8, "c"),
    CHAR_SIGNED(CDataType.CHAR, 8, "sc", COptionalType.SIGNED),
    CHAR_UNSIGNED(CDataType.CHAR, 8, "uc", COptionalType.UNSIGNED),
    SHORT(CDataType.INT, 16, "s", COptionalType.SHORT),
    SHORT_UNSIGNED(CDataType.INT, 16, "us", COptionalType.UNSIGNED, COptionalType.SHORT),
    INT(CDataType.INT, 16, "i"),
    INT_UNSIGNED(CDataType.INT, 16, "ui", COptionalType.UNSIGNED),
    LONG(CDataType.INT, 32, "l", COptionalType.LONG),
    LONG_UNSIGNED(CDataType.INT, 32, "ul", COptionalType.UNSIGNED, COptionalType.LONG),
    LONG_LONG(CDataType.INT, 64, "ll", COptionalType.LONG, COptionalType.LONG),
    LONG_LONG_UNSIGNED(CDataType.INT, 64, "ull", COptionalType.UNSIGNED, COptionalType.LONG, COptionalType.LONG),
    FLOAT(CDataType.FLOAT, 32, "f"),
    DOUBLE(CDataType.DOUBLE, 64, "d"),
    DOUBLE_LONG(CDataType.DOUBLE, 64, "dl", COptionalType.LONG);

    private final CDataType baseType;
    private final int atLeastBits;
    private final String smallId;
    private final List<COptionalType> modifiers;

    private final static EnumSet<CTypeV2> FLOAT_SET = EnumSet.of(FLOAT, DOUBLE, DOUBLE_LONG);

    private final static EnumSet<CTypeV2> UNSIGNED_SET = EnumSet.of(CHAR_UNSIGNED, SHORT_UNSIGNED, INT_UNSIGNED,
	    LONG_UNSIGNED, LONG_LONG_UNSIGNED);

    private final static EnumSet<CTypeV2> LONG_SET = EnumSet.of(LONG, LONG_UNSIGNED, LONG_LONG, LONG_LONG_UNSIGNED,
	    DOUBLE_LONG);

    private CTypeV2(CDataType baseType, int atLeastBits, String smallId, COptionalType... modifiers) {
	this.baseType = baseType;
	this.atLeastBits = atLeastBits;
	this.smallId = smallId;
	this.modifiers = Arrays.asList(modifiers);
    }

    public int getAtLeastBits() {
	return atLeastBits;
    }

    public CDataType getBaseType() {
	return baseType;
    }

    public List<COptionalType> getModifiers() {
	return modifiers;
    }

    public boolean isUnsigned() {
	return UNSIGNED_SET.contains(this);
    }

    public boolean isLong() {
	return LONG_SET.contains(this);
    }

    public boolean isInteger() {
	if (FLOAT_SET.contains(this)) {
	    return false;
	}

	return true;
    }

    /**
     * Prefixes the modifiers to the base type declaration.
     * 
     * @return
     */
    public String getDeclaration() {
	if (modifiers.isEmpty()) {
	    return baseType.getDeclaration();
	}

	StringBuilder builder = new StringBuilder();

	for (COptionalType modifier : modifiers) {
	    builder.append(modifier.getDeclarationName());
	    builder.append(" ");
	}

	builder.append(baseType.getDeclaration());

	return builder.toString();
    }

    public String getSmallId() {
	return smallId;
    }

}
