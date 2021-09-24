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

package org.specs.MatlabProcessor.MatlabParser;

import java.awt.event.KeyEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.specs.MatlabIR.Exceptions.CodeParsingException;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.BlockSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.EndForSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.EndFunctionSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.EndIfSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.EndSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.EndWhileSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.IfSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.SimpleForSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNode.nodes.statements.WhileSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.mclass.ClassdefSt;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.AssignmentNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.UnknownSymbolNode;
import org.specs.MatlabProcessor.MatlabParser.Rules.FileRules;
import org.specs.MatlabProcessor.MatlabParser.Rules.StatementRules;
import org.specs.MatlabProcessor.MatlabParser.Rules.TokenRules;
import org.specs.MatlabProcessor.Reporting.ProcessorErrorType;
import org.specs.MatlabProcessor.Reporting.ProcessorReportService;
import org.specs.MatlabProcessor.Tokenizer.MatlabLineTokenizer;

import pt.up.fe.specs.util.providers.StringProvider;
import pt.up.fe.specs.util.utilities.StringSlice;

/**
 * Converts MATLAB code into a MATLAB-IR tree, line-by-line.
 *
 * 
 * @author Joao Bispo
 * 
 */
public class MatlabLineParser {

    final private MatlabLineTokenizer tokenizer;
    final private List<StatementNode> statements;
    final private String filename;
    final private ProcessorReportService reportService;

    private int lines;
    private int nonEmptyLines;

    final private Optional<StringProvider> codeProvider;
    final private Optional<StringBuilder> currentCode;

    final private TokenRules tokenRules;
    final private StatementRules statementRules;

    /**
     * The classes of header nodes that have been parsed (e.g., function declaration)
     */
    final private Set<Class<? extends StatementNode>> seenStatements;

    /**
     * Simpler constructor, which only need a filename.
     * 
     * @param filename
     */
    public MatlabLineParser(String filename, LanguageMode languageMode, Optional<PrintStream> messageStream) {
        this(filename, languageMode, null, messageStream);
    }

    /**
     * Accepts a StringProvider besides the filename.
     * 
     * <p>
     * If StringProvider is not null, parser will not store the given lines internally.
     */
    public MatlabLineParser(String filename, LanguageMode languageMode, StringProvider code,
            Optional<PrintStream> messageStream) {
        this.filename = filename;
        reportService = new ProcessorReportService(null, filename, messageStream);
        tokenizer = new MatlabLineTokenizer(languageMode, reportService);
        statements = new ArrayList<>();
        seenStatements = new HashSet<>();

        lines = 0;
        nonEmptyLines = 0;

        codeProvider = Optional.ofNullable(code);

        // If no code provider is given, store code in a StringBuilder
        if (codeProvider.isPresent()) {
            currentCode = Optional.empty();
        } else {
            currentCode = Optional.of(new StringBuilder());
        }

        tokenRules = new TokenRules(reportService);
        statementRules = new StatementRules(reportService);
    }

    public boolean hasCreated(Class<? extends StatementNode> nodeClass) {
        return seenStatements.contains(nodeClass);
    }

    /**
     * 
     * @return the number of the last added line
     */
    public int getLines() {
        return lines;
    }

    public int getNonEmptyLines() {
        return nonEmptyLines;
    }

    /**
     * @return
     */
    public String getOriginalMatlabCode() {
        return tokenizer.getOriginalMatlabCode();
    }

    /**
     * Checks if the parsing is in an incomplete state. Currently checks for:
     * <p>
     * - If a comment block has been opened but not closed; <br>
     * - If a class block has been started, but not ended; <br>
     * 
     * <p>
     * Throws a CodeParsingException if any of the checks does not pass
     */
    private void checkParsing() {
        // Check if inside block comment
        if (tokenizer.getState().isInsideCommentBlock()) {
            throw reportService.emitError(ProcessorErrorType.SYNTAX_ERROR, "Comment block '%{' opened but not closed.");
            // return false;
        }

        // Check if classdef block was closed
        if (tokenizer.getState().isInsideClassBlock()) {
            throw reportService.emitError(ProcessorErrorType.SYNTAX_ERROR,
                    "Classdef block not closed, does not have a corresponding 'end'.");
        }

    }

