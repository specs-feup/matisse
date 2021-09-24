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

import java.util.Map;

import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Types.VariableType;

import com.google.common.collect.Maps;

import pt.up.fe.specs.util.SpecsStrings;

public class NumericTypeUtils {

    private static final Map<CTypeV2, CTypeV2> realToIntMap;
    static {
	realToIntMap = Maps.newEnumMap(CTypeV2.class);

	realToIntMap.put(CTypeV2.FLOAT, CTypeV2.INT);
	realToIntMap.put(CTypeV2.DOUBLE, CTypeV2.LONG);
	realToIntMap.put(CTypeV2.DOUBLE_LONG, CTypeV2.LONG_LONG);
    }

    /*
    private final static Map<CDataType, NumericTypeV2> CACHED_NUMERICS;
    static {
    CACHED_NUMERICS = FactoryUtils.newEnumMap(CDataType.class);

    for (CDataType type : CDataType.values()) {
        CACHED_NUMERICS.put(type, new NumericTypeV2(type, null, false));
    }

    }

    static NumericTypeV2 getCachedNumeric(CDataType type) {
    return CACHED_NUMERICS.get(type);
    }
    */

    public static NumericTypeV2 cast(VariableType type) {
	return SpecsStrings.cast(type, NumericTypeV2.class);
    }

    public static NumericTypeV2 toInteger(NumericTypeV2 type) {
	if (type.scalar().isInteger()) {
	    return type;
	}

	// Get corresponding integer type
	CTypeV2 integerCType = realToIntMap.get(type.getCtype());

	// Create new NumericType
	return type.setCType(integerCType);
    }
}
