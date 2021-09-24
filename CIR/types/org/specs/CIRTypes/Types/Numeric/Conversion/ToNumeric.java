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

package org.specs.CIRTypes.Types.Numeric.Conversion;

import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.Views.Conversion.ConversionRule;
import org.specs.CIRFunctions.Utilities.UtilityInstances;
import org.specs.CIRTypes.Types.Numeric.NumericTypeUtils;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;

import com.google.common.base.Preconditions;

/**
 * When target is also a numeric type, builds a cast to that type.
 * 
 * @author Joao Bispo
 * 
 */
// public class ToNumeric implements ConversionRule<NumericTypeV2> {
public class ToNumeric implements ConversionRule {

    /* (non-Javadoc)
     * @see org.specs.CIRv2.Types.Views.Conversion.ConversionRule#convert(org.specs.CIR.Tree.CToken, org.specs.CIR.Types.VariableType)
     */
    @Override
    // public CToken convert(CToken token, NumericTypeV2 targetType) {
    public CNode convert(CNode token, VariableType targetType) {

	// If void, just return it
	// if (token.getVariableType() instanceof VoidType) {
	// return token;
	// }

	Preconditions.checkArgument(token.getVariableType() instanceof ScalarType,
		"Casts can only be applied to scalar types, got a " + token.getVariableType()
			+ " instead, while attempting to cast to " + targetType + ":\n" + token);

	// Get type of token
	ScalarType sourceType = (ScalarType) token.getVariableType();

	// Get NumericType of target
	NumericTypeV2 targetNumeric = NumericTypeUtils.cast(targetType);

	// If source type fits into target type, and are both integer/floating, cast is not needed
	if (sourceType.scalar().isInteger() == targetNumeric.scalar().isInteger()) {

	    Optional<Boolean> fits = sourceType.scalar().fitsInto(targetNumeric);
	    if (fits.isPresent() && fits.get()) {
		return token;
	    }
	}

	// Build cast function
	FunctionInstance cast = UtilityInstances.newCastToScalar(sourceType, targetNumeric);

	// Return function call
	return FunctionInstanceUtils.getFunctionCall(cast, token);
    }
}
