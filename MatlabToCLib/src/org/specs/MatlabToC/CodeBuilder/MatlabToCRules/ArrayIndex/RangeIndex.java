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
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Types.VariableType;

/**
 * Represents an index that starts at one value and stops at another, inclusive, incrementing 'one' at each step.
 * 
 * @author Joao Bispo
 * 
 */
public class RangeIndex extends ArrayIndex {

    private static final String FUNCION_START_PREFIX = "start_";
    private static final String FUNCION_END_PREFIX = "end_";

    private final CNode startValue;
    private final CNode endValue;
    private final ForNodes forFactory;

    /**
     * @param position
     */
    public RangeIndex(int position, CNode startValue, CNode endValue, ForNodes forFactory) {

	super(position);

	this.startValue = startValue;
	this.endValue = endValue;

	this.forFactory = forFactory;
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getFor()
     */
    @Override
    public CNode getFor(List<CNode> forInstructions) {
	return forFactory.newForLoopBlock(getIndex(), startValue, endValue, COperator.LessThanOrEqual, forInstructions);
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getIndex()
     */
    @Override
    public VariableNode getIndex() {
	return getIndexVar();
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getFunctionInputs()
     */
    @Override
    public List<CNode> getFunctionInputs() {
	return Arrays.asList(startValue, endValue);
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#convertToFunction()
     */
    @Override
    public ArrayIndex convertToFunction() {
	String startName = RangeIndex.FUNCION_START_PREFIX + getPosition();
	VariableType startType = startValue.getVariableType();
	CNode startVar = CNodeFactory.newVariable(startName, startType);

	String endName = RangeIndex.FUNCION_END_PREFIX + getPosition();
	VariableType endType = endValue.getVariableType();
	CNode endVar = CNodeFactory.newVariable(endName, endType);

	return new RangeIndex(getPosition(), startVar, endVar, forFactory);
    }

    /**
     * Characterized by start and end types and position.
     */
    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getSmallId()
     */
    @Override
    public String getSmallId() {
	StringBuilder builder = new StringBuilder();

	builder.append("r");
	builder.append(getPosition());

	VariableType startType = startValue.getVariableType();
	VariableType endType = endValue.getVariableType();

	builder.append(startType.getSmallId());

	if (!startType.equals(endType)) {
	    builder.append(endType.getSmallId());
	}

	return builder.toString();
    }

    /**
     * End value - Start value + 1
     */
    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getSize()
     */
    @Override
    public CNode getSize() {
	CNode sub = forFactory.getFunctionCall(COperator.Subtraction, endValue, startValue);

	CNode one = CNodeFactory.newCNumber(1);
	return forFactory.getFunctionCall(COperator.Addition, sub, one);
    }

}
