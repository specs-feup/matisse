/**
 * Copyright 2015 SPeCS.
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

package org.specs.matisselib.typeinference;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.Scalar;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.CIRTypes.Types.StdInd.StdIntType;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.matisselib.types.DynamicCellType;
import org.specs.matisselib.types.MatlabElementType;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class TypeCombiner {
    private TypeCombiner() {
    }

    public static Optional<VariableType> getCombinedVariableType(VariableType defaultReal,
            List<VariableType> variableTypes) {

        VariableType candidateType = null;

        for (VariableType variableType : variableTypes) {
            if (candidateType == null) {
                candidateType = variableType;
                continue;
            }

            if (variableType.pointer().isByReference() != candidateType.pointer().isByReference()) {
                // Refuse to combine pointers with non-pointers.
                // TODO: Is there a better way? Maybe we should force it to be a pointer?
                return Optional.empty();
            }

            if (candidateType instanceof ScalarType) {

                if (variableType instanceof ScalarType) {

                    ScalarType variableScalarType = (ScalarType) variableType;
                    Scalar variableScalar = variableScalarType.scalar();

                    ScalarType candidateScalarType = (ScalarType) candidateType;
                    Scalar candidateScalar = candidateScalarType.scalar();

                    if (!variableScalar.removeConstant().equals(candidateScalar.removeConstant())) {
                        return combineDifferentScalarTypes(defaultReal, variableScalarType, candidateScalarType);
                    }

                    if (!candidateScalar.hasConstant()
                            || candidateScalar.getConstant().equals(variableScalar.getConstant())) {
                        // Keep same type, constant information is correct
                    } else {
                        // Same type, different constant
                        candidateType = candidateScalar.removeConstant();
                    }

                    continue;
                }

                return Optional.empty();
            }

            if (candidateType instanceof StringType) {
                if (variableType instanceof StringType) {
                    StringType candidateStringType = (StringType) candidateType;
                    StringType variableStringType = (StringType) variableType;

                    if (!candidateStringType.getCharType().equals(variableStringType.getCharType())) {
                        return Optional.empty();
                    }

                    if (!candidateStringType.getString().equals(variableStringType.getString())) {
                        return Optional.empty();
                    }

                    continue;
                }

                return Optional.empty();
            }

            if (candidateType instanceof StaticMatrixType) {
                if (candidateType.equals(variableType)) {
                    continue;
                }

                return Optional.empty();
            }

            if (candidateType instanceof DynamicMatrixType) {
                if (!(variableType instanceof DynamicMatrixType)) {
                    return Optional.empty();
                }

                DynamicMatrixType candidateMatrixType = (DynamicMatrixType) candidateType;
                DynamicMatrixType variableMatrixType = (DynamicMatrixType) variableType;

                ScalarType candidateElementType = candidateMatrixType.getElementType();
                ScalarType variableElementType = variableMatrixType.getElementType();
                Optional<VariableType> combinedElementType = getCombinedVariableType(defaultReal,
                        Arrays.asList(candidateElementType, variableElementType));

                if (!combinedElementType.isPresent()) {
                    return Optional.empty();
                }

                // Forget shape information
                TypeShape newShape = candidateMatrixType
                        .getTypeShape()
                        .combineWith(variableMatrixType.getTypeShape());
                candidateType = DynamicMatrixType.newInstance(combinedElementType.get(), newShape);
                continue;
            }

            if (candidateType instanceof DynamicCellType) {
                if (!(variableType instanceof DynamicCellType)) {
                    return Optional.empty();
                }

                DynamicCellType candidateCellType = (DynamicCellType) candidateType;
                DynamicCellType variableCellType = (DynamicCellType) variableType;

                Optional<VariableType> combinedUnderlyingType = getCombinedVariableType(defaultReal,
                        Arrays.asList(candidateCellType.getUnderlyingType(), variableCellType.getUnderlyingType()));
                if (!combinedUnderlyingType.isPresent()) {
                    return Optional.empty();
                }

                TypeShape newShape = candidateCellType
                        .getTypeShape()
                        .combineWith(variableCellType.getTypeShape());
                candidateType = new DynamicCellType(combinedUnderlyingType.get(), newShape);
                continue;
            }

            throw new NotImplementedException(candidateType.getClass());
        }

        return Optional.ofNullable(candidateType);
    }

    private static Optional<VariableType> combineDifferentScalarTypes(VariableType defaultReal,
            ScalarType variableScalarType,
            ScalarType candidateScalarType) {

        if ((variableScalarType instanceof NumericTypeV2 || variableScalarType instanceof StdIntType) &&
                (candidateScalarType instanceof NumericTypeV2 || candidateScalarType instanceof StdIntType)) {

            Optional<VariableType> resultType = getDirectNumericCastType(
                    (ScalarType) variableScalarType,
                    (ScalarType) candidateScalarType);
            if (resultType.isPresent()) {
                return resultType;
            }
        }

        MatlabElementType type1 = getMatlabType(defaultReal, variableScalarType);
        MatlabElementType type2 = getMatlabType(defaultReal, candidateScalarType);

        if (type1 == null || type2 == null) {
            return Optional.empty();
        }

        if (type1.equals(type2)) {
            // TODO: Implement constant
            return Optional.of(type1.scalar().removeConstant());
        }

        // Can't combine
        return Optional.empty();
    }

    private static Optional<VariableType> getDirectNumericCastType(ScalarType type1,
            ScalarType type2) {

        // Attempt some fast&simple casts.

        if (type1.scalar().fitsInto(type2).orElse(false)) {
            return Optional.of(type2);
        }
        if (type2.scalar().fitsInto(type1).orElse(false)) {
            return Optional.of(type1);
        }

        if (type1.scalar().isInteger() && !type2.scalar().isInteger()) {
            return Optional.of(type2);
        }

        if (type2.scalar().isInteger() && !type1.scalar().isInteger()) {
            return Optional.of(type1);
        }

        return Optional.empty();
    }

    private static MatlabElementType getMatlabType(VariableType defaultReal, ScalarType type) {
        if (type instanceof NumericTypeV2 || type instanceof StdIntType) {
            return MatlabElementType.getDefaultNumberType(defaultReal);
        }
        if (type instanceof MatlabElementType) {
            return (MatlabElementType) type;
        }

        return null;
    }
}
