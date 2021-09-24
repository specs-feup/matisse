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

package org.specs.MatlabProcessor.Tokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CellNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatrixNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ParenthesisNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.CellStartNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.FieldAccessSeparatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ParenthesisStartNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SpaceNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SquareBracketsStartNode;

/**
 * Detects the end of a condition (such as the ones used for while, if, etc).
 * 
 * 
 * @author JoaoBispo
 *
 */
public class ExpressionDetector {

    private final static Set<Class<?>> NON_TRIGGER_NODES = new HashSet<>(Arrays.asList(
	    OperatorNode.class, FieldAccessSeparatorNode.class));

    private boolean enableDetection;
    private boolean unfinishedExpression;
    private int parenthesisLevel;
    private List<MatlabNode> currentTokens;

    private String lastFalse;

    public ExpressionDetector() {
	clear();
    }

    public void clear() {
	enableDetection = false;
	unfinishedExpression = false;
	parenthesisLevel = 0;
	currentTokens = new ArrayList<>();
    }

    public String lastFalse() {
	return lastFalse;
    }

    public boolean pushToken(MatlabNode currentNode, boolean isFirstStatementToken) {
	if (!enableDetection) {
	    return false;
	}

	// Ignore spaces
	if (currentNode instanceof SpaceNode) {
	    return false;
	}

	// If not inside condition, check if reserved word at the beginning of a statement
	/*
		if (!enableDetection) {
		    notInCondition(currentNode, isFirstStatementToken);
		    return false;
		}
	*/
	// Inside condition, increase counter
	currentTokens.add(currentNode);

	// If parenthesis level greater than 0, or previous token is a parenthesis, parenthesis tokens will not trigger
	// the end of the condition.
	// Consider only the parenthesis
	if (parenthesisLevel > 0) {
	    if (isParenthesisEnd(currentNode)) {
		parenthesisLevel--;
	    }

	    if (isParenthesisStart(currentNode)) {
		parenthesisLevel++;
	    }

	    return false;
	}

	// Check if it is a node that will not trigger the end of a condition
	if (NON_TRIGGER_NODES.contains(currentNode.getClass())) {
	    unfinishedExpression = true;
	    lastFalse = "NON_TRIGGER_NODE";
	    return false;
	}

	// Treat parenthesis start
	if (isParenthesisStart(currentNode)) {
	    // If last token is an identifier
	    if (isPreviousToken(IdentifierNode.class)
		    // If is the first expression
		    || (currentTokens.size() == 1)
		    // If unfinished
		    || unfinishedExpression) {

		// Unset unfinished expression
		if (unfinishedExpression) {
		    unfinishedExpression = false;
		}

		parenthesisLevel++;
		return false;
	    }
	}

	// A triggering node reached here, unfinished expression becomes finished
	if (unfinishedExpression) {
	    lastFalse = "UNFINISHED_EXPRESSION";
	    unfinishedExpression = false;

	    return false;
	}

	// Check if not first token
	if (currentTokens.size() == 1) {
	    lastFalse = "NOT_FIRST";
	    return false;
	}

	return true;
    }

    private boolean isPreviousToken(Class<?> aClass) {
	if (currentTokens.size() < 2) {
	    return false;
	}

	return aClass.isInstance(currentTokens.get(currentTokens.size() - 2));
    }

    private static boolean isParenthesisStart(MatlabNode matlabNode) {
	return matlabNode instanceof ParenthesisStartNode
		|| matlabNode instanceof SquareBracketsStartNode
		|| matlabNode instanceof CellStartNode;
    }

    private static boolean isParenthesisEnd(MatlabNode matlabNode) {
	return matlabNode instanceof ParenthesisNode
		|| matlabNode instanceof MatrixNode
		|| matlabNode instanceof CellNode;
    }

    /*
    private void notInCondition(MatlabNode matlabNode, boolean isFirstStatementToken) {
    if (!(matlabNode instanceof ReservedWordNode)) {
        return;
    }

    ReservedWord word = ((ReservedWordNode) matlabNode).getWord();

    if (!word.hasConditional()) {
        return;
    }

    // Check if the first non-space token of the statement
    if (!isFirstStatementToken) {
        return;
    }

    // Next token will be inside a condition

    enableDetection = true;
    return;
    }
    */
}
