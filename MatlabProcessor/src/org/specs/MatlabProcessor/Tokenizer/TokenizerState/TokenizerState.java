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

package org.specs.MatlabProcessor.Tokenizer.TokenizerState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.MatlabIR.Exceptions.CodeParsingException;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.mclass.ClassdefSt;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SpaceNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.TempNodeFactory;
import org.specs.MatlabIR.Processor.TreeTransformException;
import org.specs.MatlabProcessor.Exceptions.ParserErrorException;
import org.specs.MatlabProcessor.Tokenizer.TokenizerUtils;
import org.specs.MatlabProcessor.Tokenizer.ParserModes.CatchState;
import org.specs.MatlabProcessor.Tokenizer.ParserModes.ConditionState;
import org.specs.MatlabProcessor.Tokenizer.ParserModes.ForState;
import org.specs.MatlabProcessor.Tokenizer.ParserModes.FunctionState;
import org.specs.MatlabProcessor.Tokenizer.ParserModes.StatementDetector;
import org.specs.MatlabProcessor.Utils.StatementBuilder;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.reporting.Reporter;
import pt.up.fe.specs.util.utilities.StringSlice;

/**
 * @author Joao Bispo
 * 
 */
public class TokenizerState {

    // Statement detectors, signals when the statement should finish under certain conditions
    private final ConditionState ifState = new ConditionState();
    private final List<StatementDetector> statementDetectors = Arrays.asList(
            ifState, new ForState(), new CatchState(), new FunctionState());

    private List<StatementNode> statements;

    // private List<MatlabNode> currentTokens;
    private StateNodes currentTokens;

    private StringSlice currentLine;
    private int currentLineNumber;
    // private boolean currentLineFinished;

    private ParenthesisStack parenthesisStack;
    private boolean unfinishedProcessing;

    // private boolean isInsideCommentBlock;
    private int commentBlockLevel;

    private boolean isInsideCommand;
    private boolean isInsideFunctionBlock;
    private boolean isInsideClassBlock;
    private int numClasses;

    private Reporter reportService;
    /**
     * Flags if current statement contains a condition (for 'if, 'elseif', 'while'...)
     */

    // private ConditionState conditionState;
    // private ForState forState;
    // private CatchState catchState;
    // To check if only one statement detect is active at a given time
    private Class<?> activeDetector;

    /**
     * 
     */
    public TokenizerState(Reporter reportService) {
        this.reportService = reportService;

        statements = new ArrayList<>();
        parenthesisStack = new ParenthesisStack();
        clearTempData();
    }

    public void clearTempData() {
        // currentTokens = new ArrayList<>();
        currentTokens = new StateNodes();
        // currentLine = "";
        currentLine = null;
        // currentLineFinished = true;
        currentLineNumber = -1;
        // parenthesisCalls = new ArrayList<TokenType>();
        parenthesisStack.clear();
        unfinishedProcessing = false;

        isInsideCommand = false;
        isInsideFunctionBlock = false;
        isInsideClassBlock = false;
        numClasses = 0;
        // isInsideCommentBlock = false;
        commentBlockLevel = 0;

        // conditionNodes = 0;
        // isLastNodeOperator = false;
        // conditionState = new ConditionState();
        // forState = new ForState();
        // catchState = new CatchState();
        activeDetector = null;

    }

    private void setInsideFunctionBlock(boolean isInsideFunctionBlock) {
        this.isInsideFunctionBlock = isInsideFunctionBlock;
    }

    /**
     * @return the statementTokens
     */
    public List<StatementNode> getStatementTokens() {
        return statements;
    }

    public void clearStatementTokens() {
        statements = new ArrayList<>();
    }

    public boolean isInsideCommentBlock() {
        // return isInsideCommentBlock;
        return commentBlockLevel > 0;
    }

    public boolean isInsideClassBlock() {
        return isInsideClassBlock;
    }

    public void incrementCommentBlockLevel() {
        this.commentBlockLevel++;
        // this.isInsideCommentBlock = isInsideCommentBlock;
    }

    public void decrementCommentBlockLevel(boolean isInsideCommentBlock) {
        if (this.commentBlockLevel <= 0) {
            throw new RuntimeException("Comment block level is already zero, why is decrementing?");
        }

        this.commentBlockLevel--;
        // this.isInsideCommentBlock = isInsideCommentBlock;
    }

