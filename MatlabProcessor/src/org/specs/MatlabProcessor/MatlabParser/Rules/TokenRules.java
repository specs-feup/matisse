/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.MatlabProcessor.MatlabParser.Rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.specs.MatlabIR.Exceptions.CodeParsingException;
import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CellAccessNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CompositeAccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.DynamicFieldAccessNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.FieldAccessNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.LambdaNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNumberNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatrixNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OutputsNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ParenthesisNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ReservedWordNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ForSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNode.nodes.statements.UndefinedSt;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.AssignmentNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ExpressionNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.LambdaInputsNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SpaceNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SubscriptSeparatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.TempNodeFactory;
import org.specs.MatlabIR.matlabrules.MatlabNodeRule;
import org.specs.MatlabIR.matlabrules.MatlabRulesUtils;
import org.specs.MatlabProcessor.Reporting.ProcessorErrorType;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.classmap.ClassSet;
import pt.up.fe.specs.util.reporting.Reporter;
import pt.up.fe.specs.util.treenode.NodeInsertUtils;

/**
 * Rules applied to individual tokens, related to the formation of the concrete tree.
 * 
 * @author Joao Bispo
 * 
 */
public class TokenRules {

    private final Reporter reportService;
    private final List<MatlabNodeRule> rules;

    public TokenRules(Reporter reportService) {
	this.reportService = reportService;

	rules = buildRules();
    }

    private List<MatlabNodeRule> buildRules() {
	List<MatlabNodeRule> rules = new ArrayList<>();

	// Remove space nodes
	rules.add(MatlabRulesUtils.newRemoveNodeFromChildren(SpaceNode.class));

	// Should be used before general expression formation
	rules.add(buildConditionalExpressions());

	// Needs to be applied after atomizeFieldAccesses
	rules.add(invertConjugateOperatorOrder());

	// tokenRules.add(formConjugateFunction());
	// tokenRules.add(formNonConjugateFunction());

	rules.add(formExpressions(reportService));

	// Applied over expressions, so has to be executed after Expression
	// formation.
	rules.add(formOperatorPrecedence());

	// Lambdas need to be built before assignments, because of examples such as:
	// a.b = @(x) b(x) % It will have two children after assignment
	NodeMapRule nodeRules = new NodeMapRule.Builder()
		.add(LambdaInputsNode.class, this::lambdaInputsRule)
		.add(OutputsNode.class, this::outputsRule)
		.build();

	rules.add(nodeRules);

	// Form assignments
	rules.add(buildAssignment());
	// Add Expression after assignments, when they are outside an assignment statement (e.g., when they are in a
	// for)

	// Adds an expression before Matrix that is after Assignment.
	// Needed to pass test in MatlabIRTransformationTests
	rules.add(normalizeAssignmentTokens());

	// Expressions (operations) have priority over Colon
	// tokenRules.add(formColonCall());

	// Clean up
	rules.add(removeDuplicatedExpressions());
	rules.add(removeDuplicatedParenthesis());
	rules.add(MatlabRulesUtils.newRemoveNodeFromChildren(SubscriptSeparatorNode.class));

	// TODO: Currently empty
	rules.add(formatForSt());
	rules.add(removeExprNodes());

	return rules;
    }

    private final static ClassSet<MatlabNode> TOKEN_TYPES_TO_REMOVE_FROM_PARENTHESIS;

    static {
	TOKEN_TYPES_TO_REMOVE_FROM_PARENTHESIS = new ClassSet<>();

	TokenRules.TOKEN_TYPES_TO_REMOVE_FROM_PARENTHESIS.add(ParenthesisNode.class);
	TokenRules.TOKEN_TYPES_TO_REMOVE_FROM_PARENTHESIS.add(AccessCallNode.class); // CHECK: Just SimpleAccessCall?
	TokenRules.TOKEN_TYPES_TO_REMOVE_FROM_PARENTHESIS.add(MatrixNode.class);
	TokenRules.TOKEN_TYPES_TO_REMOVE_FROM_PARENTHESIS.add(MatlabNumberNode.class);
    }
    //
    // private final static Set<MType> tokenTypesToRemoveFromPar;
    //
    // static {
    // tokenTypesToRemoveFromPar = EnumSet.of(MType.Parenthesis, MType.AccessCall, MType.Matrix,
    // MType.MatlabNumber);
    // }

