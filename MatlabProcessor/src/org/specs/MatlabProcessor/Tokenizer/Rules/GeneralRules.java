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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.specs.MatlabIR.MatlabParsingUtils;
import org.specs.MatlabIR.Exceptions.CodeParsingException;
import org.specs.MatlabIR.MatlabLanguage.ClassWord;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.InvokeNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentSingleSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.FieldAccessSeparatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.FunctionHandleSymbolNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.QuestionMarkSymbol;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SubscriptSeparatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.TempNodeFactory;
import org.specs.MatlabIR.Processor.TreeTransformException;
import org.specs.MatlabProcessor.Tokenizer.TokenizerRule;
import org.specs.MatlabProcessor.Tokenizer.TokenizerUtils;
import org.specs.MatlabProcessor.Tokenizer.TokenizerState.TokenizerState;

import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.utilities.StringSlice;

/**
 * @author Joao Bispo
 * 
 */
public class GeneralRules {

    public static List<TokenizerRule> getRuleSet(LanguageMode languageMode) {
        List<TokenizerRule> ruleset = new ArrayList<>();

        // According to data from mining a repository, the most common node are identifiers.
        // Using it as the first rule to check iteratively
        ruleset.add(identifierRule(languageMode));
        ruleset.add(NumberRules.numberRule());

        // Last rule, captures any other symbols that may appear
        ruleset.add(defaultRule());

        return ruleset;
    }

    public static Map<Character, TokenizerRule> getCharRuleset(LanguageMode languageMode) {
        Map<Character, TokenizerRule> charRuleset = new HashMap<>();

        charRuleset.put('%', commentRule("%"));
        if (languageMode == LanguageMode.OCTAVE) {
            charRuleset.put('#', commentRule("#"));
            charRuleset.put('"', octaveStringRule());
        } else {
            charRuleset.put('!', exclamationRule());
            charRuleset.put('"', matlabStringRule());
        }
        charRuleset.put('?', questionMarkRule());
        charRuleset.put(',', commaRule());
        charRuleset.put(';', semicolonRule());
        charRuleset.put('=', equalRule());
        charRuleset.put('\'', quoteRule());
        charRuleset.put('.', dotRule());
        charRuleset.put('\\', backlashRule());
        charRuleset.put(' ', spaceRule());
        charRuleset.put('\t', spaceRule());

        charRuleset.put('<', OperatorRules.relationalRule());
        charRuleset.put('>', OperatorRules.relationalRule());
        charRuleset.put('^', OperatorRules.caretRule());
        charRuleset.put(':', OperatorRules.colonRule());
        charRuleset.put('+', OperatorRules.signalRule());
        charRuleset.put('-', OperatorRules.signalRule());
        charRuleset.put('*', OperatorRules.mulDivRule());
        charRuleset.put('/', OperatorRules.mulDivRule());
        charRuleset.put('&', OperatorRules.andRule());
        charRuleset.put('|', OperatorRules.orRule());
        charRuleset.put('~', OperatorRules.tildeRule());
        if (languageMode == LanguageMode.OCTAVE) {
            charRuleset.put('!', OperatorRules.octaveExclamationRule());
        }

        charRuleset.put('(', ParenthesisRules.parenStartRule());
        charRuleset.put('[', ParenthesisRules.parenStartRule());
        charRuleset.put('{', ParenthesisRules.parenStartRule());
        charRuleset.put(')', ParenthesisRules.parenEndRule());
        charRuleset.put(']', ParenthesisRules.parenEndRule());
        charRuleset.put('}', ParenthesisRules.parenEndRule());

        return charRuleset;

    }

