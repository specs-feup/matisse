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

package org.specs.CIRTypes.Types.Void;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Conversion.AConversion;

public class VoidConversion extends AConversion {

    public VoidConversion(VoidType type) {
	super(type);
    }

    /**
     * Always returns true.
     */
    @Override
    public boolean isAssignable(VariableType targetType) {
	return true;
    }

    /**
     * When converting to void, always returns the given token.
     */
    /* (non-Javadoc)
     * @see org.specs.CIRv2.Types.Views.Conversion.AConversion#toSelf(org.specs.CIR.Tree.CToken)
     */
    @Override
    public CNode toSelf(CNode token) {
	return token;
    }
}
