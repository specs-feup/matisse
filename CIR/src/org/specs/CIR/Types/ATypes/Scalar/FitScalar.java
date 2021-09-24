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

import java.math.BigDecimal;

import org.specs.CIR.Types.VariableType;

import pt.up.fe.specs.util.SpecsStrings;

public class FitScalar implements FittingRule {

    /**
     * Verifies if range of values of the source type is inside the range of values of the target type.
     */
    @Override
    public boolean fitsInto(VariableType sourceType, VariableType targetType) {
	ScalarType sourceNumeric = SpecsStrings.cast(sourceType, ScalarType.class);
	ScalarType targetNumeric = SpecsStrings.cast(targetType, ScalarType.class);

	BigDecimal sourceMin = sourceNumeric.scalar().getMinValue();
	// In cases such as 0.1 and 300, 0.1 fits into 0.1, but you lose the number if you convert 0.1 to int (0)
	// In case source is a literal, use the literal value
	/*
	if (sourceNumeric.scalar().isLiteral() && sourceNumeric.scalar().hasConstant()) {
	    sourceMin = new BigDecimal(sourceNumeric.scalar().getConstantString());
	}
	*/

	if (sourceMin.compareTo(targetNumeric.scalar().getMinValue()) < 0) {
	    return false;
	}

	BigDecimal sourceMax = sourceNumeric.scalar().getMaxValue();
	// In case source is a literal, use the literal value
	/*
	if (sourceNumeric.scalar().isLiteral() && sourceNumeric.scalar().hasConstant()) {
	    sourceMax = new BigDecimal(sourceNumeric.scalar().getConstantString());
	}
	*/

	if (sourceMax.compareTo(targetNumeric.scalar().getMaxValue()) > 0) {
	    return false;
	}

	return true;

    }

}
