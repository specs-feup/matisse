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

package org.specs.CIR.Types.ATypes.Scalar;

import java.math.BigDecimal;
import java.util.Optional;

import org.specs.CIR.Exceptions.CirUnsupportedException;

/**
 * Scalar-related operations on a VariableType.
 * 
 * @author Joao Bispo
 * 
 */
public interface Scalar {

    /**
     * Instance providers for Scalar primitives.
     * 
     * @return
     */
    default ScalarFunctions functions() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return true if the given type has a constant assigned. False otherwise
     */
    boolean hasConstant();

    /**
     * For most cases, scalars will have a number as constant, this method returns that number in case the scalar has a
     * constant. If a scalar does not have a constant assigned, returns null.
     * <p>
     * However, if that is not the case (e.g. complex), it returns null.
     * 
     * @return if the number
     */
    Number getConstant();

    default Optional<Number> getNullableConstant() {
        return Optional.ofNullable(getConstant());
    }

    /**
     * For most cases, scalars will have a number as constant, this method sets that number in case the scalar has a
     * constant.
     * 
     * @return if the number
     */
    ScalarType setConstant(Number value);

    /**
     * 
     * @return Returns the type, without a constant value
     */
    ScalarType removeConstant();

    /**
     * 
     * @return if the type has a constant assigned, returns a String representing the C code of that constant
     */
    String getConstantString();

    /**
     * 
     * @return a new object with the same values as the current type, with the constant set to the given value
     */
    ScalarType setConstantString(String constant);

    ScalarType setBits(int bits);

    /**
     * 
     * @return true if the current scalar type represents an integer
     */
    boolean isInteger();

    boolean isNumber();

    /**
     * Modifies the given string representing a number so that it can be correctly used in C code.
     * 
     * <p>
     * For instance, a 'float' C type appends the letter 'f' to the end of the number.
     * 
     * @param number
     * @return
     */
    String getCodeNumber(String number);

    /**
     * 
     * @param sourceType
     * @return true if the given type is the same size or smaller than the current type. False otherwise, null if not
     *         implemented
     */
    Optional<Boolean> stores(ScalarType sourceType);

    /**
     * 
     * @param targetType
     * @return true if the current type is the same size or smaller than the given type. False otherwise, null if not
     *         implemented
     */
    Optional<Boolean> fitsInto(ScalarType targetType);

    /**
     * The string used when we want to printing this variable.
     * 
     * <p>
     * For instance, for an 'int' type, the string would be '%d'.
     * 
     * @return
     */
    String getPrintSymbol();

    /**
     * How the variable should be used as an argument in a print call.
     * 
     * <p>
     * For instance, for an 'int' type, the string would just the <variableName>'.
     * 
     * @param variableName
     * @return
     */
    String getPrintArgument(String variableName);

    /**
     * 
     * 
     * @return the number of bits of the type
     */
    int getBits();

    /**
     * @return true if the type represents an unsigned variable
     */
    boolean isUnsigned();

    /**
     * @return the maximum value this type can hold
     */
    BigDecimal getMaxValue();

    /**
     * @return the minimum value this type can hold
     */
    BigDecimal getMinValue();

    /**
     * 
     * @return true if the current type should be taken into account when inferring types. False otherwise
     * @deprecated
     */
    // boolean useInInferrence();

    /**
     * @param useInInferrence
     * @return returns the current type, with the 'useInInferrence' set to the given value
     * @deprecated
     */
    // ScalarType setUseInInferrence(boolean useInInferrence);

    /**
     * TODO: This method seems that is not being used
     * 
     * @return true if it is the type of a literal (e.g., the number 2)
     */
    // boolean isLiteral();

    /**
     * 
     * @param isLiteral
     * @return a new object with the same values as the current type, with isLiteral set to the given value
     */
    ScalarType setLiteral(boolean isLiteral);

    /**
     * 
     * @return the integer type that should be used for the current type, if a cast is needed
     */
    ScalarType toInteger();

    /**
     * 
     * @param number
     * @return true if the number is inside the range of this type, false otherwise
     */
    boolean testRange(String number);

    /**
     * 
     * @return if the scalar is a real number, returns the distance from 1.0 to the next largest floating point number.
     *         Else, returns empty
     */
    default Optional<String> getEps() {
        if (isInteger()) {
            return Optional.empty();
        }

        // Try the most common bit sizes
        Optional<String> eps = ScalarUtils.getEps(getBits());

        if (!eps.isPresent()) {
            throw new CirUnsupportedException(getClass());
        }

        return eps;

    }
    /**
     * 
     * @return a new object with the same values as the current type, with isLiteral set to false
     */
    // ScalarType unsetLiteral();

    /**
     * 
     * @return returns the current type, normalized
     */
    // ScalarType normalize();
}
