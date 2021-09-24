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

package org.specs.MatlabProcessor.Tokenizer.Rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.specs.MatlabIR.Exceptions.CodeParsingException;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CellNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatrixNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ParenthesisNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.CellStartNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ParenthesisStartNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SpaceNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SquareBracketsStartNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.TempNodeFactory;
import org.specs.MatlabIR.Processor.TreeTransformException;
import org.specs.MatlabProcessor.Tokenizer.TokenizerRule;
import org.specs.MatlabProcessor.Tokenizer.TokenizerUtils;
import org.specs.MatlabProcessor.Tokenizer.TokenizerState.TokenizerState;

import pt.up.fe.specs.util.treenode.TreeNodeIndexUtils;
import pt.up.fe.specs.util.utilities.StringSlice;

/**
 * @author Joao Bispo
 * 
 */
public class ParenthesisRules {

    // private final static Map<String, MType> parenthesisTypes = new HashMap<>();
    // private final static Map<MType, MType> startsAndEnds = new HashMap<>();
    private final static Map<String, Class<? extends MatlabNode>> parenthesisTypes = new HashMap<>();
    private final static Map<Class<? extends MatlabNode>, Class<? extends MatlabNode>> startsAndEnds = new HashMap<>();
    private final static Set<String> startStrings = new HashSet<>();
    private final static Set<String> endStrings = new HashSet<>();

    static {
	addParenthesis("(", ")", ParenthesisStartNode.class, ParenthesisNode.class);
	addParenthesis("[", "]", SquareBracketsStartNode.class, MatrixNode.class);
	addParenthesis("{", "}", CellStartNode.class, CellNode.class);
    }

    private static final Map<Class<? extends MatlabNode>, Supplier<MatlabNode>> PAR_START_RULES;

    static {
	PAR_START_RULES = new HashMap<>();

	ParenthesisRules.PAR_START_RULES.put(SquareBracketsStartNode.class, TempNodeFactory::newSquareBracketsStart);
	ParenthesisRules.PAR_START_RULES.put(CellStartNode.class, TempNodeFactory::newCellStart);
	ParenthesisRules.PAR_START_RULES.put(ParenthesisStartNode.class, TempNodeFactory::newParenthesisStart);
    }

    private static final Map<Class<? extends MatlabNode>, Function<List<MatlabNode>, MatlabNode>> PAR_FORM_RULES;

    static {
	PAR_FORM_RULES = new HashMap<>();

	ParenthesisRules.PAR_FORM_RULES.put(MatrixNode.class, MatlabNodeFactory::newMatrix);
	ParenthesisRules.PAR_FORM_RULES.put(CellNode.class, MatlabNodeFactory::newCell);
	ParenthesisRules.PAR_FORM_RULES.put(ParenthesisNode.class, MatlabNodeFactory::newParenthesis);
    }

    private static MatlabNode getToken(Class<? extends MatlabNode> endType, List<MatlabNode> parenthesisTokens) {
	Supplier<MatlabNode> startRule = ParenthesisRules.PAR_START_RULES.get(endType);
	if (startRule != null) {
	    return startRule.get();
	}

	Function<List<MatlabNode>, MatlabNode> endRule = ParenthesisRules.PAR_FORM_RULES.get(endType);
	if (endRule != null) {
	    return endRule.apply(parenthesisTokens);
	}

	throw new RuntimeException("Case '" + endType + "' not supported");
	/*
	switch (endType) {
	
	case SquareBracketsStart:
	    return TempNodeFactory.newSquareBracketsStart();
	case CellStart:
	    return TempNodeFactory.newCellStart();
	case ParenthesisStart:
	    return TempNodeFactory.newParenthesisStart();
	case Matrix:
	    return MatlabNodeFactory.newMatrix(parenthesisTokens);
	case Cell:
	    return MatlabNodeFactory.newCell(parenthesisTokens);
	case Parenthesis:
	    return MatlabNodeFactory.newParenthesis(parenthesisTokens);
	default:
	    throw new RuntimeException("Case '" + endType + "' not supported");
	}
	*/
    }

