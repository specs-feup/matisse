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

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;
import org.specs.MatlabIR.Processor.StatementRule;
import org.specs.MatlabIR.Processor.TreeTransformException;

/**
 * @author Joao Bispo
 *
 */
public class InfoCollector implements StatementRule {

    private final MFunctionInfo info;

    public InfoCollector() {
	info = new MFunctionInfo();
    }

    /**
     * @return the info
     */
    public MFunctionInfo getInfo() {
	return info;
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabIR.Processor.TreeTransformRule#apply(org.specs.MatlabIR.MatlabToken)
     */
    @Override
    public boolean apply(MatlabNode token) throws TreeTransformException {
	// Check if statement
	if (!(token instanceof StatementNode)) {
	    return false;
	}

	collectIdFunctionDeclaration(token);
	collectIdAssignment(token);

	return true;
    }

    /**
     * @param token
     */
    private void collectIdFunctionDeclaration(MatlabNode token) {
	if (!(token instanceof FunctionDeclarationSt)) {
	    return;
	}

	info.getIds().addAll(((FunctionDeclarationSt) token).getInputNames());
	info.getIds().addAll(((FunctionDeclarationSt) token).getOutputNames());
    }

    /**
     * Stores IDs that appear on the left hand of an assignment
     * 
     * @param token
     */
    private void collectIdAssignment(MatlabNode token) {
	if (!(token instanceof AssignmentSt)) {
	    return;
	}

	MatlabNode leftHand = ((AssignmentSt) token).getLeftHand();
	List<IdentifierNode> identifiers = leftHand.getDescendantsAndSelf(IdentifierNode.class);

	identifiers.forEach(id -> info.getIds().add(id.getName()));
    }
}
