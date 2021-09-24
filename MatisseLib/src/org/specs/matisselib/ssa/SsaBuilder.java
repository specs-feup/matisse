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

package org.specs.matisselib.ssa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.specs.MatlabIR.MatlabLanguage.MatlabNumber;
import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CellAccessNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CellNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ColonNotationNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabCharArrayNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNumberNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatrixNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OutputsNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ReservedWordNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.RowNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.SimpleAccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.UnusedVariableNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AccessCallSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.BlockSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.BreakSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ContinueSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ElseIfSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ElseSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ForSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.GlobalSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.IfSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNode.nodes.statements.WhileSt;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ExpressionNode;
import org.specs.matisselib.MatisseLibOption;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.passmanager.PassManager;
import org.specs.matisselib.services.DirectiveParser;
import org.specs.matisselib.services.TokenReportingService;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.BreakInstruction;
import org.specs.matisselib.ssa.instructions.BuiltinVariableInstruction;
import org.specs.matisselib.ssa.instructions.BuiltinVariableInstruction.BuiltinVariable;
import org.specs.matisselib.ssa.instructions.CellGetInstruction;
import org.specs.matisselib.ssa.instructions.CellMakeRow;
import org.specs.matisselib.ssa.instructions.CellSetInstruction;
import org.specs.matisselib.ssa.instructions.CommentInstruction;
import org.specs.matisselib.ssa.instructions.ContinueInstruction;
import org.specs.matisselib.ssa.instructions.EndInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.LineInstruction;
import org.specs.matisselib.ssa.instructions.MatrixGetInstruction;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.ReadGlobalInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.StringInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.ValidateAtLeastOneEmptyMatrixInstruction;
import org.specs.matisselib.ssa.instructions.ValidateBooleanInstruction;
import org.specs.matisselib.ssa.instructions.VerticalFlattenInstruction;
import org.specs.matisselib.ssa.instructions.WhileInstruction;
import org.specs.matisselib.ssa.instructions.WriteGlobalInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.collections.AccumulatorMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

public final class SsaBuilder {
    private final FunctionNode function;
    private final DataStore passData;
    private final TokenReportingService reportService;
    private final DirectiveParser directiveParser;
    private final FunctionBody functionBody;
    private final boolean suppressPrinting;
    private final AccumulatorMap<String> variableNameMap = new AccumulatorMap<>();

    private SsaBuilder(FunctionNode function, DataStore passData) {
        this.function = function;

        this.passData = passData;
        this.reportService = passData.get(PassManager.NODE_REPORTING);
        this.directiveParser = passData.get(PassManager.DIRECTIVE_PARSER);
        this.suppressPrinting = passData.get(MatisseLibOption.SUPPRESS_PRINTING);

        FunctionDeclarationSt functionDeclaration = function.getDeclarationNode();

        this.functionBody = new FunctionBody(function.getFunctionName(),
                functionDeclaration.getLine(),
                functionDeclaration.getInputNames(null),
                functionDeclaration.getOutputNames());
    }

    public static FunctionBody buildSsa(FunctionNode function, DataStore passData) {
        Preconditions.checkArgument(function != null);
        Preconditions.checkArgument(passData != null);

        return new SsaBuilder(function, passData).build();
    }

    private FunctionBody build() {
        Set<String> variables = new HashSet<>();
        BlockContext rootContext = newContext(this.functionBody);

        FunctionDeclarationSt functionDeclaration = this.function.getDeclarationNode();
        rootContext.setLine(functionDeclaration.getLine());

        int currentArgument = -1;

        for (MatlabNode node : functionDeclaration.getInputs().getChildren()) {
            ++currentArgument;

            if (node instanceof UnusedVariableNode) {
                continue;
            }

            IdentifierNode identifier = (IdentifierNode) node;
            String identifierName = identifier.getName();
            if (variables.contains(identifierName)) {
                throw this.reportService.emitError(node, PassMessage.PARSE_ERROR, "Duplicated input variable");
            }

            for (BuiltinVariable builtin : BuiltinVariable.values()) {
                String builtinName = builtin.name().toLowerCase(Locale.UK);

                if (identifierName.equals(builtinName)) {
                    this.reportService.emitMessage(node, PassMessage.SUSPICIOUS_CASE,
                            "Argument '" + builtinName + "' has the same name as a builtin variable.");
                }
            }

            variables.add(identifierName);

            rootContext.addInstruction(new ArgumentInstruction(makeName(rootContext, identifierName),
                    currentArgument));
        }

        Set<String> identifiers = this.function.getDescendantsStream()
                .filter(d -> d instanceof IdentifierNode)
                .map(d -> ((IdentifierNode) d).getName())
                .collect(Collectors.toSet());

        for (BuiltinVariable builtin : BuiltinVariable.values()) {
            String builtinName = builtin.name().toLowerCase(Locale.UK);
            if (!variables.contains(builtinName) && identifiers.contains(builtinName)) {
                variables.add(builtinName);
                rootContext.addInstruction(new BuiltinVariableInstruction(makeName(rootContext, builtinName),
                        builtin));
            }
        }

        // Grab all assignments and put them here.
        Stream<String> identifierStream = this.function.getDescendantsStream().flatMap(n -> {
            if (n instanceof AssignmentSt) {
                AssignmentSt assignment = (AssignmentSt) n;
                return getOutputVariablesStream(assignment.getLeftHand());
            }
            if (n instanceof ForSt) {
                String variable = ((ForSt) n).getIndexVar().getName();
                return Stream.of(variable);
            }
            if (n instanceof GlobalSt) {
                return ((GlobalSt) n).getIdentifiers().stream();
            }
            return Stream.empty();
        });

        List<String> declaredIdentifiers = identifierStream
                .distinct()
                .filter(name -> !variables.contains(name))
                .collect(Collectors.toList());

        variables.addAll(declaredIdentifiers);

        for (String identifier : declaredIdentifiers) {
            rootContext.addInstruction(AssignmentInstruction.fromUndefinedValue(makeName(
                    rootContext, identifier)));
        }

        BlockContext finalContext = buildFunctionBody(rootContext);

        int endLine = this.function.getEndLine();
        finalContext.setLine(endLine);

        // Now, let's assign the return values.
        for (MatlabNode outputNode : functionDeclaration.getOutputs().getChildren()) {
            IdentifierNode identifierNode = (IdentifierNode) outputNode;

            String output = identifierNode.getName() + "$ret";

            String globalName = identifierNode.getName();
            if (finalContext.isGlobal(globalName)) {
                finalContext.addInstruction(new ReadGlobalInstruction(output, "^" + globalName));
            } else {
                String input = finalContext.getCurrentName(identifierNode.getName());

                if (input == null) {
                    finalContext.addInstruction(AssignmentInstruction.fromUndefinedValue(output));
                } else {
                    finalContext.addInstruction(AssignmentInstruction.fromVariable(output, input));
                }
            }
        }

        return this.functionBody;
    }

