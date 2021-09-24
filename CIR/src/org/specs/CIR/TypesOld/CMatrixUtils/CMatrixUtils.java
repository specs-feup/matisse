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

package org.specs.CIR.TypesOld.CMatrixUtils;

import org.specs.CIR.Types.TypeShape;

/**
 * @author Joao Bispo
 * 
 */
public class CMatrixUtils {

    /**
     * @param matrixData
     * @return
     */
    public static MatrixShape2D getShape2D(TypeShape shape) {

	// Check if it has at most 2 dimensions
	// if (shape.getNumDims() == null) {
	// throw new UnsupportedOperationException("MatrixShape not defined for a Matrix with more than 2 dimensions.");
	// }
	if (shape.getNumDims() > 2) {
	    throw new UnsupportedOperationException("MatrixShape not defined for a Matrix with more than 2 dimensions.");
	}

	// Check if empty
	if (shape.isKnownEmpty()) {
	    return new MatrixShape2D(0, 0);
	}

	// Check if vector
	if (shape.getNumDims() == 1) {
	    return new MatrixShape2D(1, shape.getDim(0));
	}

	// Parse matrix
	int rows = shape.getDim(0);
	int cols = shape.getDim(1);
	return new MatrixShape2D(rows, cols);
    }

}