    /**
     * @return
     */
    /*
    public static List<TokenizerRule> ruleset() {
    List<TokenizerRule> rules = new ArrayList<TokenizerRule>();
    
    // First rule, to guarantee that tests on current line have at least on
    // character.
    rules.add(emptyStringRule());
    rules.add(commentRule());
    rules.add(commaRule());
    rules.add(semicolonRule());
    rules.add(equalRule());
    rules.add(quoteRule());
    rules.add(dotRule());
    rules.add(backlashRule());
    rules.add(OperatorRules.relationalRule());
    rules.add(OperatorRules.caretRule());
    rules.add(OperatorRules.colonRule());
    rules.add(OperatorRules.signalRule());
    rules.add(OperatorRules.mulDivRule());
    
    rules.add(OperatorRules.andRule());
    rules.add(OperatorRules.orRule());
    rules.add(OperatorRules.tildeRule());
    
    rules.add(NumberRules.numberRule());
    
    rules.add(ParenthesisRules.parenStartRule());
    rules.add(ParenthesisRules.parenEndRule());
    
    // rules.add(FunctionRules.functionRule());
    rules.add(reservedWordsRule());
    rules.add(identifierRule());
    
    return rules;
    }
    */

    // public static final Map<String, TokenType> reservedWords = new
    // HashMap<String, TokenType>();
    /*
        static {
    	addKeyword(reservedWords, TokenType.breakReserved);
    	addKeyword(reservedWords, TokenType.caseReserved);
    	addKeyword(reservedWords, TokenType.caseReserved);
    	addKeyword(reservedWords, TokenType.classdef);
    	addKeyword(reservedWords, TokenType.continueReserved);
    	addKeyword(reservedWords, TokenType.elseReserved);
    	addKeyword(reservedWords, TokenType.elseif);
    	addKeyword(reservedWords, TokenType.end);
    	addKeyword(reservedWords, TokenType.forReserved);
    	addKeyword(reservedWords, TokenType.global);
    	addKeyword(reservedWords, TokenType.ifReserved);
    	addKeyword(reservedWords, TokenType.otherwise);
    	addKeyword(reservedWords, TokenType.parfor);
    	addKeyword(reservedWords, TokenType.persistent);
    	addKeyword(reservedWords, TokenType.returnReserved);
    	addKeyword(reservedWords, TokenType.spmd);
    	addKeyword(reservedWords, TokenType.switchReserved);
    	addKeyword(reservedWords, TokenType.tryReserved);
    	addKeyword(reservedWords, TokenType.whileReserved);
    
    	addKeyword(reservedWords, TokenType.functionDeclaration);
        }
    */
    /**
     * @return
     */
    private static TokenizerRule backlashRule() {
        return new TokenizerRule() {

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith("\\");
            }

            @Override
            public void apply(TokenizerState state) throws TreeTransformException {
                // Assume '\' (left division)
                RulesUtils.applyOperatorRule("\\", state);
            }

        };
    }

    /**
     * @return
     */
    private static TokenizerRule spaceRule() {
        return new TokenizerRule() {

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith(" ") || state.getCurrentLine().startsWith("\t");
            }

            @Override
            public void apply(TokenizerState state) throws TreeTransformException {
                // Spaces should only appear inside square brackets

                // Trim
                StringSlice workline = state.getCurrentLine();
                workline = workline.trim();
                state.setCurrentLine(workline);

                if (state.isSpaceSensible()) {
                    // Add space token
                    MatlabNode spaceToken = TempNodeFactory.newSpace();
                    state.getCurrentTokens().add(spaceToken);
                }
            }

        };
    }

    /**
     * When string starts with ' (the prime symbol). Differentiates between string and transpose.
     * 
     * @return
     */
    public static TokenizerRule quoteRule() {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public void apply(TokenizerState state) throws TreeTransformException {

                // Check if transpose
                MatlabNode node = state.getLastNode();
                if (node != null) {

                    // Check if token is valid for transpose
                    boolean isTransposable = TokenizerUtils.isTransposable(node);

                    // 2nd change: is token is an operator and is a transpose, is valid
                    if (node instanceof OperatorNode) {
                        assert isTransposable == false : "Must be false";
                        MatlabOperator op = ((OperatorNode) node).getOp();

                        if (op == MatlabOperator.Transpose || op == MatlabOperator.ComplexConjugateTranspose) {
                            isTransposable = true;
                        }
                    }

                    if (isTransposable) {
                        // Transform transpose in operator
                        RulesUtils.applyOperatorRule("'", state);
                        return;
                    }
                }

                // Interpret as a string
                // String rule
                ParenthesisRules.applyStringRule(state);
            }

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith("'");
            }
        };

        return rule;
    }

    public static TokenizerRule octaveStringRule() {
        return new TokenizerRule() {

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith("\"");
            }

            @Override
            public void apply(TokenizerState state) throws TreeTransformException {
                StringSlice line = state.getCurrentLine();

                StringBuilder value = new StringBuilder();
                for (int offset = 1; offset < line.length(); ++offset) {
                    char ch = line.charAt(offset);

                    if (ch == '\\') {
                        throw new NotImplementedException("Escaped characters in Octave-style strings");
                    }

                    if (ch == '"') {
                        state.pushProducedToken(MatlabNodeFactory.newCharArray(value.toString()), offset + 1);

                        return;
                    }

                    value.append(ch);
                }
            }
        };
    }

    public static TokenizerRule matlabStringRule() {
        return new TokenizerRule() {

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith("\"");
            }

            @Override
            public void apply(TokenizerState state) throws TreeTransformException {
                StringSlice line = state.getCurrentLine();

                StringBuilder value = new StringBuilder();
                for (int offset = 1; offset < line.length(); ++offset) {
                    char ch = line.charAt(offset);

                    if (ch == '\\') {
                        // FIXME: Check if escape syntax is correct.
                        throw new NotImplementedException("Escaped characters in MATLAB strings");
                    }

                    if (ch == '"') {
                        state.pushProducedToken(MatlabNodeFactory.newString(value.toString()), offset + 1);

                        return;
                    }

                    value.append(ch);
                }

                // If arrives here, means string has not been closed
                throw new RuntimeException("Found MATLAB string that has not been closed: '" + line + "'");
            }
        };
    }

    /**
     * Applies
     * 
     * @param state
     */
    /*
    public static void applyTransposeConjRule(TokenizerState state)
        throws ParsingException {
    // state.advanceAndTrim("'");
    
    MatlabToken token = new GenericMatlabToken(TokenType.transposeConj, null, null);
    state.pushProducedToken(token, "'");
    }
    */

    /**
     * When string is empty, finishes current statement.
     * 
     * <p>
     * Never produces tokens.
     * 
     * @return
     */
    public static TokenizerRule emptyStringRule() {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public void apply(TokenizerState state) throws TreeTransformException {
                // Finish current statement. Since there is no semi-colon at the
                // end of the statement, display results
                // if (!state.isUnfinishedProcessing()) {
                state.finishStatement(true);
                state.setCurrentLineEmpty();
                // }

                // Ignore rest of the line
                // state.setCurrentLine(null);

            }

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().isEmpty();
                // return !state.hasLine();
            }
        };

        return rule;
    }

    /**
     * A comma representing the end of a statement. When found, finishes current statement.
     * 
     * @return
     */
    public static TokenizerRule commaRule() {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith(",");
            }

            @Override
            public void apply(TokenizerState state) throws TreeTransformException {

                // If not inside parenthesis, remove the comma and finish
                // statement
                if (state.getParenthesisStack().getCurrentParenthesis() == null) {
                    StringSlice workline = state.getCurrentLine();
                    workline = TokenizerUtils.advance(workline, ",", false);
                    workline = workline.trim();
                    state.setCurrentLine(workline);
                    state.finishStatement(true);
                    return;
                }

                // If inside paren, insert subscript separator

                // There can't be two consecutive subscript separators
                MatlabNode previousToken = state.getLastNode();
                if (previousToken != null && previousToken instanceof SubscriptSeparatorNode) {
                    throw new CodeParsingException("There can't be two consecutive subscript (,) separators");
                }

                MatlabNode token = TempNodeFactory.newSubscriptSeparator();
                state.pushProducedToken(token, ",");
                return;
            }

        };

        return rule;
    }

    /**
     * A semicolon representing the end of a statement. Supresses display of results. When found, finishes current
     * statement.
     * 
     * @return
     */
    public static TokenizerRule semicolonRule() {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith(";");
            }

            @Override
            public void apply(TokenizerState state) throws TreeTransformException {

                String advanceString = ";";
                // If inside square brackets, finishes row

                // TokenType currentParenthesis = state.getCurrentParenthesis();
                // MatlabNodeType currentParenthesis = state.getParenthesisStack().getCurrentParenthesis();

                if (state.getParenthesisStack().isSpaceSensible()) {
                    // if (currentParenthesis != null) {
                    // if (currentParenthesis == MType.SquareBracketsStart || currentParenthesis == MType.CellStart) {
                    TokenizerUtils.processMatrixRow(state, advanceString, true);
                    return;
                    // }
                }

                // state.advanceAndTrim(";");
                // Finish statement
                StringSlice workline = state.getCurrentLine();
                // workline = TokenizerUtils.advanceAndTrim(workline,
                // advanceString, false);
                workline = TokenizerUtils.advance(workline, advanceString, false);
                workline = workline.trim();

                state.finishStatement(false);
                state.setCurrentLine(workline);

            }

        };

        return rule;
    }

    /**
     * When string is comment, finishes current statement.
     * 
     * <p>
     * Never produces tokens.
     * 
     * @return
     */
    public static TokenizerRule commentRule(String commentStart) {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public void apply(TokenizerState state) throws TreeTransformException {

                // Get the comment text
                StringSlice comment = state.getCurrentLine().substring(commentStart.length());
                /*
                		// Build Comment Token
                		MatlabNode token = new GenericMNode(MType.Comment, null, comment);
                
                		// Create statement with the comment
                		int commentLineNumber = state.getCurrentLineNumber();
                		MatlabNode statement = MatlabNodeFactory.newStatement(Arrays.asList(token), commentLineNumber, false);
                		*/
                CommentSingleSt statement = StatementFactory.newComment(state.getCurrentLineNumber(),
                        comment.toString());

                // Finish current statement.
                // If arrived here and there is a statement before, it means that there was no ';' and we should display
                // the result
                state.finishStatement(true);
                // Ignore rest of the line
                // state.setCurrentLine(null);
                state.setCurrentLineEmpty();

                // Check index at which comment should be added
                // Right now, the statements in the state are flush after every
                // line. Have to check if that is always true. If it is, this
                // method
                // is not needed.
                /*
                int commentIndex = TokenizerUtils.calcCommentIndex(commentLineNumber,
                	state.getStatementTokens());
                */
                // System.out.println("Comment:"+comment);
                // System.out.println("End of list:"+state.getStatementTokens().size());
                // System.out.println("Index:"+commentIndex);

                // Add statement
                // state.getStatementTokens().add(statement);

                // In case there are already other statements in the queue, add
                // the comment first, to appear above the statements (in case
                // the comment is inline)
                // state.getStatementTokens().add(commentIndex, statement);
                state.getStatementTokens().add(statement);
            }

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith(commentStart);
            }
        };

        return rule;
    }

    /**
     * Matlab assignment.
     * 
     * <p>
     * Produces an assignment token.
     * 
     * @return
     */
    public static TokenizerRule equalRule() {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public void apply(TokenizerState state) throws TreeTransformException {

                // '=='
                if (state.getCurrentLine().startsWith("==")) {
                    // state.advanceAndTrim("==");
                    RulesUtils.applyOperatorRule("==", state);
                    /*
                    MatlabToken token = new GenericMatlabToken(
                        TokenType.relationalEqual, null, null);
                    state.pushProducedToken(token, "==");
                    */
                    return;
                }

                // Assume '='
                // state.advanceAndTrim("=");
                // MatlabNode token = new GenericMNode(MType.Assignment, null, "=");
                MatlabNode token = TempNodeFactory.newAssignment();
                state.pushProducedToken(token, "=");
            }

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith("=");
                /*
                if (!state.getCurrentLine().startsWith("=")) {
                    return false;
                }
                
                try {
                    if (state.getCurrentLine().charAt("=".length()) != '=') {
                	return true;
                    }
                } catch (IndexOutOfBoundsException e) {
                    return false;
                }
                
                return false;
                */
            }
        };

        return rule;
    }

    /**
     * Matlab Identifier.
     * 
     * * TODO: Change to rule when starting with word. Can also detect i/j as complex numbers
     * 
     * <p>
     * Produces an identifier token.
     * 
     * @param languageMode
     * 
     * @return
     */
    public static TokenizerRule identifierRule(LanguageMode languageMode) {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public boolean isAppliable(TokenizerState state) {
                if (!state.hasLine()) {
                    return false;
                }

                char firstChart = state.getCurrentLine().charAt(0);
                return Character.isLetter(firstChart);
            }

            @Override
            public void apply(TokenizerState state) throws TreeTransformException {
                StringSlice workline = null;

                // ["a"-"z","A"-"Z"](["_","a"-"z","A"-"Z","0"-"9"])*>
                workline = state.getCurrentLine();
                StringBuilder builder = new StringBuilder();
                int charIndex = 0;

                // try {
                char currentChar = workline.charAt(charIndex);

                assert Character.isLetter(currentChar) : "Expected a letter";
                // if (!Character.isLetter(currentChar)) {
                // throw new ParserErrorException("Expected a letter, got a '" + currentChar + "'");
                // }
                builder.append(currentChar);
                charIndex += 1;
                int worklineChars = workline.length();
                if (charIndex < worklineChars) {
                    currentChar = workline.charAt(charIndex);
                    while (MatlabParsingUtils.isIdentifierChar(currentChar)) {
                        builder.append(currentChar);
                        charIndex += 1;

                        // Break if no more chars
                        if (charIndex >= worklineChars) {
                            break;
                        }

                        currentChar = workline.charAt(charIndex);
                    }
                }
                // } catch (IndexOutOfBoundsException e) {
                // Just continue, the id will be stored in the builder
                // }

                String id = builder.toString();

                MatlabNode lastNode = state.getLastNonSpaceNode();

                // If previous node is a field access separator, build a field access
                // Done before reserved word, because fields can have reserve word names
                if (lastNode instanceof FieldAccessSeparatorNode) {

                    // Remove field access separator, get not before that

                    state.removeLastNonSpace();
                    MatlabNode left = state.getLastNonSpaceNode();
                    // Create field access with node before last node
                    MatlabNode fieldAccess = MatlabNodeFactory.newFieldAccess(left,
                            MatlabNodeFactory.newIdentifier(id));

                    // Remove previous non-space node
                    state.removeLastNonSpace();
                    state.pushProducedToken(fieldAccess, id);

                    return;

                }

                // Check if string is a reserved word
                ReservedWord word = ReservedWord.getReservedWord(id, languageMode);
                if (word != null) {
                    MatlabNode token = MatlabNodeFactory.newReservedWord(word);
                    state.pushProducedToken(token, id);

                    // Check if reserved word represents a statement
                    if (word.isStatementWord()) {
                        RulesUtils.finishStatement(state);
                    }

                    // If classdef word and there is only one current node, activate class mode
                    if (word == ReservedWord.Classdef) {
                        if (state.getCurrentTokens().size() != 1) {
                            throw new CodeParsingException("Found 'classdef' not in the beginning of a statement");
                        }

                        state.setInsideClassBlock();
                    }

                    return;
                }

                // If previous non-space token is a FunctionHandleSymbol, create a FunctionHandle
                if (lastNode instanceof FunctionHandleSymbolNode) {
                    // Create handle
                    MatlabNode fHandle = MatlabNodeFactory.newFunctionHandle(id);

                    // Remove previous non-space node
                    state.removeLastNonSpace();
                    state.pushProducedToken(fHandle, id);
                    return;
                }

                // If previous non-space token is a QuestionMarkSymbol, create a MetaClass
                if (lastNode instanceof QuestionMarkSymbol) {
                    // Create handle
                    MatlabNode inferiorClass = MatlabNodeFactory.newMetaClass(id);

                    // Remove previous non-space node
                    state.removeLastNonSpace();
                    state.pushProducedToken(inferiorClass, id);
                    return;
                }

                // If first token of statement, is in class mode, and is a class word, create ClassWordNode
                if (state.isClassContext() && state.getCurrentTokens().isEmpty() && ClassWord.isClassWord(id)) {
                    ClassWord classWord = ClassWord.getClassWord(id);
                    state.pushProducedToken(MatlabNodeFactory.newClassWord(classWord), id);

                    return;
                }

                // Otherwise, create identifier
                MatlabNode idToken = MatlabNodeFactory.newIdentifier(id);

                state.pushProducedToken(idToken, id);
                // state.advanceAndTrim(id);
            }

        };

        return rule;
    }

    /**
     * Matlab .
     * 
     * <p>
     * Can represent several cases: decimal number, element-wise operators, field accesses.
     * 
     * @return
     */
    public static TokenizerRule dotRule() {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public void apply(TokenizerState state) throws TreeTransformException {

                // '...'
                if (state.getCurrentLine().startsWith("...")) {
                    // Add a space if last seen token is not a '+' or a '-'
                    state.pushProducedToken(TempNodeFactory.newSpace(), "");

                    state.setCurrentLineEmpty();
                    state.setUnfinishedProcessing(true);
                    return;
                }

                StringSlice workline = state.getCurrentLine();
                workline = workline.substring(".".length()).trim();

                // If no more characters, just push the dot as an unknown symbol
                if (workline.isEmpty()) {
                    // Push the dot as an unknown symbol
                    state.pushProducedToken(TempNodeFactory.newUnknownSymbol("."), ".");
                    return;
                }

                // Determine the kind of .
                char aChar = workline.charAt(0);

                if (Character.isDigit(aChar)) {
                    NumberRules.numberRule().apply(state);
                    return;
                }

                if (OperatorRules.elementOperators.contains(aChar)) {
                    RulesUtils.applyElementOperator(state);
                    return;
                }

                if (Character.isLetter(aChar)) {
                    RulesUtils.applyFieldAccess(state);
                    return;
                }

                if (aChar == '(') {
                    RulesUtils.applyDynamicFieldAccess(state);
                    return;
                }

                // Non-Conjugate Transpose .'
                if (aChar == '\'') {
                    // Transform transpose in operator
                    RulesUtils.applyOperatorRule(".'", state);
                    return;
                }

                // Transform '..' into a String token
                if (state.getCurrentLine().startsWith("..")) {
                    String doubleDot = "..";
                    MatlabNode token = MatlabNodeFactory.newCharArray(doubleDot);
                    state.pushProducedToken(token, doubleDot);
                    return;
                }

                // Push the dot as an unknown symbol
                state.pushProducedToken(TempNodeFactory.newUnknownSymbol("."), ".");
            }

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith(".");
            }
        };

        return rule;
    }

    /**
     * Applied when no other rule applies.
     * 
     * <p>
     * Always produces a Never produces tokens.
     * 
     * @return
     */
    public static TokenizerRule defaultRule() {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public void apply(TokenizerState state) throws TreeTransformException {

                // Create undefined token with symbol
                String symbol = state.getCurrentLine().substring(0, 1).toString();

                MatlabNode node = getSymbol(symbol);

                // Push token
                state.pushProducedToken(node, symbol);
            }

            private MatlabNode getSymbol(String symbol) {
                if (symbol.equals("@")) {
                    return TempNodeFactory.newFunctionHandlerSymbol();
                }

                return TempNodeFactory.newUnknownSymbol(symbol);
            }

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.hasLine();
            }
        };

        return rule;
    }

    /**
     * When character is '!', represents an invoke node. Acts like a comment, everything after is treated as a literal.
     * 
     * @return
     */
    public static TokenizerRule exclamationRule() {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public void apply(TokenizerState state) throws TreeTransformException {

                // Build invoke node
                InvokeNode invoke = MatlabNodeFactory.newInvoke(state.getCurrentLine().substring("!".length())
                        .toString());
                state.pushProducedToken(invoke, state.getCurrentLine().toString());
            }

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith("!");
            }
        };

        return rule;
    }

    /**
     * When character is '?', marks an inferior class.
     * 
     * @return
     */
    public static TokenizerRule questionMarkRule() {
        TokenizerRule rule = new TokenizerRule() {

            @Override
            public void apply(TokenizerState state) throws TreeTransformException {

                // Build invoke node
                QuestionMarkSymbol questionMark = TempNodeFactory.newQuestionMark();
                state.pushProducedToken(questionMark, "?");
            }

            @Override
            public boolean isAppliable(TokenizerState state) {
                return state.getCurrentLine().startsWith("?");
            }
        };

        return rule;
    }
}
