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
import java.util.Optional;

import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNumberNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentSingleSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.Processor.StatementRule;
import org.specs.MatlabIR.Processor.TreeTransformException;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;
import org.specs.MatlabToC.MFunctions.RulesResource;
import org.specs.MatlabToC.SystemInfo.TemporaryNames;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.treenode.NodeInsertUtils;

/**
 * Transforms a statement like:<br>
 * 
 * <pre>
 * <code>U(m:n, j1) = X';</code>
 * </pre>
 * 
 * <p>
 * Into:
 * 
 * <pre>
 * <code>
 *  X_t = X';
 * 	for i=m:n
 * 		U(i, j1) = X_t(i-m+1);
 * 	end</code>
 * </pre>
 * 
 * 
 * @author Joao Bispo
 * 
 */
public class MultipleSetToFor implements StatementRule {

    private static class CheckResults {
	String matrixName;
	Integer colonIndex;
	List<MatlabNode> indexes;
	MatlabNode colonExpression;
	MatlabNode firstColonOperand;
	MatlabNode rightHand;

	public CheckResults(String matrixName, Integer colonIndex, List<MatlabNode> indexes,
		MatlabNode colonExpression,
		MatlabNode firstColonOperand, MatlabNode rightHand) {

	    this.matrixName = matrixName;
	    this.colonIndex = colonIndex;
	    this.indexes = indexes;
	    this.colonExpression = colonExpression;
	    this.firstColonOperand = firstColonOperand;
	    this.rightHand = rightHand;
	}

    }

    private static Optional<CheckResults> check(MatlabNode token) {

	// Check if assignment
	if (!(token instanceof AssignmentSt)) {
	    // if (!StatementUtils.checkStatementType(token, MStatementType.Assignment)) {
	    return Optional.empty();
	}

	// Check if on the left side there is an access call
	MatlabNode leftHand = ((AssignmentSt) token).getLeftHand();
	// MatlabNode leftHand = StatementAccess.getAssignmentLeftHand(token);
	// if (leftHand.getType() != MType.AccessCall) {
	if (!(leftHand instanceof AccessCallNode)) {
	    return Optional.empty();
	}

	AccessCallNode leftHandCall = (AccessCallNode) leftHand;
	List<MatlabNode> arguments = leftHandCall.getArguments();

	// Check which indexes of the arguments are expressions with colon operator
	List<Integer> colonIndexes = SpecsFactory.newArrayList();

	OperatorNode colonExpression = null;
	MatlabNode firstColonOperand = null;
	for (int i = 0; i < arguments.size(); i++) {

	    MatlabNode argument = arguments.get(i);

	    // Check if expression
	    /*
	    if (argument.getType() != MType.Expression) {
	    continue;
	    }
	    */
	    MatlabNode exprOp = argument.normalizeExpr();

	    // This use of ExpressionNode is safe to remove after there are no expression nodes
	    /*
	    if (argument instanceof ExpressionNode) {
	    exprOp = MatlabTokenAccess.getExpressionChild(argument);
	    }
	    */

	    // Check if operator
	    if (!(exprOp instanceof OperatorNode)) {
		// if (exprOp.getType() != MType.Operator) {
		continue;
	    }

	    OperatorNode opNode = (OperatorNode) exprOp;
	    // Check if colon
	    if (opNode.getOp() != MatlabOperator.Colon) {
		continue;
	    }

	    colonExpression = opNode;

	    // Collect first colon operand
	    firstColonOperand = colonExpression.getOperands().get(0);

	    // For now, only accept MATLAB numbers
	    if (!(firstColonOperand instanceof MatlabNumberNode)) {
		return Optional.empty();
	    }

	    colonIndexes.add(i);
	}

	if (colonIndexes.size() > 1) {
	    return Optional.empty();
	}

	if (colonIndexes.size() != 1) {
	    return Optional.empty();
	}

	// Save indexes
	Integer colonIndex = colonIndexes.get(0);
	List<MatlabNode> indexes = SpecsFactory.newArrayList(arguments);
	String matrixName = leftHandCall.getName();
	MatlabNode rightHand = ((AssignmentSt) token).getRightHand();

	return Optional.of(new CheckResults(matrixName, colonIndex, indexes, colonExpression, firstColonOperand,
		rightHand));
    }

    @Override
    public boolean apply(MatlabNode node) throws TreeTransformException {

	Optional<CheckResults> stateOptional = check(node);
	if (!stateOptional.isPresent()) {
	    return false;
	}

	AssignmentSt assignment = (AssignmentSt) node;

	CheckResults state = stateOptional.get();

	// System.out.println("MULTIPLE SET TO FOR");
	String template = SpecsIo.getResource(RulesResource.MULTIPLE_SET_TO_FOR);

	template = template.replace("<TEMP_NAME>", TemporaryNames.getTemporaryName());

	String rightHandString = state.rightHand.getCode();
	template = template.replace("<RIGHT_HAND>", rightHandString);

	String colonExprString = state.colonExpression.getCode();
	template = template.replace("<COLON_EXPRESSION>", colonExprString);

	String colonOperandString = state.firstColonOperand.getCode();
	template = template.replace("<FIRST_COLON_OPERAND>", colonOperandString);

	// Replace colon index for matisse index
	MatlabNode matisseIndex = MatlabNodeFactory.newIdentifier(RulesResource.getMatisseIndexName());
	state.indexes.set(state.colonIndex, matisseIndex);

	// Build access call
	MatlabNode accessCall = MatlabNodeFactory.newSimpleAccessCall(state.matrixName, state.indexes);
	String accessCallString = accessCall.getCode();
	template = template.replace("<ACCESS_CALL>", accessCallString);

	// FileNode templateToken = MatlabProcessorUtils.fromMFile(template, "MultipleSetToForTemplate");
	FileNode templateToken = new MatlabParser().parse(template);

	// MatlabNode script = templateToken.getChildren().get(0);
	ScriptNode script = templateToken.getScript();

	int lineNumber = assignment.getData().getLine();

	// Add comment with original code
	CommentSingleSt commentStatement = StatementFactory.newComment(lineNumber, "Matlab: "
		+ assignment.getCode().trim());

	script.addChild(0, commentStatement);

	// Add statements
	// for (MatlabNode statement : script.getChildren()) {
	for (StatementNode statement : script.getStatements()) {
	    statement.setLine(lineNumber);
	    // StatementUtils.setLineNumber(statement, lineNumber);
	    NodeInsertUtils.insertBefore(assignment, statement);
	}

	// Remove original statement
	NodeInsertUtils.delete(assignment);

	return true;
    }

    @Override
    public boolean changesStatements() {
	return true;
    }

}