    /**
     * @param currentLine
     *            the currentLine to set
     */
    public void setCurrentLine(StringSlice currentLine) {
        if (currentLine == null) {
            throw new RuntimeException("Line should not be null");
        }

        this.currentLine = currentLine;
    }

    public void setCurrentLineEmpty() {
        /*
        if (currentLine == null) {
        currentLine = "";
        }
        */
        // this.currentLine = currentLine;
        this.currentLine = null;
        // currentLineFinished = true;
        // this.currentLine = "";
    }

    /**
     * @return the currentLine
     */
    public StringSlice getCurrentLine() {
        if (!hasLine()) {
            return StringSlice.EMPTY;
        }
        // if (currentLine == null) {
        // throw new RuntimeException("Line already finished");
        // }
        return currentLine;
    }

    // Has some subtle bug that haven't figured out yet
    public boolean hasLine() {
        // return !currentLineFinished;

        // if (currentLine.isEmpty()) {

        if (currentLine == null) {
            return false;
        }

        /*	
        		if (currentLine.isEmpty()) {
        		    return false;
        		}
        */
        return true;
    }

    /**
     * @param currentLineNumber
     *            the currentLineNumber to set
     */
    public void setCurrentLineNumber(int currentLineNumber) {
        this.currentLineNumber = currentLineNumber;
    }

    /**
     * @return the currentLineNumber
     */
    public int getCurrentLineNumber() {
        return currentLineNumber;
    }

    /**
     * @return the currentTokens
     */
    public MatlabNode getLastNode() {
        return currentTokens.getLastNode();
        /*
        if (currentTokens.isEmpty()) {
        return null;
        }
        
        return currentTokens.get(currentTokens.size() - 1);
        */
    }

    public MatlabNode getLastNonSpaceNode() {
        return currentTokens.getLastNonSpaceNode();
        /*
        for (int i = currentTokens.size() - 1; i >= 0; i--) {
        MatlabNode currentNode = currentTokens.get(i);
        
        if (currentNode instanceof SpaceNode) {
        	continue;
        }
        
        return currentNode;
        }
        
        return null;
        */
    }

    public long getNumNonSpaces() {
        return currentTokens.getNumNonSpaces();
        /*
        return currentTokens.stream()
        	.filter(node -> !(node instanceof SpaceNode))
        	.count();
        	*/
    }

    /**
     * Creates a new statement if there are tokens, and clears parenthesisCalls.
     * 
     * @param displayResults
     */
    public void finishStatement(boolean displayResults) throws TreeTransformException {
        // Check if inside square brackets. If true, do nothing
        // Because of cases such as:
        //
        // RL=[ 9.998242e-001 -3.026710e-003 1.850254e-002;
        // -1.842356e-002 2.431980e-002 9.995344e-001];
        // if (parenthesisStack.isInsideSquareBrackets() ||
        // parenthesisStack.isInsideCurlyBrackets()) {
        if (parenthesisStack.isSpaceSensible()) {

            // Add space if no space before
            if (!(currentTokens.getLastNode() instanceof SpaceNode)) {
                pushProducedToken(TempNodeFactory.newSpace());
            }

            return;
        }

        // System.out.println("CLEAR");

        // Mark unfinishedProcessing as false
        this.unfinishedProcessing = false;

        // conditionState.clear();
        // forState.clear();
        // catchState.clear();
        // Clear detectors
        // statementDetectors.forEach(detector -> System.out.println("DETECTOR:" + detector.isActive()));
        statementDetectors.forEach(detector -> detector.clear());
        activeDetector = null;

        isInsideCommand = false;

        // If there are tokens, build a new statement
        if (!currentTokens.isEmpty()) {

            List<MatlabNode> nodes = currentTokens.getNodes();

            prepareNodes(nodes);

            StatementNode statement = StatementBuilder.newStatement(reportService, currentLineNumber, displayResults,
                    nodes);
            // currentTokens.getNodes());
            statements.add(statement);
            // currentTokens = new ArrayList<>();
            currentTokens = new StateNodes();

            // If statement is and 'end', build corresponding block
            /*
            	    if (statement instanceof EndSt) {
            		System.out.println("STATEMENTS:" + statements);
            		buildEnd();
            	    }
            	    */
        }

        // Clear parenthesis stack
        parenthesisStack.clear();

    }

