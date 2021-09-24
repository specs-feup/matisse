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

import org.specs.CIR.Exceptions.CirUnsupportedException;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.CommonFunctions;
import org.specs.CIR.Types.ATypes.Matrix.Functions.MatrixCopy;

/**
 * Instance providers for Matrix primitives.
 * 
 * @author Joao Bispo
 * 
 */
public interface MatrixFunctions extends CommonFunctions {

    /**
     * Instance returns an integer representing the number of elements in the given matrix.
     * 
     * <p>
     * Inputs:<br>
     * - The input matrix;
     * 
     * @return
     */
    default InstanceProvider numel() {
        throw new CirUnsupportedException(getClass());
    }

    /**
     * Instance returns the contents of a matrix in the specified index. The index can be represented with a variable
     * number of integers. This function expects 0-based indexing.
     * 
     * <p>
     * Inputs:<br>
     * - A matrix, from where the value will be read;<br>
     * - As many integers as the number of dimensions, specifying and index for each dimension. Also accepts a single
     * index. Assumes zero-based indexing;<br>
     * 
     * @return
     */
    default InstanceProvider get() {
        throw new CirUnsupportedException(getClass());
    }

    /**
     * Instance sets contents of a matrix in the specified index. The index can be represented with a variable number of
     * integers. This function expects 0-based indexing.
     * 
     * <p>
     * Inputs:<br>
     * - A matrix, whose values will be set;<br>
     * - As many integers as the number of dimensions, specifying and index for each dimension. Also accepts a single
     * index. Assumes zero-based indexing;<br>
     * - The element that will be set;<br>
     * 
     * @return
     */
    default InstanceProvider set() {
        throw new CirUnsupportedException(getClass());
    }

    /**
     * Instance performs an element-wise operation with the given matrices, returning a matrix.
     * 
     * <p>
     * Inputs:<br>
     * - A variable number of matrices (the same number as the number of function inputs);
     * 
     * @param function
     * @return
     */
    default InstanceProvider elementWise(FunctionInstance function) {
        throw new CirUnsupportedException(getClass());
    }

    /**
     * Instance returns a new matrix, with the shape indicated by the inputs. No guarantees are made about the contents
     * of the matrix (it might not be initialized). The type of the matrix is determined by the value in
     * ProviderData.getOutputType().
     * 
     * <p>
     * Inputs:<br>
     * - As many integers as the number of dimensions, specifying the size of each dimension. If only one integer is
     * passed, the function creates a row-vector (shape 1xN);<br>
     * 
     * @return
     */
    default InstanceProvider create() {
        throw new CirUnsupportedException(getClass());
    }

    /**
     * Instance returns a new matrix, with the shape indicated by the input matrix. No guarantees are made about the
     * contents of the matrix (it might not be initialized). The type of the matrix is determined by the type of the
     * input matrix.
     * 
     * <p>
     * Inputs:<br>
     * - A matrix, whose values of shape and types will be used;<br>
     * 
     * @return
     */
    default InstanceProvider createFromMatrix() {
        throw new CirUnsupportedException(getClass());
    }

    /**
     * Creates an instance that copies the elements of a matrix to another.
     * 
     * <p>
     * Only copies the elements, does not try to allocate memory.
     * 
     * <p>
     * Inputs: <br>
     * - A matrix, from where the value will be read;<br>
     * - A matrix, whose values will be set;<br>
     * 
     * @return
     */
    default InstanceProvider copy() {
        return (ProviderData data) -> new MatrixCopy(data).create();
    }

    /**
     * Returns the size of a dimension of a matrix. Uses zero-based indexing.
     * 
     * 
     * <p>
     * Inputs:<br>
     * - A matrix;<br>
     * - An integer with the index of the dimension;
     * 
     * @return
     */
    default InstanceProvider getDim() {
        throw new CirUnsupportedException(getClass());
    }

    /**
     * Returns the number of dimensions of a matrix.
     * 
     * <p>
     * A vector/column matrix returns 2 as the number of dimensions.
     * 
     * 
     * <p>
     * Inputs:<br>
     * - A matrix;<br>
     * 
     * @return
     */
    default InstanceProvider numDims() {
        throw new CirUnsupportedException(getClass());
    }

    /**
     * Returns a pointer to the first element element of the matrix.
     * 
     * <p>
     * This function must only be implemented if the matrix type stores data in a contiguous way in memory.
     * 
     * @return
     */
    default InstanceProvider data() {
        throw new CirUnsupportedException(getClass());
    }

    /**
     * Changes the shape of a matrix.
     * 
     * Inputs:<br>
     * - A matrix whose shape is to be changed;<br>
     * - A matrix with the shape;
     * 
     * @return
     */
    default InstanceProvider changeShape() {
        throw new CirUnsupportedException(getClass());
    }
}
