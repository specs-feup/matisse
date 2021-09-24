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

package org.specs.MatlabProcessor.Tokenizer.TokenizerState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SpaceNode;

/**
 * The current nodes of the tokenizer.
 * 
 * @author JoaoBispo
 *
 */
public class StateNodes {

    private final List<MatlabNode> currentTokens;

    public StateNodes() {
	currentTokens = new ArrayList<>();
    }

    public List<MatlabNode> getNodes() {
	return currentTokens;
    }

    public void add(MatlabNode node) {
	// MatlabNode previousNonSpace = getLastNonSpaceNode();

	currentTokens.add(node);

	// Apply transformations that can be done directly on the tokens
	// transformTokens(previousNonSpace);
    }

    /*
        private void transformTokens(MatlabNode previousNonSpace) {
    	// Build field accesses
    	if(previousNonSpace instanceof FieldAccessSeparatorNode) {
    	    currentTokens
    	}

        }
    */
    public boolean addAll(Collection<? extends MatlabNode> nodes) {
	return currentTokens.addAll(nodes);
    }

    public boolean isEmpty() {
	return currentTokens.isEmpty();
    }

    public boolean hasNonSpaceToken() {
	return getLastNonSpaceNode() != null;
    }

    /**
     * @return the currentTokens
     */
    public MatlabNode getLastNode() {
	if (currentTokens.isEmpty()) {
	    return null;
	}

	return currentTokens.get(currentTokens.size() - 1);
    }

    public MatlabNode getLastNonSpaceNode() {
	for (int i = currentTokens.size() - 1; i >= 0; i--) {
	    MatlabNode currentNode = currentTokens.get(i);

	    if (currentNode instanceof SpaceNode) {
		continue;
	    }

	    return currentNode;
	}

	return null;

    }

    public long getNumNonSpaces() {
	return currentTokens.stream()
		.filter(node -> !(node instanceof SpaceNode))
		.count();
    }

    public void removeLastNonSpace() {
	for (int i = currentTokens.size() - 1; i >= 0; i--) {
	    if (currentTokens.get(i) instanceof SpaceNode) {
		continue;
	    }

	    // Remove and return
	    currentTokens.remove(i);
	    return;
	}

    }

    public List<MatlabNode> popUntilFirstNonSpace() {
	if (isEmpty()) {
	    return Collections.emptyList();
	}

	List<MatlabNode> popList = new ArrayList<>();

	for (int i = currentTokens.size() - 1; i >= 0; i--) {
	    MatlabNode node = currentTokens.remove(i);
	    popList.add(0, node);

	    // If node is not a space, stop
	    if (!(node instanceof SpaceNode)) {
		break;
	    }
	}

	return popList;
    }
}
