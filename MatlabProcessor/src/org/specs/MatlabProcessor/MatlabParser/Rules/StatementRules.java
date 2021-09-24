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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CommandNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabCharArrayNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatrixNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ReservedWordNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.RowNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AccessCallSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommandSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNode.nodes.statements.UndefinedSt;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.AssignmentNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.FunctionHandleSymbolNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SpaceNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.TempNodeFactory;
import org.specs.MatlabIR.matlabrules.MatlabNodeRule;
import org.specs.MatlabIR.matlabrules.MatlabRulesUtils;
import org.specs.MatlabProcessor.Reporting.ProcessorErrorType;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.reporting.Reporter;
import pt.up.fe.specs.util.treenode.TreeNodeIndexUtils;

/**
 * Statement level transformations from the output generated from the MatlabLineTokenizer;
 * 
 * @author Joao Bispo
 * 
 */
public class StatementRules {

    private final Reporter reportService;
    private final List<MatlabNodeRule> rules;

    public StatementRules(Reporter reportService) {
        this.reportService = reportService;

        rules = buildRules();
    }

    private List<MatlabNodeRule> buildRules() {
        List<MatlabNodeRule> rules = new ArrayList<>();

        // Build commands, is executed first to be able to capture symbols that should not be read with their usual
        // meaning (=, @...)
        rules.add(buildCommand());
        // Build "global x y z" commands
        rules.add(buildGlobals());
        // Build function declarations
        rules.add(buildFunctionDeclaration());
        // Build assignments
        rules.add(buildAssignment());
        // Build single access call - should be done after assignment building, method is not testing if only has an
        // access call token
        rules.add(buildAccessCall());

        // Transforms accessCall on the left-hand of an assignment into custom functions
        // statementRules.add(leftHandAccessToFunction());
        // Cleans up Expression tokens
        // statementRules.add(cleanExpressionTokens());

        return rules;
    }

    public StatementNode apply(StatementNode statement) {
        // Apply processing methods to current tokens
        for (MatlabNodeRule rule : rules) {
            statement = (StatementNode) MatlabRulesUtils.bottomUpTraversal(statement, rule);
        }

        return statement;
    }

    /**
     * If the token is a statement and the first child is the reserved word 'function', transforms the statement into a
     * FunctionDeclaration statement.
     * 
     * @return
     */
    public MatlabNodeRule buildFunctionDeclaration() {
        return new FunctionDeclarationBuilderRule(reportService);
    }

    /**
     * @return
     */
    public static MatlabNodeRule buildAccessCall() {
        return new MatlabNodeRule() {

            private boolean check(MatlabNode token) {

                // Check if of type "Undefined"
                if (!(token instanceof UndefinedSt)) {
                    return false;
                }

                List<MatlabNode> tokens = token.getChildren();

                // Check if first token is an Access Call
                if (!(tokens.get(0) instanceof AccessCallNode)) {
                    return false;
                }

                if (tokens.size() > 1) {
                    return false;
                }

                return true;
            }

            @Override
            public MatlabNode apply(MatlabNode statement) {
                if (!check(statement)) {
                    return statement;
                }

                // We know it is a statement
                StatementData data = ((StatementNode) statement).getData();

                // Set statement type
                AccessCallSt newStatement = StatementFactory.newAccessCall(data.getLine(),
                        data.isDisplay(), statement.getChild(AccessCallNode.class, 0));

                return newStatement;
            }
        };

    }

