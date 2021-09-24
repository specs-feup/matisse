/**
 * Copyright 2014 SPeCS.
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

package org.specs.MatlabToC.CodeBuilder.MatlabToCRules.Assignment;

import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ParenthesisNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ExpressionNode;

import pt.up.fe.specs.util.treenode.NodeInsertUtils;

/**
 * @author Joao Bispo
 *
 */
public class Normalization {

    public static MatlabNode normalize(MatlabNode token) {
	// 1. Replace Operator tokens with AccessCall tokens
	token = convertOperatorToCall(token);

	// 2. Remove parenthesis and expression tokens
	token = removeParAndExp(token);
	return token;
    }

    /**
     * @param token
     * @return
     */
    private static MatlabNode removeParAndExp(MatlabNode token) {
	// Always apply the conversion first to the children, so that the transformation happens bottom-up
	if (token.hasChildren()) {
	    token.getChildren().forEach(child -> removeParAndExp(child));
	}

	if (!(token instanceof ParenthesisNode) && !(token instanceof ExpressionNode)) {
	    return token;
	}

	// Check if only has one child
	if (token.getNumChildren() != 1) {
	    throw new RuntimeException("Expression or Parenthesis with more than one child:\n" + token);
	}

	MatlabNode child = token.getChildren().get(0);

	// If has parent, replace node with child
	if (token.hasParent()) {
	    NodeInsertUtils.replace(token, child);
	}

	return child;
    }

    /**
     * Converts all Operator tokens into AccessCall tokens.
     * 
     * @param token
     * @return
     */
    public static MatlabNode convertOperatorToCall(MatlabNode token) {

	// Always apply the conversion first to the children, so that the transformation happens bottom-up
	if (token.hasChildren()) {
	    token.getChildren().forEach(child -> convertOperatorToCall(child));
	}

	if (!(token instanceof OperatorNode)) {
	    return token;
	}

	OperatorNode opNode = (OperatorNode) token;
	List<MatlabNode> operands = opNode.getOperands();
	String name = opNode.getOp().getFunctionName();
	MatlabNode call = MatlabNodeFactory.newSimpleAccessCall(name, operands);

	// If token has parent, replace it
	if (opNode.hasParent()) {
	    NodeInsertUtils.replace(opNode, call);
	}

	// Return new token
	return call;
    }

}
