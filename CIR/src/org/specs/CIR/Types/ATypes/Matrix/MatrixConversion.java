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

package org.specs.CIR.Types.ATypes.Matrix;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.Conversion.ToMatrix;
import org.specs.CIR.Types.Views.Conversion.AConversion;
import org.specs.CIR.Types.Views.Conversion.ConversionRules.ConversionMap;

// public abstract class MatrixConversion<T extends MatrixType> extends AConversion {
public abstract class MatrixConversion extends AConversion {

    private final MatrixType type;

    public MatrixConversion(MatrixType type, ConversionMap toRules, ConversionMap toSelfRules) {
	super(type, toRules, toSelfRules);
	this.type = type;

	// Add matrix conversion rule
	getRules().getToRules().put(MatrixType.class, new ToMatrix());
	// toRules.put(MatrixType.class, new ToMatrix());
    }

    public MatrixConversion(MatrixType type) {
	this(type, null, null);
    }

    @Override
    public boolean isAssignable(VariableType targetType) {

	// A matrix can only be assign to other matrix
	if (!MatrixUtils.isMatrix(targetType)) {
	    return false;
	}

	VariableType targetElementType = MatrixUtils.getElementType(targetType);
	// Check if inner type of matrix is assignable to the given target type
	return type.matrix().getElementType().conversion().isAssignable(targetElementType);

    }

}
