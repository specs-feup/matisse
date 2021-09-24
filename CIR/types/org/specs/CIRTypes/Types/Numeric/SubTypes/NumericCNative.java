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

package org.specs.CIRTypes.Types.Numeric.SubTypes;

import java.math.BigDecimal;

import org.specs.CIR.Language.Types.CDataType;
import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Types.ATypes.CNative.ACNative;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;

public class NumericCNative extends ACNative {

    private final NumericTypeV2 type;

    public NumericCNative(NumericTypeV2 type) {
	this.type = type;
    }

    @Override
    public String parseNumber(String number) {

	// We can't just remove everything after the dot because "1.23E2" is a valid integer
	if (type.scalar().isInteger()) {
	    number = new BigDecimal(number).toBigInteger().toString();
	}
	// If real, add '.0' if not present
	else {
	    if (number.indexOf('.') == -1) {
		number += ".0";
	    }
	}

	// If 'float', append f
	if (type.getCtype().getBaseType() == CDataType.FLOAT) {
	    number += "f";
	}

	// If unsigned, append suffix U
	if (type.getCtype().isUnsigned()) {
	    number += "u";
	}

	// If long, append suffix L
	if (type.getCtype().isLong()) {
	    number += "l";
	}

	return number;
    }

    @Override
    public CTypeV2 getCType() {
	return type.getCtype();
    }

}
