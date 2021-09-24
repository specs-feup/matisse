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
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatrixNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ParenthesisNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ReservedWordNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.AssignmentNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.CellStartNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ParenthesisStartNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SpaceNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SquareBracketsStartNode;
import org.specs.MatlabProcessor.Tokenizer.TokenizerState.StateNodes;

/**
 * Detects the end of a for expression.
 * 
 * 
 * @author JoaoBispo
 *
 */
public class ForState implements StatementDetector {

    private boolean validFor;
    private boolean insideExpression;
    private boolean unfinishedExpression;
    private int parenthesisLevel;
    private List<MatlabNode> currentTokens;

    private String lastFalse;

    public ForState() {
	clear();
    }

    @Override
    public void clear() {
	validFor = false;
	insideExpression = false;
	unfinishedExpression = false;
	parenthesisLevel = 0;
	currentTokens = new ArrayList<>();
    }

    @Override
    public String lastFalse() {
	return lastFalse;
    }

    // public boolean pushToken(MatlabNode currentNode, boolean isFirstStatementToken) {
    @Override
    public boolean pushToken(StateNodes stateNodes, MatlabNode currentNode) {

	// Ignore spaces
	if (currentNode instanceof SpaceNode) {
	    return false;
	}

	// If not inside condition, check if assignment after valid for
	if (!insideExpression) {
	    boolean isFirstStatementToken = !stateNodes.hasNonSpaceToken();
	    notInExpression(currentNode, isFirstStatementToken);
	    return false;
	}

	// Inside condition, increase counter
	currentTokens.add(currentNode);
	// numConditionNodes++;

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
	    // if (matlabNode instanceof OperatorNode) {
	    // notTriggerNode(matlabNode);
	    // System.out.println("CLASS:" + matlabNode.getClass());
	    lastFalse = "NON_TRIGGER_NODE";
	    // System.out.println("NON-TRIGGER");
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
	    // System.out.println("UNFINISHED PARENTHESIS");
	    unfinishedExpression = false;

	    return false;
	}

	/*
	if (matlabNode instanceof ParenthesisStartNode || matlabNode instanceof SquareBracketsStartNode) {
	    lastFalse = "PARENTESIS_START";
	    insideParenthesis = true;
	    return false;
	}
	*/

	// Check if not first token
	// if (numConditionNodes == 1) {
	if (currentTokens.size() == 1) {
	    lastFalse = "NOT_FIRST";
	    // System.out.println("NOT FIRST");
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

    private void notInExpression(MatlabNode matlabNode, boolean isFirstStatementToken) {
	// Check if first 'for'
	if (matlabNode instanceof ReservedWordNode && isFirstStatementToken) {
	    ReservedWord word = ((ReservedWordNode) matlabNode).getWord();
	    if (word == ReservedWord.For || word == ReservedWord.Parfor) {
		validFor = true;
	    }
	    return;
	}

	// Check if assignment inside 'for'
	if (!validFor) {
	    return;
	}

	// If parenthesis before assignment, disable
	if (matlabNode instanceof ParenthesisStartNode) {
	    clear();
	    return;
	}

	if (!(matlabNode instanceof AssignmentNode)) {
	    return;
	}

	// Next token will be inside a for expression
	insideExpression = true;

	return;
    }

    @Override
    public boolean isActive() {
	return insideExpression;
    }

}
