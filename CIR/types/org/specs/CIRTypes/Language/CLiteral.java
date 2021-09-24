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

package org.specs.CIRTypes.Language;

import java.util.Map;

import org.specs.CIR.CirUtils;
import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Types.CNumberParser;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.CNative.CNativeType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.TypesOld.CNumber;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.CIRTypes.Types.StdInd.StdIntFactory;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsStrings;

/**
 * Represents a number in C.
 * 
 * @author Joao Bispo
 * 
 */
public class CLiteral implements CNumber {

    // Maps Java Number classes to VariableTypes.
    // Mapping all floating points to double, to avoid losing precision.
    private final static Map<Class<?>, CNativeType> CLASS_TO_TYPE;

    static {
        CLASS_TO_TYPE = SpecsFactory.newHashMap();

        // classToNumeric.put(BigDecimal.class, NumericType.Double);
        CLiteral.CLASS_TO_TYPE.put(Double.class, NumericTypeV2.newInstance(CTypeV2.DOUBLE, 64));
        // CLASS_TO_TYPE.put(Float.class, NumericTypeV2.newInstance(CTypeV2.FLOAT, 32));
        CLiteral.CLASS_TO_TYPE.put(Float.class, NumericTypeV2.newInstance(CTypeV2.DOUBLE, 64));

        CLiteral.CLASS_TO_TYPE.put(Character.class, NumericTypeV2.newInstance(CTypeV2.CHAR_SIGNED, 8));
        CLiteral.CLASS_TO_TYPE.put(Byte.class, StdIntFactory.newInt8());
        CLiteral.CLASS_TO_TYPE.put(Short.class, StdIntFactory.newInt16());
        // classToType.put(Integer.class, StdIntFactory.newInt32());
        CLiteral.CLASS_TO_TYPE.put(Integer.class, NumericTypeV2.newInstance(CTypeV2.INT, 32));
        CLiteral.CLASS_TO_TYPE.put(Long.class, StdIntFactory.newInt64());
        // classToNumeric.put(BigInteger.class, NumericType.Int64);
    }

    private final Number number;
    private final ScalarType type;

    /**
     * @param leftHand
     * @param rightHand
     * @param exponent
     * @param isComplex
     */
    private CLiteral(Number number, ScalarType type) {
        // assert !ScalarUtils.hasConstant(type)
        // || number.doubleValue() == ScalarUtils.getConstant(type).doubleValue() : "Constant mismatch between "
        // + number + " and " + type;

        this.number = number;
        this.type = processType(number, type);

        if (type.pointer().isByReference()) {
            throw new RuntimeException("A literal cannot be a pointer");
        }

        // Better do it on a cast, literals can change types all the time
        // if (!type.scalar().testRange(number.toString())) {
        // throw new RuntimeException("Number '" + number + "' is out of range for type '" + type
        // + "', cannot build CLiteral");

        // }

    }

    private static ScalarType processType(Number number, ScalarType type) {
        // Propagate constant
        VariableType newType = type
                // Propagate constant
                .scalar().setConstant(number)
                // Set literal to true, since this class represents a literal
                .scalar().setLiteral(true);

        if (CirUtils.useWeakTypes()) {
            newType = newType.setWeakType(true);
        }

        newType = newType.makeUnique(type);
        /*
        	// Types of numbers are considered weak types
        		.setWeakType(true)
        		// If newType points to the same original type, return a copy
        		.makeUnique(type);
        */
        return (ScalarType) newType;
    }

    /**
     * @param cNumber
     * @param baseType
     * @return
     */
    public static CLiteral newInstance(CNumber cNumber, ScalarType newType) {
        return newInstance(cNumber.getNumber(), newType);
    }

    /**
     * @param cNumber
     * @param baseType
     * @return
     */
    public static CLiteral newInstance(Number number, ScalarType type) {
        return new CLiteral(number, type);
    }

    /*
    public NumericType getType() {
    return type;
    }
    */

    @Override
    public String toCString() {
        String numberString = this.number.toString();

        if (this.type instanceof CNumberParser) {
            return ((CNumberParser) this.type).parseNumber(numberString);
        }
        return "((" + this.type.code().getSimpleType() + ")" + numberString + ")";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return toCString();
    }

    @Override
    public ScalarType getType() {
        return this.type;
    }

    /*
    public CNativeType getCType() {
    return type;
    }
    */

    @Override
    public boolean isInteger() {
        return this.type.scalar().isInteger();
    }

    /**
     * @param i
     * @return
     */
    public static CLiteral newInteger(int i) {
        return newInteger(new Integer(i));
    }

    /**
     * Creates a new CLiteral with the type 'int32_t'
     * 
     * @param integerNumber
     * @return
     */
    public static CLiteral newInteger(Number integerNumber) {
        /*
        // Check types
        if (!integerClasses.contains(integerNumber.getClass())) {
            throw new RuntimeException("Given Number class ('" + integerNumber.getClass()
        	    + "') is not considerer an Integer.\n Integer classes:" + integerClasses);
        }
        */

        // return new CNumber(integerNumber, NumberType.INTEGER);

        // NumericType type = getType(integerNumber);
        // return new CLiteral(integerNumber, NumericFactoryV2.DEFAULT.newInt(CTypeV2.INT));
        return new CLiteral(integerNumber, StdIntFactory.newInt32());
    }

    /**
     * Creates a new CLiteral for the C double type, using the default value of 64 bits for the variable length.
     * 
     * @param i
     * @return
     */
    public static CLiteral newReal(Number decimalNumber) {
        /*
        // Check types
        if (!realClasses.contains(decimalNumber.getClass())) {
            throw new RuntimeException("Given Number class ('" + decimalNumber.getClass()
        	    + "') is not considerer an Integer.\n Integer classes:" + integerClasses);
        }
        */
        // NumericType type = getType(decimalNumber);

        // return new CNumber(decimalNumber, NumberType.REAL);
        return new CLiteral(decimalNumber, NumericTypeV2.newInstance(CTypeV2.DOUBLE, 64));
    }

    /**
     * @param decimalNumber
     * @return
     */
    /*
    private static NumericType getType(Number number) {
    NumericType type = classToNumeric.get(number.getClass());
    
    if (type == null) {
        throw new RuntimeException("Class not supported:" + number.getClass());
    }
    
    return type;
    }
    */

    /**
     * The type of a new number is adjust to class extracted from the string. E.g., an Integer will have type int, etc.
     * 
     * @param string
     * @return
     */
    public static CLiteral newNumber(String string) {
        Number aNumber = SpecsStrings.parseNumber(string, false);

        if (aNumber == null) {
            SpecsLogs.warn("Could not parse '" + string + "' to a number.");
            return null;
        }

        CNativeType type = getType(aNumber);
        // CNativeType type = NumericTypeV2.newInstance(CTypeV2.DOUBLE, 64);
        return new CLiteral(aNumber, type);

    }

    /**
     * @param decimalNumber
     * @return
     */

    private static CNativeType getType(Number number) {
        CNativeType type = CLiteral.CLASS_TO_TYPE.get(number.getClass());

        if (type == null) {
            throw new RuntimeException("Class not supported:" + number.getClass());
        }

        return type;
    }

    @Override
    public Number getNumber() {
        return this.number;
    }

    /*
    public static CNumber newInstance(Number number, VariableType type) {
    return newInstance(number, CNativeUtils.getCNativeType(type));
    }
    */

}
