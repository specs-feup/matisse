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

import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.Views.Conversion.ConversionRule;
import org.specs.CIRFunctions.Utilities.UtilityInstances;
import org.specs.CIRTypes.Types.StdInd.StdIntType;
import org.specs.CIRTypes.Types.StdInd.StdIntTypeUtils;

import com.google.common.base.Preconditions;

/**
 * When target is also a stdint, builds a cast to that type.
 * 
 * @author Joao Bispo
 * 
 */
public class ToStdInt implements ConversionRule {

    /* (non-Javadoc)
     * @see org.specs.CIRv2.Types.Views.Conversion.ConversionRule#convert(org.specs.CIR.Tree.CToken, org.specs.CIR.Types.VariableType)
     */
    @Override
    public CNode convert(CNode token, VariableType targetType) {

	Preconditions.checkArgument(token.getVariableType() instanceof ScalarType,
		"Casts can only be applied to scalar types, got a " + token.getVariableType() + " instead:\n" + token);

	// Get type of token
	ScalarType sourceType = (ScalarType) token.getVariableType();

	// Get NumericType of target
	StdIntType targetStdIntType = StdIntTypeUtils.cast(targetType);

	// TODO: Check if this is valid. I just added this here to prevent f(x) where x is an output-as-input
	// from becoming f(&(uint64_t*) &x)
	if (sourceType.scalar().isInteger() == targetStdIntType.scalar().isInteger()) {

	    Optional<Boolean> fits = sourceType.scalar().fitsInto(targetStdIntType);
	    if (fits.isPresent() && fits.get()) {
		return token;
	    }
	}

	// Build cast function
	FunctionInstance cast = UtilityInstances.newCastToScalar(sourceType, targetStdIntType);

	// Return function call
	return FunctionInstanceUtils.getFunctionCall(cast, token);
    }

}
