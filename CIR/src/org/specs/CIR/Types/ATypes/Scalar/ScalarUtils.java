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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.Utils.ToScalar;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsStrings;

/**
 * 
 * @author Joao Bispo
 * 
 */
public class ScalarUtils {

    private static final Map<Integer, String> BITS_TO_EPS;

    static {
        BITS_TO_EPS = new HashMap<>();
        ScalarUtils.BITS_TO_EPS.put(16, "9.765625e-04");
        ScalarUtils.BITS_TO_EPS.put(32, "1.192093e-07");
        ScalarUtils.BITS_TO_EPS.put(64, "2.220446e-16");
    }

    public static Optional<String> getEps(int numBits) {
        String value = ScalarUtils.BITS_TO_EPS.get(numBits);
        return Optional.ofNullable(value);
    }

    /**
     * 
     * @param type
     * @return true if the given type implements ScalarType. If type is null, then returns false.
     */
    public static boolean isScalar(VariableType type) {
        return type instanceof ScalarType;
    }

    public static boolean isScalar(Optional<VariableType> type) {
        return isScalar(type.orElse(null));
    }

    /**
     * Converts a type to scalar.
     * 
     * <p>
     * If the type implements ScalarType, returns the type itself. Otherwise, checks if the type implements the
     * interface ToScalar. If could not convert to ScalarType, returns null.
     * 
     * @param type
     * @return
     */
    public static ScalarType toScalar(VariableType type) {
        if (type instanceof ScalarType) {
            return (ScalarType) type;
        }

        if (type instanceof ToScalar) {
            return ((ToScalar) type).toScalarType();
        }

        // return null;

        throw new RuntimeException("Could not convert type '" + type + "' of class '" + type.getClass() + "' to "
                + ScalarType.class + ". Consider implementing the interface '" + ToScalar.class + "'");
    }

    /**
     * 
     * @param type
     * @return a Scalar, if the given type implements ScalarType. Otherwise, throws an exception
     */
    public static Scalar getScalar(VariableType type) {
        ScalarType scalarType = SpecsStrings.cast(type, ScalarType.class);

        return scalarType.scalar();
    }

    /**
     * Helper method for Scalar.setConstant. If given type does not implement ScalarType, returns 'type' unmodified.
     * 
     * @param type
     * @param constant
     * @return
     */
    public static VariableType setConstantString(VariableType type, String constant) {
        if (!isScalar(type)) {
            return type;
        }

        return getScalar(type).setConstantString(constant);
    }

    /**
     * Removes the constant of a ScalarType.
     * 
     * @param type
     * @return
     */
    public static ScalarType removeConstant(ScalarType type) {
        return (ScalarType) setConstantString(type, null);
    }

    /**
     * Uses fitsInto and stores methods to determine if sourceType fits inside target type.
     * 
     * @param sourceType
     * @param targetType
     * @return true if sourceType first into targetType
     */
    public static boolean fitsInto(VariableType sourceType, VariableType targetType) {
        if (sourceType == null) {
            throw new IllegalArgumentException("sourceType must not be null");
        }
        if (targetType == null) {
            throw new IllegalArgumentException("targetType must not be null");
        }

        ScalarType sourceScalar = SpecsStrings.cast(sourceType, ScalarType.class);
        ScalarType targetScalar = SpecsStrings.cast(targetType, ScalarType.class);

        // Check if source type has 'fitsInto' rule
        Optional<Boolean> result = sourceScalar.scalar().fitsInto(targetScalar);
        if (result.isPresent()) {
            return result.get();
        }

        // Check if target type has 'stores' rule
        result = targetScalar.scalar().stores(sourceScalar);
        if (result.isPresent()) {
            return result.get();
        }

        SpecsLogs.warn("No fitting rules between types '" + sourceType + "' and '" + targetType
                + "'. Returning false.");
        return false;
        /*
        
        
        // Check if source type has 'fitsInto' rule
        targetScalar.
        
        return getScalar(sourceType).fitsInto(targetScalar);
        */
        // return getScalar(sourceType).fitsInto(targetScalar);
    }

    public static ScalarType cast(VariableType type) {
        return SpecsStrings.cast(type, ScalarType.class);
    }

    public static List<ScalarType> cast(List<VariableType> types) {
        List<ScalarType> scalarTypes = SpecsFactory.newArrayList();

        for (VariableType type : types) {
            scalarTypes.add(cast(type));
        }

        return scalarTypes;
    }

    public static boolean areScalar(List<VariableType> types) {
        return types.stream()
                .allMatch(ScalarUtils::isScalar);
    }

    /**
     * Helper method with variadic inputs.
     * 
     * @param types
     * @return
     */
    public static boolean areInteger(ScalarType... types) {
        return areInteger(Arrays.asList(types));
    }