    /**
     * Transforms a line of MatLab code into a List of MatlabTokens of the type Statement.
     * 
     * @param line
     * @param lineNumber
     * @return
     */
    public void addLine(String line) {
        // Increment line counters
        lines++;
        if (!line.isEmpty()) {
            nonEmptyLines++;
        }

        // If no code provider, store the line
        if (!codeProvider.isPresent()) {
            currentCode.get().append(line).append("\n");
        }

        // reportService.setCurrentLine(lineNumber, line);
        reportService.setCurrentLine(lines, line);

        // Get unprocessed statements
        List<StatementNode> tokenLists = tokenizer.getTokens(new StringSlice(line), lines);

        // For each list of tokens, process it and build a MatlabStatement
        for (StatementNode statement : tokenLists) {

            statement = processStatement(statement);

            // If statement still has undefined nodes at this point, launch code parsing exception
            Optional<MatlabNode> temporaryNode = statement.getDescendantsAndSelfStream()
                    .filter(c -> c.isTemporary())
                    .findFirst();

            // Process temporary node
            if (temporaryNode.isPresent()) {
                MatlabNode node = temporaryNode.get();
                // If a parenthesis, code parsing about unbalanced parenthesis
                if (node instanceof UnknownSymbolNode) {
                    String symbol = node.getCode();
                    if (symbol.equals("(")
                            || symbol.equals(")") || symbol.equals("[") || symbol.equals("]")
                            || symbol.equals("{") || symbol.equals("}")) {

                        throw reportService.emitError(ProcessorErrorType.SYNTAX_ERROR, "Unbalanced '" + symbol + "'");
                    }

                    if (symbol.length() == 1 && !isPrintableChar(symbol.charAt(0))) {
                        String unicode = "\\u" + Integer.toHexString(symbol.charAt(0) | 0x10000).substring(1);

                        throw reportService.emitError(ProcessorErrorType.SYNTAX_ERROR,
                                "Found non-printable character with code " + unicode);
                    }

                    if (symbol.equals("\"")) {
                        throw reportService
                                .emitError(ProcessorErrorType.OCTAVE_INCOMPATIBILITY,
                                        "The character '\"' is not valid in MATLAB statements or expressions.");
                    }

                } else if (node instanceof AssignmentNode) {
                    throw reportService.emitError(ProcessorErrorType.SYNTAX_ERROR, "Found stray '" + node.getCode()
                            + "' (did you mean '==' ?)");
                }

                // System.out.println("NODES:" + node.getParent());
                throw reportService.emitError(ProcessorErrorType.SYNTAX_ERROR,
                        "Statement not fully parsed, contains temporary node '"
                                + node.getNodeName() + "':\n" + node);
            }

            /*
            // Check if expression nodes have more than one child
            List<MatlabNode> expressionNodes = statement.getDescendantsAndSelfStream()
                .filter(c -> c instanceof ExpressionNode)
                .filter(node -> node.numChildren() != 1)
                .collect(Collectors.toList());
            
            if (!expressionNodes.isEmpty()) {
            throw new RuntimeException("Expression nodes with more than one child:\n" + expressionNodes);
            }
            */

            // Add the statement
            statements.add(statement);

            // Include class in the seen statements set
            seenStatements.add(statement.getClass());

            // Build block, if statement is an End statement
            buildEnd();

        }

    }

