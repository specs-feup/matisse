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

package org.specs.CIR.Types.ATypes.Matrix;

import java.util.List;
import java.util.Optional;

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.TypesOld.MatrixUtils.MatrixImplementation;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsStrings;

public class MatrixUtils {

    /**
     * 
     * @param type
     * @return a Matrix, if the given type implements MatrixType. Otherwise, throws an exception
     */
    public static Matrix getMatrix(VariableType type) {
        MatrixType matrixType = SpecsStrings.cast(type, MatrixType.class);

        return matrixType.matrix();
    }

    public static MatrixType cast(VariableType type) {
        return SpecsStrings.cast(type, MatrixType.class);
    }

    public static MatrixType getView(VariableType tensorType) {
        return getMatrix(tensorType).getView();
    }

    public static boolean isView(VariableType matrixType) {
        // TODO: Check if should return false if not matrix type
        return getMatrix(matrixType).isView();
    }

    public static TypeShape getShape(VariableType matrixType) {
        return getMatrix(matrixType).getShape();
    }

    public static List<Integer> getShapeDims(VariableType matrixType) {
        return getShape(matrixType).getDims();
    }

    public static List<ScalarType> getElementTypes(List<VariableType> types) {
        List<ScalarType> elementTypes = SpecsFactory.newArrayList();

        for (VariableType type : types) {
            ScalarType elementType = MatrixUtils.getElementType(type);
            elementTypes.add(elementType);
        }

        return elementTypes;
    }

    public static boolean isMatrix(List<VariableType> types) {
        for (VariableType type : types) {
            if (!MatrixUtils.isMatrix(type)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the VariableType of the matrix elements.
     * 
     * <p>
     * If the given type is not a matrix, throws an exception.
     * 
     * @param matrixType
     * @return
     */
    public static ScalarType getElementType(VariableType matrixType) {
        return getMatrix(matrixType).getElementType();
    }

    /**
     * 
     * @param type
     * @return true if the given type implements MatrixType
     */
    public static boolean isMatrix(VariableType type) {
        return type instanceof MatrixType;
    }

    /**
     * * TODO: Replace by a 'usesDynamicAllocation' kind of function
     * 
     * @param type
     * @return true if the given type is a statically-declared matrix
     */
    public static boolean isStaticMatrix(VariableType type) {
        if (!MatrixUtils.isMatrix(type)) {
            return false;
        }

        // return getMatrix(type).getImplementation() == MatrixImplementation.DECLARED;
        return !getMatrix(type).usesDynamicAllocation();
    }

    public static boolean usesDynamicAllocation(Optional<VariableType> type) {
        return type.isPresent() && usesDynamicAllocation(type.get());
    }

    /**
     * 
     * @param type
     * @return true if type is a MatrixType, and uses dynamic allocation
     */
    public static boolean usesDynamicAllocation(VariableType type) {
        if (!MatrixUtils.isMatrix(type)) {
            return false;
        }

        // return getMatrix(type).getImplementation() == MatrixImplementation.ALLOCATED;
        return getMatrix(type).usesDynamicAllocation();
    }

    /**
     * 
     * @param matrixType
     * @return the implementation type of the matrix variable
     * @deprecated replace with isStatic/isDynamic/usesDynamicAllocation
     */
    @Deprecated
    public static MatrixImplementation getImplementation(VariableType matrixType) {

        // return getMatrix(matrixType).getImplementation();
        if (getMatrix(matrixType).usesDynamicAllocation()) {
            return MatrixImplementation.ALLOCATED;
        }

        return MatrixImplementation.DECLARED;
    }

    /**
     * The shape of the result matrix from the multiplication of input1 with input2.
     * 
     * @param input1
     * @param input2
     * @return
     */
    public static TypeShape getMultiplicationShape(MatrixType input1, MatrixType input2) {
        // Check if both have fully-defined shapes
        TypeShape shape1 = input1.matrix().getShape();
        TypeShape shape2 = input2.matrix().getShape();
        if (!shape1.isFullyDefined() || !shape2.isFullyDefined()) {
            // Return shape with 2 dims
            return TypeShape.newDimsShape(2);
        }

        // Get first size of first shape and second size of second shape
        return TypeShape.newInstance(shape1.getDims().get(0), shape2.getDims().get(1));
    }

    public static boolean isMatrix(Optional<VariableType> type) {
        return type.isPresent() && isMatrix(type.get());
    }

    public static boolean isKnownEmptyMatrix(VariableType type) {
        if (!isMatrix(type)) {
            return false;
        }

        return getShape(type).isKnownEmpty();
    }

    public static boolean isKnownEmptyMatrix(Optional<VariableType> type) {
        return type.isPresent() && isKnownEmptyMatrix(type.get());
    }

}