    private BlockContext newContext(FunctionBody function) {
        // parentContext is null for the root context

        SsaBlock block = new SsaBlock();
        int blockId = this.functionBody.addBlock(block);

        BlockContext ctx = new BlockContext(function, null, block, blockId);
        ctx.breakPoints = Collections.emptyList();
        ctx.continuePoints = Collections.emptyList();

        return ctx;
    }

    private BlockContext newContext(BlockContext parentContext) {
        // parentContext is null for the root context

        SsaBlock block = new SsaBlock();
        int blockId = this.functionBody.addBlock(block);

        BlockContext ctx = new BlockContext(parentContext.getFunction(), parentContext, block, blockId);
        ctx.breakPoints = parentContext.breakPoints;
        ctx.continuePoints = parentContext.continuePoints;
        ctx.setVariableNames(parentContext.getVariableNames());

        return ctx;
    }

    private BlockContext newLoopContext(BlockContext parentContext) {
        assert parentContext != null;

        BlockContext newContext = newContext(parentContext);

        newContext.breakPoints = new ArrayList<>();
        newContext.continuePoints = new ArrayList<>();

        return newContext;
    }

    private static BlockContext newShadowingContext(BlockContext oldContext) {
        assert oldContext != null;

        BlockContext newContext = new BlockContext(oldContext.getFunction(), oldContext, oldContext.getBlock(),
                oldContext.getBlockId());
        newContext.breakPoints = oldContext.breakPoints;
        newContext.continuePoints = oldContext.continuePoints;
        newContext.setVariableNames(oldContext.getVariableNames());
        return newContext;
    }

    private String makeName(BlockContext context, String identifier) {
        String newName = identifier + "$" + this.variableNameMap.add(identifier);
        context.setCurrentName(identifier, newName);

        return newName;
    }

    private String getOrGenerateSsaVariableName(BlockContext context, String identifier) {
        if (context.isGlobal(identifier)) {
            String ssaName = makeTemporary(identifier);

            context.addInstruction(new ReadGlobalInstruction(ssaName, "^" + identifier));
            return ssaName;
        }
        return context.getCurrentName(identifier);
    }

    public String makeTemporary(String semantics) {
        return this.functionBody.makeTemporary(semantics);
    }

    private BlockContext buildFunctionBody(BlockContext startContext) {
        BlockContext activeContext = startContext;

        for (StatementNode stmt : this.function.getStatements()) {
            activeContext = buildStatement(stmt, activeContext);
        }

        return activeContext;
    }

    private BlockContext buildStatement(StatementNode stmt, BlockContext currentContext) {

        if (!currentContext.loopProperties.isEmpty()) {
            if (!(stmt instanceof CommentSt) &&
                    !(stmt instanceof BlockSt)) {
                throw reportService.emitError(stmt, PassMessage.CORRECTNESS_ERROR, "Loop property "
                        + SpecsCollections.last(currentContext.loopProperties) + " must come imediately before loop.");
            }
        }

        if (stmt instanceof FunctionDeclarationSt) {
            // Skip
        } else if (stmt instanceof AssignmentSt) {
            currentContext.setLine(stmt.getLine());
            AssignmentSt assignment = (AssignmentSt) stmt;

            if (isErasureStatement(assignment)) {
                return performErasure(assignment, currentContext);
            } else {
                List<String> outputVariables = buildOutputVariables(
                        NameUtils.getSuggestedName(((AssignmentSt) stmt).getRightHand()), assignment.getLeftHand(),
                        currentContext);
                currentContext = buildRootLevelExpression(assignment.getRightHand(), outputVariables, currentContext);

                if (!suppressPrinting && assignment.getData().isDisplay()) {
                    throw this.reportService.emitError(stmt, PassMessage.NOT_YET_IMPLEMENTED,
                            "Display results not implemented.");
                }

                return performAssignment(assignment.getLeftHand(), outputVariables, currentContext);
            }
        } else if (stmt instanceof BreakSt) {
            currentContext.setLine(stmt.getLine());
            currentContext.addInstruction(new BreakInstruction());

            // Break "finishes" a loop, so there is no block after it.

            if (!currentContext.doBreak()) {
                this.reportService.emitError(stmt, PassMessage.PARSE_ERROR,
                        "Found break statement that's not inside loop or switch.");
            }

            return null;
        } else if (stmt instanceof ContinueSt) {
            currentContext.setLine(stmt.getLine());
            currentContext.addInstruction(new ContinueInstruction());

            // Break "finishes" a loop, so there is no block after it.

            if (!currentContext.doContinue()) {
                this.reportService.emitError(stmt, PassMessage.PARSE_ERROR,
                        "Found break statement that's not inside loop.");
            }

            return null;
        } else if (stmt instanceof BlockSt) {
            BlockSt blockSt = (BlockSt) stmt;
            StatementNode header = blockSt.getHeaderNode();

            if (!currentContext.loopProperties.isEmpty()) {
                if (!(header instanceof ForSt)) {
                    throw reportService.emitError(stmt, PassMessage.CORRECTNESS_ERROR, "Loop property "
                            + SpecsCollections.last(currentContext.loopProperties)
                            + " must come imediately before loop.");
                }
            }

            if (header instanceof IfSt) {
                return buildIfStatement(blockSt, currentContext);
            }
            if (header instanceof WhileSt) {
                return buildWhileStatement(blockSt, currentContext);
            }
            if (header instanceof ForSt) {
                return buildForStatement(blockSt, currentContext);
            }

            throw new UnsupportedOperationException("Block: " + header.getNodeName());
        } else if (stmt instanceof CommentSt) {
            currentContext.setLine(stmt.getLine());
            String comment = ((CommentSt) stmt).getCommentString();

            if (comment.trim().startsWith("!")) {
                buildDirective((CommentSt) stmt, currentContext);
            } else {
                SsaInstruction instruction = new CommentInstruction(comment);
                currentContext.addInstruction(instruction);
            }
        } else if (stmt instanceof AccessCallSt) {
            currentContext.setLine(stmt.getLine());
            AccessCallSt accessStmt = (AccessCallSt) stmt;

            // TODO
            // One problem here is how to deal with void functions.
            // We only know if a function is "void" after type inference
            // but we need to know if we're supposed to emit a print instruction.
            // Even if the results are not printed, the output of the access should be assigned
            // to the `ans` variable.

            if (!suppressPrinting && accessStmt.getData().isDisplay()) {
                throw new UnsupportedOperationException("Display results not implemented");
            }

            currentContext = buildExpression(accessStmt.getChild(0), Arrays.asList(), currentContext);
        } else if (stmt instanceof GlobalSt) {
            currentContext.setLine(stmt.getLine());

            GlobalSt globalSt = (GlobalSt) stmt;
            if (!currentContext.isRoot()) {
                throw reportService.emitError(globalSt, PassMessage.NOT_SUPPORTED,
                        "Global declarations are only supported in the root context.");
            }

            for (String global : globalSt.getIdentifiers()) {
                if (currentContext.isGlobal(global)) {
                    // No-op
                    continue;
                }

                currentContext.addGlobal(global);
            }
        } else {
            throw new UnsupportedOperationException("Statement: " + stmt + ", in " + this.function.getCode());
        }

        return currentContext;
    }

