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

package org.specs.MatlabProcessor.Tokenizer.Rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CellAccessNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CellNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.DynamicFieldAccessNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.DynamicFieldAccessSeparatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.FieldAccessNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.ParenthesisNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ReservedWordNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.RowNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.FunctionHandleSymbolNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.LambdaInputsNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SpaceNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.TempNodeFactory;
import org.specs.MatlabProcessor.Exceptions.ParserErrorException;
import org.specs.MatlabProcessor.MatlabParser.Rules.FunctionDeclarationBuilderRule;
import org.specs.MatlabProcessor.Reporting.ProcessorErrorType;
import org.specs.MatlabProcessor.Tokenizer.TokenizerState.TokenizerState;

import pt.up.fe.specs.util.reporting.Reporter;
import pt.up.fe.specs.util.treenode.TreeNodeIndexUtils;

public class ApplyFormParenthesis {

    private static final Set<Class<?>> ACCESS_CALL_PREVIOUS = new HashSet<>(Arrays.asList(
	    IdentifierNode.class, CellAccessNode.class, FieldAccessNode.class, DynamicFieldAccessNode.class));

    private final Reporter reportService;
    private final FunctionDeclarationBuilderRule functionDecRule;

    public ApplyFormParenthesis(Reporter reportService) {
	this.reportService = reportService;
	functionDecRule = new FunctionDeclarationBuilderRule(reportService);
    }