    /**
     * 
     * @param types
     * @return true if all the given types are integers
     */
    public static boolean areInteger(List<ScalarType> types) {
        for (ScalarType type : types) {
            if (!type.scalar().isInteger()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Helper method with variadic inputs.
     * 
     * @param types
     * @return
     */
    public static List<Integer> getUnsigned(ScalarType... types) {
        return getUnsigned(Arrays.asList(types));
    }

    /**
     * 
     * @param types
     * @return a list of indexes of unsigned types
     */
    public static List<Integer> getUnsigned(List<ScalarType> types) {
        List<Integer> unsignedIndexes = SpecsFactory.newArrayList();

        for (int i = 0; i < types.size(); i++) {
            ScalarType type = types.get(i);
            if (type.scalar().isUnsigned()) {
                unsignedIndexes.add(i);
            }
        }

        return unsignedIndexes;
    }

    /**
     * If two types have the same fitness, priority is given to the first in the list.
     * 
     * @param scalarTypes
     * @return the type which best fits the given types, or null if no such type could be found. Also returns null if
     *         the list is empty
     */
    @SuppressWarnings("unchecked")
    public static <T extends ScalarType> T getMaxRank(List<T> scalarTypes) {
        if (scalarTypes.isEmpty()) {
            return null;
        }

        T bestFit = scalarTypes.get(0);
        for (int i = 1; i < scalarTypes.size(); i++) {
            T currentType = scalarTypes.get(i);

            // If current type is greater than current best fit, replace
            if (currentType.compareTo(bestFit) > 0) {
                bestFit = currentType;
            }
        }

        return (T) bestFit.scalar().removeConstant();
    }

    public static boolean isUnsigned(VariableType numericType) {
        return getScalar(numericType).isUnsigned();
    }

    public static VariableType setBits(VariableType type, int bits) {
        return getScalar(type).setBits(bits);
    }

    /**
     * @param inputType
     * @return true if it is a float of single precision (<=32 bits)
     */
    public static boolean isSinglePrecision(VariableType inputType) {
        if (!isScalar(inputType)) {
            return false;
        }

        Scalar scalar = getScalar(inputType);

        if (scalar.isInteger()) {
            return false;
        }

        if (scalar.getBits() > 32) {
            return false;
        }

        // Should return true only if num bits equals 32?
        return true;
    }

    public static Number getConstant(VariableType type) {
        return getScalar(type).getConstant();
    }

    /**
     * If all scalar types have a constant, returns a list of strings representing the constants. Otherwise, returns
     * null.
     * 
     * @param types
     * @return
     */
    public static <T extends ScalarType> List<String> getConstantStrings(List<T> types) {
        List<String> constants = SpecsFactory.newArrayList();

        for (T type : types) {
            // Check if scalar
            // if (!isScalar(type)) {
            // return null;
            // }

            // Scalar scalar = getScalar(type);
            String constantString = type.scalar().getConstantString();

            if (constantString == null) {
                return null;
            }

            constants.add(constantString);
        }

        return constants;
    }

    /**
     * Helper method for Scalar.getConstantString. Throws exception if given type does not implement ScalarType.
     * 
     * @param type
     * @return a String representing the constant if the given type is a scalar type and has a constant assigned. If the
     *         given type is not a scalar type, or does not have a constant, returns null
     */
    public static String getConstantString(VariableType type) {
        return getScalar(type).getConstantString();
    }

    /**
     * Helper method for Scalar.hasConstant. Returns false if type does not have a constant
     * 
     * @param type
     * @return true if the given type has a constant
     */
    public static boolean hasConstant(VariableType type) {
        // Check if scalar
        if (!isScalar(type)) {
            return false;
        }

        return getScalar(type).hasConstant();
    }

    public static boolean hasConstant(Optional<VariableType> type) {
        return type.isPresent() && hasConstant(type.get());
    }

    /**
     * Propagate the constant in source type to target type.
     * 
     * @param targetType
     * @param sourceType
     * @return targetType with constant of sourceType
     */
    public static VariableType propagateConstant(VariableType sourceType, VariableType targetType) {

        // Perform propagation only if both are Scalar
        if (!isScalar(sourceType) || !isScalar(targetType)) {
            return targetType;
        }

        // Get constant
        String constant = getScalar(sourceType).getConstantString();

        // Set constant
        return getScalar(targetType).setConstantString(constant);
    }

    /**
     * Helper method for Scalar.isInteger. Throws exception if given type does not implement ScalarType.
     * 
     * @param type
     * @return
     */
    public static boolean isInteger(VariableType type) {
        return getScalar(type).isInteger();
    }

    public static VariableType setConstant(VariableType type, Number value) {
        return getScalar(type).setConstant(value);
    }

    public static VariableType removeConstant(VariableType type) {
        return getScalar(type).removeConstant();
    }

    /**
     * Returns true if the given type can be converted to scalar.
     * 
     * <p>
     * TODO: Check if this method case be removed when testing if a matrix has a scalar element (now it always has, by
     * design)
     * 
     * @param type
     * @return
     */
    public static boolean hasScalarType(VariableType sourceType) {
        // VariableType scalarType = sourceType.conversion().toScalarType();
        // VariableType scalarType = toScalar(sourceType);

        // return scalarType != null;

        if (sourceType instanceof ScalarType) {
            return true;
        }

        if (sourceType instanceof ToScalar) {
            return true;
        }

        return false;
    }

    public static ScalarType normalize(ScalarType leftHandType) {
        // TODO Auto-generated method stub
        return null;
    }
}
