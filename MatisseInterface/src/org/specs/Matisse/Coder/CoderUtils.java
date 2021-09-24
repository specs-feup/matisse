/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.Matisse.Coder;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.Scalar;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRTypes.Types.Numeric.NumericTypeUtils;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public class CoderUtils {

    private static final Set<CTypeV2> CHAR_TYPES = EnumSet.of(CTypeV2.CHAR, CTypeV2.CHAR_SIGNED, CTypeV2.CHAR_UNSIGNED);
    private static final Set<Integer> VALID_BIT_SIZES = SpecsFactory.newHashSet(Arrays.asList(8, 16, 32, 64));

    /**
     * TODO: Replace with a cast when there is a type for the MatlabCode types?
     * 
     * @param type
     * @return
     */
    public static String getCoderType(VariableType type) {

	Scalar scalar = ScalarUtils.getScalar(type);

	// Check if float
	if (!scalar.isInteger()) {
	    // If up to 32 bits, return Single
	    if (scalar.getBits() <= 32) {
		return "real32_T";
	    }

	    // Otherwise, return Double
	    return "real_T";
	}

	// Check if char
	if (type instanceof NumericTypeV2) {
	    if (CHAR_TYPES.contains(NumericTypeUtils.cast(type).getCtype())) {
		return "char_T";
	    }
	}

	// Check for the bit size
	if (!VALID_BIT_SIZES.contains(scalar.getBits())) {
	    throw new RuntimeException("Type not supported:" + type);
	}

	StringBuilder builder = new StringBuilder();

	if (scalar.isUnsigned()) {
	    builder.append("u");
	}
	builder.append("int");
	builder.append(scalar.getBits());
	builder.append("_T");

	return builder.toString();
    }
}