    private boolean isErasureStatement(AssignmentSt assignment) {
        MatlabNode right = assignment.getRightHand();

        if (!(right instanceof MatrixNode)) {
            return false;
        }

        MatrixNode rightMatrix = (MatrixNode) right;
        if (!rightMatrix.getRows().isEmpty()) {
            return false;
        }

        return true;
    }

    private void buildDirective(CommentSt stmt, BlockContext currentContext) {
        directiveParser.parseDirective(stmt, currentContext, passData);
    }

    private BlockContext performAssignment(MatlabNode outputs,
            List<String> outputVariables,
            BlockContext context) {

        List<MatlabNode> outputNodes;

        if (outputs instanceof IdentifierNode ||
                outputs instanceof SimpleAccessCallNode ||
                outputs instanceof CellAccessNode) {

            outputNodes = Arrays.asList(outputs);
        } else if (outputs instanceof OutputsNode) {
            outputNodes = outputs.getChildren();
        } else {
            throw new UnsupportedOperationException();
        }

        for (int i = 0; i < outputNodes.size(); ++i) {
            MatlabNode outputNode = outputNodes.get(i);

            String outputVariable = outputVariables.get(i);

            if (outputNode instanceof IdentifierNode) {
                String name = ((IdentifierNode) outputNode).getName();

                if (context.isGlobal(name)) {
                    String newVar = makeName(context, name);
                    context.addInstruction(AssignmentInstruction.fromVariable(newVar, outputVariable));

                    context.addInstruction(new WriteGlobalInstruction("^" + name, newVar));
                } else {
                    context.addInstruction(AssignmentInstruction.fromVariable(makeName(context, name),
                            outputVariable));
                }
            } else if (outputNode instanceof SimpleAccessCallNode) {
                SimpleAccessCallNode accessCall = (SimpleAccessCallNode) outputNode;
                String name = accessCall.getName();

                // First, we'll evaluate the arguments
                List<String> arguments = new ArrayList<>();
                List<MatlabNode> argumentsNodes = accessCall.getArguments();
                context = makeOutputCallArguments(context, name, arguments, argumentsNodes);

                // Now, we'll set the matrix itself.
                boolean global = context.isGlobal(name);
                String oldName = getOrGenerateSsaVariableName(context, name);
                String newName = makeName(context, name);

                context.addInstruction(new MatrixSetInstruction(newName, oldName, arguments, outputVariable));

                if (global) {
                    context.addInstruction(new WriteGlobalInstruction("^" + name, newName));
                }
            } else if (outputNode instanceof CellAccessNode) {
                CellAccessNode cellAccessNode = (CellAccessNode) outputNode;
                MatlabNode left = cellAccessNode.getLeft();
                if (!(left instanceof IdentifierNode)) {
                    throw this.reportService.emitError(outputNode,
                            PassMessage.NOT_YET_IMPLEMENTED,
                            "Output node: " + outputNode.getCode());
                }

                String name = ((IdentifierNode) left).getName();
                boolean global = context.isGlobal(name);

                // First, we'll evaluate the arguments
                List<String> arguments = new ArrayList<>();
                List<MatlabNode> argumentsNodes = cellAccessNode.getArguments();
                context = makeOutputCallArguments(context, name, arguments, argumentsNodes);

                String oldName;
                String newName;
                if (global) {
                    oldName = makeName(context, name);
                    context.addInstruction(new ReadGlobalInstruction(oldName, "^" + name));
                    newName = makeName(context, name);
                } else {
                    // Now, we'll set the matrix itself.
                    oldName = getOrGenerateSsaVariableName(context, name);
                    newName = makeName(context, name);
                }

                context.addInstruction(new CellSetInstruction(newName, oldName, arguments, outputVariable));

                if (global) {
                    context.addInstruction(new WriteGlobalInstruction("^" + name, newName));
                }
            } else if (outputNode instanceof UnusedVariableNode) {
                // No action needed
            } else {
                throw this.reportService.emitError(outputNode,
                        PassMessage.NOT_YET_IMPLEMENTED,
                        "Output node: " + outputNode.getCode());
            }
        }

        return context;
    }

