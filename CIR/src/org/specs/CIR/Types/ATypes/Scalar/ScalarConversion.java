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
import org.specs.CIR.Types.Views.Conversion.AConversion;
import org.specs.CIR.Types.Views.Conversion.ConversionRules.ConversionMap;

import pt.up.fe.specs.util.SpecsLogs;

public abstract class ScalarConversion extends AConversion {

    private final ScalarType type;

    public ScalarConversion(ScalarType type, ConversionMap toRules, ConversionMap toSelfRules) {
	super(type, toRules, toSelfRules);
	this.type = type;
    }

    /*
        @Override
        public VariableType toScalarType() {
    	// return PointerUtils.getType(type, false);
    	return type;
        }
    */
    @Override
    public boolean isAssignable(VariableType targetType) {
	// Is only assignable to scalars
	// if (!ScalarUtils.isScalar(targetType)) {
	if (!ScalarUtils.hasScalarType(targetType)) {
	    SpecsLogs.warn("CHECK IF THIS IS CORRECT");
	    return false;
	}

	// Get scalar type of target
	// VariableType targetScalar = targetType.conversion().toScalarType();
	VariableType targetScalar = ScalarUtils.toScalar(targetType);
	if (targetScalar == null) {
	    throw new RuntimeException("Could not extract a scalar type from '" + targetType + "'");
	}

	// Check if the current type fits into the target type
	// return ScalarUtils.fitsInto(type, targetType);
	return ScalarUtils.fitsInto(type, targetScalar);
    }

}
