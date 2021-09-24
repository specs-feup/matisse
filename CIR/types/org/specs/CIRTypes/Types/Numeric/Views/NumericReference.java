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

package org.specs.CIRTypes.Types.Numeric.Views;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Pointer.AReference;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;

public class NumericReference extends AReference {

    private final NumericTypeV2 numericType;

    public NumericReference(NumericTypeV2 numericType) {
	super(numericType.isByReference());

	this.numericType = numericType;
    }

    @Override
    public VariableType getType(boolean isPointer) {
	if (isPointer == isByReference()) {
	    return numericType;
	}

	// return NumericTypeV2.newInstance(numericType, isPointer);
	return numericType.setPointer(isPointer);
    }

}