    /*
    static {
    parenthesisTypes.put("(", TokenType.parenStart);
    parenthesisTypes.put(")", TokenType.parenthesis);
    parenthesisTypes.put("[", TokenType.squareBracketsStart);
    parenthesisTypes.put("]", TokenType.squareBrackets);
    parenthesisTypes.put("{", TokenType.cellStart);
    parenthesisTypes.put("}", TokenType.cell);
    }
    
    
    static {
    startsAndEnds.put(TokenType.squareBrackets,
    	TokenType.squareBracketsStart);
    startsAndEnds.put(TokenType.parenthesis, TokenType.parenStart);
    startsAndEnds.put(TokenType.cell, TokenType.cellStart);
    }
    
    
    static {
    startStrings.add("(");
    startStrings.add("[");
    startStrings.add("{");
    }
    */
    /**
     * Method to initialize a type of parenthesis.
     * 
     * @param startString
     * @param endString
     * @param startToken
     * @param endToken
     */
    private static void addParenthesis(String startString, String endString, Class<? extends MatlabNode> startToken,
	    Class<? extends MatlabNode> endToken) {

	ParenthesisRules.parenthesisTypes.put(startString, startToken);
	ParenthesisRules.parenthesisTypes.put(endString, endToken);

	ParenthesisRules.startsAndEnds.put(endToken,
		startToken);

	ParenthesisRules.startStrings.add(startString);
	ParenthesisRules.endStrings.add(endString);
    }

    /**
     * @return
     */
    /*
    public static List<TokenizerRule> ruleset() {
    
    List<TokenizerRule> rules = new ArrayList<TokenizerRule>();
    
    // First rule, to guarantee that tests on current line have at least on
    // character.
    rules.add(parenStartRule());
    
    return rules;
    
    }
    */

    /**
     * Matlab open parenthesis.
     * 
     * <p>
     * Produces an parenStart token.
     * 
     * @return
     */
    public static TokenizerRule parenStartRule() {
	TokenizerRule rule = new TokenizerRule() {

	    @Override
	    public void apply(TokenizerState state) throws TreeTransformException {

		String paren = Character.toString(state.getCurrentLine()
			.charAt(0));

		checkPreviousTokens(state, paren);

		Class<? extends MatlabNode> type = ParenthesisRules.parenthesisTypes.get(paren);
		if (type == null) {
		    throw new TreeTransformException();
		}

		MatlabNode token = getToken(type, Collections.emptyList());

		// If square brackets, start a row
		state.pushProducedToken(token, paren);
		state.getParenthesisStack().addParenthesisCall(type);

		if (type.equals(SquareBracketsStartNode.class) || type.equals(CellStartNode.class)) {
		    // state.addTokenToCurrentList(token);

		    MatlabNode rowToken = MatlabNodeFactory.newRow(Collections.emptyList());
		    state.pushProducedToken(rowToken, "");
		}

		// state.setInsideParenthesis(true);
		// state.addParenthesisCall(TokenType.parenStart);
	    }

	    private void checkPreviousTokens(TokenizerState state, String currentPar) {
		// TODO: Check if previous is function (don't remember why this TODO is here)

		if (state.getCurrentTokens().isEmpty()) {
		    return;
		}

		MatlabNode lastNode = state.getLastNode();

		// If previous is CellNode and current is cell also, it is ok
		if (lastNode instanceof CellNode && currentPar.equals("{")) {
		    return;
		}

		// Throw a CodeParsingException if previous token is either a matrix or a cell
		if (lastNode instanceof MatrixNode || lastNode instanceof CellNode) {
		    throw new CodeParsingException("Unexpected parenthesis '" + state.getCurrentLine().charAt(0)
			    + "' after a " + lastNode.getClass().getSimpleName());
		}

	    }

	    @Override
	    public boolean isAppliable(TokenizerState state) {
		if (!state.hasLine()) {
		    return false;
		}

		char aChar = state.getCurrentLine().charAt(0);
		return ParenthesisRules.startStrings.contains(Character.toString(aChar));
		/*
				try {
				    char aChar = state.getCurrentLine().charAt(0);
				    return startStrings.contains(Character.toString(aChar));
				} catch (IndexOutOfBoundsException e) {
				    return false;
				}
				*/
		// return state.getCurrentLine().startsWith("(")
		// || state.getCurrentLine().startsWith("[");
	    }
	};

	return rule;
    }