    /**
     * If there is an assignment token in the statement children, tries to transform the statement into an assignment
     * statement.
     * 
     * @return
     */
    public MatlabNodeRule buildAssignment() {
        return new MatlabNodeRule() {

            private boolean check(MatlabNode token) {

                if (!(token instanceof StatementNode)) {
                    return false;
                }

                // List<MatlabToken> tokens = token.getChildren();

                // Check if first token is an ID
                List<Integer> assignIndex = token.indexesOf(AssignmentNode.class);
                // List<Integer> assignIndex = TreeNodeIndexUtils.allIndexesOf(MType.Assignment, token);
                if (assignIndex.size() > 1) {
                    throw new RuntimeException("Token contains more than one assignment. Check this:\n" + token);
                }

                if (assignIndex.isEmpty()) {
                    return false;
                }

                return true;
            }

            @Override
            public MatlabNode apply(MatlabNode statement) {
                if (!check(statement)) {
                    return statement;
                }

                List<MatlabNode> tokens = statement.getChildren();

                // Find Assignment token
                int index = 1;
                boolean foundAssign = false;
                while (index < tokens.size()) {
                    MatlabNode token = tokens.get(index);
                    if (token instanceof AssignmentNode) {
                        foundAssign = true;
                        break;
                    } else if (token instanceof SpaceNode) {
                        index += 1;
                    } else {
                        break;
                    }
                }

                // If valid assignment not found, return.
                if (!foundAssign) {
                    return statement;
                }
                // System.out.println("ASSIGNMENT:\n" + statement);
                // Check if there are tokens on the right of assign
                if (index + 1 >= tokens.size()) {
                    SpecsLogs.warn("Did not find tokens on the right of an assignment.");
                    return statement;
                }

                // Assignment left hand
                MatlabNode leftHand = tokens.get(0);
                // If left hand is a Matrix, convert to Outputs
                if (leftHand instanceof MatrixNode) {
                    List<RowNode> rows = ((MatrixNode) leftHand).getRows();
                    if (rows.isEmpty()) {
                        throw reportService.emitError(ProcessorErrorType.PARSE_ERROR,
                                "Empty output list is not allowed for assignments.");
                    }
                    if (rows.size() > 1) {
                        throw reportService.emitError(ProcessorErrorType.PARSE_ERROR,
                                "Assignment left-hand values must be separated by commas");
                    }

                    RowNode rowNode = rows.get(0);
                    // RowNode rowNode = MatlabTokenAccess.getMatrixSingleRow(leftHand);
                    if (rowNode == null) {
                        throw reportService.emitError(ProcessorErrorType.PARSE_ERROR,
                                "No elements inside outputs, expected at least one");
                    }

                    leftHand = MatlabNodeFactory.newOutputs(rowNode.getChildren());
                }

                // Assignment right hand
                List<MatlabNode> rightHandExpr = new ArrayList<>(tokens.subList(index + 1, tokens.size()));
                MatlabNode rightHand = TempNodeFactory.newExpression(rightHandExpr);

                // return statement;
                return StatementFactory.newAssignment(((StatementNode) statement).getData(), leftHand, rightHand);
            }
        };
    }

    private MatlabNodeRule buildGlobals() {
        return new MatlabNodeRule() {
            @Override
            public MatlabNode apply(MatlabNode token) {
                if (!(token instanceof StatementNode)) {
                    return token;
                }
                StatementNode stmt = (StatementNode) token;

                List<MatlabNode> nodes = token.getChildren();
                if (nodes.size() == 0) {
                    return token;
                }

                MatlabNode firstNode = nodes.get(0);
                if (firstNode instanceof ReservedWordNode
                        && ((ReservedWordNode) firstNode).getWord() == ReservedWord.Global) {

                    if (nodes.size() == 1) {
                        throw reportService.emitError(ProcessorErrorType.PARSE_ERROR,
                                "Got empty global declaration statement.");
                    }

                    for (int i = 1; i < nodes.size(); ++i) {
                        MatlabNode child = nodes.get(i);

                        if (!(child instanceof IdentifierNode)) {
                            throw reportService.emitError(ProcessorErrorType.PARSE_ERROR,
                                    "Global declaration statement can only contain identifiers, got " + child.getCode()
                                            + ".");
                        }
                    }

                    return StatementFactory.newGlobal(stmt.getLine(), stmt.isDisplay(), nodes);
                }

                return token;
            }
        };
    }

