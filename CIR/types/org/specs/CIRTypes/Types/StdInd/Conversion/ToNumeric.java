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

package org.specs.CIRTypes.Types.StdInd.Conversion;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.Scalar;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Conversion.ConversionRule;
import org.specs.CIRFunctions.Utilities.UtilityInstances;
import org.specs.CIRTypes.Types.Numeric.NumericTypeUtils;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;

/**
 * When target is an StdInt, build a cast to that type.
 * 
 * @author Joao Bispo
 * 
 */
public class ToNumeric implements ConversionRule {

    /* (non-Javadoc)
     * @see org.specs.CIRv2.Types.Views.Conversion.ConversionRule#convert(org.specs.CIR.Tree.CToken, org.specs.CIR.Types.VariableType)
     */
    @Override
    public CNode convert(CNode token, VariableType targetType) {

	// Get type of token
	VariableType sourceType = token.getVariableType();

	// Check if cast is not needed
	Scalar sourceScalar = ScalarUtils.getScalar(sourceType);
	Scalar targetScalar = ScalarUtils.getScalar(targetType);

	// If signedness is the same
	if (sourceScalar.isUnsigned() == targetScalar.isUnsigned()) {
	    // If target type has the same or more bits
	    if (sourceScalar.getBits() >= targetScalar.getBits()) {
		// No cast needed
		return token;
	    }
	}

	// Get NumericType of target
	NumericTypeV2 targetNumeric = NumericTypeUtils.cast(targetType);

	// Build cast function
	FunctionInstance cast = UtilityInstances.newCastToScalar(sourceType, targetNumeric);

	// Return function call
	return FunctionInstanceUtils.getFunctionCall(cast, token);
    }

}
