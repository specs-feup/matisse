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

import static com.google.common.base.Preconditions.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.specs.CIR.CirKeys;
import org.specs.CIR.Language.Types.CTypeSizes;
import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Types.ATypes.CNative.CNativeType;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class NumericFactory {

    private final CTypeSizes sizes;
    private final Map<CTypeV2, NumericTypeV2> cache;
    private final Optional<NumericTypeV2> defaultReal;

    // This field is transient because XStream currently does not support serialization of lambdas
    private transient Map<Class<?>, NumericTypeV2Provider> converter = initConverter();

    public NumericFactory(CTypeSizes sizes, NumericTypeV2 defaultReal) {
        this.sizes = sizes;
        cache = new HashMap<>();
        this.defaultReal = Optional.ofNullable(defaultReal);
    }

    public NumericFactory(CTypeSizes sizes) {
        this(sizes, null);
    }

    public NumericFactory(DataStore settings) {
        this(settings.get(CirKeys.C_BIT_SIZES));
    }

    /**
     * Constructor that uses default values in CirKeys.C_BIT_SIZES.
     */
    public NumericFactory() {
        this(DataStore.newInstance("NumericFactoryDefault"));
    }

    private Map<Class<?>, NumericTypeV2Provider> initConverter() {
        Map<Class<?>, NumericTypeV2Provider> converter = new HashMap<>();

        converter.put(Character.class, () -> newChar());
        converter.put(Byte.class, () -> newChar());
        converter.put(Short.class, () -> newShort());
        converter.put(Integer.class, () -> newInt());
        converter.put(Long.class, () -> newLong());

        return converter;
    }

    public NumericTypeV2 newNumeric(CTypeV2 type) {
        return newNumeric(type, null);
    }

    public NumericTypeV2 newNumeric(CTypeV2 type, Number constant) {
        checkArgument(type != null, "type must not be null");
        int bits = sizes.getSize(type);
        BigDecimal maxValue = sizes.getMaxValue(type);
        BigDecimal minValue = sizes.getMinValue(type);

        return new NumericTypeV2(type, bits, constant, false, maxValue, minValue);
    }

    public static NumericFactory defaultFactory() {
        return new NumericFactory(CTypeSizes.DEFAULT_SIZES);
    }

    public CTypeSizes getSizes() {
        return sizes;
    }

    /**
     * Creates a new 'int' type with the given value as constant.
     * 
     * @param value
     * @return
     */
    public NumericTypeV2 newInt(int value) {
        return newNumeric(CTypeV2.INT, value);
    }

    public NumericTypeV2 newInt(Optional<Integer> value) {
        if (value.isPresent()) {
            return newInt(value.get());
        }

        return newInt();
    }

    public NumericTypeV2 newType(CTypeV2 ctype) {
        // Check if type was already created
        NumericTypeV2 type = cache.get(ctype);
        if (type == null) {
            type = newNumericFromCType(ctype);
            cache.put(ctype, type);
        }

        return type;
    }

    /**
     * Creates a new 'char' type.
     * 
     * @return
     */
    public NumericTypeV2 newChar() {
        return newType(CTypeV2.CHAR);
    }

    /**
     * Creates a new 'short' type.
     * 
     * @return
     */
    public NumericTypeV2 newShort() {
        return newType(CTypeV2.SHORT);
    }

    /**
     * Creates a new 'int' type.
     * 
     * @return
     */
    public NumericTypeV2 newInt() {
        return newType(CTypeV2.INT);
    }

    /**
     * Creates a new 'long' type.
     * 
     * @return
     */
    public NumericTypeV2 newLong() {
        return newType(CTypeV2.LONG);
    }

    /**
     * Creates a new 'float' type.
     * 
     * @return
     */
    public NumericTypeV2 newFloat() {
        return newType(CTypeV2.FLOAT);
    }

    /**
     * Creates a new 'double' type.
     * 
     * @return
     */
    public NumericTypeV2 newDouble() {
        if (defaultReal.isPresent()) {
            return defaultReal.get();
        }

        return newType(CTypeV2.DOUBLE);
    }

    /**
     * Creates a new NumericType based on the given ctype, without a constant and not being a pointer.
     * 
     * @param ctype
     */
    private NumericTypeV2 newNumericFromCType(CTypeV2 ctype) {
        return NumericTypeV2.newInstance(ctype, sizes.getSize(ctype), null, false);
    }

    public NumericTypeV2 newDouble(Double value) {
        return newNumeric(CTypeV2.DOUBLE, value);
    }

    /**
     * Infers a type from a string representation of a number.
     * 
     * <p>
     * Throws an exception if the string does not represent a number.
     * 
     * @param matlabString
     * @return
     */
    /*
    public VariableType newNumeric(String numberString, VariableType defaultReal) {
    Number number = ParseUtils.parseNumber(numberString, false);
    
    return newNumeric(number, defaultReal);
    }
     */

    /**
     * Infers a type from a Number class .
     * 
     * @param number
     * @param defaultReal
     * @return
     */
    public CNativeType newNumeric(Number number, CNativeType defaultReal) {
        Class<?> numberClass = number.getClass();

        // If number if double or float, return default float
        if (numberClass.equals(Double.class) || numberClass.equals(Float.class)
                || numberClass.equals(BigDecimal.class)) {
            return defaultReal;
        }

        /*
        if (numberClass.equals(Character.class)) {
            return newChar();
        }
        
        if (numberClass.equals(Byte.class)) {
            return newChar();
        }
        
        if (numberClass.equals(Short.class)) {
            return newShort();
        }
        
        if (numberClass.equals(Integer.class)) {
            return newInt();
        }
        
        if (numberClass.equals(Long.class)) {
            return newLong();
        }
         */
        /*
        converter.put(Character.class, () -> newChar());
        converter.put(Byte.class, () -> newChar());
        converter.put(Short.class, () -> newShort());
        converter.put(Integer.class, () -> newInt());
        converter.put(Long.class, () -> newLong());
         */

        // Get type from table
        NumericTypeV2Provider provider = converter.get(numberClass);
        if (provider != null) {
            return provider.getType();
        }

        // Could not decode number
        throw new RuntimeException("Could not decode number '" + number + "'");
    }

    /**
     * Helper interface for map with classes and types.
     * 
     * @author JoaoBispo
     *
     */
    interface NumericTypeV2Provider {
        NumericTypeV2 getType();
    }

    /*
    public static NumericFactory newInstance(Setup setup) {
    return new NumericFactory(setup.getValue(CirOption.C_BIT_SIZES, CTypeSizes.class));
    }
     */

}
