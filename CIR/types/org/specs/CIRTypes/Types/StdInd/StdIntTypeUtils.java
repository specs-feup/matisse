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

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;

import pt.up.fe.specs.util.SpecsStrings;

public class StdIntTypeUtils {

    public static final String N_BITS = "<N_BITS>";

    public static String getSimpleType(StdIntType type) {
	int bits = type.getnBits();
	boolean isUnsigned = type.isUnsigned();

	return type.getStdIntType().getType(bits, isUnsigned);
    }

    public static StdIntType cast(VariableType type) {
	return SpecsStrings.cast(type, StdIntType.class);
    }

    public static ScalarType getInteger(ScalarType valueType) {
        if (valueType.scalar().isInteger()) {
            return valueType;
        }
    
        // Create integer type with similar number of bits
        return StdIntType.newInstance(valueType.scalar().getBits(), valueType.scalar().isUnsigned());
    }

}