    /**
     * Forms an access call (or cell access) expression, if appropriate. Note that if there is a space before the open
     * parenthesis token and we are inside a matrix or cell array declaration, there should *not* be an access call
     * expression. That is, [A (1)] is composed of two separate columns.
     * 
     * @param tokens
     */
    public void apply(List<MatlabNode> tokens, Class<? extends MatlabNode> parenType, TokenizerState state) {
	// public void apply(List<MatlabNode> tokens, MatlabNodeType parenType, TokenizerState state) {

	// Only apply transformation to parenthesis or cells
	if ((!parenType.equals(ParenthesisNode.class)) && (!parenType.equals(CellNode.class))) {
	    return;
	}

	// Check if last token is of type parenthesis
	// int parenIndex = MatlabTokenUtils.lastIndexOf(parenType, tokens);
	Optional<Integer> parenIndexTry = TreeNodeIndexUtils.lastIndexOf(tokens, parenType);
	// if (parenIndex == -1) {
	if (!parenIndexTry.isPresent()) {
	    return;
	}

	int parenIndex = parenIndexTry.get();

	MatlabNode parenToken = tokens.get(parenIndex);

	// Check if there are more tokens besides the parenthesis
	if (tokens.size() == 1) {
	    return;
	}

	// Check if there are tokens before the parenthesis
	if (parenIndex < 1) {
	    return;
	}

	// Rewrite code below
	// boolean ignoreSpace = !state.getParenthesisStack().isSpaceSensible();

	// If inside square brackets, check the previous token, minding if it is
	// a space
	List<Class<? extends MatlabNode>> exceptions = Collections.emptyList();
	boolean insideSquareOrCurly = state.getParenthesisStack().isInsideSquareBrackets() ||
		state.getParenthesisStack().isInsideCurlyBrackets();

	if (!insideSquareOrCurly) {
	    exceptions = Arrays.asList(SpaceNode.class);
	}
	/*
	MType[] exceptions2 = null;
	
	if (state.getParenthesisStack().isInsideSquareBrackets() ||
		state.getParenthesisStack().isInsideCurlyBrackets()) {
	    exceptions2 = new MType[0];
	} else {
	    exceptions2 = new MType[1];
	    exceptions2[0] = MType.Space;
	}
	*/

	// System.out.println("EXCEPTIONS:" + exceptions);
	// System.out.println("EXCEPTIONS_OR:" + Arrays.toString(exceptions2));
	// Get previous token that is not a space
	List<MatlabNode> tokensUntilPar = tokens.subList(0, parenIndex);
	Optional<Integer> endIndexTry = TreeNodeIndexUtils.lastIndexExcept(tokensUntilPar, exceptions);

	// MatlabNode previousToken2 = MatlabTokenUtils.getLastToken(
	// tokens.subList(0, parenIndex), exceptions2);
	// int endIndex2 = MatlabTokenUtils.getLastTokenIndex(
	// tokens.subList(0, parenIndex), exceptions2);

	// if (previousToken == null) {
	if (!endIndexTry.isPresent()) {
	    return;
	}

	// System.out.println("PREVIOUS TOKEN:" + previousTokenTry.get());
	// System.out.println("PREVIOUS TOKEN_OR:" + previousToken2);
	int endIndex = endIndexTry.get();
	MatlabNode previousToken = tokensUntilPar.get(endIndex);

	// System.out.println("END INDEX:" + endIndex);
	// System.out.println("END INDEX_OR:" + endIndex2);

	if (previousToken instanceof AccessCallNode) {
	    throw reportService
		    .emitError(ProcessorErrorType.SYNTAX_ERROR,
			    "While forming an AccessCall, expected an identifier before parenthesis, found another AccessCall.");
	}

	// Remove spaces after endIndex, if in state where they do not have meaning
	if (!state.isSpaceSensible()) {
	    while (((endIndex + 1) < tokens.size()) && (tokens.get(endIndex + 1) instanceof SpaceNode)) {
		tokens.remove(endIndex + 1);
		parenIndex--;
	    }
	}

	// If previous node is a function handle, build a lambda
	if (previousToken instanceof FunctionHandleSymbolNode) {
	    // LambdaNode lambda = MatlabNodeFactory.newLambda(parenToken.getChildren());
	    List<MatlabNode> inputs = new ArrayList<>();
	    functionDecRule.buildInputs(inputs, parenToken.getChildren());
	    LambdaInputsNode lambda = TempNodeFactory.newLambdaInputs(inputs);

	    // Replace identifier
	    tokens.set(endIndex, lambda);
	    // Remove parenthesis
	    tokens.remove(parenIndex);
	    return;
	}

	// If previous node is a dynamic field access separator, build a dynamic field access
	if (previousToken instanceof DynamicFieldAccessSeparatorNode) {

	    // Check parenthesis has one child
	    if (parenToken.getNumChildren() != 1) {
		throw new ParserErrorException("Parenthesis with '" + parenToken.getNumChildren()
			+ "' instead of 1, when building Dynamic Field Access, check if correct");
	    }

	    MatlabNode fieldAccess = MatlabNodeFactory.newDynamicFieldAccess(tokens.get(endIndex - 1),
		    parenToken.getChild(0));

	    // Remove parenthesis and field separator
	    tokens.remove(parenIndex);
	    tokens.remove(endIndex);
	    // Replace node before separator
	    tokens.set(endIndex - 1, fieldAccess);

	    return;
	}

	// If a keyword that marks the beginning of a statement with expression,
	if (previousToken instanceof ReservedWordNode) {
	    // If for or parfor, set parenthesis children in place of paren and finish statement
	    ReservedWord word = ((ReservedWordNode) previousToken).getWord();
	    if (word == ReservedWord.For || word == ReservedWord.Parfor) {
		// Remove parenthesis
		tokens.remove(parenIndex);
		// Add children
		tokens.addAll(parenToken.getChildren());
		state.finishStatement(true);

	    }

	    return;
	}

	// Check if access call can be formed
	if (!ApplyFormParenthesis.ACCESS_CALL_PREVIOUS.contains(previousToken.getClass())) {
	    return;
	}

	// Check if identifier or cell access
	// if (!(previousToken instanceof IdentifierNode)
	// && !(previousToken instanceof CellAccessNode)) {
	// return;

	// }

	// Form AccessCall
	List<MatlabNode> children = parenToken.getChildren();

	if (parenToken instanceof CellNode) {
	    MatlabNode row = null;
	    for (MatlabNode child : children) {
		if (child instanceof RowNode) {
		    if (row != null) {
			throw new ParserErrorException("Unbalanced expression: Cell access is invalid.");
		    }
		    row = child;
		} else if (child instanceof SpaceNode) {
		    // Ignore
		} else {
		    throw new ParserErrorException("Unexpected token " + child + " at cell access.");
		}
	    }

	    if (row == null) {
		throw new ParserErrorException("Could not find a RowNode child");
	    }

	    children = row.getChildren();
	}

	MatlabNode accessCall = getAccessCall(parenType, previousToken, children);

	// Replace identifier
	tokens.set(endIndex, accessCall);
	// Remove parenthesis
	tokens.remove(parenIndex);
    }

    private static MatlabNode getAccessCall(Class<? extends MatlabNode> parenType, MatlabNode left,
	    List<MatlabNode> children) {
	if (parenType.equals(ParenthesisNode.class)) {
	    return MatlabNodeFactory.newAccessCall(left, children);
	}

	if (parenType.equals(CellNode.class)) {
	    return MatlabNodeFactory.newCellAccess(left, children);
	}

	throw new RuntimeException("Not defined for type '" + parenType.getSimpleName() + "'");
    }

}