    /**
     * If first token is an identifier and second token is a space, tries to transform the statement into a command
     * token.
     * 
     * <p>
     * The command is not formed if: <br>
     * - If there is an op alone between spaces; <br>
     * - If there is an assignment after the identifier; <br>
     * 
     * @return
     */
    public MatlabNodeRule buildCommand() {
        return new MatlabNodeRule() {

            private boolean check(MatlabNode token) {

                if (!(token instanceof StatementNode)) {
                    return false;
                }

                List<MatlabNode> tokens = token.getChildren();

                // Check if statement has children
                if (tokens.isEmpty()) {
                    return false;
                }

                // Check if first token is an ID
                if (!(tokens.get(0) instanceof IdentifierNode)) {
                    return false;
                }

                // Check if second token is a space
                if (tokens.size() < 2) {
                    return false;
                }

                if (!(tokens.get(1) instanceof SpaceNode)) {
                    return false;
                }

                // Check if first token that is not a space after the identifier is an assignment
                // If it is an assignment, should not form command

                Optional<MatlabNode> firstNonSpace = tokens.subList(2, tokens.size()).stream()
                        .filter(node -> !(node instanceof SpaceNode))
                        .findFirst();

                if (firstNonSpace.isPresent() && (firstNonSpace.get() instanceof AssignmentNode)) {
                    return false;
                }

                // Check if possible command is instead an expression
                if (firstNonSpace.isPresent() && (isOperatorExpression(token.getChildrenIterator()))) {
                    return false;
                }

                return true;
            }

            /**
             * Heuristic for trying to figure out if instead of a command, this statement should be interpreted as an
             * operator expression.
             * 
             * <p>
             * - Check if first non-space token after identifier is an operator;<br>
             * - Check if next token is a space; <br>
             * - Check if there is a non-space token after the operator and the space; <br>
             * - If next non-space token is an op excluding + - or ~, a function handler or an assignment, it is
             * considered a command; <br>
             * 
             * @param matlabNode
             * @return
             */
            private boolean isOperatorExpression(MatlabNodeIterator listIterator) {
                // Advace past first identifier
                listIterator.next(IdentifierNode.class);

                // Get first non-space node (this function is called after we test that there is a non-space node)
                MatlabNode firstNonSpace = listIterator.nextNot(SpaceNode.class).get();

                // 1. Check if first non-space token after identifier is an operator
                if (!(firstNonSpace instanceof OperatorNode)) {
                    return false;
                }

                // 2. Check if next token is a space;
                if (!listIterator.hasNext()) {
                    return false;
                }

                MatlabNode nodeAfterOp = listIterator.next();
                if (!(nodeAfterOp instanceof SpaceNode)) {
                    return false;
                }

                // 3. Check if there is a non-space token after the operator and the space
                if (!listIterator.hasNext()) {
                    return false;
                }

                // This nextNot might be unneeded, since the tokenizer trims strings and there shouldn't be two
                // consecutive SPACE nodes. However, it was left here defensively.
                Optional<MatlabNode> secondNonSpaceOp = listIterator.nextNot(SpaceNode.class);
                if (!secondNonSpaceOp.isPresent()) {
                    return false;
                }

                // If next non-space token is an op excluding + - or ~, a function handler or an assignment, it is
                // considered a command
                MatlabNode secondNonSpace = secondNonSpaceOp.get();

                if (secondNonSpace instanceof OperatorNode) {
                    MatlabOperator op = ((OperatorNode) secondNonSpace).getOp();

                    // If op is neither of theses operators, then it is a command
                    if (op != MatlabOperator.Addition && op != MatlabOperator.Subtraction
                            && op != MatlabOperator.UnaryMinus) {
                        return false;
                    }
                }

                // If node is a function handler, it is a command
                if (secondNonSpace instanceof FunctionHandleSymbolNode) {
                    return false;
                }

                // If node is an assignment, it is a command
                if (secondNonSpace instanceof AssignmentNode) {
                    return false;
                }

                return true;
            }

            @Override
            public MatlabNode apply(MatlabNode statement) {

                if (!check(statement)) {
                    return statement;
                }

                List<MatlabNode> tokens = statement.getChildren();

                // First child is an Identifier
                String command = statement.getChild(IdentifierNode.class, 0).getName();
                // String command = MatlabTokenContent.getIdentifierName(tokens.get(0));

                List<MatlabNode> elements = tokens.subList(2, tokens.size());

                // Remove consecutive spaces
                elements = removeConsecutiveSpaces(elements);

                // Get tokens separated by spaces
                // List<Integer> spaceIndexes = MatlabTokenUtils.allIndexesOf(MType.Space, elements);
                List<Integer> spaceIndexes = TreeNodeIndexUtils.indexesOf(elements, SpaceNode.class);

                // Add a space index for the last argument
                spaceIndexes.add(elements.size());

                // List<Integer> spaceIndexes = getSpaceIndexes(elements);
                List<String> arguments = new ArrayList<>();
                int beginIndex = 0;
                for (Integer spaceIndex : spaceIndexes) {
                    List<MatlabNode> input = new ArrayList<>(elements.subList(beginIndex, spaceIndex));

                    // If empty list (e.g.: two spaces together) continue
                    if (input.isEmpty()) {
                        continue;
                    }

                    // Transform input into a literal string
                    String literalArgument = input.stream()
                            .map(node -> StatementRules.COMMAND_LITERAL.apply(node))
                            .collect(Collectors.joining());

                    arguments.add(literalArgument);
                    beginIndex = spaceIndex + 1;
                }

                // Build Command token
                CommandNode commandToken = MatlabNodeFactory.newCommand(command, arguments);

                StatementData data = ((StatementNode) statement).getData();
                CommandSt newStatement = StatementFactory.newCommand(data.getLine(), data.isDisplay(), commandToken);

                // NodeInsertUtils.replace(statement, newStatement);
                return newStatement;
                // Replace statement tokens
                // TokenUtils.setChildAndRemove(statement, commandToken, 0, statement.numChildren());

                // return statement;
            }

            private List<MatlabNode> removeConsecutiveSpaces(List<MatlabNode> elements) {
                List<MatlabNode> newElements = new ArrayList<>(elements.size());

                boolean lastAddedWasSpace = false;
                for (MatlabNode node : elements) {
                    if (node instanceof SpaceNode) {
                        // If last added was a space, ignore space
                        if (lastAddedWasSpace) {
                            continue;
                        }

                        lastAddedWasSpace = true;
                    } else {
                        lastAddedWasSpace = false;
                    }

                    newElements.add(node);
                }

                return newElements;
            }
            /*
            	    private List<Integer> getSpaceIndexes(List<MatlabNode> elements) {
            		// List<Integer> spaceIndexes = MatlabTokenUtils.allIndexesOf(MType.Space, elements);
            
            		List<Integer> spaceIndexes = new ArrayList<>();
            		// Initializing to -2 to have at least a distance of 2 between the first index (0)
            		int lastSpaceIndex = -2;
            		for (int i = 0; i < elements.size(); i++) {
            		    MatlabNode node = elements.get(i);
            
            		    // Ignore non-space nodes
            		    if (!(node instanceof SpaceNode)) {
            			continue;
            		    }
            
            		    // Check distance between previous space
            		    boolean previousIsSpace = (i - lastSpaceIndex) == 1;
            
            		    // If previous is space, remove previous index
            		    if (previousIsSpace) {
            			spaceIndexes.remove(spaceIndexes.size() - 1);
            		    }
            
            		    spaceIndexes.add(i);
            		    lastSpaceIndex = i;
            		}
            
            		// Add a space index for the last argument
            		spaceIndexes.add(elements.size());
            
            		return spaceIndexes;
            	    }
            	*/
        };
    }

    private static final Function<MatlabNode, String> COMMAND_DEFAULT = node -> node.getCode();
    private static final FunctionClassMap<MatlabNode, String> COMMAND_LITERAL = new FunctionClassMap<>(
            StatementRules.COMMAND_DEFAULT);

    static {
        StatementRules.COMMAND_LITERAL.put(MatlabCharArrayNode.class, string -> string.getString());
        StatementRules.COMMAND_LITERAL.put(OperatorNode.class, op -> op.getOp().getLiteral());

    }

}