    /*
        private void buildEnd() {
    	// End statement is last statement
    	int lastIndex = statements.size() - 1;
    
    	assert statements.get(lastIndex) instanceof EndSt;
    
    	EndSt endSt = (EndSt) statements.get(lastIndex);
    
    	// Remove it from list
    	statements.remove(lastIndex);
    	lastIndex--;
    
    	// Go back, until a block header is found
    	boolean headerFound = false;
    	// Adding nodes at the head
    	List<StatementNode> blockChildren = new LinkedList<>();
    	while (!headerFound && !statements.isEmpty()) {
    	    StatementNode previousNode = (StatementNode) statements.get(lastIndex);
    
    	    // Add to children list and remove from iterator
    	    blockChildren.add(0, previousNode);
    	    statements.remove(lastIndex);
    	    lastIndex--;
    
    	    // If block header, finish
    	    if (previousNode.isBlockHeader()) {
    		// System.out.println("HEADER:" +
    		// previousNode.getNodeName());
    		BlockSt blockSt = StatementFactory.newBlock(endSt.getLine(), blockChildren);
    		statements.add(blockSt);
    
    		headerFound = true;
    
    		continue;
    	    }
    	}
    
    	// If no header found, mark as stray 'end'
    	if (!headerFound) {
    	    throw new CodeParsingException("end", endSt.getLine(),
    		    "Stray 'end', could not find corresponding header statement");
    	}
    
        }
    */
    /**
     * Removes space nodes at the beginning of the list. If list becomes empty, adds a space node.
     * 
     * @param nodes
     * @return
     */
    private static void prepareNodes(List<MatlabNode> nodes) {

        while (!nodes.isEmpty() && nodes.get(0) instanceof SpaceNode) {
            nodes.remove(0);
        }

        if (nodes.isEmpty()) {
            nodes.add(TempNodeFactory.newSpace());
        }

    }

    /**
     * 
     * @param prefix
     * @return
     */
    private void advanceAndTrim(String prefix) throws TreeTransformException {
        StringSlice workLine = TokenizerUtils.advance(getCurrentLine(), prefix, false);

        // Check if inside square brackets, where spaces have meaning
        if (!parenthesisStack.isInsideSquareBrackets()) {
            workLine = workLine.trim();
        }

        setCurrentLine(workLine);
    }

    private void advanceAndTrim(int numChars) {
        StringSlice workLine = TokenizerUtils.advance(getCurrentLine(), numChars);

        if (!parenthesisStack.isInsideSquareBrackets()) {
            workLine = workLine.trim();
        }

        setCurrentLine(workLine);
    }

    public void pushProducedToken(MatlabNode matlabToken, String advanceAndTrim) throws TreeTransformException {

        pushTokenInternal(matlabToken, advanceAndTrim.length());
        advanceAndTrim(advanceAndTrim);
    }

    public void pushProducedToken(MatlabNode matlabToken, int numChars) throws TreeTransformException {
        pushTokenInternal(matlabToken, numChars);
        advanceAndTrim(numChars);
    }

    private void pushTokenInternal(MatlabNode matlabToken, int numChars) {
        if (matlabToken == null) {
            SpecsLogs.warn("MatlabToken is null.");
            return;
        }
        if (numChars > getCurrentLine().length()) {
            throw new IndexOutOfBoundsException("After pushing " + matlabToken);
        }

        // System.out.println("TOKEN:" + matlabToken);

        pushProducedToken(matlabToken);

        // After pushing the token, transformations could be applied here according to the pushed token
        // Currently done after pushing a token, maybe that way is more decentralized

        boolean insertSpace = TokenizerUtils.isSpaceInsertable(this, numChars);

        if (insertSpace) {
            // If adding a space after an identifier, and identifier is the first and only element of current
            // tokens, we have just entered a command context
            if (getCurrentTokens().size() == 1
                    && getCurrentTokens().get(0) instanceof IdentifierNode) {

                setCommandContext();
            }

            if (isSpaceSensible()) {
                MatlabNode spaceToken = TempNodeFactory.newSpace();
                pushProducedToken(spaceToken);
            }
        }

    }

