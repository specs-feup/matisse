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

package org.specs.MatlabToC.MatlabRules;

import java.util.List;

import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.ReservedWordNode;
import org.specs.MatlabIR.Processor.StatementRule;
import org.specs.MatlabIR.Processor.TreeTransformException;

import pt.up.fe.specs.util.treenode.NodeInsertUtils;

/**
 * Transforms a statement like:<br>
 * 
 * <pre>
 * <code>A = B(end);</code>
 * </pre>
 * 
 * <p>
 * Into:
 * 
 * <pre>
 * <code>A = B(numel(B));</code>
 * </pre>
 * 
 * @author Joao Bispo
 *
 */
public class EndToNumel implements StatementRule {

    private final MFunctionInfo info;

    public EndToNumel(MFunctionInfo info) {
	this.info = info;
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabIR.Processor.TreeTransformRule#apply(org.specs.MatlabIR.MatlabToken)
     */
    @Override
    public boolean apply(MatlabNode token) throws TreeTransformException {
	List<ReservedWordNode> reservedWords = token.getDescendantsAndSelf(ReservedWordNode.class);
	reservedWords.forEach(reservedWord -> processReservedWord(reservedWord));

	return true;
    }

    /**
     * @param reservedWord
     * @return
     */
    private void processReservedWord(ReservedWordNode reservedWord) {

	if (reservedWord.getWord() != ReservedWord.End) {
	    return;
	}

	// Bind identifier to 'end'
	String id = bindId(reservedWord);
	// MatlabToken parent = reservedWord.getParent();
	// Create access call to numel
	MatlabNode numelCall = MatlabNodeFactory.newSimpleAccessCall("numel", MatlabNodeFactory.newIdentifier(id));
	NodeInsertUtils.replace(reservedWord, numelCall);
    }

    /**
     * @param reservedWord
     * @return
     */
    private String bindId(ReservedWordNode reservedWord) {
	// Walk backwards until finding an access call with an identifier
	MatlabNode currentToken = reservedWord.getParent();
	while (currentToken != null) {
	    MatlabNode token = currentToken;
	    currentToken = token.getParent();

	    // Check if access call
	    // if (token.getType() != MType.AccessCall) {
	    if (!(token instanceof AccessCallNode)) {
		continue;
	    }

	    String accessCallName = ((AccessCallNode) token).getName();

	    // Check if is an id or a function
	    if (!info.getIds().contains(accessCallName)) {
		continue;
	    }

	    return accessCallName;
	}

	throw new RuntimeException("Could not find a parent access call for 'end'. Token:\n"
		+ reservedWord.getAncestorTry(StatementNode.class));
    }

}
