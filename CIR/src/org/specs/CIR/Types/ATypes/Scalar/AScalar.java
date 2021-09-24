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

package org.specs.CIR.Types.ATypes.Scalar;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import org.specs.CIR.Utilities.BigDecimalFactory;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsStrings;

public abstract class AScalar implements Scalar {

    private final static FittingRules RULES;
    static {
        // Create 'to' rules
        Map<Class<? extends ScalarType>, FittingRule> fitRules = SpecsFactory.newLinkedHashMap();

        // Populate table
        // fitRules.put(ScalarType.class, new FitScalarWithoutRanges());
        fitRules.put(ScalarType.class, new FitScalar());

        RULES = new FittingRules(null, fitRules);
    }

    private final ScalarType type;

    // private boolean useInInferrence = true;

    public AScalar(ScalarType type) {
        super();

        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }

        this.type = type;
    }

    @Override
    public boolean hasConstant() {
        return false;
    }

    /**
     * As default, tries to convert the constant string to a Number.
     */
    @Override
    public Number getConstant() {
        if (!hasConstant()) {
            return null;
        }

        return SpecsStrings.parseNumber(getConstantString());
    }

    @Override
    public String getConstantString() {
        return null;
    }

    /**
     * Sets the constant associated with the current scalar.
     * 
     * <p>
     * If the given String is null, removes any constant associated with the current scalar.
     * 
     */
    @Override
    // public VariableType setConstant(String constant) {
    public ScalarType setConstantString(String constant) {
        boolean hasConstant = hasConstant();

        // If constant is null and type has no constant, return type
        if (constant == null && !hasConstant) {
            return type;
        }

        // If constant is null and type has constant, remove type
        if (constant == null && hasConstant) {
            return removeConstant();
        }

        return setConstantPrivate(constant);
    }

    /**
     * As default, transforms the number into a String and calls setConstant with String.
     */
    @Override
    public ScalarType setConstant(Number value) {
        return setConstantString(value.toString());
    }

    @Override
    public ScalarType setBits(int bits) {
        throw new UnsupportedOperationException();
    }

    /**
     * As default, the value is true.
     */
    /*
    @Override
    public boolean useInInferrence() {
    return useInInferrence;
    }
    
    @Override
    public ScalarType setUseInInferrence(boolean useInInferrence) {
    this.useInInferrence = useInInferrence;
    return type;
    }
    */

    @Override
    public boolean isInteger() {
        throw new UnsupportedOperationException("Unsuppored operation for type '" + getClass() + "'");
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public ScalarType toInteger() {
        if (isInteger()) {
            return type;
        }

        throw new UnsupportedOperationException("Unsuppored operation for type '" + getClass()
                + "' when it is not an integer.");
    }

    @Override
    public int getBits() {
        throw new UnsupportedOperationException("Unsuppored operation for type '" + getClass() + "'");
    }

    @Override
    public boolean isUnsigned() {
        throw new UnsupportedOperationException("Unsuppored operation for type '" + getClass() + "'");
    }

    /*
    @Override
    public boolean isLiteral() {
    throw new UnsupportedOperationException("Unsuppored operation for type '" + getClass() + "'");
    }
    */

    @Override
    public ScalarType setLiteral(boolean isLiteral) {

        // If setting isLiteral to true, check if it has a constant
        if (isLiteral && !hasConstant()) {
            SpecsLogs.warn("Setting a ScalarType '" + getClass()
                    + "' to literal, but type does not have a constant. Please, set a constant first.");
        }

        return setLiteralPrivate(isLiteral);
    }

    protected abstract ScalarType setLiteralPrivate(boolean isLiteral);

    /**
     * Implemented by default for integer types.
     */
    @Override
    public BigDecimal getMaxValue() {
        if (!isInteger()) {
            throw new UnsupportedOperationException("Method not implemented as default for floating types");
        }

        return BigDecimalFactory.instance().getIntegerMax(getBits(), isUnsigned());
        /*
        if (isUnsigned()) {
        // 2^n - 1
        BigDecimal power = new BigDecimal(2).pow(getBits());
        return power.subtract(BigDecimal.ONE);
        }
        
        // 2^(n-1) - 1
        BigDecimal power = new BigDecimal(2).pow(getBits() - 1);
        return power.subtract(BigDecimal.ONE);
        */
    }

    /**
     * Implemented by default for integer types.
     */
    @Override
    public BigDecimal getMinValue() {
        if (!isInteger()) {
            throw new UnsupportedOperationException("Method not implemented as default for floating types");
        }

        return BigDecimalFactory.instance().getIntegerMin(getBits(), isUnsigned());
        /*
        if (isUnsigned()) {
        return BigDecimal.ZERO;
        }
        
        // -2^(n-1)
        BigDecimal power = new BigDecimal(2).pow(getBits() - 1);
        return power.negate();
        */
    }

    @Override
    public String getCodeNumber(String number) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Boolean> stores(ScalarType sourceType) {
        // First check the rules defined by the method
        Optional<Boolean> result = getFitRules().stores(sourceType, type);

        if (result.isPresent()) {
            return result;
        }

        return RULES.stores(sourceType, type);
    }

    @Override
    public Optional<Boolean> fitsInto(ScalarType targetType) {
        // First check the rules defined by the method
        Optional<Boolean> result = getFitRules().fitsInto(type, targetType);

        if (result.isPresent()) {
            return result;
        }

        // Use the rules provided by default
        return RULES.fitsInto(type, targetType);
    }

    /**
     * If not integer, uses %e; <br>
     * If integer longer than 32 bits, uses appropriate macro; <br>
     * Otherwise, uses %d or %u;
     */
    @Override
    public String getPrintSymbol() {
        // If not an integer, use %e
        if (!type.scalar().isInteger()) {
            // Try to determine how many places it should have, in excess
            double precision = Math.ceil(Math.log10(Math.pow(2, getBits())));

            return "%." + (int) precision + "e";
        }

        // If more than 32 bits, use the macro
        if (type.scalar().getBits() > 32) {
            if (type.scalar().isUnsigned()) {
                return "%\" PRIu64 \"";
            }
            return "%\" PRId64 \"";

        }

        if (type.scalar().isUnsigned()) {
            return "%u";
        }
        return "%d";

    }

    /**
     * Returns the variable name, dereferencing from pointer if necessary.
     */
    @Override
    public String getPrintArgument(String variableName) {
        if (type.pointer().isByReference()) {
            return "*" + variableName;
        }

        return variableName;
    }

    public FittingRules getFitRules() {
        return RULES;
    }

    @Override
    public boolean testRange(String number) {
        BigDecimal testNumber = new BigDecimal(number);

        if (testNumber.compareTo(getMaxValue()) > 0) {
            return false;
        }

        if (testNumber.compareTo(getMinValue()) < 0) {
            return false;
        }

        return true;
    }

    protected abstract ScalarType setConstantPrivate(String constant);

    // protected abstract ScalarType removeConstant();

}
