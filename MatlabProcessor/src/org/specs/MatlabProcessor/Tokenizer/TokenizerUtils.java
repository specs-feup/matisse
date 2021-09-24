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

package org.specs.MatlabProcessor.Tokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CellAccessNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CellNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.DynamicFieldAccessNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.FieldAccessNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNumberNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatrixNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OutputsNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ParenthesisNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.RowNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SpaceNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SquareBracketsStartNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SubscriptSeparatorNode;
import org.specs.MatlabIR.Processor.TreeTransformException;
import org.specs.MatlabProcessor.Tokenizer.TokenizerState.TokenizerState;

import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.specs.util.classmap.ClassSet;
import pt.up.fe.specs.util.treenode.TreeNodeIndexUtils;
import pt.up.fe.specs.util.utilities.StringSlice;

/**
 * @author Joao Bispo
 * 
 */
public class TokenizerUtils {

    public final static ClassSet<MatlabNode> IS_TRANSPOSABLE;

    static {
	IS_TRANSPOSABLE = new ClassSet<>();

	TokenizerUtils.IS_TRANSPOSABLE.add(IdentifierNode.class);
	TokenizerUtils.IS_TRANSPOSABLE.add(MatlabNumberNode.class);
	TokenizerUtils.IS_TRANSPOSABLE.add(MatrixNode.class);
	TokenizerUtils.IS_TRANSPOSABLE.add(OutputsNode.class);
	TokenizerUtils.IS_TRANSPOSABLE.add(ParenthesisNode.class);
	// TokenizerUtils.IS_TRANSPOSABLE.add(CompositeAccessCallNode.class);
	TokenizerUtils.IS_TRANSPOSABLE.add(AccessCallNode.class);
	// TokenizerUtils.IS_TRANSPOSABLE.add(TransposeConjNode.class);
	// TokenizerUtils.IS_TRANSPOSABLE.add(TransposeNonConjNode.class);
	TokenizerUtils.IS_TRANSPOSABLE.add(CellNode.class);
	TokenizerUtils.IS_TRANSPOSABLE.add(CellAccessNode.class);
	TokenizerUtils.IS_TRANSPOSABLE.add(FieldAccessNode.class);
	TokenizerUtils.IS_TRANSPOSABLE.add(DynamicFieldAccessNode.class);
    }

    public static StringSlice advance(StringSlice string, String advancement, boolean optional) {

	// Check
	if (!string.startsWith(advancement)) {
	    if (!optional) {
		throw new RuntimeException("Current line '" + string + "' does not start with '"
			+ advancement + "'.");
		// LoggingUtils.msgWarn("String '" + string + "' does not start with '"
		// + advancement + "'.");
		// return null;
	    }

	    return string;
	}

	return advance(string, advancement.length());
    }

    public static StringSlice advance(StringSlice string, int numChars) {
	string = string.substring(numChars);
	return string;
    }

    /**
     * 
     * 
     * @param string
     * @param advancement
     * @param optional
     *            If the advancement should be optional or not
     * @return
     */
    /*
    public static String advanceAndTrim(String string, String advancement,
        boolean optional) {
    /*
    // Check
    if (!string.startsWith(advancement)) {
        if (!optional) {
    	LoggingUtils.msgWarn("String '" + string
    		+ "' does not start with '" + advancement + "'.");
    	return null;
        } else {
    	return string;
        }
    }*/

    // return advanceAndTrim(string, advancement.length());
    // }

    /*
    public static String advanceAndTrim(String string, int numChars) {
    string = advance(string, numChars);
    // string = string.substring(numChars);
    string = string.trim();
    return string;
    }
    */

    /**
     * Calls a sequence of rules that produce tokens in the state and adds them to the tokens list.
     * 
     * <p>
     * Not all rules in the sequence need to apply, supports optional rules.
     * 
     * @param rules
     * @param tokens
     * @return
     */
    public static void produceTokens(TokenizerState state, List<TokenizerRule> rules,
	    List<MatlabNode> tokens) throws TreeTransformException {

	for (TokenizerRule rule : rules) {
	    if (rule.isAppliable(state)) {
		rule.apply(state);
	    }

	}
    }

    /**
     * Returns true when a ' , used after this token, represents transpose.
     * 
     * @param token
     * @return
     */
    public static boolean isTransposable(MatlabNode token) {
	return TokenizerUtils.IS_TRANSPOSABLE.contains(token);
    }

    private static List<String> commandCancellationOperators = Arrays.asList(
	    "<",
	    "<=",
	    ">",
	    ">=",
	    "==",
	    "~=",
	    "+",
	    "-",
	    "*",
	    "\\",
	    ".*",
	    ".\\",
	    "./",
	    "^",
	    ".^",
	    ":",
	    "&",
	    "|",
	    "&&",
	    "||");

