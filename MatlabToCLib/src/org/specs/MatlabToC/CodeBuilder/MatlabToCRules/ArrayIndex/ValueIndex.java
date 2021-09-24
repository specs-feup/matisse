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

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;

/**
 * Single value index.
 * 
 * @author Joao Bispo
 * 
 */
public class ValueIndex extends ArrayIndex {

    private static final String FUNCTION_VAR_PREFIX = "value_";

    private final CNode expression;

    /**
     * @param position
     */
    public ValueIndex(int position, CNode expression) {
	super(position);

	this.expression = expression;

    }

    /**
     * Does not need a for, returns null.
     */
    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getFor()
     */
    @Override
    public CNode getFor(List<CNode> forInstructions) {
	return null;
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getIndex()
     */
    @Override
    public CNode getIndex() {
	return expression;
    }

    /**
     * The input is the value this indexes is based on.
     */
    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getFunctionInputs()
     */
    @Override
    public List<CNode> getFunctionInputs() {
	return Arrays.asList(expression);
    }

    /**
     * Replaces the value for a Variable with the same type and a name for the function.
     */
    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#convertToFunction()
     */
    @Override
    public ArrayIndex convertToFunction() {
	String fName = FUNCTION_VAR_PREFIX + getPosition();
	// VariableType fType = DiscoveryUtils.getVarTypeClean(expression);
	VariableType fType = expression.getVariableType().normalize();

	CNode fVar = CNodeFactory.newVariable(fName, fType);

	return new ValueIndex(getPosition(), fVar);
    }

    /**
     * Characterized by position and value type.
     */
    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getSmallId()
     */
    @Override
    public String getSmallId() {
	StringBuilder builder = new StringBuilder();

	builder.append("v");
	builder.append(getPosition());
	builder.append(expression.getVariableType().getSmallId());

	return builder.toString();
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getSize()
     */
    @Override
    public CNode getSize() {
	return CNodeFactory.newCNumber(1);
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex#getFunctionInputNames()
     */
    /*
    @Override
    public List<String> getFunctionInputNames() {
    return Arrays.asList("e_" + Integer.toString(getPosition()));
    }
    */

}
