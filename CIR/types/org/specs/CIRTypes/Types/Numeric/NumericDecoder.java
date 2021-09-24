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

package org.specs.CIRTypes.Types.Numeric;

import java.util.Collection;
import java.util.Map;

import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Utilities.TypeDecoder;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * 
 * @author Joao Bispo
 * 
 */
public class NumericDecoder implements TypeDecoder {

    private static final Map<String, CTypeV2> MAP_TO_CTYPE;
    static {
	MAP_TO_CTYPE = SpecsFactory.newLinkedHashMap();
	for (CTypeV2 ctype : CTypeV2.values()) {
	    MAP_TO_CTYPE.put(ctype.getDeclaration(), ctype);
	}

	MAP_TO_CTYPE.put("single", CTypeV2.FLOAT);
    }

    // private final NumericFactoryV2 numerics;
    private final NumericFactory numerics;

    public NumericDecoder(NumericFactory numerics) {
	this.numerics = numerics;
    }

    @Override
    public VariableType decode(String typeString) {

	// It is enough to know the CType
	CTypeV2 ctype = MAP_TO_CTYPE.get(typeString);
	if (ctype == null) {
	    SpecsLogs.msgLib("Could not decode a CType from '" + typeString + "'");
	    /*
	    if (verbose) {
	    LoggingUtils.msgInfo("Could not decode a CType from '" + typeString + "'");
	    }
	    */
	    return null;
	}

	return numerics.newNumeric(ctype);
    }

    @Override
    public Collection<String> supportedTypes() {
	return MAP_TO_CTYPE.keySet();
    }

}