    private BlockContext performErasure(AssignmentSt assignment, BlockContext currentContext) {
        MatlabNode leftHand = assignment.getLeftHand();

        if (leftHand instanceof IdentifierNode) {
            return handleEmptyMatrixAssignment((IdentifierNode) leftHand, assignment.isDisplay(), currentContext);
        } else if (leftHand instanceof SimpleAccessCallNode) {
            // Explanation of MATLAB rules:

            // In A(...) = []:
            // Either one of the indices of A is an empty matrix
            // Or at most one index of A is a non-":" value.
            // The former case will be handled by the validate_at_least_one_empty_matrix instruction.
            // The latter is dealt with in two ways:
            // 1) If ALL values of A are :, then the expression is equivalent to A = []; and we will deal with it
            // accordingly.
            // 2) If ONE value of A is non-:, then we will use the delete instruction.

            // Fortunately, A(1, :, :, 1) = [] is an error even when ndims(A) is 3.
            // Repeated indices (i.e., A([1, 1]) = []) are OK, but out-of-range deletions are not.

            // Normally, in A(expr1, expr2) = [], the is-[] validation happens at runtime.
            // However, if expr1/expr2 are range expressions, then the error happens at parse-time, regardless of the
            // range values.
            // Notably, A(1:0, 1:0, :) is an error, but a = 1:0; A(a, a, :) = [] is not.
            // Specifically, if a deletion statement has more than one range expression argument, it will always yield
            // an error.
            // A(1:0, 1, :) is not an error (assuming size(A, 2) >= 1).

            // FIXME:
            // A(1:0, 1:0, []) and A(colon(1, 0), colon(1, 0), []) should be different, as the former throws
            // an error and the latter does not.
            // We ignore that part, for simplicity.

            // FIXME:
            // A(2, []) = []; should be an error if size(A, 1) < 2, but we will ignore that part, for simplicity.

            SimpleAccessCallNode accessNode = (SimpleAccessCallNode) leftHand;
            IdentifierNode baseMatrix = accessNode.getIdentifier();
            String matrixName = baseMatrix.getName();

            String inName;
            if (currentContext.isGlobal(matrixName)) {
                inName = makeTemporary(matrixName);
                currentContext.addInstruction(new ReadGlobalInstruction(inName, "^" + matrixName));
            } else {
                inName = currentContext.getCurrentName(matrixName);
            }

            List<String> referencedVariables = new ArrayList<>();
            int nonColonIndex = -1;
            boolean foundRange = false;

            List<MatlabNode> arguments = accessNode.getArguments();
            for (int i = 0; i < arguments.size(); i++) {
                MatlabNode argument = arguments.get(i);
                if (!(argument instanceof ColonNotationNode)) {
                    currentContext.pushEndContext("deletion index", inName, i, arguments.size());
                    nonColonIndex = i;

                    if (argument instanceof SimpleAccessCallNode) {
                        if (((SimpleAccessCallNode) argument).getIdentifier().equals("colon")) {
                            if (foundRange) {
                                throw reportService.emitError(argument, PassMessage.CORRECTNESS_ERROR,
                                        "A null assignment can have only one non-colon index.");
                            }
                            foundRange = true;
                        }
                    }

                    String variable = makeTemporary(
                            NameUtils.getSuggestedName(accessNode.getIdentifier()) + "_index" + (i + 1));
                    currentContext = buildExpression(argument, Arrays.asList(variable), currentContext);
                    referencedVariables.add(variable);

                    currentContext.popEndContext();
                }
            }

            if (referencedVariables.isEmpty()) {
                reportService.emitMessage(assignment, PassMessage.SUSPICIOUS_CASE,
                        "Full-coverage deletion assignment. Consider just using " + matrixName + " = []");

                return handleEmptyMatrixAssignment(baseMatrix, assignment.isDisplay(), currentContext);
            } else if (referencedVariables.size() == 1) {
                String output;
                if (currentContext.isGlobal(matrixName)) {
                    output = makeTemporary(matrixName);
                } else {
                    output = makeName(currentContext, matrixName);
                }

                String nonColonIndexVariable = makeTemporary("non_colon_index");
                currentContext.addInstruction(AssignmentInstruction.fromInteger(nonColonIndexVariable, nonColonIndex));
                String numIndices = makeTemporary("num_indices");
                currentContext.addInstruction(AssignmentInstruction.fromInteger(numIndices, arguments.size()));

                currentContext.addInstruction(
                        new UntypedFunctionCallInstruction("MATISSE_delete", Arrays.asList(output),
                                Arrays.asList(inName,
                                        referencedVariables.get(0),
                                        nonColonIndexVariable, numIndices)));

                if (currentContext.isGlobal(matrixName)) {
                    currentContext.addInstruction(new WriteGlobalInstruction("^" + matrixName, output));
                }
            } else {
                reportService.emitMessage(assignment, PassMessage.SUSPICIOUS_CASE,
                        "Deletion statement with more than one non-: indices. This only works if the indices are empty matrices, in which case the statement is useless.");

                currentContext.addInstruction(new ValidateAtLeastOneEmptyMatrixInstruction(referencedVariables));
            }
        } else {
            throw new NotImplementedException(leftHand);
        }

        return currentContext;
    }

    private BlockContext handleEmptyMatrixAssignment(IdentifierNode leftHand, boolean display,
            BlockContext currentContext) {
        MatlabNode rightHand = MatlabNodeFactory.newSimpleAccessCall("vertcat");
        AssignmentSt newAssignment = StatementFactory.newAssignment(leftHand, rightHand);

        return buildStatement(newAssignment, currentContext);
    }

    private BlockContext makeOutputCallArguments(BlockContext context, String name, List<String> arguments,
            List<MatlabNode> argumentsNodes) {

        String outerSsaName;
        if (context.isGlobal(name)) {
            // Necessary for `end` expressions.
            // e.g. A(end) = 1;
            outerSsaName = makeTemporary(name);
            context.addInstruction(new ReadGlobalInstruction(outerSsaName, "^" + name));
        } else {
            outerSsaName = context.getCurrentName(name);
        }

        for (int j = 0; j < argumentsNodes.size(); j++) {
            MatlabNode node = argumentsNodes.get(j);

            // For A(X) = ..., call the temporary of X "A_index"
            // For A(X, Y) = ..., call the temporary of X "A_index1" and the temporary of Y "A_index2"
            String temporaryName = name + "_index";
            if (argumentsNodes.size() != 1) {
                temporaryName += (j + 1);
            }

            String var = makeTemporary(temporaryName);
            arguments.add(var);
            context.pushEndContext("output call", outerSsaName, j, argumentsNodes.size());
            context = buildExpression(node, Arrays.asList(var), context);
            context.popEndContext();
        }

        assert !context.getCurrentEndContext().isPresent();

        return context;
    }

