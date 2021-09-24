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
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SpaceNode;
import org.specs.MatlabProcessor.Tokenizer.TokenizerState.StateNodes;

/**
 * Detects the end of a for expression.
 * 
 * 
 * @author JoaoBispo
 *
 */
public class CatchState implements StatementDetector {

    private static final Set<Class<?>> EXPRESSION_NODES = new HashSet<>(Arrays.asList(AssignmentNode.class));

    private boolean insideCatch;
    private List<MatlabNode> currentTokens;
    private List<MatlabNode> nextStatementTokens;

    private String lastFalse;

    public CatchState() {
	clear();
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabProcessor.Tokenizer.ParserModes.StatementDetector#clear()
     */
    @Override
    public void clear() {
	insideCatch = false;
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

	// If not inside catch, check if valid now is
	if (!insideCatch) {
	    boolean isFirstNode = !stateNodes.hasNonSpaceToken();
	    notInCatch(currentNode, isFirstNode);
	    return false;
	}

	// If there is already one token, finish statement
	if (currentTokens.size() == 1) {
	    // If this node continues an expression, or is non-triggering, previous node is part of next
	    // statement, remove it from state nodes and add it nextStatementNodes.

	    if (EXPRESSION_NODES.contains(currentNode.getClass())
		    || NON_TRIGGER_NODES.contains(currentNode.getClass())) {
		// Remove nodes until non-space node
		nextStatementTokens = stateNodes.popUntilFirstNonSpace();
	    }

	    return true;
	}

	// First token after try, if not an identifier, this node is part of the next statement
	if (!(currentNode instanceof IdentifierNode)) {
	    return true;
	}

	// Found identifier, add to list
	currentTokens.add(currentNode);
	return false;

    }

    private void notInCatch(MatlabNode matlabNode, boolean isFirstStatementToken) {
	// Check if first 'catch'
	if (matlabNode instanceof ReservedWordNode && isFirstStatementToken) {
	    ReservedWord word = ((ReservedWordNode) matlabNode).getWord();
	    if (word == ReservedWord.Catch) {
		// Inside catch now
		insideCatch = true;
	    }
	    return;
	}
    }

    @Override
    public boolean isActive() {
	return insideCatch;
    }
}
