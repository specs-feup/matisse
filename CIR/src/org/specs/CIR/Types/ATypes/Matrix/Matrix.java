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

package org.specs.CIR.Types.ATypes.Matrix;

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;

/**
 * Matrix-related operations on a VariableType.
 * 
 * @author Joao Bispo
 * 
 */
public interface Matrix {

    /**
     * Instance providers for Matrix primitives.
     * 
     * @return
     * @deprecated Use MatrixType.functions() directly instead.
     */
    MatrixFunctions functions();

    /**
     * The number of elements in the matrix.
     * 
     * <p>
     * If the number of elements is not known at the moment of the method call, returns null.
     * 
     * @return
     */
    Integer getNumElements();

    /**
     * A Matrix is a view if it refers to data from other matrix (i.e. is not owner of the data).
     * 
     * <p>
     * It has implications, for instance, when deallocating a view, it should not deallocate the data, and the correct
     * method should be called.
     * 
     * @return
     */
    boolean isView();

    /**
     * Returns the MatrixType which represent a view of the current type. If the current type is already a view, the
     * result type will be the same as the current type.
     * 
     * @param isPointer
     * @return
     */
    MatrixType getView();

    /**
     * Returns the VariableType of the matrix elements.
     * 
     * @param matrixType
     * @return
     */
    // VariableType getElementType();
    ScalarType getElementType();

    /**
     * The shape of the matrix.
     * 
     * @return
     */
    TypeShape getShape();

    /**
     * Returns a copy of the MatrixType with the given matrix shape.
     * 
     * @return
     */
    MatrixType setShape(TypeShape shape);

    /**
     * Returns a copy of the MatrixType with the given element type.
     * 
     * @param elementType
     * @return
     */
    MatrixType setElementType(ScalarType elementType);

    /**
     * 
     * @return true, if the matrix type uses dynamically allocated memory (e.g., needs malloc)
     */
    boolean usesDynamicAllocation();
}