    private BlockContext buildWhileStatement(BlockSt blockSt, BlockContext context) {
        WhileSt whileStmt = (WhileSt) blockSt.getHeaderNode();
        context.setLine(whileStmt.getLine());

        // We know the condition is while 1 because a previous pass (WhileSimplifier) takes care of that.
        assert whileStmt.getCondition() instanceof MatlabNumberNode;
        assert ((MatlabNumberNode) whileStmt.getCondition()).getNumberString().equals("1");

        BlockContext loopContext = newLoopContext(context);
        for (String variable : context.getVariables()) {
            if (!loopContext.isGlobal(variable)) {
                makeName(loopContext, variable);
            }
        }

        // Create a new context, but don't create a new block.
        // We need loopContext and loopBodyContext to be separate because we're going to use the original variables
        // and write to the first loop block.
        BlockContext loopBodyContext = newShadowingContext(loopContext);

        for (StatementNode stmt : blockSt.getStatements()) {
            if (stmt instanceof WhileSt) {
                // Skip header
                continue;
            }
            if (loopBodyContext == null) {
                break;
            }

            loopBodyContext = buildStatement(stmt, loopBodyContext);
        }

        loopBodyContext.doContinue();

        // Now that the loop is finished, we'll add the phi nodes at the beginning
        for (String variable : context.getVariables()) {
            if (loopContext.isGlobal(variable)) {
                continue;
            }

            String nameInLoop = loopContext.getCurrentName(variable);

            List<String> names = new ArrayList<>();
            List<Integer> blockIds = new ArrayList<>();

            // Variables before loop
            names.add(context.getCurrentName(variable));
            blockIds.add(context.getBlockId());

            for (BlockContext continuePoint : loopContext.continuePoints) {
                names.add(continuePoint.getCurrentName(variable));
                blockIds.add(continuePoint.getBlockId());
            }

            loopContext.prependInstruction(new PhiInstruction(nameInLoop,
                    names,
                    blockIds));
        }

        // And we'll also add the phi nodes after the loop

        BlockContext afterLoopContext = newContext(context);
        afterLoopContext.setLine(blockSt.getLine());

        mergeVariables(afterLoopContext, loopContext.breakPoints);

        context.addInstruction(new WhileInstruction(loopContext.getBlockId(), afterLoopContext.getBlockId()));

        return afterLoopContext;
    }

    private BlockContext buildForStatement(BlockSt blockSt,
            BlockContext currentContext) {

        ForSt forStmt = (ForSt) blockSt.getHeaderNode();
        currentContext.setLine(forStmt.getLine());

        MatlabNode expression = forStmt.getExpression().normalizeExpr();
        // if (expression instanceof ExpressionNode) {
        // expression = expression.getChild(0);
        // }

        String start, interval, end;
        BlockContext loopContext;

        if (expression instanceof SimpleAccessCallNode
                && ((SimpleAccessCallNode) expression).getName().equals("colon")) {
            SimpleAccessCallNode accessCall = (SimpleAccessCallNode) expression;

            int colonArgumentsCount = accessCall.getArguments().size();
            assert colonArgumentsCount == 2 || colonArgumentsCount == 3;

            int endVariableIndex = colonArgumentsCount == 2 ? 1 : 2;

            start = makeTemporary("start");
            currentContext = buildRootLevelExpression(accessCall.getArguments().get(0), Arrays.asList(start),
                    currentContext);

            interval = makeTemporary("interval");
            if (colonArgumentsCount == 2) {
                currentContext.addInstruction(AssignmentInstruction.fromInteger(interval, 1));
            } else {
                buildRootLevelExpression(accessCall.getArguments().get(1), Arrays.asList(interval), currentContext);
            }

            end = makeTemporary("end");
            currentContext = buildRootLevelExpression(accessCall.getArguments().get(endVariableIndex),
                    Arrays.asList(end),
                    currentContext);

            loopContext = newLoopContext(currentContext);
            for (String variable : currentContext.getVariables()) {
                String ssaName;
                if (loopContext.isGlobal(variable)) {
                    ssaName = makeTemporary(variable);
                } else {
                    ssaName = makeName(loopContext, variable);
                }

                if (variable.equals(forStmt.getIndexVar().getName())) {
                    if (loopContext.isGlobal(variable)) {
                        loopContext.prependInstruction(new WriteGlobalInstruction("^" + variable, ssaName));
                    }
                    loopContext.prependInstruction(new IterInstruction(ssaName));
                }
            }

        } else {
            String sourceMatrix = makeTemporary("source");
            currentContext = buildExpression(expression, Arrays.asList(sourceMatrix), currentContext);

            start = makeTemporary("start");
            currentContext.addInstruction(AssignmentInstruction.fromInteger(start, 1));

            interval = makeTemporary("interval");
            currentContext.addInstruction(AssignmentInstruction.fromInteger(interval, 1));

            String lines = makeTemporary("lines");
            currentContext.addInstruction(new EndInstruction(lines, sourceMatrix, 0, 2));

            end = makeTemporary("end");
            currentContext.addInstruction(new EndInstruction(end, sourceMatrix, 1, 2));

            loopContext = newLoopContext(currentContext);
            for (String variable : currentContext.getVariables()) {
                String ssaName = null;
                if (!loopContext.isGlobal(variable)) {
                    ssaName = makeName(loopContext, variable);
                }

                if (variable.equals(forStmt.getIndexVar().getName())) {
                    String index = makeTemporary("iter");

                    if (loopContext.isGlobal(variable)) {
                        assert ssaName == null;
                        ssaName = makeTemporary(variable);
                        loopContext.prependInstruction(new WriteGlobalInstruction("^" + variable, ssaName));
                    }

                    loopContext.prependInstruction(new MatrixGetInstruction(ssaName, sourceMatrix, Arrays.asList(lines,
                            index)));
                    loopContext.prependInstruction(new IterInstruction(index));
                }
            }
        }

        BlockContext currentLoopContext = newShadowingContext(loopContext);
        currentLoopContext.setLine(forStmt.getLine());

        for (StatementNode stmt : blockSt.getStatements()) {
            if (stmt instanceof ForSt) {
                // Skip header
                continue;
            }
            if (currentLoopContext == null) {
                break;
            }

            currentLoopContext = buildStatement(stmt, currentLoopContext);
        }

        if (currentLoopContext != null) {
            currentLoopContext.doContinue();
        }

        // Now that the loop is finished, we'll add the phi nodes at the beginning
        for (String variable : currentContext.getVariables()) {
            if (loopContext.isGlobal(variable)) {
                continue;
            }

            String nameInLoop = loopContext.getCurrentName(variable);

            if (variable.equals(forStmt.getIndexVar().getName())) {
                // Already added this variable before
                continue;
            }

            List<String> names = new ArrayList<>();
            List<Integer> blockIds = new ArrayList<>();

            names.add(currentContext.getCurrentName(variable));
            blockIds.add(currentContext.getBlockId());

            for (BlockContext context : loopContext.continuePoints) {
                names.add(context.getCurrentName(variable));
                blockIds.add(context.getBlockId());
            }

            loopContext.prependInstruction(new PhiInstruction(nameInLoop,
                    names,
                    blockIds));
        }

        loopContext.prependInstruction(new LineInstruction(forStmt.getLine()));

        BlockContext afterContext = newContext(currentContext);
        currentContext.addInstruction(new ForInstruction(start, interval, end, loopContext.getBlockId(), afterContext
                .getBlockId(), currentContext.loopProperties));
        currentContext.loopProperties.clear();

        afterContext.setLine(blockSt.getLine());

        List<BlockContext> terminatingContexts = new ArrayList<>();
        terminatingContexts.add(currentContext);
        terminatingContexts.addAll(loopContext.breakPoints);
        terminatingContexts.addAll(loopContext.continuePoints); // In for loops, every continue can
        // potentially end the loop
        mergeVariables(afterContext, terminatingContexts);

        return afterContext;
    }