    private void pushProducedToken(MatlabNode node) {

        // Iterator over statement detectors
        for (StatementDetector detector : statementDetectors) {
            // for (int i = 0; i < statementDetectors.size(); i++) {
            // StatementDetector detector = statementDetectors.get(i);
            // Push token, detect is finished
            boolean finishStatement = detector.pushToken(currentTokens, node);

            // Confirm it is the only active detector at this point
            checkDetector(detector);

            if (finishStatement) {
                // Save next state nodes before finishing statement, otherwise they'll get cleared
                List<MatlabNode> nextStateNodes = detector.getNextStatementTokens();

                finishStatement(true);
                currentTokens.addAll(nextStateNodes);

                // No need to continue, detectors have been cleared
                // Feed token again to all detectors, it might be the start of a detector
                for (StatementDetector detectorV2 : statementDetectors) {

                    boolean oneTokenDetector = detectorV2.pushToken(currentTokens, node);
                    // This should be false
                    if (oneTokenDetector) {
                        throw new ParserErrorException("Detectors should not trigger after one token");
                    }
                }

                break;
            }

        }

        currentTokens.add(node);
    }

    private void checkDetector(StatementDetector detector) {
        if (!detector.isActive()) {
            return;
        }

        if (activeDetector == null) {
            activeDetector = detector.getClass();

            // If Function Detector is active, mark that we are inside a Function block
            if (FunctionState.class == activeDetector) {
                setInsideFunctionBlock(true);
            }

            return;
        }

        if (activeDetector.equals(detector.getClass())) {
            return;
        }

        throw new ParserErrorException("Statement detector '" + detector.getClass().getSimpleName()
                + "' became active when detector '" + activeDetector.getSimpleName()
                + "' was already active");
    }

    /**
     * @return the currentTokens
     */
    public List<MatlabNode> getCurrentTokens() {
        // return currentTokens;
        return currentTokens.getNodes();
    }

    /**
     * @return the parenthesisStack
     */
    public ParenthesisStack getParenthesisStack() {
        return parenthesisStack;
    }

    /**
     * 
     * @param unfinishedProcessing
     */
    public void setUnfinishedProcessing(boolean unfinishedProcessing) {
        this.unfinishedProcessing = unfinishedProcessing;
    }

    /**
     * @return the unfinishedProcessing
     */
    public boolean isUnfinishedProcessing() {
        return unfinishedProcessing;
    }

    public void removeLastNonSpace() {
        currentTokens.removeLastNonSpace();
        /*
        for (int i = currentTokens.size() - 1; i >= 0; i--) {
        if (currentTokens.get(i) instanceof SpaceNode) {
        	continue;
        }
        
        // Remove and return
        currentTokens.remove(i);
        return;
        }
        */

    }

    /**
     * 
     * @return true, if the spaces have meaning in the current context
     */
    public boolean isSpaceSensible() {
        if (parenthesisStack.isSpaceSensible()) {
            return true;
        }

        if (isInsideCommand) {
            return true;
        }

        return false;
    }

    public void setCommandContext() {
        isInsideCommand = true;
    }

    public void setInsideClassBlock() {
        if (numClasses > 0) {
            throw new CodeParsingException("There is already a class defined in this file");
        }
        isInsideClassBlock = true;
    }

    /**
     * It is in a class context if it is inside a classdef, and not inside a Function block.
     * 
     * @return
     */
    public boolean isClassContext() {

        return isInsideClassBlock && !isInsideFunctionBlock;
    }

    /**
     * 
     * @return true if parsing is currently inside the condition of an 'if' statement
     */
    public boolean isInsideIfCondition() {
        return ifState.isInsideCondition();
    }

    /**
     * Signal to TokenizerState that a block of code (function, class...) has ended
     * 
     * @param node
     */
    public void blockEnded(MatlabNode blockHeader) {
        if (blockHeader instanceof FunctionDeclarationSt) {
            setInsideFunctionBlock(false);
            return;
        }

        if (blockHeader instanceof ClassdefSt) {
            isInsideClassBlock = false;
            numClasses++;
            return;
        }

    }

    public Reporter getReportService() {
        return reportService;
    }
}