    private void lambdaInputsRule(LambdaInputsNode previousNode, MatlabNodeIterator iterator) {
	if (!iterator.hasNext()) {
	    throw reportService.emitError(ProcessorErrorType.PARSE_ERROR, "Anonymous function without body");
	}

	// Create lambda node with next node
	MatlabNode expression = iterator.next();

	// If expression is itself a lambda inputs, call function recursively
	if (expression instanceof LambdaInputsNode) {
	    lambdaInputsRule((LambdaInputsNode) expression, iterator);
	    // Update expression
	    expression = iterator.next();
	}

	LambdaNode lambdaNode = MatlabNodeFactory.newLambda(previousNode.getInputs(), expression);

	iterator.replace(lambdaNode, 2);
    }

    /**
     * Looks for certain tokens which use an expression for a conditional value (e.g., for, if, while...) and puts those
     * tokens inside an expression token.
     * 
     * <p>
     * Should be used before general expression formation
     * 
     * @return
     */
    private static MatlabNodeRule buildConditionalExpressions() {
	return new MatlabNodeRule() {
	    private boolean check(MatlabNode token) {
		// Token must have children.
		if (!token.hasChildren()) {
		    return false;
		}

		List<MatlabNode> statementTokens = token.getChildren();

		// Check if first child is a reserved word
		MatlabNode firstToken = statementTokens.get(0);
		if (!(firstToken instanceof ReservedWordNode)) {
		    return false;
		}

		// Check if first child has expressions
		return ((ReservedWordNode) firstToken).getWord().hasExpression();

		// String keyword = MatlabTokenContent.getReservedWordString(firstToken);
		// if (!MatlabParserUtils.CONDITIONAL_EXPRESSION_KEYWORDS.contains(keyword)) {
		// return false;
		// }
		//
		// return true;
		//
	    }

	    @Override
	    public MatlabNode apply(MatlabNode token) {
		if (!check(token)) {
		    return token;
		}

		List<MatlabNode> statementTokens = token.getChildren();

		// Build expression
		int startIndex = 1;
		int endIndex = statementTokens.size();

		List<MatlabNode> children = new ArrayList<>();
		children.addAll(statementTokens.subList(startIndex, endIndex));

		MatlabNode exprToken = TempNodeFactory.newExpression(children);

		// Remove all children tokens but for the first
		token.removeChildren(startIndex + 1, endIndex);
		// TokenUtils.removeChildren(token, startIndex + 1, endIndex);

		// Replace first child
		token.setChild(startIndex, exprToken);

		return token;
	    }
	};
    }

    /**
     * Change For statement from:<br>
     * - The first child is the reserved word 'for' <br>
     * - The second child is the for expression which should have the following format: <br>
     * -- The first child is the identifier <br>
     * -- The second child is the assignment token <br>
     * -- The third child is an expression <br>
     * <p>
     * To:<br>
     * - The first child is the identifier <br>
     * - The second child is an expression <br>
     * 
     * @return
     */
    private static MatlabNodeRule formatForSt() {
	return node -> {
	    // Check if for statement
	    // if (!(node instanceof SimpleForSt) && !(node instanceof ParForSt)) {
	    if (!(node instanceof ForSt)) {
		return node;
	    }

	    List<MatlabNode> newChildren = new ArrayList<>();

	    newChildren.add(node.getChild(1, 0));
	    newChildren.add(node.getChild(1, 2));

	    node.setChildren(newChildren);

	    return node;
	};
    }