    private BlockContext buildIfStatement(BlockSt blockSt, BlockContext context) {
        // We know there are no ElseIfSts because of ElseIfUnroller removed them.
        assert !blockSt.getChildren().stream().anyMatch(node -> node instanceof ElseIfSt);

        IfSt ifStmt = (IfSt) blockSt.getHeaderNode();
        context.setLine(ifStmt.getLine());

        MatlabNode condition = ifStmt.getExpression();
        String conditionName = makeTemporary("condition");
        context = buildRootLevelExpression(condition, Arrays.asList(conditionName), context);

        int ifStart = 1;
        int ifEnd = blockSt.getNumChildren();
        for (int i = 0; i < blockSt.getNumChildren(); ++i) {
            if (blockSt.getChild(i) instanceof ElseSt) {
                ifEnd = i;
                break;
            }
        }

        BlockContext trueContext = newContext(context);
        BlockContext currentTrueContext = trueContext;

        for (int i = ifStart; i < ifEnd && currentTrueContext != null; ++i) {
            currentTrueContext = buildStatement(blockSt.getStatements().get(i), currentTrueContext);
        }

        BlockContext falseContext = newContext(context);
        BlockContext currentFalseContext = falseContext;

        for (int i = ifEnd + 1; i < blockSt.getNumChildren() && currentFalseContext != null; ++i) {
            currentFalseContext = buildStatement(blockSt.getStatements().get(i), currentFalseContext);
        }

        BlockContext endContext = newContext(context);
        endContext.setLine(blockSt.getLine());

        mergeVariables(endContext,
                currentTrueContext,
                currentFalseContext);

        // Add branch instruction

        context
                .addInstruction(new BranchInstruction(conditionName,
                        trueContext.getBlockId(),
                        falseContext.getBlockId(),
                        endContext.getBlockId()));

        return endContext;
    }

    private void mergeVariables(BlockContext endContext, BlockContext... sourceContexts) {
        mergeVariables(endContext, Arrays.asList(sourceContexts));
    }

    private void mergeVariables(BlockContext endContext, List<BlockContext> sourceContexts) {

        assert endContext != null;

        List<BlockContext> sourceContextsList = new ArrayList<>();
        for (BlockContext context : sourceContexts) {
            if (context != null) {
                sourceContextsList.add(context);
            }
        }

        // FIXME
        // This assertion is valid for our current tests, but probably not in the general case.
        // Figure out how to deal with this.
        // For an example, consider the case of `if(cond) break; else break;`
        assert sourceContextsList.size() > 0;

        BlockContext firstContext = sourceContextsList.get(0);

        List<Integer> blockIds = sourceContextsList.stream()
                .map(ctx -> ctx.getBlockId())
                .collect(Collectors.toList());

        for (String key : firstContext.getVariables()) {

            List<String> sourceNames = sourceContextsList.stream()
                    .map(ctx -> ctx.getCurrentName(key))
                    .collect(Collectors.toList());

            if (sourceNames.stream().distinct().count() > 1) {
                String newName = makeName(endContext, key);

                endContext.addInstruction(new PhiInstruction(newName,
                        sourceNames,
                        blockIds));
            } else {
                endContext.setCurrentName(key, firstContext.getCurrentName(key));
            }
        }
    }

    private List<String> buildOutputVariables(String baseSemantics, MatlabNode leftHand, BlockContext blockContext) {
        List<String> outputVariables = new ArrayList<>();

        if (leftHand instanceof IdentifierNode) {
            outputVariables.add(makeTemporary(baseSemantics));
        } else if (leftHand instanceof OutputsNode) {
            List<MatlabNode> children = leftHand.getChildren();
            for (int i = 0; i < children.size(); i++) {
                MatlabNode output = children.get(i);
                String suggestedName = baseSemantics + "_out";
                if (children.size() != 1) {
                    suggestedName += (i + 1);
                }
                outputVariables.add(buildOutputVariables(suggestedName, output, blockContext).get(0));
            }
        } else {
            outputVariables.add(makeTemporary(baseSemantics));
        }

        return outputVariables;
    }

    private BlockContext buildRootLevelExpression(MatlabNode expression, List<String> outputVariables,
            BlockContext blockContext) {

        BlockContext context = buildExpression(expression, outputVariables, blockContext);

        assert !context.getCurrentEndContext().isPresent();

        return context;
    }

