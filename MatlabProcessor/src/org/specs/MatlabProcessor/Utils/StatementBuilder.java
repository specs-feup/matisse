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

package org.specs.MatlabProcessor.Utils;

import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newAccessCall;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newBreak;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newCase;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newComment;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newContinue;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newElse;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newElseIf;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newEnd;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newEndFor;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newEndFunction;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newEndIf;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newEndWhile;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newExpression;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newFor;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newIf;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newInvoke;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newOtherwise;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newParfor;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newReturn;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newSwitch;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newTry;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newUndefined;
import static org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory.newWhile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.specs.MatlabIR.Exceptions.CodeParsingException;
import org.specs.MatlabIR.MatlabLanguage.ClassWord;
import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AttributeNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AttributesNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.BaseClassesNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CellNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ClassWordNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CommentNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.FieldAccessNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.InvokeNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabCharArrayNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ParenthesisNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ReservedWordNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.SimpleAccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNode.nodes.statements.mclass.ClassNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.AssignmentNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SpaceNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SubscriptSeparatorNode;
import org.specs.MatlabProcessor.Reporting.ProcessorErrorType;

import pt.up.fe.specs.util.reporting.Reporter;

/**
 * Determines the type of Statements by inspecting its children.
 * 
 * @author JoaoBispo
 *
 */
public class StatementBuilder {

    /**
     * Creates a new Statement, from generic tokens. Attempts to find the correct type of statement. If it couldn't
     * determine the kind of statement, returns an UndefinedStatement.
     * 
     * @param nodes
     * @param lineNumber
     * @param displayResults
     * @return
     */
    public static StatementNode newStatement(Reporter reportService, int lineNumber, boolean displayResults,
            List<MatlabNode> nodes) {
        // System.out.println("NODES:" + nodes);
        // Data to create a StatementNode
        BuilderData data = new BuilderData(reportService, lineNumber, displayResults, nodes);

        // Clear spaces
        // nodes = clearSpaces(nodes);

        // If first child is a reserved word, use reserved word builder
        Optional<StatementNode> statement = getStatementFromReservedWord(data);

        if (statement.isPresent()) {
            return statement.get();
        }

        // Build based on the class of the first node
        Class<? extends MatlabNode> nodeClass = nodes.get(0).getClass();

        if (StatementBuilder.CLASS_BUILDER.containsKey(nodeClass)) {
            return StatementBuilder.CLASS_BUILDER.get(nodeClass).apply(data);
        }

        /*
        Optional<StatementNode> newStatement = CLASS_BUILDER.applyTry(nodes.get(0).getClass(), data);
        if (newStatement.isPresent()) {
        // System.out.println("CLASS:" + nodes.get(0).getClass());
        // System.out.println("CODE:" + newStatement.get().getCode());
        return newStatement.get();
        }
        */

        /*
        if (CLASS_BUILDER.has(nodes.get(0).getClass())) {
        System.out.println("CLASS:" + nodes.get(0).getClass());
        return CLASS_BUILDER.on(nodes.get(0).getClass(), data);
        }
        */

        // Otherwise, build based on the class of the first node
        // return CLASS_BUILDER.on(nodes.get(0).getClass(), data);

        // Special cases:
        // A single identifier is an ExpressionSt
        if (data.nodes.size() == 1 && data.nodes.get(0) instanceof IdentifierNode) {
            return newExpression(data.l, data.d, data.nodes.get(0));
        }

        // As last resort, use UndefinedSt
        return newUndefined(data.l, data.d, data.nodes);
    }

    /*
    private static List<MatlabNode> clearSpaces(List<MatlabNode> nodes) {
    
    return nodes.stream()
    	.filter(node -> !(node instanceof SpaceNode))
    	.collect(Collectors.toList());
    
    }
    */

    private static Optional<StatementNode> getStatementFromReservedWord(BuilderData data) {

        if (!(data.nodes.get(0) instanceof ReservedWordNode)) {
            return Optional.empty();
        }

        ReservedWordNode word = (ReservedWordNode) data.nodes.get(0);

        if (!StatementBuilder.RESERVED_BUILDER.containsKey(word.getWord())) {
            return Optional.empty();
        }

        return Optional.of(StatementBuilder.RESERVED_BUILDER.get(word.getWord()).apply(data));
    }