    /**
     * @param tokenizerState
     * @param advanceAndTrim
     * @return
     */
    public static boolean isSpaceInsertable(TokenizerState state, int numChars) {
	// Before advancing and trimming, check if there
	// is a whitespace after the string

	StringSlice workline = state.getCurrentLine().substring(numChars);

	// if (!workline.startsWith(" ") && !workline.startsWith("\t")) {
	if (!workline.startsWith(" ") && !workline.startsWith("\t")) {
	    return false;
	}

	if (state.isSpaceSensible()) {
	    return true;
	}

	workline = workline.trim();

	if (workline.isEmpty() || workline.startsWith("(")) {
	    return false;
	}

	for (String commandCancellationOperator : TokenizerUtils.commandCancellationOperators) {
	    if (workline.startsWith(commandCancellationOperator)) {
		StringSlice afterOperator = workline.substring(commandCancellationOperator.length());
		if (!afterOperator.startsWith(" ") && !afterOperator.startsWith("\t")) {
		    return true;
		}
		afterOperator = afterOperator.trim();

		if (afterOperator.isEmpty()) {
		    return true;
		}

		char firstChar = afterOperator.charAt(0);

		if (firstChar == '(' || firstChar == '[' || firstChar == ']' ||
			firstChar == '+' || firstChar == '-' ||
			SpecsStrings.isDigitOrLetter(firstChar)) {
		    // (firstChar >= '0' && firstChar <= '9') ||
		    // (firstChar >= 'a' && firstChar <= 'z') || (firstChar >= 'A' && firstChar <= 'Z')) {
		    return true;
		}

		return false;
	    }
	}

	if (workline.startsWith("=")) {
	    // Starts with = but not ==
	    return false;
	}

	return true;
    }

    /**
     * @param state
     * @return
     */
    public static boolean isSignalParsable(TokenizerState state) {
	Class<? extends MatlabNode> type = state.getParenthesisStack().getCurrentParenthesis();
	if (type == null) {
	    return false;
	}

	if (!(type.equals(SquareBracketsStartNode.class))) {
	    return false;
	}

	// Check if previous token is space or open square
	// brackets
	// System.out.println("Previous tokens:");
	// System.out.println(state.getCurrentTokens());
	MatlabNode previousToken = state.getLastNode();
	boolean validToken = previousToken instanceof SpaceNode
		|| previousToken instanceof SquareBracketsStartNode;

	if (!validToken) {
	    return false;
	}

	return true;

    }

    /**
     * @param state
     */
    public static void processMatrixRow(TokenizerState state, String advance, boolean startRow)
	    throws TreeTransformException {

	List<MatlabNode> currentTokens = state.getCurrentTokens();
	Optional<Integer> lastRowIndexTry = TreeNodeIndexUtils.lastIndexOf(currentTokens, RowNode.class);
	if (!lastRowIndexTry.isPresent()) {
	    throw new TreeTransformException("Could not find 'row':" + currentTokens);
	}

	int lastRowIndex = lastRowIndexTry.get();

	// Build list with children of row
	List<MatlabNode> rowTokens = new ArrayList<>(currentTokens.subList(
		lastRowIndex + 1, currentTokens.size()));

	// System.out.println("ROW TOKENS:" + rowTokens);
	// Check if row tokens have only one elements, and is a space
	// In this case, ignore and do not create row

	// Remove tokens from current list, bracket and children tokens
	TokenizerUtils.remove(currentTokens, lastRowIndex, currentTokens.size());

	/*
	// Remove spaces from rowTokens, check if any token is left
	rowTokens = rowTokens.stream()
		.filter(node -> !(node instanceof SpaceNode))
		.collect(Collectors.toList());
	System.out.println("CLREAN TOKENS:" + rowTokens);
	// If no tokens left, return
	if (rowTokens.isEmpty()) {
	    return;
	}
	*/

	// Replace previous row object
	// MatlabNode previousRow = new GenericMNode(MType.Row, rowTokens, null);
	if (rowTokens.stream().anyMatch(node -> !(node instanceof SubscriptSeparatorNode))) {
	    // Only add if row tokens are not a single space
	    if (!(rowTokens.size() == 1 && rowTokens.get(0) instanceof SpaceNode)) {
		RowNode previousRow = MatlabNodeFactory.newRow(rowTokens);
		state.pushProducedToken(previousRow, "");
	    }

	}

	// Build new row object
	if (startRow) {
	    // MatlabNode newRow = new GenericMNode(MType.Row, null, null);
	    RowNode newRow = MatlabNodeFactory.newRow(Collections.emptyList());
	    state.pushProducedToken(newRow, advance);
	}
    }

    /**
     * @param currentTokens
     * @param startIndex
     *            Inclusive
     * @param endIndex
     *            Exclusive
     */
    public static void remove(List<MatlabNode> currentTokens, int startIndex, int endIndex) {

	for (int i = endIndex - 1; i >= startIndex; i--) {
	    currentTokens.remove(i);
	}

    }

    /**
     * For comments that are put in front of statements, returns the index corresponding to the first statement with
     * that line number. The following rules are applied:
     * <p>
     * - If the comment line number is greater than the line number of the last statement, returns the size of the list
     * (last position in the list)<br>
     * - If the comment line number is the same as the line number of the last statement, returns the index of the first
     * statement with that line number
     * 
     * @param commentLineNumber
     * @param statementTokens
     * @return
     */
    /*
    public static int calcCommentIndex(int commentLineNumber, List<MatlabNode> statementTokens) {
    	int currentIndex = statementTokens.size();
    	while (currentIndex > 1) {
    	    MatlabNode statement = statementTokens.get(currentIndex - 1);
    	    StatementData data = CompatibilityUtils.getData(statement);
    
    	    // This should never happen
    	    if (commentLineNumber < data.getLine()) {
    		LoggingUtils.msgWarn("Comment line (" + commentLineNumber
    			+ ") should never be lower than any other statement ("
    			+ data.getLine() + ")");
    		return currentIndex;
    	    }
    
    	    // Case where the comment line is below the last seen statement
    	    if (commentLineNumber > data.getLine()) {
    		return currentIndex;
    	    }
    
    	    // While they are in the same line, decrement currentIndex
    	    currentIndex -= 1;
    
    	}
    
    	return currentIndex;
    }
    */
}
