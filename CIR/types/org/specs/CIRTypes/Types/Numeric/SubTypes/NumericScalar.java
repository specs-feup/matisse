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

import org.specs.CIR.Types.ATypes.CNative.CNativeScalar;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.Numeric.NumericTypeUtils;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsStrings;

public class NumericScalar extends CNativeScalar {

    /*
    private final static FittingRules RULES;
    static {
    // Create 'to' rules
    Map<Class<? extends ScalarType>, FittingRule> fitRules = FactoryUtils.newHashMap();
    // Populate table
    fitRules.put(NumericTypeV2.class, new FitNumeric());
    
    RULES = new FittingRules(fitRules, null);
    }
    */

    private final NumericTypeV2 type;

    public NumericScalar(NumericTypeV2 type) {
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
    
    return NumericTypeV2.newInstance(type, parsedNumber);
    }
    */

    @Override
    protected ScalarType setConstantPrivate(String constant) {
        Number parsedNumber = SpecsStrings.parseNumber(constant);

        if (parsedNumber == null) {
            SpecsLogs.warn("Could not set constant for type '" + this.type + "'");
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
        // return NumericTypeV2.newInstance(type, bits);
        return this.type.setBits(bits);
    }

    @Override
    public boolean isInteger() {
        return this.type.getCtype().getBaseType().isInteger();
    }

    @Override
    public int getBits() {
        return this.type.getBits();
    }

    @Override
    public boolean isUnsigned() {
        return this.type.getCtype().isUnsigned();
    }

    /**
     * For integer values, use the default implementation. Otherwise, use the value stored in NumericType.
     */
    @Override
    public BigDecimal getMaxValue() {
        if (isInteger()) {
            return super.getMaxValue();
        }

        return this.type.getMaxValue();
    }

    /**
     * For integer values, use the default implementation. Otherwise, use the value stored in NumericType.
     */
    @Override
    public BigDecimal getMinValue() {
        if (isInteger()) {
            return super.getMinValue();
        }

        return this.type.getMinValue();
    }

    /*
    @Override
    protected FittingRules getFitRules() {
    return RULES;
    }
    */

    // @Override
    // public boolean isLiteral() {
    // return this.type.isLiteral();
    // }

    @Override
    protected ScalarType setLiteralPrivate(boolean isLiteral) {
        return this.type.setLiteral(isLiteral);
    }

    @Override
    public ScalarType toInteger() {
        return NumericTypeUtils.toInteger(this.type);
    }

}
