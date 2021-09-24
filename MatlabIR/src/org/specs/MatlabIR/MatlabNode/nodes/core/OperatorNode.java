/**
 * Copyright 2015 SPeCS.
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

package org.specs.MatlabIR.MatlabNode.nodes.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * Literal representation of a MatLab operator (see class Base.MatlabLanguage.MatlabOperator).
 * 
 * <p>
 * The content is a String.
 * 
 * <p>
 * Each children is an operand.
 * 
 * @author JoaoBispo
 *
 */
public class OperatorNode extends MatlabNode {

    private final MatlabOperator op;

    OperatorNode(MatlabOperator op, Collection<MatlabNode> operands) {
	// super(op.getLiteral(), operands);
	super(operands);

	this.op = op;
    }

    OperatorNode(Object content, Collection<MatlabNode> children) {
	this(MatlabOperator.getOp((String) content), children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new OperatorNode(getOp(), Collections.emptyList());
    }

    public MatlabOperator getOp() {
	return op;
    }

    public List<MatlabNode> getOperands() {
	return getChildren();
    }

    public int getNumOperands() {
	return getOperands().size();
    }

    @Override
    public String getCode() {

	String operatorString = op.getMatlabString();

	// Add space between operator if binary
	if (op.getNumOperands() == 2 && op != MatlabOperator.Colon) {
	    operatorString = " " + operatorString + " ";
	}

	if (!hasChildren()) {
	    return operatorString;
	}

	// Get first operand
	String firstOperand = getChild(0).getCode();

	// Transpose special case
	if (op == MatlabOperator.Transpose || op == MatlabOperator.ComplexConjugateTranspose) {
	    return firstOperand + operatorString;
	}

	// Unary operator
	if (op.getNumOperands() == 1) {
	    return operatorString + firstOperand;
	}

	StringJoiner joiner = new StringJoiner(operatorString);
	joiner.add(firstOperand);
	getChildren().subList(1, getNumChildren())
		.forEach(child -> joiner.add(child.getCode()));

	return joiner.toString();
    }

    @Override
    public String toContentString() {
	return getOp().getLiteral();
    }

}
