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

package pt.up.fe.specs.matisse.weaver.VariableTypes;

import java.util.List;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Utilities.TypeDecoder;
import org.specs.CIRTypes.Types.BaseTypes;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabAspects.MatlabAspects;
import org.specs.matisselib.types.DynamicCellType;

import pt.up.fe.specs.util.SpecsLogs;

/**
 * Parses the string representation of variable types
 * 
 * @author Joao Bispo
 * 
 */
public class TypesParser {

    private final TypeDecoder decoder;

    public TypesParser(NumericFactory numerics) {
        decoder = BaseTypes.newTypeDecode(numerics);
    }

    /**
     * @param setup
     * @param varTypeString
     * @return
     */
    public VariableType parse(String varTypeString) {
        return MatlabAspects.getTypeFromString(varTypeString, decoder);

    }

    public static String generate(VariableType type) {
        // if (type.getType() == CType.Numeric) {
        if (ScalarUtils.isScalar(type)) {
            // return LARA_NAMES.get(VariableTypeUtilsOld.getNumericType(type));
            String simpleType = type.code().getSimpleType();

            // Remove suffix '_t', in the case of StdInt
            if (simpleType.endsWith("_t")) {
                simpleType = simpleType.substring(0, simpleType.length() - 2);
            }

            return simpleType;
        }

        if (MatrixUtils.isMatrix(type)) {
            if (MatrixUtils.usesDynamicAllocation(type)) {
                String matrixType = generate(MatrixUtils.getElementType(type));
                return matrixType + "[]";
            }

            if (MatrixUtils.isStaticMatrix(type)) {
                String matrixType = generate(MatrixUtils.getElementType(type));
                List<Integer> shape = MatrixUtils.getShapeDims(type);

                StringBuilder builder = new StringBuilder();

                builder.append(matrixType);
                for (Integer size : shape) {
                    builder.append("[").append(size).append("]");
                }

                return builder.toString();
            }
        }

        if (type instanceof DynamicCellType) {
            return generate(((DynamicCellType) type).getUnderlyingType()) + "{}*";
        }

        SpecsLogs.warn("Could not decode type '" + type + "'");
        return "";
    }

    public TypeDecoder getDecoder() {
        return decoder;
    }

}
