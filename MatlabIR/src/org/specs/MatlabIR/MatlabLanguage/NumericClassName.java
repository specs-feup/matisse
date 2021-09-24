/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.MatlabIR.MatlabLanguage;

import java.math.BigInteger;
import java.util.Map;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;

import pt.up.fe.specs.util.SpecsEnums;
import pt.up.fe.specs.util.providers.KeyProvider;

/**
 * Represents the numeric types of MATLAB.
 * 
 * @author Joao Bispo
 * 
 */
public enum NumericClassName implements KeyProvider<String> {

    DOUBLE("double") {
	@Override
	public Number parseNumber(String value) {
	    return Double.parseDouble(value);
	}
    },
    SINGLE("single") {
	@Override
	public Number parseNumber(String value) {
	    return Float.parseFloat(value);
	}
    },
    INT8("int8") {
	@Override
	public Number parseNumber(String value) {
	    return Byte.parseByte(value);
	}
    },
    UINT8("uint8") {
	@Override
	public Number parseNumber(String value) {
	    return (new BigInteger(value)).byteValue();
	}
    },
    INT16("int16") {
	@Override
	public Number parseNumber(String value) {
	    return Short.parseShort(value);
	}
    },
    UINT16("uint16") {
	@Override
	public Number parseNumber(String value) {
	    return (new BigInteger(value)).shortValue();
	}
    },
    INT32("int32") {
	@Override
	public Number parseNumber(String value) {
	    return Integer.parseInt(value);
	}
    },
    UINT32("uint32") {
	@Override
	public Number parseNumber(String value) {
	    return (new BigInteger(value)).intValue();
	}
    },
    INT64("int64") {
	@Override
	public Number parseNumber(String value) {
	    return Long.parseLong(value);
	}
    },
    UINT64("uint64") {
	@Override
	public Number parseNumber(String value) {
	    return (new BigInteger(value)).longValue();
	}
    },
    CHAR("char") {
	@Override
	public Number parseNumber(String value) {
	    if (value.length() != 1) {
		throw new RuntimeException("Could not parse character from '" + value + "'");
	    }

	    return (int) value.charAt(0);
	}
    };

    private final String matlabString;
    private final static Map<String, NumericClassName> nameMap = SpecsEnums.buildMap(NumericClassName.class);

    /**
     * @param matlabString
     */
    private NumericClassName(String matlabString) {
	this.matlabString = matlabString;
    }

    /**
     * @return the matlabString
     */
    public String getMatlabString() {
	return matlabString;
    }

    @Override
    public String getKey() {
	return getMatlabString();
    }

    /**
     * @param string
     * @return
     */
    public static NumericClassName getNumericClassName(String string) {
	return nameMap.get(string);
    }

    public boolean isInteger() {
	if (this == DOUBLE) {
	    return false;
	}

	if (this == SINGLE) {
	    return false;
	}

	return true;
    }

    public abstract Number parseNumber(String value);

    /**
     * Returns a String MatlabToken, representing the Numeric Class
     * 
     * @return
     */
    public MatlabNode getToken() {
	return MatlabNodeFactory.newCharArray(getMatlabString());
    }

}
