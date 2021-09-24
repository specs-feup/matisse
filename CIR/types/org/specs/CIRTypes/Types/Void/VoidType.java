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

package org.specs.CIRTypes.Types.Void;

import org.specs.CIR.Types.AVariableType;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Code.Code;
import org.specs.CIR.Types.Views.Conversion.Conversion;

/**
 * @author Joao Bispo
 * 
 */
public final class VoidType extends AVariableType {

    // private final VoidCode voidCode;
    /*
        VoidType() {
    	this.voidCode = new VoidCode();
        }
    */

    public static VoidType newInstance() {
	return new VoidType();
    }

    @Override
    public String getSmallId() {
	return "v";
    }

    @Override
    public Code code() {
	// return voidCode;
	return new VoidCode(this);
    }

    /* (non-Javadoc)
     * @see org.specs.CIRv2.Types.AVariableType#conversion()
     */
    @Override
    public Conversion conversion() {
	return new VoidConversion(this);
    }

    @Override
    public boolean strictEquals(VariableType type) {
	return equals(type);
    }

}
