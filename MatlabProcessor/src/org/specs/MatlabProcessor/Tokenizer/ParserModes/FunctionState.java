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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ReservedWordNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.AssignmentNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.FieldAccessSeparatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ParenthesisStartNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SpaceNode;
import org.specs.MatlabProcessor.Tokenizer.TokenizerState.StateNodes;

/**
 * Detects the end of a function, for the case 'function <id> <body>'.
 * 
 * 
 * @author JoaoBispo
 *
 */
public class FunctionState implements StatementDetector {

    // private static final Set<Class<?>> EXPRESSION_NODES = new HashSet<>(Arrays.asList(AssignmentNode.class));

    private static final Set<Class<?>> VALID_NODES_AFTER_IDENTIFIER = new HashSet<>(Arrays.asList(
	    ParenthesisStartNode.class, AssignmentNode.class, FieldAccessSeparatorNode.class
	    ));

    private boolean insideFunction;
    private List<MatlabNode> currentTokens;
    private List<MatlabNode> nextStatementTokens;

    private String lastFalse;

    public FunctionState() {
	clear();
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabProcessor.Tokenizer.ParserModes.StatementDetector#clear()
     */
    @Override
    public void clear() {
	insideFunction = false;
	currentTokens = new ArrayList<>();
	nextStatementTokens = Collections.emptyList();
    }

    @Override
    public List<MatlabNode> getNextStatementTokens() {
	return nextStatementTokens;
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabProcessor.Tokenizer.ParserModes.StatementDetector#lastFalse()
     */
    @Override
    public String lastFalse() {
	return lastFalse;
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabProcessor.Tokenizer.ParserModes.StatementDetector#pushToken(org.specs.MatlabIR.MatlabNode.MatlabNode, boolean)
     */
    @Override
    public boolean pushToken(StateNodes stateNodes, MatlabNode currentNode) {

	// Ignore spaces
	if (currentNode instanceof SpaceNode) {
	    return false;
	}

	// If not inside function, check if valid now is
	if (!insideFunction) {
	    boolean isFirstNode = !stateNodes.hasNonSpaceToken();
	    notInFunction(currentNode, isFirstNode);
	    return false;
	}

	// If already more than one token, no more tests to check
	if (currentTokens.size() > 1) {
	    return false;
	}

	// If one identifier token and next node is not a parenthesis start, signal end of statement
	if (currentTokens.size() == 1) {
	    if (currentTokens.get(0) instanceof IdentifierNode
		    // && !(currentNode instanceof ParenthesisStartNode || currentNode instanceof AssignmentNode)) {
		    && !VALID_NODES_AFTER_IDENTIFIER.contains(currentNode.getClass())) {

		return true;
	    }
	}

	// Add to list
	currentTokens.add(currentNode);
	return false;

    }

    private void notInFunction(MatlabNode matlabNode, boolean isFirstStatementToken) {
	// Check if first 'catch'
	if (matlabNode instanceof ReservedWordNode && isFirstStatementToken) {
	    ReservedWord word = ((ReservedWordNode) matlabNode).getWord();
	    if (word == ReservedWord.Function) {
		// Inside function now
		insideFunction = true;
	    }
	    return;
	}
    }

    @Override
    public boolean isActive() {
	return insideFunction;
    }
}
