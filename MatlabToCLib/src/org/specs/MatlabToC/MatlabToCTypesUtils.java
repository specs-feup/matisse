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

package org.specs.MatlabToC;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.specs.CIR.CirKeys;
import org.specs.CIR.CirUtils;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.Types.CTypeSizes;
import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;
import org.specs.CIR.Utilities.TypeDecoder;
import org.specs.CIRTypes.Types.BaseTypes;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.StdInd.StdIntFactory;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabIR.MatlabLanguage.NumericClassName;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Utility methods related with variable types.
 * 
 * @author Joao Bispo
 * 
 */
public class MatlabToCTypesUtils {

    private static Map<VariableType, NumericClassName> variableTypeToClassNameMap;
    private static Map<NumericClassName, VariableType> classNameToVariableTypeMap;

    static {

        // No problem in using DEFAULT_SIZES, the types here are only used as keys, which depend only on their
        // getSmallId()
        // NumericFactoryV2 factory = new NumericFactoryV2(CTypeSizes.DEFAULT_SIZES);
        NumericFactory factory = new NumericFactory(CTypeSizes.DEFAULT_SIZES);

        MatlabToCTypesUtils.variableTypeToClassNameMap = SpecsFactory.newHashMap();

        MatlabToCTypesUtils.variableTypeToClassNameMap.put(factory.newChar(), NumericClassName.CHAR);
        MatlabToCTypesUtils.variableTypeToClassNameMap.put(factory.newNumeric(CTypeV2.INT), NumericClassName.INT32);
        MatlabToCTypesUtils.variableTypeToClassNameMap.put(factory.newNumeric(CTypeV2.DOUBLE), NumericClassName.DOUBLE);
        MatlabToCTypesUtils.variableTypeToClassNameMap.put(factory.newNumeric(CTypeV2.FLOAT), NumericClassName.SINGLE);
        MatlabToCTypesUtils.variableTypeToClassNameMap.put(StdIntFactory.newInt8(), NumericClassName.INT8);
        MatlabToCTypesUtils.variableTypeToClassNameMap.put(StdIntFactory.newInt16(), NumericClassName.INT16);
        MatlabToCTypesUtils.variableTypeToClassNameMap.put(StdIntFactory.newInt32(), NumericClassName.INT32);
        MatlabToCTypesUtils.variableTypeToClassNameMap.put(StdIntFactory.newInt64(), NumericClassName.INT64);
        MatlabToCTypesUtils.variableTypeToClassNameMap.put(StdIntFactory.newUInt8(), NumericClassName.UINT8);
        MatlabToCTypesUtils.variableTypeToClassNameMap.put(StdIntFactory.newUInt16(), NumericClassName.UINT16);
        MatlabToCTypesUtils.variableTypeToClassNameMap.put(StdIntFactory.newUInt32(), NumericClassName.UINT32);
        MatlabToCTypesUtils.variableTypeToClassNameMap.put(StdIntFactory.newUInt64(), NumericClassName.UINT64);

        MatlabToCTypesUtils.classNameToVariableTypeMap = new EnumMap<>(NumericClassName.class);

        MatlabToCTypesUtils.classNameToVariableTypeMap.put(NumericClassName.INT8, StdIntFactory.newInt8());
        MatlabToCTypesUtils.classNameToVariableTypeMap.put(NumericClassName.INT16, StdIntFactory.newInt16());
        MatlabToCTypesUtils.classNameToVariableTypeMap.put(NumericClassName.INT32, StdIntFactory.newInt32());
        MatlabToCTypesUtils.classNameToVariableTypeMap.put(NumericClassName.INT64, StdIntFactory.newInt64());
        MatlabToCTypesUtils.classNameToVariableTypeMap.put(NumericClassName.UINT8, StdIntFactory.newUInt8());
        MatlabToCTypesUtils.classNameToVariableTypeMap.put(NumericClassName.UINT16, StdIntFactory.newUInt16());
        MatlabToCTypesUtils.classNameToVariableTypeMap.put(NumericClassName.UINT32, StdIntFactory.newUInt32());
        MatlabToCTypesUtils.classNameToVariableTypeMap.put(NumericClassName.UINT64, StdIntFactory.newUInt64());
    }

    private static VariableType getNumeric(NumericClassName className, NumericFactory numerics) {

        if (className == NumericClassName.DOUBLE) {
            return numerics.newDouble();
        }

        if (className == NumericClassName.SINGLE) {
            return numerics.newFloat();
        }

        if (className == NumericClassName.CHAR) {
            return numerics.newChar();
        }

        return null;
    }

