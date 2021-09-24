/**
 *  Copyright 2012 SPeCS Research Group.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.specs.CIR.TypesOld.CMatrixUtils;

/**
 * The shape of a Matrix.
 * 
 * @author Joao Bispo
 *
 */
public class MatrixShape2D {

    private final int numRows;
    private final int numCols;
    
    /**
     * @param numRows
     * @param numCols
     */
    public MatrixShape2D(int numRows, int numCols) {
	this.numRows = numRows;
	this.numCols = numCols;
    }
    
    /**
     * @return the numCols
     */
    public int getCols() {
	return numCols;
    }
    
    /**
     * @return the numRows
     */
    public int getRows() {
	return numRows;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return numRows+"x"+numCols;
    }

    public boolean isEmptyMatrix() {
	if(numRows == 0) {
	    return true;
	}
	
	if(numCols == 0) {
	    return true;
	}
	
	return false;
    }
}