    private static MatlabNodeRule removeExprNodes() {
	return node -> {
	    // System.out.println("NODE TYPE:" + node.getType());
	    // Get all expression nodes
	    // List<MatlabNode> expressions = TokenUtils.getChildrenRecursively(node, MType.Expression);
	    /*
	    	    System.out.println("EXPRESSIONS:" + expressions.size());
	    
	    	    expressions.stream()
	    		    .forEach(expr -> NodeInsertUtils.replace(expr, expr.getChild(0)));
	    
	    return node;
	    */

	    // if (node.getType() != MType.Expression) {
	    if (!(node instanceof ExpressionNode)) {
		return node;
	    }

	    // Only remove expressions which have one child
	    if (node.getNumChildren() != 1) {
		return node;
	    }

	    return node.getChild(0);
	    /*
	    // If an expression, return the (unique) child
	    if (node.numChildren() != 1) {
	    // Expression should only have one node, but at this point, this can represent a code error
	    throw new RuntimeException(
	    	"Found expression with '" + node.numChildren() + "', should be 1: " + node);
	    }
	    
	    // System.out.println("NODE:" + node);
	    // System.out.println("CHILD:" + node.getChild(0));
	    return node;
	    // return node.getChild(0);
	    */
	    // return node;
	};

    }

    private static MatlabNodeRule normalizeAssignmentTokens() {
	return token -> {
	    // Get all assignment tokens
	    List<AssignmentNode> assignments = token.getDescendantsAndSelf(AssignmentNode.class);
	    // List<MatlabNode> assignments = TokenUtils.getChildrenRecursively(token, MType.Assignment);
	    assignments.forEach(assignment -> {

		// If parent statement is an assignment, skip
		// All assignments at this point must have a parent statement
		StatementNode statement = assignment.getAncestorTry(StatementNode.class).get();
		// MatlabNode statement = NodeInsertUtils.getParent(assignment, MType.Statement);
		if (statement instanceof AssignmentSt) {
		    // if (StatementUtils.getType(statement) == MStatementType.Assignment) {
		    return;
		}

		// If token next to assign is an expression, skip
		int assignIndex = assignment.indexOfSelf();
		MatlabNode parent = assignment.getParent();
		/*
		 * TEST, because of bug, can remove
		if (assignIndex == parent.numChildren() - 1) {
		System.out.println("INDEX OF SELF:" + assignIndex);
		System.out.println("NUM CHILDREN:" + parent.numChildren());
		System.out.println("NO RH?;\n" + assignment.getParent());
		return;
		}
		*/

		MatlabNode rightHand = parent.getChildren().get(assignIndex + 1);
		if (rightHand instanceof ExpressionNode) {
		    return;
		}

		// If token next assign is not a Matrix, skip
		if (!(rightHand instanceof MatrixNode)) {
		    return;
		}

		// If token next to assign is not an expression, put it inside an expression
		MatlabNode expr = TempNodeFactory.newExpression(rightHand);
		NodeInsertUtils.replace(rightHand, expr);
	    });

	    return token;
	};
    }

    /**
     * Removes a parenthesis token if the token immediately below is a parenthesis or an accessCall.
     * 
     * @return
     */
    private static MatlabNodeRule removeDuplicatedParenthesis() {
	return token -> {
	    // if (!check(token)) {
	    // return false;
	    // }
	    // Apply when token has children
	    if (!token.hasChildren()) {
		return token;
	    }

	    // Check all tokens of type parenthesis
	    List<Integer> indexes = token.indexesOf(ParenthesisNode.class);

	    if (indexes.isEmpty()) {
		return token;
	    }

	    for (Integer index : indexes) {
		MatlabNode child = token.getChildren().get(index);

		// Check if meets criteria
		if (child.getChildren().size() != 1) {
		    continue;
		}

		MatlabNode grandChild = child.getChildren().get(0);

		// Tokens which to not need parenthesis
		// if (TokenRules.tokenTypesToRemoveFromPar.contains(grandChild.getType())) {
		if (TokenRules.TOKEN_TYPES_TO_REMOVE_FROM_PARENTHESIS.contains(grandChild)) {
		    // Replace child with grandchild
		    // token.getChildren().set(index, grandChild);
		    token.setChild(index, grandChild);
		    continue;
		}

		// Parenthesis are added to transpose operators, to aid in the precendence construction
		// After expression formation is done, remove them
		if (grandChild instanceof ExpressionNode) {
		    MatlabNode grandGrandChild = grandChild.getChild(0);
		    if (grandGrandChild instanceof OperatorNode) {
			MatlabOperator op = ((OperatorNode) grandGrandChild).getOp();
			if (op == MatlabOperator.Transpose || op == MatlabOperator.ComplexConjugateTranspose) {
			    token.setChild(index, grandChild);
			    continue;
			}
		    }
		}

	    }

	    return token;

	};
    }