    private static final Map<ReservedWord, Function<BuilderData, StatementNode>> RESERVED_BUILDER = new HashMap<>();

    static {
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.For, StatementBuilder::newForSt);
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.Parfor, data -> newParfor(data.l, data.nodes));
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.While, data -> newWhile(data.l, data.nodes));
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.End, data -> newEnd(data.l));
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.EndFunction, data -> newEndFunction(data.l));
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.EndIf, data -> newEndIf(data.l));
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.EndFor, data -> newEndFor(data.l));
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.EndWhile, data -> newEndWhile(data.l));
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.If, data -> newIf(data.l, data.nodes));
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.Else, data -> newElse(data.l));
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.Elseif, data -> newElseIf(data.l, data.nodes));
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.Switch, data -> newSwitch(data.l, data.nodes));
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.Break, data -> newBreak(data.l));
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.Case, data -> newCase(data.l, data.nodes));
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.Catch, StatementBuilder::newCatchSt);
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.Return, data -> newReturn(data.l));
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.Otherwise, data -> newOtherwise(data.l, data.d));
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.Try, data -> newTry(data.l, data.nodes));
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.Continue, data -> newContinue(data.l, data.d, data.nodes));
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.Spmd, StatementBuilder::newSpmdSt);
        StatementBuilder.RESERVED_BUILDER.put(ReservedWord.Classdef, StatementBuilder::newClassdefSt);
    }

    private static StatementNode newClassdefSt(BuilderData data) {

        AttributesNode attributes = null;
        List<MatlabNode> baseClassesList = new ArrayList<>();
        String className = null;

        boolean afterExtendsOperator = false;

        // Clean spaces in nodes
        // List<MatlabNode> nodes = data.nodes.stream().filter(node -> (node instanceof SpaceNode))
        // .collect(Collectors.toList());

        Iterator<MatlabNode> it = data.nodes.iterator();
        // Iterator<MatlabNode> it = nodes.iterator();
        while (it.hasNext()) {
            MatlabNode node = it.next();

            if (node instanceof ParenthesisNode) {
                attributes = parseParenthesis(data.reportService, node);
            } else if (node instanceof OperatorNode) {
                afterExtendsOperator = true;
                it.remove();
            } else if (node instanceof IdentifierNode) {
                if (afterExtendsOperator) {
                    it.remove();
                    baseClassesList.add(node);
                } else {
                    className = ((IdentifierNode) node).getName();
                }
            } else if (node instanceof FieldAccessNode) {
                if (!afterExtendsOperator) {
                    throw data.reportService.emitError(ProcessorErrorType.PARSE_ERROR,
                            "Unexpected field access after classdef.");
                }

                it.remove();
                baseClassesList.add(node);
            }
        }

        // Check if classname was defined
        if (className == null) {
            throw new CodeParsingException("No classname defined");
        }

        if (attributes == null) {
            attributes = MatlabNodeFactory.newAttributes(Arrays.asList());
        }
        BaseClassesNode baseClasses = MatlabNodeFactory.newBaseClasses(baseClassesList);

        return StatementFactory.newClassdef(data.l, attributes, className, baseClasses);
    }

    private static enum AttributeParseMode {
        ExpectingIdentifier,
        ExpectingAssignment,
        ExpectingExpression,
        ExpectingComma
    }

    private static AttributesNode parseParenthesis(Reporter reportService, MatlabNode attributes) {
        if (attributes.getNumChildren() == 0) {
            throw new CodeParsingException("Empty attributes lists are invalid.");
        }

        AttributeParseMode mode = AttributeParseMode.ExpectingIdentifier;
        MatlabNode attributeName = null;
        List<AttributeNode> parsedAttributes = new ArrayList<>();
        for (MatlabNode node : attributes.getChildren()) {
            switch (mode) {
            case ExpectingIdentifier:
                if (node instanceof IdentifierNode) {
                    attributeName = node;
                    mode = AttributeParseMode.ExpectingAssignment;
                } else {
                    throw reportService.emitError(ProcessorErrorType.PARSE_ERROR, "Expected attribute name, found "
                            + node.getNodeName());
                }
                break;
            case ExpectingAssignment:
                if (node instanceof AssignmentNode) {
                    mode = AttributeParseMode.ExpectingExpression;
                } else if (node instanceof SubscriptSeparatorNode) {
                    parsedAttributes.add(MatlabNodeFactory.newAttribute(attributeName));
                    mode = AttributeParseMode.ExpectingIdentifier;
                } else {
                    throw reportService.emitError(ProcessorErrorType.PARSE_ERROR,
                            "Expecting =, found " + node.getNodeName());
                }
                break;
            case ExpectingExpression:
                // TODO: This is just a stub.
                if (node instanceof IdentifierNode || node instanceof CellNode || node instanceof MatlabCharArrayNode) {
                    parsedAttributes.add(MatlabNodeFactory.newAttribute(attributeName, node));
                    mode = AttributeParseMode.ExpectingComma;
                }
                break;
            case ExpectingComma:
                if (node instanceof SubscriptSeparatorNode) {
                    mode = AttributeParseMode.ExpectingIdentifier;
                } else {
                    throw reportService.emitError(ProcessorErrorType.PARSE_ERROR,
                            "Expecting comma, found " + node.getNodeName());
                }
                break;
            default:
                throw new RuntimeException("Not implemented yet: " + mode);
            }
        }

        if (mode == AttributeParseMode.ExpectingAssignment) {
            parsedAttributes.add(MatlabNodeFactory.newAttribute(attributeName));
        } else if (mode != AttributeParseMode.ExpectingComma) {
            throw reportService.emitError(ProcessorErrorType.PARSE_ERROR, "Unfinished attribute list");
        }

        return MatlabNodeFactory.newAttributes(parsedAttributes);
    }

    private static StatementNode newCatchSt(BuilderData data) {

        // Check if there is an identifier node in the children
        Optional<MatlabNode> id = data.nodes.stream()
                .filter(child -> child instanceof IdentifierNode)
                .findFirst();

        if (id.isPresent()) {
            return StatementFactory.newCatch(data.l, id.get());
        }

        return StatementFactory.newCatch(data.l);
    }

    private static StatementNode newSpmdSt(BuilderData data) {

        if (data.nodes.size() > 1) {
            throw new RuntimeException("SPMD statements with parameters not implemented yet");
        }

        return StatementFactory.newSpmd(data.l);
    }

    private static StatementNode newForSt(BuilderData data) {
        // Verify if the node between the 'for' and the assignment is an identifier
        int forIndex = getFirstIndex(ReservedWordNode.class, data.nodes);
        int assignmentIndex = getFirstIndex(AssignmentNode.class, data.nodes);

        if (assignmentIndex == -1) {
            throw data.reportService.error("'for' expression is malformed");
        }

        List<MatlabNode> nodesToTest = data.nodes.subList(forIndex + 1, assignmentIndex);

        boolean identifierAppeard = false;
        for (MatlabNode node : nodesToTest) {
            // Ignore spaces
            if (node instanceof SpaceNode) {
                continue;
            }

            // Can only appear one IdentifierNode
            if (node instanceof IdentifierNode) {
                if (!identifierAppeard) {
                    identifierAppeard = true;
                    continue;
                }
            }

            throw new CodeParsingException(
                    "Expected a single identifier in the left hand of a 'for' assignment, found '"
                            + node.getClass().getSimpleName() + "'");
        }

        return newFor(data.l, data.nodes);
    }

    private static int getFirstIndex(Class<?> aClass, List<MatlabNode> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            if (aClass.isInstance(nodes.get(i))) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Maps Nodes to Statements.
     */
    /*
    private static final ActionMap<Class<? extends MatlabNode>, BuilderData, StatementNode> CLASS_BUILDER = new ActionMapBuilder<Class<? extends MatlabNode>, BuilderData, StatementNode>()
        .add(AccessCallNode.class, data -> newAccessCall(data.l, data.d, data.nodes))
        .add(CommentNode.class, data -> newComment(data.l, ((CommentNode) data.first()).getString()))
        .add(InvokeNode.class, data -> newInvoke(data.l, data.d, ((InvokeNode) data.first())))
        .add(ClassWordNode.class, StatementBuilder::newClassWordSt)
        // As default, create undefined statement
        // .setDefaultActlet(data -> newUndefined(data.l, data.d, data.nodes))
        .build();
    */
    // private static final FunctionClassMap<MatlabNode, BuilderData, StatementNode> CLASS_BUILDER = new
    // FunctionClassMap<>();
    private static final Map<Class<? extends MatlabNode>, Function<BuilderData, StatementNode>> CLASS_BUILDER = new HashMap<>();

    static {
        StatementBuilder.CLASS_BUILDER.put(SimpleAccessCallNode.class,
                data -> newAccessCall(data.l, data.d, data.nodes));
        StatementBuilder.CLASS_BUILDER.put(CommentNode.class,
                data -> newComment(data.l, ((CommentNode) data.first()).getString()));
        StatementBuilder.CLASS_BUILDER.put(InvokeNode.class,
                data -> newInvoke(data.l, data.d, ((InvokeNode) data.first())));
        StatementBuilder.CLASS_BUILDER.put(ClassWordNode.class, StatementBuilder::newClassWordSt);
    }

    private static StatementNode newClassWordSt(BuilderData data) {
        ClassWordNode classWord = (ClassWordNode) data.nodes.get(0);

        if (classWord.getWord() == ClassWord.PROPERTIES) {
            if (data.nodes.size() == 1) {
                return ClassNodeFactory.newProperties(data.l);
            }

            // Check if second node is a parenthesis
            MatlabNode attributes = data.nodes.get(1);
            if (!(attributes instanceof ParenthesisNode)) {
                throw data.reportService.emitError(ProcessorErrorType.PARSE_ERROR,
                        "Expecting a parenthesis after properties, found '"
                                + attributes.getClass().getSimpleName() + "'");
            }

            attributes = parseParenthesis(data.reportService, attributes);

            return ClassNodeFactory.newProperties(data.l, attributes);
        }

        if (classWord.getWord() == ClassWord.METHODS) {
            if (data.nodes.size() == 1) {
                return ClassNodeFactory.newMethods(data.l);
            }

            // Check if second node is a parenthesis
            MatlabNode attributes = data.nodes.get(1);
            if (!(attributes instanceof ParenthesisNode)) {
                throw data.reportService.emitError(ProcessorErrorType.PARSE_ERROR,
                        "Expecting a parenthesis after properties, found '"
                                + attributes.getClass().getSimpleName() + "'");
            }

            attributes = parseParenthesis(data.reportService, attributes);

            return ClassNodeFactory.newMethods(data.l, attributes);
        }

        if (classWord.getWord() == ClassWord.EVENTS) {
            if (data.nodes.size() == 1) {
                return ClassNodeFactory.newEvents(data.l);
            }

            // Check if second node is a parenthesis
            MatlabNode attributes = data.nodes.get(1);
            if (!(attributes instanceof ParenthesisNode)) {
                throw data.reportService.emitError(ProcessorErrorType.PARSE_ERROR,
                        "Expecting a parenthesis after properties, found '"
                                + attributes.getClass().getSimpleName() + "'");
            }

            attributes = parseParenthesis(data.reportService, attributes);

            return ClassNodeFactory.newEvents(data.l, attributes);
        }

        if (classWord.getWord() == ClassWord.ENUMERATION) {
            if (data.nodes.size() == 1) {
                return ClassNodeFactory.newEnumeration(data.l);
            }

            throw new RuntimeException("Not implemented yet:" + classWord.getWord());

        }

        throw new RuntimeException("Case node defined:" + classWord.getWord());
    }
    /**
     * Maps Nodes to Statements.
     */
    /*
    private static final Map<Class<? extends MatlabNode>, StatementType> NODE_CLASS_MAP = new ImmutableMap.Builder<Class<? extends MatlabNode>, StatementType>()
    
    
        .put(DirectiveNode.class, MStatementType.Directive)
        .build();
        */

}
