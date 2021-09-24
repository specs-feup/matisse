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

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public class CMatrix {

    private List<CRow> rows;

    /**
     * @param rows
     */
    public CMatrix() {
	this.rows = SpecsFactory.newArrayList();
    }

    public void addRow(List<CNode> rowElements) {
	CRow row = new CRow(rowElements);
	rows.add(row);
    }

    public List<CNode> getRowElements(int index) {
	return rows.get(index).getElements();
    }

    /**
     * Returns a MatrixShape which the shape of the matrix.
     * 
     * <p>
     * For instance, if this is a 2x3 Matrix, returns a shape with 2 rows and 3 columns.
     * 
     * @return
     */
    public MatrixShape2D getShape() {
	// If there are no rows, the shape is 0x0
	if (rows.isEmpty()) {
	    return new MatrixShape2D(0, 0);
	}

	// Get the shape of the first element
	MatrixShape2D firstShape = rows.get(0).getShape();

	// Shape of the matrix row will have the same number of columns and
	// a sum of the rows.
	int totalRows = firstShape.getRows();
	int totalCols = firstShape.getCols();

	// Get the shape of each remaining element, check against first element
	for (int i = 1; i < rows.size(); i++) {
	    // Get shape of the row
	    MatrixShape2D typeShape = rows.get(i).getShape();

	    // Compare
	    if (typeShape.getCols() != totalCols) {
		if (totalCols == 0) {
		    totalCols = typeShape.getCols();
		} else {
		    SpecsLogs.msgInfo("Element '" + i + "' has different number of cols ("
			    + typeShape.getRows() + ") than previous elements (" + totalCols + ").");
		    return null;
		}
	    }

	    // Accumulate
	    totalRows += typeShape.getRows();
	}

	return new MatrixShape2D(totalRows, totalCols);
    }

    /**
     * @param cMatrix
     * @return
     */
    public boolean areAllNumeric() {
	for (CRow row : rows) {
	    if (!row.areAllNumeric()) {
		return false;
	    }
	}

	return true;
    }

    /**
     * Returns the number of rows of the Matrix.
     * 
     * <p>
     * A row can be bigger than one line, and all elements in rows have the same number of columns.
     * 
     * @return
     */
    public int numRows() {
	return rows.size();
    }

    /**
     * @return
     */
    /*
    public List<CRow> getRows() {
    return rows;
    }
    */
    @Override
    public String toString() {
	return "Num rows:" + rows.size() + "\nRows:" + rows;
    }
}