    /**
     * Puts the conjugate operators in front of the expression to transpose, instead staying in the back.
     * 
     * <p>
     * The transpose is the only unary operator which accepts an operand from the left. Current code (e.g.: operator
     * precendence calculation) assumes that all unary operators accept an operand from the right.
     * 
     * @return
     */
    private static MatlabNodeRule invertConjugateOperatorOrder() {
	return (token) -> {

	    // Apply when token has children
	    if (!token.hasChildren()) {
		return token;
	    }

	    // Get indexes for all operators
	    List<Integer> operatorIndexes = token.indexesOf(OperatorNode.class);
	    if (operatorIndexes.isEmpty()) {
		return token;
	    }

	    // Get indexes for all transpose operators
	    List<Integer> transposeOpIndexes = new ArrayList<>();
	    for (Integer index : operatorIndexes) {
		OperatorNode operator = token.getChild(OperatorNode.class, index);
		MatlabOperator op = operator.getOp();

		if (op == MatlabOperator.Transpose) {
		    transposeOpIndexes.add(index);
		}
		if (op == MatlabOperator.ComplexConjugateTranspose) {
		    transposeOpIndexes.add(index);
		}
	    }
	    if (transposeOpIndexes.isEmpty()) {
		return token;
	    }

	    // Replace the operand and the operator with a parenthesis,
	    // use a correction factor because of the reducing list
	    // Transpose operations have to be done from left to right
	    int correctionFactor = 0;

	    for (Integer index : transposeOpIndexes) {
		index -= correctionFactor;

		// Get token before transpose operator
		MatlabNode operand = token.getChildren().get(index - 1);
		MatlabNode operator = token.getChildren().get(index);

		// Build parenthesis
		MatlabNode paren = MatlabNodeFactory.newParenthesis(operator, operand);

		// Replace operand
		// TokenUtils.setChildAndRemove(token, paren, index - 1, index + 1);
		token.setChildAndRemove(paren, index - 1, index + 1);

		// List shrinks by 1 every iteration
		correctionFactor += 1;
	    }

	    return token;
	};
    }

    /**
     * Builds operator precedence hierarchy and remove Expression tokens.
     * 
     * <p>
     * Rules is applied over expression tokens, so has to be executed after Expression formation. Removes the expression
     * token after operator precedence is done, or if there is no operators in the given token.
     * 
     * @return
     */
    private static MatlabNodeRule formOperatorPrecedence() {

	return new MatlabNodeRule() {

	    private boolean check(MatlabNode matlabToken) {
		// Is of type expression?
		if (!(matlabToken instanceof ExpressionNode)) {
		    return false;
		}

		// Has children?
		if (!matlabToken.hasChildren()) {
		    SpecsLogs.warn("Expression without children?\n" + matlabToken);
		    return false;
		}

		// Has operator tokens?
		if (!matlabToken.getFirstChildOptional(OperatorNode.class).isPresent()) {
		    // int index = MatlabTokenUtils.indexOf(MType.Operator, matlabToken.getChildren());
		    // if (index == -1) {
		    return false;
		}

		return true;
	    }

	    @Override
	    public MatlabNode apply(MatlabNode token) {
		if (!check(token)) {
		    return token;
		}

		List<MatlabNode> tokens = token.getChildren();

		// Algorithm changes list
		List<MatlabNode> copy = new ArrayList<>(tokens);
		// System.out.println("COPY:" + copy);
		MatlabNode opToken = TokenRulesUtils.generateTreeWithPrecedences(copy);
		// System.out.println("COPY AFTER:" + copy);
		// Check if null
		if (opToken == null) {
		    throw new RuntimeException("Problem when applying operator precedences:\n"
			    + token.toString());
		}

		MatlabNode newExprToken = TempNodeFactory.newExpression(opToken);

		// TokenUtils.removeChildren(token, 0, token.numChildren());
		token.removeChildren(0, token.getNumChildren());
		token.addChildren(newExprToken.getChildren());

		return token;
	    }

	};
    }