    private void buildEnd() {
        assert !statements.isEmpty();

        // End statement is last statement
        int lastIndex = statements.size() - 1;
        StatementNode endSt = statements.get(lastIndex);

        Class<? extends MatlabNode> headerType;

        if (endSt instanceof EndSt) {
            headerType = null;
        } else if (endSt instanceof EndFunctionSt) {
            headerType = FunctionDeclarationSt.class;
        } else if (endSt instanceof EndIfSt) {
            headerType = IfSt.class;
        } else if (endSt instanceof EndWhileSt) {
            headerType = WhileSt.class;
        } else if (endSt instanceof EndForSt) {
            headerType = SimpleForSt.class;
        } else {
            return;
        }

        // Remove it from list
        statements.remove(lastIndex);
        lastIndex--;

        // Go back, until a block header is found
        boolean headerFound = false;
        // Adding nodes at the head
        List<StatementNode> blockChildren = new LinkedList<>();
        while (!headerFound && !statements.isEmpty()) {
            StatementNode previousNode = statements.get(lastIndex);

            // Add to children list and remove from iterator
            blockChildren.add(0, previousNode);
            statements.remove(lastIndex);
            lastIndex--;

            // If block header, finish
            if (previousNode.isBlockHeader()) {
                BlockSt blockSt = StatementFactory.newBlock(endSt.getLine(), blockChildren);
                statements.add(blockSt);

                headerFound = true;

                // Signal TokenizerState that a block ended
                MatlabNode headerNode = blockSt.getHeaderNode();
                if (headerType != null && headerNode.getClass() != headerType) {
                    throw reportService.emitError(ProcessorErrorType.PARSE_ERROR,
                            "'" + headerNode.getCode().trim() + "' matched with '" + endSt.getCode().trim() + "'");
                }

                tokenizer.getState().blockEnded(headerNode);

                verify(headerNode);

                continue;
            }
        }

        // If no header found, mark as stray 'end'
        if (!headerFound) {
            throw reportService.emitError(ProcessorErrorType.SYNTAX_ERROR,
                    "Stray 'end', could not find corresponding header statement");
        }

    }

    private void verify(MatlabNode headerStatement) {

        // If classdef, verify if there are no other statements besides comments before
        if (headerStatement instanceof ClassdefSt) {
            for (int i = 0; i < statements.size() - 1; i++) {
                if (statements.get(i) instanceof CommentSt) {
                    continue;
                }

                throw new CodeParsingException("Found '" + statements.get(i).getNodeName()
                        + "' statement before 'classdef', only comments are allowed");
            }
            return;
        }

    }

    public static boolean isPrintableChar(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c)) &&
                c != KeyEvent.CHAR_UNDEFINED &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS;
    }

    private StatementNode processStatement(StatementNode statement) {
        // Transformation which needs to be performed on the statement
        // as a whole

        statement = statementRules.apply(statement);
        // statement = (StatementNode) MatlabParserUtils.applyStatementRules(reportService, statement);

        // Transformation to be performed after statement rules, on each
        // token
        // System.out.println("TOKEN RULES BEGIN");
        statement = tokenRules.apply(statement);
        // statement = (StatementNode) MatlabParserUtils.applyTokenRules(reportService, statement);

        // System.out.println("TOKEN RULES END");
        return statement;
    }

    private List<StatementNode> close() {
        List<MatlabNode> lastNodes = new ArrayList<>();

        // Add missing tokens
        lastNodes.addAll(tokenizer.getState().getCurrentTokens());

        if (!lastNodes.isEmpty()) {
            // Use line number of previous statement
            int lineNumber = 1;
            if (!statements.isEmpty()) {
                lineNumber = statements.get(statements.size() - 1).getLine();
            }
            StatementNode statement = processStatement(StatementFactory.newUndefined(lineNumber, true, lastNodes));
            statements.add(statement);
        }

        return statements;
    }

    /**
     * Closes the parser and generates a FileNode.
     * 
     * @return
     */
    public FileNode getFileNode() {
        List<StatementNode> mfileStatements = close();

        // If for some reason parsing is still unfinished, throw Exception
        checkParsing();

        // Use the given CodeProvider at construction, or if not present, the code that collected during parsing
        StringProvider code = codeProvider
                .orElse(StringProvider.newInstance(currentCode.get().toString()));

        return new FileRules(filename, code, reportService).parse(mfileStatements);
    }
}