    private BlockContext buildExpression(MatlabNode expression, List<String> outputVariables,
            BlockContext blockContext) {

        expression = expression.normalize();

        // while (expression instanceof ParenthesisNode) {
        // expression = expression.getChild(0);
        // }

        assert !(expression instanceof MatrixNode);

        if (expression instanceof MatlabNumberNode) {
            if (outputVariables.size() != 1) {
                throw this.reportService.emitError(expression, PassMessage.PARSE_ERROR, "Too many output arguments");
            }

            String output = outputVariables.get(0);
            MatlabNumber input = ((MatlabNumberNode) expression).getNumber();

            blockContext.addInstruction(AssignmentInstruction.fromNumber(output, input));
            return blockContext;
        }
        if (expression instanceof ExpressionNode) {
            throw new RuntimeException("THERE SHOULD BE NO EXPRESSION NODE");
            // return buildExpression(expression.getChild(0), outputVariables, blockContext);
        }
        if (expression instanceof SimpleAccessCallNode) {
            SimpleAccessCallNode accessCall = (SimpleAccessCallNode) expression;

            // This may either be a function or a matrix access.
            // We need to find out which.

            String leftName = accessCall.getName();

            if (accessCall.getArguments().size() == 1) {
                MatlabNode argument = accessCall.getArguments().get(0);

                if (argument instanceof ColonNotationNode) {
                    if (outputVariables.size() != 1) {
                        throw this.reportService.emitError(expression, PassMessage.PARSE_ERROR,
                                "Too many output arguments");
                    }

                    VerticalFlattenInstruction instruction = new VerticalFlattenInstruction(
                            outputVariables.get(0),
                            getOrGenerateSsaVariableName(blockContext, leftName));
                    blockContext.addInstruction(instruction);
                    return blockContext;
                }
            }

            boolean isVariable = blockContext.hasVariable(leftName);
            BuildInputVariablesResult result = buildInputVariables(accessCall, blockContext);
            List<String> inputs = result.inputs;
            blockContext = result.blockContext;

            SsaInstruction instruction;
            if (isVariable) {
                if (outputVariables.size() != 1) {
                    throw this.reportService.emitError(expression, PassMessage.PARSE_ERROR,
                            "Too many output arguments");
                }

                String currentLeftName = getOrGenerateSsaVariableName(blockContext, leftName);
                instruction = new MatrixGetInstruction(outputVariables.get(0), currentLeftName, inputs);
            } else {
                instruction = new UntypedFunctionCallInstruction(leftName, outputVariables, inputs);
            }
            blockContext.addInstruction(instruction);

            return blockContext;
        }
        if (expression instanceof CellAccessNode) {
            CellAccessNode cellAccess = (CellAccessNode) expression;

            if (cellAccess.getArguments().size() == 1) {
                MatlabNode argument = cellAccess.getArguments().get(0);

                if (argument instanceof ColonNotationNode) {
                    throw this.reportService.emitError(expression, PassMessage.NOT_YET_IMPLEMENTED,
                            "Colon notation for cell access");
                }
            }

            if (outputVariables.size() != 1) {
                throw this.reportService.emitError(expression, PassMessage.PARSE_ERROR,
                        "Too many output arguments");
            }

            BuildInputVariablesResult result = buildInputVariables(cellAccess, blockContext);
            List<String> inputs = result.inputs;
            blockContext = result.blockContext;

            String temp = this.functionBody.makeTemporary("cell_array");
            blockContext = buildExpression(cellAccess.getLeft(), Arrays.asList(temp), blockContext);
            blockContext.addInstruction(new CellGetInstruction(outputVariables.get(0), temp, inputs));

            return blockContext;
        }
        if (expression instanceof IdentifierNode) {
            IdentifierNode identifier = (IdentifierNode) expression;

            // This may either be a function call or a variable.
            // We need to figure out which.

            String name = identifier.getName();

            if (blockContext.isGlobal(name)) {
                // Variable
                if (outputVariables.size() > 1) {
                    throw this.reportService.emitError(identifier, PassMessage.PARSE_ERROR, "Too many outputs");
                }

                String outputVariable = outputVariables.get(0);
                blockContext.addInstruction(new ReadGlobalInstruction(outputVariable, "^" + name));
                return blockContext;
            } else if (blockContext.hasVariable(name)) {
                // Variable
                if (outputVariables.size() > 1) {
                    throw this.reportService.emitError(identifier, PassMessage.PARSE_ERROR, "Too many outputs");
                }

                blockContext.addInstruction(AssignmentInstruction.fromVariable(
                        outputVariables.get(0),
                        blockContext.getCurrentName(name)));
                return blockContext;
            }

            SsaInstruction instruction = new UntypedFunctionCallInstruction(name, outputVariables, Arrays.asList());
            blockContext.addInstruction(instruction);

            return blockContext;
        }
        if (expression instanceof MatlabCharArrayNode) {
            if (outputVariables.size() > 1) {
                throw this.reportService.emitError(expression, PassMessage.PARSE_ERROR, "Too many outputs");
            }

            String string = ((MatlabCharArrayNode) expression).getString();
            SsaInstruction instruction = new StringInstruction(outputVariables.get(0), string);
            blockContext.addInstruction(instruction);

            return blockContext;
        }
        if (expression instanceof OperatorNode) {
            // Can be && or ||
            if (outputVariables.size() > 1) {
                throw this.reportService.emitError(expression, PassMessage.PARSE_ERROR, "Too many outputs");
            }

            OperatorNode operator = (OperatorNode) expression;

            String operatorFunctionName = operator.getOp().getFunctionName();
            String leftOut = makeTemporary(operatorFunctionName + "_left");
            MatlabNode leftOperand = operator.getChild(0);
            blockContext = buildExpression(leftOperand, Arrays.asList(leftOut), blockContext);

            blockContext.addInstruction(new ValidateBooleanInstruction(leftOut));

            // We will now create the branch.
            BlockContext trueContext = newContext(blockContext);
            BlockContext falseContext = newContext(blockContext);
            BlockContext endContext = newContext(blockContext);

            blockContext.addInstruction(new BranchInstruction(leftOut,
                    trueContext.getBlockId(), falseContext.getBlockId(), endContext.getBlockId()));

            BlockContext continueContext, shortContext;
            BuiltinVariable constant;
            if (operator.getOp() == MatlabOperator.ShortCircuitAnd) {
                continueContext = trueContext;
                shortContext = falseContext;
                constant = BuiltinVariable.FALSE;
            } else if (operator.getOp() == MatlabOperator.ShortCircuitOr) {
                continueContext = falseContext;
                shortContext = trueContext;
                constant = BuiltinVariable.TRUE;
            } else {
                // These should be removed in a previous phase (OperatorReplacementPass)
                throw new UnsupportedOperationException("Found unexpected operator");
            }

            String rightOut = makeTemporary(operatorFunctionName + "_right");
            MatlabNode rightOperand = operator.getChild(1);
            continueContext = buildExpression(rightOperand, Arrays.asList(rightOut), continueContext);
            continueContext.addInstruction(new ValidateBooleanInstruction(rightOut));
            String rightAsLogical = makeTemporary(operatorFunctionName + "_right_as_logical");
            continueContext.addInstruction(
                    new UntypedFunctionCallInstruction("logical",
                            Arrays.asList(rightAsLogical), Arrays.asList(rightOut)));

            String constantOut = makeTemporary(operatorFunctionName + "_out");
            shortContext.addInstruction(new BuiltinVariableInstruction(constantOut, constant));

            List<String> inputs = Arrays.asList(rightAsLogical, constantOut);
            List<Integer> sourceBlocks = Arrays.asList(continueContext.getBlockId(), shortContext.getBlockId());
            endContext.addInstruction(new PhiInstruction(outputVariables.get(0), inputs, sourceBlocks));

            // TODO: Cast to logical

            return endContext;
        }
        if (expression instanceof ColonNotationNode) {
            // The case where the colon notation is the only child has already been handled
            // above, in the SimpleAccessCallNode.
            if (expression.getParent() instanceof SimpleAccessCallNode) {
                SimpleAccessCallNode parent = (SimpleAccessCallNode) expression.getParent();

                String inputName = parent.getName();
                if (!blockContext.hasVariable(inputName)) {
                    throw this.reportService.emitError(expression, PassMessage.CORRECTNESS_ERROR, inputName
                            + " is not a variable.");
                }

                String input = getOrGenerateSsaVariableName(blockContext, inputName);

                String rangeStart = makeTemporary("start");
                String rangeEnd = makeTemporary("end");

                // The first child is the name. So the first argument is child 1
                blockContext.addInstruction(AssignmentInstruction.fromInteger(rangeStart, 1));

                int index = parent.indexOfChild(expression) - 1;
                int numIndices = parent.getArguments().size();
                blockContext.addInstruction(new EndInstruction(rangeEnd, input, index, numIndices));

                blockContext.addInstruction(new UntypedFunctionCallInstruction("colon", outputVariables, Arrays.asList(
                        rangeStart, rangeEnd)));

                return blockContext;
            }

            // TODO ?
        }
        if (expression instanceof ReservedWordNode) {
            ReservedWordNode reservedWord = (ReservedWordNode) expression;

            if (reservedWord.getWord() == ReservedWord.End) {
                SsaBuilderEndContext endContext = blockContext.getCurrentEndContext()
                        .orElseThrow(() -> this.reportService.emitError(reservedWord, PassMessage.CORRECTNESS_ERROR,
                                "Could not find matrix access 'end' refers to."));

                String input = endContext.referencedSsaVariable;
                int index = endContext.index;
                int numIndices = endContext.numIndices;

                String output = outputVariables.get(0);

                blockContext.addInstruction(new EndInstruction(output, input, index, numIndices));

                return blockContext;
            }

        }
        if (expression instanceof CellNode) {
            CellNode cellNode = (CellNode) expression;

            if (outputVariables.size() > 1) {
                throw this.reportService.emitError(expression, PassMessage.PARSE_ERROR, "Too many outputs");
            }
            String output = outputVariables.get(0);

            List<RowNode> rows = cellNode.getRows();
            assert rows.size() <= 1;

            List<MatlabNode> contentNodes = rows.size() != 0 ? rows.get(0).getChildren() : Collections.emptyList();

            List<String> content = new ArrayList<>();
            for (MatlabNode node : contentNodes) {
                String cellOutput = functionBody.makeTemporary(NameUtils.getSuggestedName(node));
                blockContext = buildExpression(node, Arrays.asList(cellOutput), blockContext);
                content.add(cellOutput);
            }

            blockContext.addInstruction(new CellMakeRow(output, content));
            return blockContext;
        }

        throw new NotImplementedException(expression + ", at " + expression.getParent());
    }

