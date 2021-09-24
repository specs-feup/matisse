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

package org.specs.MatlabToC.CodeBuilder.MatToMatRules;

import java.util.List;
import java.util.Set;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.Processor.TreeTransformException;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public class UnfoldAssignWithTemp implements MatlabToMatlabRule {

    /* (non-Javadoc)
     * @see org.specs.MatlabIR.Processor.TreeTransformRule#check(org.specs.MatlabIR.MatlabToken)
     */
    @Override
    public boolean check(MatlabNode token, MatlabToCFunctionData data) {

	// Check if assignment
	if (!(token instanceof AssignmentSt)) {
	    // if (!StatementUtils.checkStatementType(token, MStatementType.Assignment)) {
	    return false;
	}

	AssignmentSt assignment = (AssignmentSt) token;

	// Get identifiers from right hand and left hand
	MatlabNode leftHand = assignment.getLeftHand();
	// MatlabNode leftHand = StatementAccess.getAssignmentLeftHand(token);
	Set<String> leftHandIds = getIdentifiers(leftHand, data);

	MatlabNode rightHand = assignment.getRightHand();
	// MatlabNode rightHand = StatementAccess.getAssignmentRightHand(token);
	Set<String> rightHandIds = getIdentifiers(rightHand, data);

	// Intersect the two sets, and check if it is not empty
	leftHandIds.retainAll(rightHandIds);

	if (leftHandIds.isEmpty()) {
	    return false;
	}

	// System.out.println("POSSIBLE OPTIMIZATION 2:\n" + MatlabProcessorUtils.toMFile(token));

	return true;
    }

    public static Set<String> getMatricesOnBothSides(MatlabNode leftHand, MatlabNode rightHand,
	    MatlabToCFunctionData data) {

	// Get identifiers from right hand and left hand
	Set<String> leftHandIds = getIdentifiers(leftHand, data);

	Set<String> rightHandIds = getIdentifiers(rightHand, data);

	// Intersect the two sets, and check if it is not empty
	leftHandIds.retainAll(rightHandIds);

	return leftHandIds;
    }

    /**
     * @param token
     * @return
     */
    private static Set<String> getIdentifiers(MatlabNode token, MatlabToCFunctionData data) {

	List<IdentifierNode> leftHandIds = token.getDescendantsAndSelf(IdentifierNode.class);

	Set<String> leftHandIdNames = SpecsFactory.newHashSet();
	for (IdentifierNode leftHandId : leftHandIds) {
	    String name = leftHandId.getName();

	    // Check if identifier belogs to a matrix
	    VariableType type = data.getVariableType(name);
	    if (type == null) {
		continue;
	    }

	    if (!MatrixUtils.isMatrix(type)) {
		continue;
	    }

	    // System.out.println("ADDING "+name+ " -> "+type);

	    leftHandIdNames.add(name);
	}

	return leftHandIdNames;
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabIR.Processor.TreeTransformRule#apply(org.specs.MatlabIR.MatlabToken)
     */
    @Override
    public List<MatlabNode> apply(MatlabNode token, MatlabToCFunctionData data) throws TreeTransformException {

	// At this point, we know that token is an AssignmentSt
	AssignmentSt assignment = (AssignmentSt) token;

	// StatementData statementData = CompatibilityUtils.getData(token);

	List<MatlabNode> statements = SpecsFactory.newArrayList();

	MatlabNode leftHand = assignment.getLeftHand();
	MatlabNode rightHand = assignment.getRightHand();

	MatlabNode tempVar = MatlabNodeFactory.newIdentifier(data.nextTempVarName());

	// Create 'temp = rightHand'
	MatlabNode firstAssign = StatementFactory.newAssignment(assignment.getData(), tempVar, rightHand);

	// Create 'leftHand = temp'
	MatlabNode secondAssign = StatementFactory.newAssignment(assignment.getData(), leftHand, tempVar);

	statements.add(firstAssign);
	statements.add(secondAssign);
	System.out.println("BEFORE:");
	System.out.println(token.getCode());
	System.out.println("AFTER:");
	System.out.println(firstAssign.getCode());
	System.out.println(secondAssign.getCode());
	// System.out.println("AFTER:\n"+statements);
	return statements;
    }

}
