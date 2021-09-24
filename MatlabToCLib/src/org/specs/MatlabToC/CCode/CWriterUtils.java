/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.MatlabToC.CCode;

import java.util.Map;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabIR.MatlabLanguage.NumericClassName;
import org.specs.MatlabToC.MatlabToCTypesUtils;

import com.jmatio.types.MLArray;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public class CWriterUtils {

    private static final Map<Integer, NumericClassName> MLARRAY_TO_NUMERIC_CLASS;
    static {
	MLARRAY_TO_NUMERIC_CLASS = SpecsFactory.newHashMap();

	MLARRAY_TO_NUMERIC_CLASS.put(MLArray.mxDOUBLE_CLASS, NumericClassName.DOUBLE);
	MLARRAY_TO_NUMERIC_CLASS.put(MLArray.mxSINGLE_CLASS, NumericClassName.SINGLE);
	MLARRAY_TO_NUMERIC_CLASS.put(MLArray.mxINT8_CLASS, NumericClassName.INT8);
	MLARRAY_TO_NUMERIC_CLASS.put(MLArray.mxINT16_CLASS, NumericClassName.INT16);
	MLARRAY_TO_NUMERIC_CLASS.put(MLArray.mxINT32_CLASS, NumericClassName.INT32);
	MLARRAY_TO_NUMERIC_CLASS.put(MLArray.mxINT64_CLASS, NumericClassName.INT64);
	MLARRAY_TO_NUMERIC_CLASS.put(MLArray.mxUINT8_CLASS, NumericClassName.UINT8);
	MLARRAY_TO_NUMERIC_CLASS.put(MLArray.mxUINT16_CLASS, NumericClassName.UINT16);
	MLARRAY_TO_NUMERIC_CLASS.put(MLArray.mxUINT32_CLASS, NumericClassName.UINT32);
	MLARRAY_TO_NUMERIC_CLASS.put(MLArray.mxUINT64_CLASS, NumericClassName.UINT64);
	MLARRAY_TO_NUMERIC_CLASS.put(MLArray.mxCHAR_CLASS, NumericClassName.CHAR);
    }

    /**
     * Gets the C type equivalent of the Matlab type of this variable.
     * 
     * @param variable
     * @return
     */
    /*
    public static NumericType getEquivalentCNumericType(MLArray variable) {

    switch (variable.getType()) {
    case MLArray.mxDOUBLE_CLASS:
        return NumericType.Double;
    case MLArray.mxSINGLE_CLASS:
        return NumericType.Float;
    case MLArray.mxINT8_CLASS:
        return NumericType.Int8;
    case MLArray.mxUINT8_CLASS:
        return NumericType.Uint8;
    case MLArray.mxINT16_CLASS:
        return NumericType.Int16;
    case MLArray.mxUINT16_CLASS:
        return NumericType.Uint16;
    case MLArray.mxINT32_CLASS:
        return NumericType.Int32;
    case MLArray.mxUINT32_CLASS:
        return NumericType.Uint32;
    case MLArray.mxINT64_CLASS:
        return NumericType.Int64;
    case MLArray.mxUINT64_CLASS:
        return NumericType.Uint64;
    default:
        throw new RuntimeException("Case not supported on 'getEquivalentCType': '" + variable.getType() + "'.");
    }
    }
    */

    /**
     * Gets the equivalent NumericClass of the Matlab type of this variable.
     * 
     * @param variable
     * @return
     */
    public static NumericClassName getEquivalentNumericClass(MLArray variable) {
	NumericClassName type = MLARRAY_TO_NUMERIC_CLASS.get(variable.getType());

	if (type == null) {
	    throw new RuntimeException("Case not supported on 'getEquivalentCType': '" + variable.getType() + "'.");
	}

	return type;
    }

    /**
     * @param type
     * @param aspectDefinitions
     * @return
     */
    public static VariableType getType(String variableName, NumericClassName type, TypesMap aspectDefinitions,
	    NumericFactory numerics) {

	// Check if there is a definition for the given variable
	VariableType aspectType = aspectDefinitions.getSymbol(variableName);
	if (aspectType == null) {
	    // return MatlabToCTypesG.getVariableType(type, numerics);
	    aspectType = MatlabToCTypesUtils.getVariableType(type, numerics);
	}

	return ScalarUtils.toScalar(aspectType);

	// Replace input type with aspect type
	// return aspectType;
    }

}