    /**
     * Matlab close parenthesis.
     * 
     * <p>
     * Produces an parenthesis token.
     * 
     * @return
     */
    public static TokenizerRule parenEndRule() {
	TokenizerRule rule = new TokenizerRule() {

	    @Override
	    public void apply(TokenizerState state) throws TreeTransformException {
		// Get parenthesis type
		String paren = Character.toString(state.getCurrentLine()
			.charAt(0));
		Class<? extends MatlabNode> endType = ParenthesisRules.parenthesisTypes.get(paren);
		if (endType == null) {
		    throw new TreeTransformException();
		}

		// Get corresponding start
		Class<? extends MatlabNode> startType = ParenthesisRules.startsAndEnds.get(endType);
		if (startType == null) {
		    throw new TreeTransformException();
		}

		// End Row
		if (endType.equals(MatrixNode.class) || endType.equals(CellNode.class)) {
		    TokenizerUtils.processMatrixRow(state, "", false);
		}

		// Get parenStart token from current list of tokens going
		// backwards in the list.
		List<MatlabNode> currentTokens = state.getCurrentTokens();
		// int parenStartIndex = TokenizerUtils.lastIndexOf(currentTokens,
		// startType);
		Optional<Integer> parenStartIndex = TreeNodeIndexUtils.lastIndexOf(currentTokens,
			startType);

		// Found an open parenthesis without closing equivalent. Treat it as an undefined symbol
		// if (parenStartIndex == -1) {
		if (!parenStartIndex.isPresent()) {
		    GeneralRules.defaultRule().apply(state);
		    return;
		}

		// Build list with children of parenthesis
		List<MatlabNode> parenthesisTokens = new ArrayList<>(
			currentTokens.subList(parenStartIndex.get() + 1,
				currentTokens.size()));

		// Remove tokens from current list
		TokenizerUtils.remove(currentTokens, parenStartIndex.get(), currentTokens.size());

		// Call this method before producing token so that when advancing and trimming,
		// the method knows it is outside of parenthesis
		state.getParenthesisStack().removeParenthesisCall(startType, paren);

		// Remove spaces from parenthesis tokens, if '('
		if (paren.equals(")")) {
		    // System.out.println("PAREN:" + parenthesisTokens);
		    parenthesisTokens = parenthesisTokens.stream().filter(node -> !(node instanceof SpaceNode))
			    .collect(Collectors.toList());

		}

		MatlabNode token = getToken(endType, parenthesisTokens);

		state.pushProducedToken(token, paren);

		// Apply accessCall and other transformations when finishing a "parenthesis"
		new ApplyFormParenthesis(state.getReportService()).apply(state.getCurrentTokens(), endType,
			state);

	    }

	    @Override
	    public boolean isAppliable(TokenizerState state) {
		if (!state.hasLine()) {
		    return false;
		}

		char aChar = state.getCurrentLine().charAt(0);
		return ParenthesisRules.endStrings.contains(Character.toString(aChar));
		/*
				try {
				    char aChar = state.getCurrentLine().charAt(0);
				    return endStrings.contains(Character.toString(aChar));
				} catch (IndexOutOfBoundsException e) {
				    return false;
				}
		*/
		// return state.getCurrentLine().startsWith(")")
		// || state.getCurrentLine().startsWith("]");
	    }
	};

	return rule;
    }

    /**
     * Extracts a string from backquote '.
     * 
     * @param state
     */
    public static void applyStringRule(TokenizerState state)
	    throws TreeTransformException {
	// state.advanceAndTrim("'"); // You might want spaces after '

	StringBuilder builder = new StringBuilder();

	StringSlice workline = state.getCurrentLine();
	// int currentIndex = "'".length();
	int currentIndex = 0;
	// Character currentChar = workline.charAt(currentIndex);
	Character currentChar = null;

	try {
	    boolean stopString = false;
	    while (!stopString) {
		currentIndex += 1;
		currentChar = workline.charAt(currentIndex);

		if (currentChar != '\'') {
		    builder.append(currentChar);
		    continue;
		}

		// Check if stop condition or a literal backquote
		try {
		    char nextChar = workline.charAt(currentIndex + 1);
		    // If char is different than backquote, stop string
		    if (nextChar != '\'') {
			stopString = true;
			continue;
		    }

		    // It is an escaped backquote, advance an extra index
		    currentIndex += 1;
		    builder.append("'");

		} catch (IndexOutOfBoundsException e) {
		    // There is no more string, so it is valid
		    stopString = true;
		    continue;
		}
	    }
	} catch (IndexOutOfBoundsException e) {
	    throw new CodeParsingException("Unbalanced backquotes, string is not terminated properly");
	    /*
	    	    LoggingUtils.msgInfo("Unbalanced backquotes on line "
	    		    + state.getCurrentLineNumber() + ".");
	    	    throw new TreeTransformException();
	    	    */
	}
	// Advance the number of read characters
	// System.out.println("Workline:"+workline);
	// System.out.println("String:"+builder.toString());
	// System.out.println("Advance chars:"+(currentIndex+1));
	// state.advanceAndTrim(currentIndex+1);

	MatlabNode token = MatlabNodeFactory.newCharArray(builder.toString());
	state.pushProducedToken(token, currentIndex + 1);

    }

}
