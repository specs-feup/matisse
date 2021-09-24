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

package org.specs.MatlabIR.Utilities;

import java.util.List;

import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;

/**
 * Parses the expression of a MATLAB 'for' token.
 * 
 * @author Joao Bispo
 * 
 */
public class ForInfo {

    public final MatlabNode startValue;
    public final MatlabNode endValue;
    public final MatlabNode increment;

    private ForInfo(MatlabNode startValue, MatlabNode endValue, MatlabNode increment) {
	this.startValue = startValue;
	this.endValue = endValue;
	this.increment = increment;
    }

    /**
     * Extracts information from a FOR expression.
     * 
     * @param forExpr
     * @return
     */
    public static ForInfo parseForExpression(MatlabNode forExpr) {

	// Assume token is an expression, get its child.
	// MatlabNode colonOp = MatlabTokenAccess.getExpressionChild(forExpr);
	MatlabNode expression = forExpr.normalizeExpr();

	if (!(expression instanceof OperatorNode)) {
	    throw new RuntimeException("Expected OperatorNode, found '" + expression.getNodeName() + "'");
	}

	OperatorNode colonOp = (OperatorNode) expression;

	if (colonOp.getOp() != MatlabOperator.Colon) {
	    throw new RuntimeException("Expected colon (:) operator, found '" + colonOp.getOp() + "'");
	}

	List<MatlabNode> colonOperands = colonOp.getOperands();

	// Get StartValue
	MatlabNode startValue = colonOperands.get(0);

	// Check if second operand is a colon operator
	boolean hasIncrement = colonOperands.size() == 3;

	MatlabNode endValue = null;
	MatlabNode increment = null;
	// boolean increase = true;
	if (hasIncrement) {
	    // Second Operand is a colon operator
	    increment = colonOperands.get(1);
	    endValue = colonOperands.get(2);
	} else {
	    // Use an increment of 1 as default
	    increment = MatlabNodeFactory.newNumber("1");
	    endValue = colonOperands.get(1);
	}

	return new ForInfo(startValue, endValue, increment);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();

	builder.append("Start:").append(startValue).append("\n");
	builder.append("End:").append(endValue).append("\n");
	builder.append("Increment:").append(increment).append("\n");

	return builder.toString();
    }
}
