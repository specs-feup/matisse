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
import java.util.List;
import java.util.Map;

import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.CommentBlockStartSt;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.TempNodeFactory;
import org.specs.MatlabIR.Processor.TreeTransformException;
import org.specs.MatlabProcessor.Tokenizer.Rules.GeneralRules;
import org.specs.MatlabProcessor.Tokenizer.TokenizerState.TokenizerState;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.reporting.Reporter;
import pt.up.fe.specs.util.utilities.StringSlice;

/**
 * @author Joao Bispo
 * 
 */
public class MatlabLineTokenizer {

    private final Reporter reportService;
    private TokenizerState state;
    // Rules whose test is the first character of the current line
    private final Map<Character, TokenizerRule> charRules;
    private final List<TokenizerRule> rules;
    private StringBuilder sourceMatlab;
    private TokenizerResult previousResult;

    // private boolean verbose;

    /**
     * 
     */
    public MatlabLineTokenizer(LanguageMode languageMode, Reporter reportService) {
        this.reportService = reportService;

        rules = GeneralRules.getRuleSet(languageMode);
        charRules = GeneralRules.getCharRuleset(languageMode);
        reset();
        // verbose = false;
    }

    /**
     * @param verbose
     *            the verbose to set
     */
    /*
    public void setVerbose(boolean verbose) {
    this.verbose = verbose;
    }
    */

    /**
     * 
     */
    private void reset() {
        state = new TokenizerState(reportService);
        previousResult = null;
        clearSourceMatlab();
    }

    /**
     * @return the state
     */
    public TokenizerState getState() {
        return state;
    }

    public String getOriginalMatlabCode() {
        return sourceMatlab.toString();
    }

    private void clearSourceMatlab() {
        sourceMatlab = new StringBuilder();
    }

    /**
     * Transforms a MatlabLine into a parsed list of MatlabTokens lists.
     * 
     * <p>
     * If previous state was Success or Failed, clears the original code when the next line is given.
     * 
     * <p>
     * If line could not be tokenized due to an exception, null is returned.
     * <p>
     * If line is part of a multi-line statement, returns an empty list.
     * 
     * @param matlabLine
     * @param lineNumber
     * @return
     */
    public List<StatementNode> getTokens(StringSlice line, int lineNumber) {
        // If previous result was other than UnfinishedProcessing, clear
        // original code
        if (previousResult != TokenizerResult.UnfinishedProcessing) {
            clearSourceMatlab();
        }

        // Tokenize line
        TokenizerResult result = tokenizeLine(line, lineNumber);
        // Save result
        previousResult = result;
        // Add line to current source
        sourceMatlab.append(line).append("\n");

        // If Tokenizer failed, return null
        if (result == TokenizerResult.Failed) {
            return null;
        }

        // If unfinished, return empty list
        if (result == TokenizerResult.UnfinishedProcessing) {
            return new ArrayList<>();
        }

        List<StatementNode> statementTokens = getState().getStatementTokens();
        getState().clearStatementTokens();

        return statementTokens;
    }

    /**
     * Tokenizes a new line. Returns Success if line could be successfully tokenized.
     * 
     * @param matlabLine
     * @param lineNumber
     * @return
     */
    private TokenizerResult tokenizeLine(StringSlice matlabLine, int lineNumber) {
        state.setCurrentLine(matlabLine.trim());
        state.setCurrentLineNumber(lineNumber);

        // Check block comments
        TokenizerResult blockCommentResult = checkBlockComment();
        if (blockCommentResult != null) {
            return blockCommentResult;
        }

        TokenizerResult result = null;
        // while (state.getCurrentLine() != null) {
        while (state.hasLine()) {
            result = tokenizeStatement();

            if (result == TokenizerResult.Failed) {
                // Clean state
                state.clearTempData();
                return result;
            }
        }

        // HACK
        if (!state.isUnfinishedProcessing()) {
            if (state.getParenthesisStack().isInsideSquareBrackets() ||
                    state.getParenthesisStack().isInsideCurlyBrackets()) {

                // Despite its name, "processMatrixRow" seems to be used for cell arrays too.
                TokenizerUtils.processMatrixRow(state, "", true);
            }
        }

        return result;
    }

    /**
     * 
     * @return true if there was an action an line was processed
     */
    private TokenizerResult checkBlockComment() {
        try {
            // Check if line is the start of a comment block
            // Line can only contain %{, after trimming
            if (state.getCurrentLine().trim().equalsString("%{")) {
                // Finish current statement
                state.finishStatement(false);

                // Ignore rest of the line
                // state.setCurrentLine(null);
                state.setCurrentLineEmpty();

                // Set state as being inside comment block
                state.incrementCommentBlockLevel();

                // Add comment block start marker
                // Integer commentLineNumber = state.getCurrentLineNumber();
                // MatlabNode startBlock = new GenericMNode(MType.CommentBlockStart, null, commentLineNumber);
                StatementNode startBlock = TempNodeFactory.newCommentBlockStart(state.getCurrentLineNumber());
                state.getStatementTokens().add(startBlock);
                return TokenizerResult.UnfinishedProcessing;
            }

            // If inside comment block, line is automatically a comment
            if (state.isInsideCommentBlock()) {
                return processCommentBlock(state);
            }
        } catch (TreeTransformException e) {
            SpecsLogs.warn("Error message:\n", e);
        }

        return null;
    }