    public static String getNumericType(VariableType numericType) {
        NumericClassName numericClassName = MatlabToCTypesUtils.getNumericClass(numericType);

        if (numericClassName == null) {
            throw new RuntimeException("Numeric class not defined for " + numericType);
        }

        return numericClassName.getMatlabString();
    }

    /**
     * Transforms a VariableType of type String into an equivalent VariableType of type NumericType
     * 
     * @param lastArg
     * @return
     */
    public static VariableType getNumericFromString(VariableType lastArg, NumericFactory numerics) {
        String string = StringTypeUtils.getString(lastArg);

        // Get Numeric class name
        NumericClassName numericClassName = NumericClassName.getNumericClassName(string);
        if (numericClassName != null) {
            return MatlabToCTypesUtils.getVariableType(numericClassName, numerics);
        }

        // Try to decode to a MATISSE type
        VariableType type = BaseTypes.newTypeDecode(numerics).decode(string);
        if (type != null) {
            return type;
        }

        throw new RuntimeException("Could not decode numeric class name '" + string + "'");
    }

    /**
     * 
     * @param typeString
     * @param decoder
     * @return
     */
    public static VariableType getType(String typeString, TypeDecoder decoder) {
        return decoder.decode(typeString);
    }

    /**
     * @param numeric
     * @return
     */
    public static NumericClassName getNumericClass(VariableType numeric) {
        numeric = ScalarUtils.toScalar(numeric);

        // Remove pointer information
        numeric = ReferenceUtils.getType(numeric, false);
        NumericClassName nClass = MatlabToCTypesUtils.variableTypeToClassNameMap.get(numeric);
        if (nClass == null) {
            throw new RuntimeException("Numeric class not defined for " + numeric);
        }

        return nClass;
    }

    /**
     * Maps MATLAB numeric class names to C variable types.
     * <p>
     * E.g., class name 'int32' maps to C 'int32_t'.
     * 
     * @param numeric
     * @return
     */
    public static VariableType getVariableType(NumericClassName className, NumericFactory numerics) {
        VariableType variableType = MatlabToCTypesUtils.classNameToVariableTypeMap.get(className);

        if (variableType != null) {
            return variableType;
        }

        // It can still be a numeric, for which we will need the NumericFactory
        variableType = getNumeric(className, numerics);

        if (variableType == null) {
            throw new RuntimeException("Variable type not defined for " + className);
        }

        return variableType;
    }

    /**
     * Calculates the element type for cases where the last input type can be a string with the MATLAB type.
     * 
     * <p>
     * Returns the first that applies:<br>
     * 
     * 1. Returns the type of the string, if last input is of type StringType; <br>
     * 2. Returns the output type in ProviderData, if defined, converted to scalar; <br>
     * 3. Returns the variable type defined by MatlabToCOption.DEFAULT_FLOAT, as a weak type; <br>
     * 
     * @param builderData
     * @return
     */
    public static VariableType getElementType(ProviderData builderData) {
        List<VariableType> inputTypes = builderData.getInputTypes();

        if (!inputTypes.isEmpty()) {
            VariableType lastType = inputTypes.get(inputTypes.size() - 1);

            // If last type is a string, decode it and return as element type
            if (StringTypeUtils.isString(lastType)) {

                // Get VariableType
                NumericFactory numerics = builderData.getNumerics();
                return getNumericFromString(lastType, numerics);
            }
        }

        // Check if output type is defined in ProviderData
        if (builderData.getOutputType() != null) {
            VariableType outputType = builderData.getOutputType();
            if (ScalarUtils.hasScalarType(outputType)) {
                return ScalarUtils.toScalar(outputType);
            }

        }

        // As last resort, return default float type, as a weak type
        VariableType type = builderData.getSettings().get(CirKeys.DEFAULT_REAL);

        if (CirUtils.useWeakTypes()) {
            return type.setWeakType(true);
        }

        return type;
    }

    /**
     * In MATLAB, the minimum number of dimensions is 2. Adjusts the number of dimensions accordingly.
     * 
     * <p>
     * If the matrix type is not fully defined, throws an exception.
     * 
     * @param matrix
     * @return
     */
    public static int getMatlabNumDims(MatrixType matrix) {
        if (!matrix.getTypeShape().isFullyDefined()) {
            throw new RuntimeException("Matrix is not fully defined: " + matrix);
        }

        // Use the size of the dimension list as the number of dimensions, so that a vector returns 2.
        int numDims = matrix.getTypeShape().getNumDims();

        // Adjust number of dimensions
        if (numDims < 2) {
            numDims = 2;
        }
        return numDims;
    }
}