    /**
     * If an expression only has one child, and is an expression, replaced the expression by the child.
     * 
     * @return
     */
    private static MatlabNodeRule removeDuplicatedExpressions() {
	return token -> {

	    if (!token.hasChildren()) {
		return token;
	    }

	    // List<MatlabNode> tokens = token.getChildren();
	    // List<Integer> indexes = MatlabTokenUtils.allIndexesOf(MType.Expression, tokens);
	    List<Integer> indexes = token.indexesOf(ExpressionNode.class);

	    for (Integer index : indexes) {
		// MatlabNode exprToken = tokens.get(index);
		MatlabNode exprToken = token.getChild(index);

		// Check if only one child
		if (exprToken.getNumChildren() != 1) {
		    // LoggingUtils.msgWarn("Found expression with more than one children!\n" + exprToken);
		    continue;
		}

		// Check if expression
		MatlabNode child = exprToken.getChildren().get(0);
		if (!(child instanceof ExpressionNode)) {
		    continue;
		}

		// Replace current expression
		// MatlabTokenUtils.replaceToken(child, tokens, index, index
		// + 1);
		// TokenUtils.setChildAndRemove(token, child, index, index + 1);
		token.setChildAndRemove(child, index, index + 1);
	    }

	    return token;

	};
    }

    /**
     * Aggregates operations into expressions. Uses MatlabToken objects of type 'operator' as the base to build
     * expressions.
     * 
     * 
     * @return
     */
    private static MatlabNodeRule formExpressions(Reporter reportService) {
	return (token) -> {

	    if (!token.hasChildren()) {
		return token;
	    }

	    if (token instanceof OutputsNode) {
		return token;
	    }

	    // Get iterator
	    MatlabNodeIterator iterator = token.getChildrenIterator();

	    // Continue until there are operator nodes
	    while (true) {
		Optional<OperatorNode> op = iterator.next(OperatorNode.class);

		// If no operator, stop here
		if (!op.isPresent()) {
		    break;
		}

		List<MatlabNode> expressionChildren = new ArrayList<>();

		// If binary op, get previous node
		MatlabOperator operator = op.get().getOp();
		if (operator.isBinary()) {
		    expressionChildren.add(iterator.back(2));
		    iterator.remove();
		    // Put in position to remove op (the same position before 'if')
		    iterator.next();
		}

		// Add op
		expressionChildren.add(op.get());
		iterator.remove();

		// Add nodes until end of expression
		int numNonOpNodes = 0;
		while (true) {
		    // If no more nodes, there is a problem, expected another node after an operator node.
		    if (!iterator.hasNext() && (numNonOpNodes == 0)) {
			throw reportService.emitError(ProcessorErrorType.PARSE_ERROR,
				"Expected another node after operator '"
					+ op.get().getCode()
					+ "', found nothing");
		    }

		    // If no more nodes, stop
		    if (!iterator.hasNext()) {
			break;
		    }

		    MatlabNode currentNode = iterator.next();

		    // Stop adding nodes if there are two consecutive non-operator nodes
		    if (currentNode instanceof OperatorNode) {
			numNonOpNodes = 0;
		    } else {
			numNonOpNodes++;

			// Go back, node unused
			if (numNonOpNodes == 2) {
			    iterator.previous();
			    break;
			}
		    }

		    // Add current node
		    expressionChildren.add(currentNode);
		    iterator.remove();

		}

		// Create expression and add node
		iterator.add(TempNodeFactory.newExpression(expressionChildren));
	    }

	    return token;

	};
    }

