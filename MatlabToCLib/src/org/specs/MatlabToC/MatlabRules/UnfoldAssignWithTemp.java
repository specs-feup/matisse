/**
 * Copyright 2013 SPeCS Research Group.
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
import java.util.Set;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabIR.Processor.TreeTransformException;
import org.specs.MatlabIR.Processor.TreeTransformRule;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public class UnfoldAssignWithTemp implements TreeTransformRule {

    /* (non-Javadoc)
     * @see org.specs.MatlabIR.Processor.TreeTransformRule#check(org.specs.MatlabIR.MatlabToken)
     */
    // @Override
    private static boolean check(MatlabNode token) {
	// Check if assignment
	if (!(token instanceof AssignmentSt)) {
	    // if (!StatementUtils.checkStatementType(token, MStatementType.Assignment)) {
	    return false;
	}

	AssignmentSt assignment = (AssignmentSt) token;

	// Get identifiers from right hand and left hand
	MatlabNode leftHand = assignment.getLeftHand();
	Set<String> leftHandIds = getIdentifiers(leftHand);

	MatlabNode rightHand = assignment.getRightHand();
	Set<String> rightHandIds = getIdentifiers(rightHand);

	// Intersect the two sets, and check if it is not empty
	leftHandIds.retainAll(rightHandIds);
	if (leftHandIds.isEmpty()) {
	    return false;
	}

	System.out.println("POSSIBLE OPTIMIZATION:\n" + token.getCode());

	return false;
    }

    /**
     * @param token
     * @return
     */
    private static Set<String> getIdentifiers(MatlabNode token) {

	List<IdentifierNode> leftHandIds = token.getDescendantsAndSelf(IdentifierNode.class);

	Set<String> leftHandIdNames = SpecsFactory.newHashSet();
	for (IdentifierNode leftHandId : leftHandIds) {
	    String name = leftHandId.getName();
	    leftHandIdNames.add(name);
	}
	return leftHandIdNames;
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabIR.Processor.TreeTransformRule#apply(org.specs.MatlabIR.MatlabToken)
     */
    @Override
    public boolean apply(MatlabNode token) throws TreeTransformException {
	if (!check(token)) {
	    return false;
	}

	return true;
    }

}
