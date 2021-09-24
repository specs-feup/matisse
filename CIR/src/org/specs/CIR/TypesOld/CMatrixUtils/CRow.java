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

import java.util.List;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;

import pt.up.fe.specs.util.SpecsLogs;

/**
 * Represents a row of the CMatrix.
 * 
 * @author Joao Bispo
 * 
 */
class CRow {

    private static final TypeShape SCALAR_SHAPE = TypeShape.newInstance(1, 1);

    private List<CNode> rowElements;

    /**
     * @param rowElements
     */
    public CRow(List<CNode> rowElements) {
	this.rowElements = rowElements;
    }

    /**
     * @return the rowElements
     */
    public List<CNode> getElements() {
	return rowElements;
    }

    /**
     * Returns the shape of the row.
     * 
     * @return
     */
    public MatrixShape2D getShape() {
	// If there are no elements, the shape is 0x0
	if (rowElements.isEmpty()) {
	    return new MatrixShape2D(0, 0);
	}

	// Get the shape of the first element
	VariableType firstType = rowElements.get(0).getVariableType();
	MatrixShape2D firstShape = CMatrixUtils.getShape2D(getShape(firstType));

	// Shape of the row will have the same number of rows as all elements,
	// and
	// a sum of the columns.
	int totalRows = firstShape.getRows();
	int totalCols = firstShape.getCols();

	// Get the shape of each remaining element, check against first element
	for (int i = 1; i < rowElements.size(); i++) {
	    CNode element = rowElements.get(i);
	    // Get type
	    VariableType type = element.getVariableType();
	    // Get shape of type
	    // MatrixShape2D typeShape = CMatrixUtilsG.getShape2D(type);
	    MatrixShape2D typeShape = CMatrixUtils.getShape2D(getShape(type));

	    // Compare
	    if (typeShape.getRows() != totalRows) {
		SpecsLogs.msgInfo("Element '" + i + "' has different number of rows (" + typeShape.getRows()
			+ ") than the first element (" + totalRows + ").");
		return null;
	    }

	    // Accumulate
	    totalCols += typeShape.getCols();
	}

	return new MatrixShape2D(totalRows, totalCols);
    }

    private static TypeShape getShape(VariableType type) {
	if (ScalarUtils.isScalar(type)) {
	    return SCALAR_SHAPE;
	}

	return MatrixUtils.getShape(type);
    }

    /**
     * @return
     */
    public boolean areAllNumeric() {
	for (CNode element : rowElements) {
	    // Get type
	    VariableType type = element.getVariableType();

	    if (!ScalarUtils.hasScalarType(type)) {
		return false;
	    }
	}

	return true;
    }

    @Override
    public String toString() {
	return rowElements.toString();
    }

}
