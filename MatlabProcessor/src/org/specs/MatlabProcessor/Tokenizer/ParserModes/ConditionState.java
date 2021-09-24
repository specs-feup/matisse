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

package org.specs.MatlabProcessor.Tokenizer.ParserModes;

import java.util.ArrayList;
import java.util.List;

import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CellNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.FieldAccessNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatrixNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ParenthesisNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ReservedWordNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.CellStartNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ParenthesisStartNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SpaceNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SquareBracketsStartNode;
import org.specs.MatlabProcessor.Tokenizer.TokenizerState.StateNodes;

/**
 * Detects the end of a condition (such as the ones used for while, if, etc).
 * 
 * 
 * @author JoaoBispo
 *
 */
public class ConditionState implements StatementDetector {

    private boolean insideCondition;
    private boolean unfinishedExpression;
    private int parenthesisLevel;
    private List<MatlabNode> currentTokens;

    private String lastFalse;

    public ConditionState() {
	clear();
    }

    @Override
    public void clear() {

	// numConditionNodes = 0;
	insideCondition = false;
	unfinishedExpression = false;
	parenthesisLevel = 0;
	currentTokens = new ArrayList<>();
    }

    @Override
    public String lastFalse() {
	return lastFalse;
    }

    public boolean isInsideCondition() {
	return insideCondition;
    }

    // public boolean pushToken(MatlabNode currentNode, boolean isFirstStatementToken) {
    @Override
    public boolean pushToken(StateNodes stateNodes, MatlabNode currentNode) {
	// Ignore spaces
	if (currentNode instanceof SpaceNode) {
	    return false;
	}

	// System.out.println("CURRENT NODE:" + currentNode);

	// If not inside condition, check if reserved word at the beginning of a statement
	if (!insideCondition) {
	    boolean isFirstStatementToken = !stateNodes.hasNonSpaceToken();
	    notInCondition(currentNode, isFirstStatementToken);
	    return false;
	}

	MatlabNode lastToken = null;
	if (!currentTokens.isEmpty()) {
	    lastToken = currentTokens.get(currentTokens.size() - 1);
	}
	// System.out.println("PREVIOUS NODE:" + lastToken);

	// Inside condition, increase counter
	currentTokens.add(currentNode);
	// numConditionNodes++;

	// If parenthesis level greater than 0, or previous token is a parenthesis, parenthesis tokens will not trigger
	// the end of the condition.
	// Consider only the parenthesis
	if (parenthesisLevel > 0) {
	    // || (isParenthesisEnd(lastToken) && isParenthesisStart(currentNode))) {
	    if (isParenthesisEnd(currentNode)) {
		parenthesisLevel--;
	    }

	    if (isParenthesisStart(currentNode)) {
		parenthesisLevel++;
	    }

	    return false;
	}

	// Check if it is a node that will not trigger the end of a condition
	if (StatementDetector.NON_TRIGGER_NODES.contains(currentNode.getClass())) {

	    unfinishedExpression = true;
	    // if (matlabNode instanceof OperatorNode) {
	    // notTriggerNode(matlabNode);
	    // System.out.println("CLASS:" + matlabNode.getClass());
	    lastFalse = "NON_TRIGGER_NODE";
	    // System.out.println("NON-TRIGGER");
	    return false;
	}

	// Treat parenthesis start
	if (isParenthesisStart(currentNode)) {
	    if ((isValidBeforeOpenParenthesis(lastToken) && isOpenParenthesis(currentNode))
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
	    // System.out.println("UNFINISHED PARENTHESIS");
	    unfinishedExpression = false;

	    return false;
	}

	// Check if not first token
	// if (numConditionNodes == 1) {
	if (currentTokens.size() == 1) {
	    // System.out.println("NOT FIRST");
	    return false;
	}

	// Special case: last node is a cell, current node is a parenthesis start or a cell start
	if (lastToken instanceof CellNode
		&& (currentNode instanceof ParenthesisStartNode || currentNode instanceof CellStartNode)) {

	    parenthesisLevel++;

	    return false;
	}

	// System.out.println("FINISH IF");

	return true;
    }

    /*
    private boolean isPreviousToken(Class<?> aClass) {
    if (currentTokens.size() < 2) {
        return false;
    }
    
    return aClass.isInstance(currentTokens.get(currentTokens.size() - 2));
    }
    */

    private static boolean isParenthesisStart(MatlabNode matlabNode) {
	return matlabNode instanceof ParenthesisStartNode
		|| matlabNode instanceof SquareBracketsStartNode
		|| matlabNode instanceof CellStartNode;
    }

    private static boolean isParenthesisEnd(MatlabNode matlabNode) {
	return matlabNode instanceof ParenthesisNode
		|| matlabNode instanceof MatrixNode
		|| matlabNode instanceof CellNode;
	// || matlabNode instanceof FieldAccessNode
	// || matlabNode instanceof DynamicFieldAccessNode;
    }

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
	// System.out.println("INSIDE CONDITION");
	// Next token will be inside a condition
	insideCondition = true;
	return;
    }

    @Override
    public boolean isActive() {
	return insideCondition;
    }

    public List<MatlabNode> getCurrentNodes() {
	return currentTokens;
    }

    /**
     * 
     * @param node
     *            can be null
     * @return true, if the given node is an open parenthesis ( '(' or '{')
     */
    private static boolean isOpenParenthesis(MatlabNode node) {

	if (node instanceof ParenthesisStartNode || node instanceof CellStartNode) {
	    return true;
	}

	return false;
    }

    /**
     * 
     * @param node
     *            can be null
     * @return true, if the given node can appear just before an open parenthesis ( '(' or '{')
     */
    public static boolean isValidBeforeOpenParenthesis(MatlabNode node) {

	if (node instanceof IdentifierNode || node instanceof FieldAccessNode) {
	    return true;
	}

	return false;
    }
}
