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

package org.specs.CIRTypes.Types.StdInd.SubTypes;

import java.math.BigDecimal;

import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Types.ATypes.CNative.ACNative;
import org.specs.CIRTypes.Types.StdInd.StdIntType;

public class StdIntCNative extends ACNative {

    private final StdIntType type;

    public StdIntCNative(StdIntType type) {
	this.type = type;
    }

    @Override
    public String parseNumber(String number) {

	// StdInt types are always integers
	int indexOfDecimal = number.indexOf('.');
	if (indexOfDecimal != -1) {
	    number = new BigDecimal(number).toBigInteger().toString();
	}

	// If unsigned, append suffix U
	if (type.isUnsigned()) {
	    number += "u";
	}

	// If long, append suffix L.
	if (type.isLong()) {
	    number += "l";
	}

	return number;
    }

    @Override
    public CTypeV2 getCType() {
	if (type.isLong()) {
	    if (type.isUnsigned()) {
		return CTypeV2.LONG_UNSIGNED;
	    }
	    return CTypeV2.LONG;

	}

	if (type.isUnsigned()) {
	    return CTypeV2.INT_UNSIGNED;
	}

	return CTypeV2.INT;

    }
}
