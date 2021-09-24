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

package org.specs.CIRTypes.Types.DynamicMatrix;

import org.specs.CIR.Types.ATypes.Matrix.MatrixConversion;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.Views.Conversion.ConversionRules.ConversionMap;
import org.specs.CIRTypes.Types.DynamicMatrix.Conversion.ToScalarType;

public class DynamicMatrixConversion extends MatrixConversion {

    private static final ConversionMap TO_RULES;
    static {
	// Create 'to' rules
	TO_RULES = new ConversionMap();

	// Populate table
	TO_RULES.put(ScalarType.class, new ToScalarType());

    }

    public DynamicMatrixConversion(DynamicMatrixType type) {
	super(type, TO_RULES, null);
    }

}
