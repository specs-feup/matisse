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

package org.specs.MatlabProcessor.MatlabParser.Rules;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;

import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public class TokenRulesUtils {

    /**
     * 
     * @param tokens
     * @return
     */
    public static MatlabNode generateTreeWithPrecedencesV2(List<MatlabNode> tokens) {
	System.out.println("TOKENS:" + tokens);
	return null;
    }

    /**
     * 
     * @param tokens
     * @return
     * @author Remi Fradet
     */
    public static MatlabNode generateTreeWithPrecedences(List<MatlabNode> tokens) {

	// ************************************************** Init
	// The stack the operators will be put in
	// List<MatlabNode> operatorStack = new ArrayList<>();
	Deque<OperatorNode> operatorStack = new ArrayDeque<>();

	// The stack the tokens will be stocked in
	List<MatlabNode> outputQueue = new ArrayList<>();

	// ********************************* Fill the output stack
	// tokensTransformRule in the list, we are going to create a tree
	for (MatlabNode token : tokens) {

	    // If the token is an operator, we check the priority of this one
	    if (token instanceof OperatorNode) {
		OperatorNode operator = (OperatorNode) token;
		if (operator.getOp().getNumOperands() > 1 && !operatorStack.isEmpty()) {

		    // MatlabNode lastOperator = operatorStack.get(operatorStack.size() - 1);
		    OperatorNode lastOperator = operatorStack.peek();

		    while (!operatorStack.isEmpty()
			    // Precedence of token is greater than precedence of last
			    // token in the stack
			    && !hasHigherPriority(operator, lastOperator)) {
			// pop the last operator from the operator stack
			// lastOperator = operatorStack.remove(operatorStack.size() - 1);
			lastOperator = operatorStack.pop();

			// push it to the output stack
			outputQueue.add(lastOperator);

			// update last operator if not empty
			if (!operatorStack.isEmpty()) {
			    // lastOperator = operatorStack.get(operatorStack.size() - 1);
			    lastOperator = operatorStack.peek();
			}
		    }
		}

		operatorStack.push(operator);

	    }
	    // If it is an operand, we only add it to the output stack
	    else {

		outputQueue.add(token);
	    }

	}
	// System.out.println("OUTPUT QUEUE:" + outputQueue);
	// System.out.println("OUTPUT STACK:" + operatorStack);

	// System.out.println("Stack pop:" + operatorStack.pop());
	// System.out.println("Queue" + outputQueue.get(0));

	// Reverse and add the operator stack to the output one
	/*
		int lastIndexOperatorStack = operatorStack.size() - 1;
	
		for (int i = lastIndexOperatorStack; i >= 0; i--) {
		    MatlabNode matlabToken = operatorStack.remove(i);
		    outputQueue.add(matlabToken);
		}
	*/
	while (!operatorStack.isEmpty()) {
	    outputQueue.add(operatorStack.pop());
	}

	// System.out.println("OUTPUT QUEUE:" + outputQueue);

	// System.out.println("Post-fix:");
	// System.out.println(outputQueue);
	// *************** Pull from the stack / Generate the tree
	// System.out.println("HERE:\n" + outputQueue);
	MatlabNode neededToken = stackToTree(outputQueue);
	// System.out.println("NEEDED TOKEN:" + neededToken);
	// System.out.println("HERE2");
	if (neededToken == null) {
	    // A bug happened. Check it.
	    return null;
	}
	// System.out.println("After Post-fix:");
	// System.out.println(neededToken);
	return neededToken;
    }

    // --------------------------------------------------------------- secondary
    // Methods
    // ---------- ---------- static

    /**
     * <p>
     * return true if the token1 has a higher priority than the token2. Returns false if the priority of token2 is
     * higher or equal than token1.
     * </p>
     * 
     * 
     * @param token1
     * @param token2
     * @return
     * @author Remi Fradet
     */
    public static boolean hasHigherPriority(OperatorNode token1, OperatorNode token2) {

	// MatlabOperator operator1 = MatlabOperator.getOp(token1.getContentString());
	// MatlabOperator operator2 = MatlabOperator.getOp(token2.getContentString());
	// MatlabOperator operator1 = MatlabTokenContent.getOperator(token1);
	// MatlabOperator operator2 = MatlabTokenContent.getOperator(token2);

	return token1.getOp().getPriority() > token2.getOp().getPriority();

	// System.out.println(operator1 + " has higher priority than "+operator2+":"+result);

	// return result;

    }

    /**
     * 
     * @param tokens
     * @return
     * @author Remi Fradet
     */
    public static MatlabNode stackToTree(List<MatlabNode> tokens) {
	// System.out.println("TOKENS SIZE:" + tokens.size());

	if (tokens == null || tokens.size() == 0) {
	    SpecsLogs.warn("Check if this should happen.");
	    return null;
	}

	// We take the last item of the output stack
	MatlabNode token = tokens.remove(tokens.size() - 1);

	// If it is an operand
	if (!(token instanceof OperatorNode)) {
	    // The token is already ready
	    return token;
	}

	OperatorNode operatorNode = (OperatorNode) token;

	List<MatlabNode> children = new LinkedList<>();

	// Get the number of children the operator is meant to have
	MatlabOperator operator = operatorNode.getOp();
	int numOperand = operator.getNumOperands();

	// Build the children
	for (int i = 0; i < numOperand; i++) {

	    MatlabNode childToken = stackToTree(tokens);
	    if (childToken == null) {
		return null;
	    }
	    if (operator == MatlabOperator.Colon
		    && childToken instanceof OperatorNode
		    && ((OperatorNode) childToken).getOp() == MatlabOperator.Colon
		    && ((OperatorNode) childToken).getOperands().size() < 3) {
		// We've got a range or ranges
		// We want to parse 1:2:3:4 as (1:2:3):4, not ((1:2):3):4
		// So when we get this case we flatten the ranges.
		// However, we *only* flatten the range to get 3 range operands. Beyond that we
		// want to keep having ranges-of-ranges.
		children.addAll(0, childToken.getChildren());
	    } else {
		// Add at the head, so it is in reverse-order
		children.add(0, childToken);
	    }
	}

	token.setChildren(children);
	return token;
    }
}
