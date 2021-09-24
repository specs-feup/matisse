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

package org.specs.MatlabToC.CodeBuilder.MatlabToCRules;

import java.util.List;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.TypesOld.CMatrixUtils.CMatrix;
import org.specs.CIR.TypesOld.CMatrixUtils.MatrixShape2D;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementProcessor.MatlabToCException;
import org.specs.MatlabToC.Functions.MatlabBuiltin;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Utility methods related to transformation of MATLAB tokens which can appear in expressions, to C tokens.
 * 
 * @author Joao Bispo
 * 
 */
public class ExpressionUtils {

    /**
     * Transforms a CMatrix into a CToken.
     * 
     * @param data
     * @param cMatrix
     * @return
     * @throws MatlabToCException
     */
    static CNode buildMatrixCreatorFunction(MatlabToCFunctionData data, CMatrix cMatrix) throws MatlabToCException {

	MatrixShape2D shape = cMatrix.getShape();

	// If empty matrix return function call to empty matrix
	// This function is a "marker function", it will be used later to
	// do transformation in the tree
	if (shape.isEmptyMatrix()) {
	    List<CNode> inputs = SpecsFactory.newArrayList();

	    return data.getFunctionCall(MatlabBuiltin.EMPTY_MATRIX.getName(), inputs);
	}

	// If only one row, build function row_<type>(arg1, arg2, ...)
	if (cMatrix.numRows() == 1) {
	    List<CNode> inputs = cMatrix.getRowElements(0);

	    return data.getFunctionCall(MatlabBuiltin.HORZCAT.getName(), inputs);
	}

	// If only one column, build function row_<type>(arg1, arg2, ...)
	if (shape.getCols() == 1) {
	    // Get column elements
	    List<CNode> columnElements = SpecsFactory.newArrayList();
	    for (int i = 0; i < cMatrix.numRows(); i++) {
		CNode firstRowElement = cMatrix.getRowElements(i).get(0);
		columnElements.add(firstRowElement);
	    }

	    return data.getFunctionCall(MatlabBuiltin.VERTCAT.getName(), columnElements);
	}

	// If more than one row, for each row build function row_<type>(arg1,
	// arg2, ...)
	// Then, pass functions as arguments of new function call to
	// col_<type>(f1, f2, ...)
	List<CNode> rowCalls = SpecsFactory.newArrayList();

	// Increment function call level
	data.incrementFunctionCallLevel();
	for (int i = 0; i < cMatrix.numRows(); i++) {
	    List<CNode> inputs = cMatrix.getRowElements(i);

	    CNode rowCall = data.getFunctionCall(MatlabBuiltin.HORZCAT.getName(), inputs);

	    // Add function to arguments
	    rowCalls.add(rowCall);
	}
	// Decrement function call level
	data.decrementFunctionCallLevel();

	// Add final function call
	return data.getFunctionCall(MatlabBuiltin.VERTCAT.getName(), rowCalls);
    }

}