    class BuildInputVariablesResult {
        List<String> inputs;
        BlockContext blockContext;

        BuildInputVariablesResult(List<String> inputs, BlockContext blockContext) {
            this.inputs = inputs;
            this.blockContext = blockContext;
        }
    }

    private BuildInputVariablesResult buildInputVariables(SimpleAccessCallNode accessCall,
            BlockContext blockContext) {

        List<String> inputs = new ArrayList<>();

        String leftName = accessCall.getName();
        boolean isVariable = blockContext.hasVariable(leftName);
        String ssaName = null;
        if (isVariable) {
            if (blockContext.isGlobal(leftName)) {
                // Read value so we can use it for `end` expressions.
                ssaName = makeTemporary(leftName);
                blockContext.addInstruction(new ReadGlobalInstruction(ssaName, "^" + leftName));
            } else {
                ssaName = blockContext.getCurrentName(leftName);
                assert ssaName != null;
            }
        }

        List<MatlabNode> arguments = accessCall.getArguments();
        for (int i = 0; i < arguments.size(); i++) {
            MatlabNode argument = arguments.get(i);
            String variable = makeTemporary(accessCall.getName() + "_arg" + (i + 1));
            if (isVariable) {
                blockContext.pushEndContext("input vars", ssaName, i, arguments.size());
            }
            blockContext = buildExpression(argument, Arrays.asList(variable), blockContext);
            if (isVariable) {
                blockContext.popEndContext();
            }

            inputs.add(variable);
        }

        return new BuildInputVariablesResult(inputs, blockContext);
    }

    private BuildInputVariablesResult buildInputVariables(CellAccessNode cellAccess,
            BlockContext blockContext) {

        List<String> inputs = new ArrayList<>();

        String leftName = cellAccess.getName();
        String ssaName;
        if (blockContext.isGlobal(leftName)) {
            // Read value so we can use it for `end` expressions.
            ssaName = makeTemporary(leftName);
            blockContext.addInstruction(new ReadGlobalInstruction(ssaName, "^" + leftName));
        } else {
            ssaName = blockContext.getCurrentName(leftName);
            assert ssaName != null : leftName + " has null SSA name.";
        }

        String baseName = NameUtils.getSuggestedName(ssaName);
        List<MatlabNode> arguments = cellAccess.getArguments();
        for (int i = 0; i < arguments.size(); i++) {
            MatlabNode argument = arguments.get(i);
            String variable = makeTemporary(baseName + "_arg" + (i + 1));

            blockContext.pushEndContext("cell input", ssaName, i, arguments.size());
            blockContext = buildExpression(argument, Arrays.asList(variable), blockContext);
            blockContext.popEndContext();

            inputs.add(variable);
        }

        return new BuildInputVariablesResult(inputs, blockContext);
    }

    private Stream<String> getOutputVariablesStream(MatlabNode node) {
        if (node instanceof IdentifierNode) {
            return Stream.of(((IdentifierNode) node).getName());
        }
        if (node instanceof UnusedVariableNode) {
            return Stream.empty();
        }
        if (node instanceof SimpleAccessCallNode) {
            SimpleAccessCallNode accessCall = (SimpleAccessCallNode) node;
            return Stream.of(accessCall.getName());
        }
        if (node instanceof OutputsNode) {
            return node.getChildrenStream().flatMap(this::getOutputVariablesStream);
        }
        if (node instanceof CellAccessNode) {
            return getOutputVariablesStream(((CellAccessNode) node).getLeft());
        }

        throw new NotImplementedException(node.getClass());
    }
}