    /**
     * If one of the children is an assignment token, tries to transform the statement into an Assignment statement.
     * 
     * @return
     */
    private static MatlabNodeRule buildAssignment() {
	return new MatlabNodeRule() {

	    private boolean check(MatlabNode token) {

		if (!(token instanceof UndefinedSt)) {
		    return false;
		}

		return true;
	    }

	    @Override
	    public MatlabNode apply(MatlabNode node) {
		if (!check(node)) {
		    return node;
		}

		// At this point, it is an UndefinedSt
		UndefinedSt statement = (UndefinedSt) node;

		List<MatlabNode> tokens = statement.getChildren();

		// Get index of assignment
		List<Integer> indexes = statement.indexesOf(AssignmentNode.class);

		// If empty, did not find assignment tokens
		if (indexes.isEmpty()) {
		    return statement;
		}

		// If size is larger than one, warning
		if (indexes.size() != 1) {
		    SpecsLogs.warn("Statement has more than one assignment. Check it:");
		    SpecsLogs.warn(statement.toString());
		    return statement;
		}

		int index = indexes.get(0);

		// Check if there are tokens on the right of assign
		if (index + 1 >= tokens.size()) {
		    SpecsLogs.warn("Did not find tokens on the right of an assignment.");
		    return statement;
		}

		// If using things like 'while i=1:20', index will be zero
		if (index == 0) {
		    throw new CodeParsingException("Invalid assignment");
		}

		if (index != 1) {
		    throw new CodeParsingException(
			    "Assignment left hand with more than one child on line:\n" + tokens.toString());
		}

		MatlabNode leftHand = tokens.get(0);
		// MatlabNode leftHand = MatlabTokenUtils.getAssignmentHand(tokens, 0, index);
		// If left hand is a Matrix, convert to Outputs
		if (leftHand instanceof MatrixNode) {
		    List<MatlabNode> outputs = ((MatrixNode) leftHand).getSingleRow().getChildren();
		    leftHand = MatlabNodeFactory.newOutputs(outputs);
		}

		if (tokens.size() - (index + 1) != 1) {
		    throw new CodeParsingException(
			    "Assignment right hand with more than one child on line:\n" + tokens.toString());
		}
		// MatlabNode rightHand = MatlabTokenUtils.getAssignmentHand(tokens, index + 1, tokens.size());
		MatlabNode rightHand = tokens.get(index + 1);

		return StatementFactory.newAssignment(statement.getData(), leftHand, rightHand);
	    }
	};
    }

    private void outputsRule(OutputsNode previousNode, MatlabNodeIterator iterator) {
	MatlabNodeIterator it = previousNode.getChildrenIterator();

	boolean immediatlyAfterUnusedVariable = false;

	while (it.hasNext()) {
	    MatlabNode node = it.next();
	    if (node instanceof SubscriptSeparatorNode) {
		immediatlyAfterUnusedVariable = false;
		continue;
	    }

	    if (immediatlyAfterUnusedVariable) {
		throw reportService
			.emitError(ProcessorErrorType.PARSE_ERROR,
				"Invalid expression in outputs declaration.\nDid you forget a comma between two of the outputs?");
	    }

	    if (node instanceof IdentifierNode) {
		continue;
	    }
	    if (node instanceof AccessCallNode || node instanceof CompositeAccessCallNode) {
		continue;
	    }
	    if (node instanceof FieldAccessNode || node instanceof DynamicFieldAccessNode) {
		continue;
	    }
	    if (node instanceof CellAccessNode) {
		continue;
	    }
	    if (node instanceof OperatorNode) {
		MatlabOperator op = ((OperatorNode) node).getOp();
		if (op == MatlabOperator.LogicalNegation) {
		    immediatlyAfterUnusedVariable = true;
		    it.set(MatlabNodeFactory.newUnusedVariable());
		    continue;
		}
	    }
	    reportService.emitError(ProcessorErrorType.PARSE_ERROR, "Unexpected token " + node.getNodeName()
		    + " in outputs declaration.");
	}
    }

    public StatementNode apply(StatementNode statement) {
	// Apply processing methods to current tokens
	for (MatlabNodeRule rule : rules) {
	    statement = (StatementNode) MatlabRulesUtils.bottomUpTraversal(statement, rule);
	}

	return statement;
    }

}