    private TokenizerResult tokenizeStatement() {

        // Try to apply the rules until the there are no more characters to
        // parse. If none of the rules apply
        // in a given passage, an exception is thrown by applyRules.
        try {
            // while (state.getCurrentLine() != null) {
            while (state.hasLine()) {
                applyRules();
            }
        } catch (TreeTransformException e) {
            throw new RuntimeException("MatlabTokenizer: Could not tokenize line " + state.getCurrentLineNumber()
                    + ". Stopped at:\n" + state.getCurrentLine(), e);

            // if (verbose) {
            // LoggingUtils
            // .msgLib("MatlabTokenizer: Could not tokenize line " + state.getCurrentLineNumber()
            // + ". Stopped at:\n" + state.getCurrentLine());
            // }
            // return false;
            // return TokenizerResult.Failed;
        }

        if (state.isUnfinishedProcessing()) {
            return TokenizerResult.UnfinishedProcessing;
        }
        return TokenizerResult.Success;

    }

    private static TokenizerResult processCommentBlock(TokenizerState state) throws TreeTransformException {
        StringSlice line = state.getCurrentLine();

        // Finish current statement
        state.finishStatement(false);
        // Ignore rest of the line
        state.setCurrentLineEmpty();
        // state.setCurrentLine(null);

        // Check if end of comment block
        if (line.trim().equalsString("%}")) {
            buildCommentBlock(state);
            state.decrementCommentBlockLevel(false);

            // If still inside comment block, return unfinished.
            if (state.isInsideCommentBlock()) {
                return TokenizerResult.UnfinishedProcessing;
            }

            return TokenizerResult.Success;

        }
        /*
        	// Add line as a comment
        	MatlabNode token = new GenericMNode(MType.Comment, null, line);
        	// MatlabNode token = MatlabNodeFactory.newCommentStatements(line, lineNumber)new GenericMNode(MType.Comment,
        	// null, line);
        
        	// Create statement with the comment
        	int commentLineNumber = state.getCurrentLineNumber();
        	MatlabNode statement = MatlabNodeFactory.newStatement(Arrays.asList(token), commentLineNumber, true);
        */
        StatementNode statement = StatementFactory.newComment(state.getCurrentLineNumber(), line.toString());
        // Add statement
        state.getStatementTokens().add(statement);

        return TokenizerResult.UnfinishedProcessing;

    }

    private static void buildCommentBlock(TokenizerState state) {

        // Find start of the block
        int commentBlockStartIndex = state.getStatementTokens().size() - 1;
        MatlabNode commentBlockStart = state.getStatementTokens().get(commentBlockStartIndex);

        // while (commentBlockStart.getType() != MType.CommentBlockStart) {
        while (!(commentBlockStart instanceof CommentBlockStartSt)) {
            if (commentBlockStartIndex == 0) {
                throw new RuntimeException("Could not find the start of a comment block");
            }
            commentBlockStartIndex--;
            commentBlockStart = state.getStatementTokens().get(commentBlockStartIndex);
        }

        // Collect tokens until block start
        List<StatementNode> comments = state.getStatementTokens().subList(commentBlockStartIndex + 1,
                state.getStatementTokens().size());

        // Build Comment Block
        Integer lineNumber = ((CommentBlockStartSt) commentBlockStart).getLine();
        StatementNode commentBlock = StatementFactory.newCommentBlockStatement(comments, lineNumber);

        // Remove comments, replace CommentBlockStart
        SpecsCollections.remove(state.getStatementTokens(), commentBlockStartIndex + 1, state.getStatementTokens()
                .size());
        state.getStatementTokens().set(commentBlockStartIndex, commentBlock);
    }

    /**
     * @param currentLine
     * @return
     */
    private void applyRules() throws TreeTransformException {
        // First, test if line is empty
        if (state.getCurrentLine().isEmpty()) {
            state.finishStatement(true);
            state.setCurrentLineEmpty();
            return;
        }

        // Try to apply a rule based on the first character
        TokenizerRule charRule = charRules.get(state.getCurrentLine().getFirstChar());
        if (charRule != null) {
            charRule.apply(state);
            return;
        }

        // Iterate over the remaining rules
        for (TokenizerRule rule : rules) {
            if (rule.isAppliable(state)) {
                /*
                System.out.println("Applying rule '"
                	+ rule.getClass().getName() + "' to:\n"
                	+ state.getCurrentLine() + "\n");
                	*/
                rule.apply(state);
                return;
            }
        }

        throw new TreeTransformException("Could not find a tokenizer rule for character '"
                + state.getCurrentLine().charAt(0) + "'");
    }

}
