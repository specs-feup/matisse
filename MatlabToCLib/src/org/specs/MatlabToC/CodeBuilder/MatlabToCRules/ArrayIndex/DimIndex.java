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

import java.util.Collections;
import java.util.List;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRFunctions.MatrixFunction;

/**
 * Index that iterates over the dimension of a matrix.
 * 
 * @author Joao Bispo
 * 
 */
public class DimIndex extends ArrayIndex {

    private final CNode arrayVariable;
    private final ForNodes forFactory;
    private final boolean isOnlyIndex;

    public DimIndex(int position, CNode arrayVariable, boolean isOnlyIndex, ForNodes forFactory) {
	super(position);

	this.arrayVariable = arrayVariable;
	this.isOnlyIndex = isOnlyIndex;
	this.forFactory = forFactory;
    }

    /**
     * @param position
     */
    /*
    public ColonNotationIndex(int position, CToken arrayVariable, ForFactory forFactory) {
    this(position, arrayVariable, false, forFactory);
    }
    */

    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getFor()
     */
    @Override
    public CNode getFor(List<CNode> forInstructions) {
	CNode endValue = getEndValue();

	return forFactory.newForLoopBlock(getIndex(), endValue, forInstructions);
    }

    /**
     * @return
     */
    private CNode getEndValue() {
	// If only index, use numel
	if (isOnlyIndex) {
	    // Build call to numel
	    MatrixType type = (MatrixType) arrayVariable.getVariableType();
	    CNode numel = forFactory.getFunctionCall(type.matrix().functions().numel(), arrayVariable);
	    return numel;
	}

	// Otherwise, use size

	// Build call to size
	CNode sizeNumber = CNodeFactory.newCNumber(getPosition());
	CNode size = forFactory.getFunctionCall(MatrixFunction.DIM_SIZE, arrayVariable, sizeNumber);
	return size;

    }

    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getIndex()
     */
    @Override
    public VariableNode getIndex() {
	return getIndexVar();
    }

    /**
     * Does not need inputs, since it is done over the original matrix, which is already present.
     */
    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getFunctionInputs()
     */
    @Override
    public List<CNode> getFunctionInputs() {
	return Collections.emptyList();
    }

    /**
     * Create variable for input matrix, but with the name it will have inside the function.
     */
    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#convertToFunction()
     */
    @Override
    public ArrayIndex convertToFunction() {
	String fName = ArrayIndexUtils.getReadMatrixName();
	// VariableType fType = DiscoveryUtils.getVarTypeClean(arrayVariable);
	VariableType fType = arrayVariable.getVariableType().normalize();

	CNode fVar = CNodeFactory.newVariable(fName, fType);

	return new DimIndex(getPosition(), fVar, isOnlyIndex, forFactory);
    }

    /**
     * Characterized by the position and if is the only position.
     */
    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getSmallId()
     */
    @Override
    public String getSmallId() {
	StringBuilder builder = new StringBuilder();

	builder.append("d");

	if (!isOnlyIndex) {
	    builder.append(getPosition());
	}

	return builder.toString();
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getSize()
     */
    @Override
    public CNode getSize() {
	return getEndValue();
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getFunctionInputNames()
     */
    /*
    @Override
    public List<String> getFunctionInputNames() {
    return Collections.emptyList();
    }
    */

}
