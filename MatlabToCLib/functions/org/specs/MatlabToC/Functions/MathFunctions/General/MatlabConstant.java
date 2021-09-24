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

package org.specs.MatlabToC.Functions.MathFunctions.General;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.specs.CIR.CirUtils;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.StdInd.StdIntFactory;

import pt.up.fe.specs.util.SpecsEnums;
import pt.up.fe.specs.util.providers.KeyProvider;

/**
 * @author Joao Bispo
 * 
 */
public enum MatlabConstant implements KeyProvider<String> {

    PI("pi", "3.14159265358979323846") {
	@Override
	public VariableType getTypePrivate(NumericFactory numerics, VariableType defaultReal) {
	    return defaultReal;
	}
    },
    /**
     * DEPRECATED: EPS must be a function and not a constant, since it returns different values according to the type
     */
    EPS("eps", "2.2204e-16") {
	@Override
	public VariableType getTypePrivate(NumericFactory numerics, VariableType defaultReal) {
	    return defaultReal;
	}
    },
    /**
     * DEPRECATED: REALMIN must be a function and not a constant, since it returns different values according to the
     * type
     */
    REALMIN("realmin", "2.2251e-308") {
	@Override
	public VariableType getTypePrivate(NumericFactory numerics, VariableType defaultReal) {
	    return defaultReal;
	}
    },
    /**
     * DEPRECATED: INTMAX must be a function and not a constant, since it returns different values according to the type
     */
    INTMAX("intmax", "2147483647") {
	@Override
	public VariableType getTypePrivate(NumericFactory numerics, VariableType defaultReal) {
	    return StdIntFactory.newInt32();
	}
    },
    TRUE("true", "1") {
	@Override
	public VariableType getTypePrivate(NumericFactory numerics, VariableType defaultReal) {
	    return numerics.newInt();
	}
    },
    FALSE("false", "0") {
	@Override
	public VariableType getTypePrivate(NumericFactory numerics, VariableType defaultReal) {
	    return numerics.newInt();
	}
    };

    private final static Map<String, MatlabConstant> constantMap = SpecsEnums.buildMap(MatlabConstant.class);

    private final String constantName;
    // private final String value;
    private final List<String> values;

    // private final VariableType outputType;

    /*
    private MatlabConstant(String constantName, String value, VariableType outputType) {
    this(constantName, Arrays.asList(value), outputType);
    };
    */

    private MatlabConstant(String constantName, String constant) {
	this(constantName, Arrays.asList(constant));
    }

    // private MatlabConstant(String constantName, String value, VariableType outputType) {
    private MatlabConstant(String constantName, List<String> value) {
	this.constantName = constantName;
	values = value;
	// this.outputType = outputType;
    }

    public static MatlabConstant getConstant(String varName) {
	return MatlabConstant.constantMap.get(varName);
    }

    @Override
    public String getKey() {
	return getId();
	// return getName();
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Utilities.ConstantValue#getId()
     */
    public String getId() {
	return constantName;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Utilities.ConstantValue#getValues()
     */
    public List<String> getValues() {
	return values;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Utilities.ConstantValue#getDefaultType()
     */
    public VariableType getDefaultType(NumericFactory numerics, VariableType defaultReal) {
	VariableType type = getTypePrivate(numerics, defaultReal);

	// Constant types are weak types
	if (CirUtils.useWeakTypes()) {
	    return type.setWeakType(true);
	}

	return type;

    }

    protected abstract VariableType getTypePrivate(NumericFactory numerics, VariableType defaultReal);
}
