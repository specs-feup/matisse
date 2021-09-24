/*
 * Copyright 2013 SPeCS.
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

package pt.up.fe.specs.matisse.weaver.utils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionFileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptFileNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

import pt.up.fe.specs.matisse.weaver.MatlabJoinpoints;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AJoinPoint;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.exceptions.CaseNotDefinedException;
import pt.up.fe.specs.util.treenode.NodeInsertUtils;

public class Action {

    /**
     * Enumeration for the actions available in Matisse
     *
     * @author Tiago
     */
    public static enum Actions {
        insert,
        def,
    }

    public static enum AttributesDef {
        TYPE,
        DISPLAY,
        DEFAULT;
        public static boolean contains(String attribute) {
            for (AttributesDef attr : values()) {
                if (attr.name().equals(attribute)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * MatlabToken approach. Insert code before/after/around the join point
     *
     * @param joinpointReference
     *            reference to a join point
     * @param when
     *            location for insertion: before, after or around
     * @param parameters
     *            contains the file location and possible variable replacement needed
     */
    public static AJoinPoint[] insert(MatlabNode joinpointReference, String when, String code) {

        FileNode fileNode = generateMatlabScript(code);
        if (fileNode == null) {
            System.err
                    .println("[MWeaver] Warning: Inserting code was invalid. Will ignore insert action");
            return null;
        }

        if (fileNode instanceof ScriptFileNode) {
            insertMatlabTokenChildren(fileNode.getScript().getChildren(), joinpointReference, when);
            return fileNode.getScript().getChildren().stream()
                    .map(node -> (AJoinPoint) MatlabJoinpoints.newJoinpoint(node, null))
                    .toArray(AJoinPoint[]::new);
        }

        if (fileNode instanceof FunctionFileNode) {
            insertMatlabTokenChildren(fileNode.getFunctions(), joinpointReference, when);
            return fileNode.getFunctions().stream()
                    .map(node -> (AJoinPoint) MatlabJoinpoints.newJoinpoint(node, null))
                    .toArray(AJoinPoint[]::new);
        }

        throw new CaseNotDefinedException(fileNode.getClass());
    }

    /**
     * Insert children from a matlab token before/after/around the reference token
     *
     * @param to
     * @param when
     * @param from
     */
    private static void insertMatlabTokenChildren(Collection<? extends MatlabNode> nodes, MatlabNode to, String when) {
        for (MatlabNode child : nodes) {
            insertMatlabNode(child, to, when);
        }

    }

    public static void insertMatlabNode(MatlabNode child, MatlabNode to, String when) {

        switch (When.valueOf(when)) {
        case before:
            to = getValidStatement(to);
            NodeInsertUtils.insertBefore(to, child);
            break;

        case after:
            to = getValidStatement(to);
            // If block header, insert after the corresponding block
            NodeInsertUtils.insertAfter(to, child);
            break;

        case around:
        case replace:
            // If 'to' is not a statement, get node inside child
            if (!(to instanceof StatementNode)) {
                if (child.getNumChildren() != 1) {
                    SpecsLogs.warn("Expected 1 child, found '" + child.getNumChildren()
                            + "'. What should we do?\n"
                            + child);
                }

                child = child.getChild(0);
            }
            NodeInsertUtils.replace(to, child);
            break;
        default:
            break;
        }

    }

    /**
     * Returns the first valid statement where we can insert another node in the after/before inserts
     *
     * @param to
     * @return
     */
    private static MatlabNode getValidStatement(MatlabNode to) {
        if (to instanceof StatementNode) {
            return to;
        }

        Optional<StatementNode> statementParent = to.getAncestorTry(StatementNode.class);

        if (!statementParent.isPresent()) {
            return to;
        }

        return statementParent.get();
    }

    /**
     * TomToken approach. Insert code before/after/around the join point
     *
     * @param joinpointReference
     *            reference to a join point
     * @param when
     *            location for insertion: before, after or around
     * @param parameters
     *            contains the file location and possible variable replacement needed
     */
    /*
    public static void insert(MFile mFile, TomToken joinpointReference, String when, String code) {
    TomToken root = mFile.getTomRoot();
    TomTokens statementList = generateStatementList(code);

    if (statementList == null) {
        System.err
    	    .println("[MWeaver] Warning: Inserting code was invalid. Will ignore insert action");
        return;
    }

    TomToken newRoot = TomActions.insert(when, statementList, joinpointReference, root);
    mFile.setTomRoot(newRoot);
    }
     */
    /*
    private static TomTokens generateStatementList(String code) {
    	FileNode fileToken = generateMatlabIR(code);
    	if (fileToken == null)
    	    return null;
    	TomToken insertingTokensContainer = TomTokenUtils.generateTomToken(fileToken);

    	if (insertingTokensContainer == null)
    	    return null;

    	return insertingTokensContainer.getchildren();
    }
     */
    /**
     * Generates a MatlabToken of the inserting script, according to the parameters specified
     *
     * @param parameters
     *            map containing the "code" location and the replacements needed
     * @return a MatlabToken
     */
    public static FileNode generateMatlabScript(String code) {
        try {
            FileNode returnValue = new MatlabParser().parse(code);

            if (returnValue == null) {
                return null;
            }

            return returnValue;
        } catch (Exception e) {
            throw new RuntimeException("Could not generate MATLAB code:\n" + code, e);
        }

    }

    /**
     * An identifier node of the tree with a function whose input with the same name as the identifier will be removed.
     *
     * @param variable
     * @param value
     */
    public static void replaceInput(IdentifierNode variable, String value) {
        // Get the FunctionNode
        Optional<FunctionNode> functionNode = variable.getAncestorTry(FunctionNode.class);
        if (!functionNode.isPresent()) {
            throw new RuntimeException("Node '" + variable + "' is not part of a function");
        }

        // Parse value, extract node for left hand
        FileNode parsedValue = new MatlabParser().parse(value);

        List<MatlabNode> valueChildren = parsedValue.getUnits().get(0).getStatements().get(0).getChildren();

        if (valueChildren.size() != 1) {
            throw new RuntimeException("Given code for value of input '" + variable.getName() + "' is not valid: "
                    + value);
        }

        // Build assignment
        MatlabNode leftHand = variable.copy();
        MatlabNode rightHand = valueChildren.get(0);

        AssignmentSt assign = StatementFactory.newAssignment(leftHand, rightHand);

        // Remove input
        functionNode.get().getDeclarationNode().getInputs().removeInput(variable.getName());

        // Add assignment to beginning of body
        MatlabNodeIterator iterator = functionNode.get().getChildrenIterator();
        // Advance until it finds the Function Declaration, and insert the assignment
        iterator.next(FunctionDeclarationSt.class);
        iterator.add(assign);
    }
}
