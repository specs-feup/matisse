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

import org.specs.CIR.Types.ATypes.CNative.CNativeScalar;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.StdInd.StdIntType;

import pt.up.fe.specs.util.SpecsStrings;

public class StdIntScalar extends CNativeScalar {

    /*
    private final static FittingRules RULES;
    static {
    // Create 'to' rules
    Map<Class<? extends ScalarType>, FittingRule> fitRules = FactoryUtils.newHashMap();
    // Populate table
    fitRules.put(StdIntType.class, new FitStdInt());
    
    RULES = new FittingRules(fitRules, null);
    }
    */

    private final StdIntType type;

    public StdIntScalar(StdIntType type) {
        super(type);
        this.type = type;
    }

    @Override
    public boolean hasConstant() {
        return this.type.getConstant() != null;
    }

    @Override
    public Number getConstant() {
        return this.type.getConstant();
    }

    @Override
    public String getConstantString() {
        if (!hasConstant()) {
            return null;
        }

        return this.type.getConstant().toString();
    }

    /*
        @Override
        public VariableType setConstant(String constant) {
    	Number parsedNumber = ParseUtils.parseNumber(constant);
    
    	if (parsedNumber == null) {
    	    return type;
    	}
    
    	return StdIntType.newInstance(type, parsedNumber);
        }
    */
    @Override
    // protected VariableType setConstantPrivate(String constant) {
    protected ScalarType setConstantPrivate(String constant) {
        Number parsedNumber = SpecsStrings.parseNumber(constant);

        if (parsedNumber == null) {
            return this.type;
        }

        return this.type.setConstant(parsedNumber);
    }

    @Override
    public ScalarType setConstant(Number value) {
        return this.type.setConstant(value);
    }

    @Override
    public ScalarType removeConstant() {
        return setConstant(null);
    }

    @Override
    public ScalarType setBits(int bits) {
        return this.type.setBits(bits);
    }

    /*
        @Override
        // protected VariableType removeConstant() {
        protected ScalarType removeConstant() {
    	// return new StdIntType.Builder(type).constant(null).build();
    	return type.setConstant(null);
        }
    */
    /**
     * StdInt is always an integer.
     */
    @Override
    public boolean isInteger() {
        return true;
    }

    /**
     * StdInt is always an integer.
     */
    @Override
    public ScalarType toInteger() {
        return this.type;
    }

    @Override
    public int getBits() {
        return this.type.getnBits();
    }

    @Override
    public boolean isUnsigned() {
        return this.type.isUnsigned();
    }

    // @Override
    // public boolean isLiteral() {
    // return this.type.isLiteral();
    // }

    @Override
    protected ScalarType setLiteralPrivate(boolean isLiteral) {
        return this.type.setLiteral(isLiteral);
    }

    /*
    @Override
    protected FittingRules getFitRules() {
    return RULES;
    }
    */

}
