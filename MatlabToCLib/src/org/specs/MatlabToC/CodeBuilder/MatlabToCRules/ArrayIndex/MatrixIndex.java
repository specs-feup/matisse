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

package org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRFunctions.MatrixFunction;

/**
 * Index that uses the values of a matrix.
 * 
 * @author Joao Bispo
 * 
 */
public class MatrixIndex extends ArrayIndex {

    private static final String FUNCTION_VAR_PREFIX = "array_";

    private final CNode matrix;
    private final boolean subtractOne;
    private final ForNodes forFactory;

    /**
     * @param position
     */
    public MatrixIndex(int position, CNode matrix, boolean subtractOne, ForNodes forFactory) {
	super(position);

	this.matrix = matrix;
	this.subtractOne = subtractOne;
	this.forFactory = forFactory;
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getFor(java.util.List)
     */
    @Override
    public CNode getFor(List<CNode> forInstructions) {
	// for(index=0; i<numel(matrix); i++)
	CNode endValue = getEndValue();
	return forFactory.newForLoopBlock(getIndexVar(), endValue, forInstructions);
    }

    /**
     * @return
     */
    private CNode getEndValue() {
	MatrixType type = (MatrixType) matrix.getVariableType();
	CNode endValue = forFactory.getFunctionCall(type.matrix().functions().numel(), matrix);
	return endValue;
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getIndex()
     */
    @Override
    public CNode getIndex() {
	// matrix(index)
	CNode originalIndex = forFactory.getFunctionCall(MatrixFunction.GET, matrix, getIndexVar());

	if (!subtractOne) {
	    return originalIndex;
	}

	CNode numberOne = CNodeFactory.newCNumber(1);
	return forFactory.getFunctionCall(COperator.Subtraction, originalIndex, numberOne);
    }

    /**
     * Needs the matrix as input.
     */
    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getFunctionInputs()
     */
    @Override
    public List<CNode> getFunctionInputs() {
	return Arrays.asList(matrix);
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#convertToFunction()
     */
    @Override
    public ArrayIndex convertToFunction() {
	String fName = FUNCTION_VAR_PREFIX + getPosition();
	// VariableType fType = DiscoveryUtils.getVarTypeClean(matrix);
	VariableType fType = matrix.getVariableType().normalize();

	CNode fVar = CNodeFactory.newVariable(fName, fType);

	return new MatrixIndex(getPosition(), fVar, subtractOne, forFactory);
    }

    /**
     * Characterized by the matrix type, the position and if it needs to subtract one.
     */
    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getSmallId()
     */
    @Override
    public String getSmallId() {
	StringBuilder builder = new StringBuilder();

	builder.append("m");
	if (!subtractOne) {
	    builder.append("o");
	}
	builder.append(getPosition());
	builder.append(matrix.getVariableType().getSmallId());

	return builder.toString();
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getSize()
     */
    @Override
    public CNode getSize() {
	return getEndValue();
    }

}
