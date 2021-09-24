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

package org.specs.matisselib.types;

import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.StdInd.StdIntType;

public enum MatlabTypeGroup {
    DOUBLE("d", "double", null),
    SINGLE("s", "float", null),
    INT8("i8", "int8_t", SystemInclude.Stdint),
    UINT8("u8", "uint8_t", SystemInclude.Stdint),
    INT16("i16", "int16_t", SystemInclude.Stdint),
    UINT16("u16", "uint16_t", SystemInclude.Stdint),
    INT32("i32", "int32_t", SystemInclude.Stdint),
    UINT32("u32", "uint32_t", SystemInclude.Stdint),
    INT64("i64", "int64_t", SystemInclude.Stdint),
    UINT64("u64", "uint64_t", SystemInclude.Stdint),
    CHAR("c", "uint16_t", SystemInclude.Stdint),
    LOGICAL("l", "int", null);

    private final String smallId;
    private final String code;
    private final SystemInclude include;

    private MatlabTypeGroup(String smallId, String code, SystemInclude include) {

	this.smallId = smallId;
	this.code = code;
	this.include = include;
    }

    public String getSmallId() {
	return this.smallId;
    }

    public String getCode() {
	return this.code;
    }

    public SystemInclude getInclude() {
	return this.include;
    }

    public ScalarType getUnderlyingCType(NumericFactory numericFactory) {
	switch (this) {
	case DOUBLE:
	    return numericFactory.newDouble();
	case SINGLE:
	    return numericFactory.newFloat();
	case LOGICAL:
	    return numericFactory.newInt();
	case CHAR:
	    return StdIntType.newInstance(16, true);
	default:
	    boolean isUnsigned = getSmallId().charAt(0) == 'u';
	    int nBits = Integer.parseInt(getSmallId().replace("^[ui](\\d+)_t$", "$1"));

	    return StdIntType.newInstance(nBits, isUnsigned);
	}
    }

    public boolean isInteger() {
	switch (this) {
	case DOUBLE:
	case SINGLE:
	case LOGICAL:
	case CHAR:
	    return false;
	default:
	    return true;
	}
    }

}
