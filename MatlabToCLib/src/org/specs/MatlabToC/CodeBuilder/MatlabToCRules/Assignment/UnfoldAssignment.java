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

package org.specs.MatlabToC.CodeBuilder.MatlabToCRules.Assignment;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ExpressionNode;
import org.specs.MatlabToC.CodeBuilder.CodeBuilderUtils;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.CodeBuilder.MatToMatRules.UnfoldAssignWithTemp;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public class UnfoldAssignment {

    private static final Set<MatlabOperator> SAFE_OPERATORS;

    static {
	SAFE_OPERATORS = SpecsFactory.newHashSet(Arrays.asList(MatlabOperator.Addition, MatlabOperator.ElementWiseAnd,
		MatlabOperator.ElementWiseOr, MatlabOperator.Equal, MatlabOperator.GreaterThan,
		MatlabOperator.GreaterThanOrEqual, MatlabOperator.LeftDivision, MatlabOperator.LessThan,
		MatlabOperator.LessThanOrEqual, MatlabOperator.LogicalNegation, MatlabOperator.Multiplication,
		MatlabOperator.NotEqual, MatlabOperator.Power, MatlabOperator.RightDivision,
		MatlabOperator.ShortCircuitAnd, MatlabOperator.ShortCircuitOr, MatlabOperator.Subtraction,
		MatlabOperator.UnaryMinus, MatlabOperator.UnaryPlus));

    }

    private static final Set<String> SAFE_FUNCTIONS;

    static {
	SAFE_FUNCTIONS = SpecsFactory.newHashSet();

	UnfoldAssignment.SAFE_FUNCTIONS.add("sqrt");
    }

    private final MatlabToCFunctionData data;

    /**
     * @param data
     */
    public UnfoldAssignment(MatlabToCFunctionData data) {
	this.data = data;
    }

    /**
     * @param leftHand
     * @param rightHand
     * @return
     */
    public boolean check(MatlabNode leftHand, MatlabNode rightHand, CNode rightHandC) {

	// Check if there are matrix variables in both sides of the assignment, and the
	Set<String> matOnBothHands = UnfoldAssignWithTemp.getMatricesOnBothSides(leftHand, rightHand, data);
	if (matOnBothHands.isEmpty()) {
	    return false;
	}

	// Check if return type of right hand is a matrix (problem when multiple outputs?)
	if (!MatrixUtils.isMatrix(rightHandC.getVariableType())) {
	    return false;
	}

	// Check if affected matrix tokens are subject to an element operator
	List<IdentifierNode> ids = rightHand.getDescendantsAndSelf(IdentifierNode.class);

	// boolean onlyElementWise = true;
	for (IdentifierNode id : ids) {
	    // If not in set, skip
	    String varName = id.getName();
	    if (!matOnBothHands.contains(varName)) {
		continue;
	    }

	    // Check parent
	    boolean isSafe = checkParent(id);

	    // If parent is not a safe operation, return immediately
	    if (!isSafe) {
		return true;
	    }
	}

	return false;
    }

    /**
     * @param id
     * @return
     */
    private boolean checkParent(MatlabNode id) {
	MatlabNode parent = id.getParent();

	// Id might be inside an access call
	if (parent instanceof AccessCallNode) {
	    // Check if id is an access call, or is being called
	    int indexOfVar = parent.getChildren().indexOf(id);

	    // Is an access call, call function recursively with this token
	    if (indexOfVar == 0) {
		return checkParent(parent);
	    }

	    // Is being call by a function/variable. Get its name and check if it is safe.
	    String name = ((AccessCallNode) parent).getName();
	    return isSafe(name);
	}

	// Assume assignment is unsafe (e.g., SRmat(p+1:m+1, :) = SRmat(p:m, :);)
	/*
	if (parent.getType() == MTokenType.Assignment) {
	    return false;
	}
	*/
	// Reached the top level, assume unsafe
	if (parent instanceof StatementNode) {
	    return false;
	}

	// Skip Expression token
	if (parent instanceof ExpressionNode) {
	    return checkParent(parent);
	}

	// Skip Expression token
	if (parent instanceof OperatorNode) {
	    return isSafe(((OperatorNode) parent).getOp());
	}

	SpecsLogs.warn("Case not defined!:" + parent.getNodeName());
	return false;
    }

    /**
     * @param operator
     * @return
     */
    private static boolean isSafe(MatlabOperator operator) {
	return UnfoldAssignment.SAFE_OPERATORS.contains(operator);
    }

    /**
     * Checks if the given name is associated to a safe function.
     * 
     * @param name
     * @return
     */
    private static boolean isSafe(String name) {
	if (UnfoldAssignment.SAFE_FUNCTIONS.contains(name)) {
	    return true;
	}

	// TODO: Can use 'data' to check custom safe functions

	System.out.println("ASSUMING '" + name + "' is unsafe function.");
	return false;
    }

    /**
     * @param leftHand
     * @param rhExpression
     * @return
     */
    // public CToken unfold(MatlabToken leftHand, CToken rhExpression) {
    public CNode unfold(MatlabNode leftHand, MatlabNode rightHand) {
	String tempName = data.nextTempVarName();
	MatlabNode tempMat = MatlabNodeFactory.newIdentifier(tempName);

	// - create matlab for first statement
	AssignmentSt firstAssign = StatementFactory.newAssignment(tempMat, rightHand);

	// - call converter
	CNode firstInst = CodeBuilderUtils.matlabToC(firstAssign, data);

	/*
	// - create ctoken for first statement
	VariableType outputType = DiscoveryUtils.getVarType(rhExpression);
	CToken tempVar = CTokenFactory.newVariable(tempName, outputType);
	
	CToken firstInst = CTokenFactory.newAssignment(tempVar, rhExpression);
	
	// - add output name and type to data
	data.addVariableType(tempName, outputType);
	*/
	// - create matlab for second statement

	StatementNode secondAssign = StatementFactory.newAssignment(leftHand, tempMat);

	// - call converter
	CNode secondInst = CodeBuilderUtils.matlabToC(secondAssign, data);

	return CNodeFactory.newBlock(firstInst, secondInst);
    }

}
