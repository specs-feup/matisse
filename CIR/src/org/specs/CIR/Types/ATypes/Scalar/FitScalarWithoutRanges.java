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

package org.specs.CIR.Types.ATypes.Scalar;

import org.specs.CIR.Types.VariableType;

import pt.up.fe.specs.util.SpecsStrings;

public class FitScalarWithoutRanges implements FittingRule {

    /**
     * Verifies if the number of bits of sourceType is less or equal than the number of bits of targetType
     * 
     * TODO: Should it do a more complicated check with ranges?
     */
    @Override
    public boolean fitsInto(VariableType sourceType, VariableType targetType) {
	ScalarType sourceNumeric = SpecsStrings.cast(sourceType, ScalarType.class);
	ScalarType targetNumeric = SpecsStrings.cast(targetType, ScalarType.class);

	// If source is not integer, it will not fit in an integer type
	if (!sourceNumeric.scalar().isInteger() && targetNumeric.scalar().isInteger()) {
	    return false;
	}

	if (ScalarUtils.areInteger(sourceNumeric, targetNumeric)) {
	    return testIntegers(sourceNumeric, targetNumeric);
	}

	// Remaining case: both are not integers, or source is integer and target is float
	return sourceNumeric.scalar().getBits() <= targetNumeric.scalar().getBits();

    }

    private static boolean testIntegers(ScalarType sourceNumeric, ScalarType targetNumeric) {
	// If source is signed and target is unsigned, return false
	if (!sourceNumeric.scalar().isUnsigned() && targetNumeric.scalar().isUnsigned()) {
	    return false;
	}

	// If source is unsigned and target is signed, check if target has at least one more bit than unsigned
	if (sourceNumeric.scalar().isUnsigned() && !targetNumeric.scalar().isUnsigned()) {
	    return sourceNumeric.scalar().getBits() < targetNumeric.scalar().getBits();
	}

	// Check bits
	return sourceNumeric.scalar().getBits() <= targetNumeric.scalar().getBits();
    }
}
