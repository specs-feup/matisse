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

package org.specs.MatlabIR.MatlabNode;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.specs.MatlabIR.MatlabNode.nodes.core.ParenthesisNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ExpressionNode;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.treenode.ATreeNode;

/**
 * Base node for all nodes in the MATLAB IR.
 * 
 * @author Joao Bispo
 * 
 */
public abstract class MatlabNode extends ATreeNode<MatlabNode> {

    private static final List<Class<? extends MatlabNode>> BYPASS_SET = Arrays.asList(ParenthesisNode.class,
            ExpressionNode.class);

    public static Map<Class<?>, Integer> classes = new HashMap<>();
    private final boolean countInstances = false;

    public MatlabNode() {
        super(Collections.emptyList());
    }

    /**
     * @param type
     * @param children
     * @param content
     */
    public MatlabNode(Collection<? extends MatlabNode> children) {
        super(children);

        if (countInstances) {
            MatlabNode.classes.putIfAbsent(getClass(), 0);
            MatlabNode.classes.put(getClass(), MatlabNode.classes.get(getClass()) + 1);
        }
    }

    /**
     * Creates a new node of the same type, with the given content. Does not copy the children.
     * 
     * @param content
     * @return
     */
    /*
    public MatlabNode newNode(Object content) {
    return new GenericMNode(getType(), null, content);
    }
    */

    @Override
    public MatlabNode getThis() {
        return this;
    }

    /**
     * 
     * @return the MATLAB code corresponding to this node
     */
    // public abstract String getCode();

    public String getCode() {
        throw new UnsupportedOperationException("Not implemented for class '" + getClass().getSimpleName() + "'");
    }

    /**
     * The tab. Currently it is equivalent to 3 spaces.
     * 
     * @return
     */
    protected String getTab() {
        return "   ";
    }

    /**
     * Get the first child of the given class, with an optional.
     * 
     * @param targetType
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends MatlabNode> Optional<T> getFirstChildOptional(Class<T> targetType) {
        // It is safe to cast to T, since it returns a node that implements the given class
        return (Optional<T>) getChildren().stream()
                // .filter(node -> node.getClass().equals(targetType.getClass()))
                .filter(node -> targetType.isInstance(node))
                .findFirst();
    }

    /**
     * Get the first child of the given class, or throw an exception if no node of the target type is found.
     * 
     * @param targetType
     * @return
     */
    public <T extends MatlabNode> T getFirstChild(Class<T> targetType) {
        return getFirstChildOptional(targetType).orElseThrow(
                () -> {
                    StringJoiner joiner = new StringJoiner(", ");
                    getChildrenStream()
                            .map(child -> child.getClass().getSimpleName())
                            .forEach(name -> joiner.add(name));

                    return new RuntimeException("Could not find a '" + targetType.getSimpleName() + "' inside "
                            + getClass().getSimpleName() + ". Children classes:\n" + joiner.toString());
                });
    }

    /**
     * Get the first child of the given class.
     * 
     * <p>
     * The search is done over all children nodes. Traverse is done in a breath-first manner.
     * 
     * @param targetType
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends MatlabNode> Optional<T> getFirstChildRecursiveOptional(Class<T> targetType) {
        // It is safe to cast to T, since it returns a node that implements the given class
        Optional<T> foundChild = (Optional<T>) getChildren().stream()
                // .filter(node -> node.getClass().equals(targetType.getClass()))
                .filter(node -> targetType.isInstance(node))
                .findFirst();

        // If found, return it
        if (foundChild.isPresent()) {
            return foundChild;
        }

        // Child not found, search in each of the children
        for (MatlabNode child : getChildren()) {
            foundChild = child.getFirstChildRecursiveOptional(targetType);
            if (foundChild.isPresent()) {
                return foundChild;
            }
        }

        // Could not find child of the given class, return empty optional
        return Optional.empty();
    }

    /**
     * Get the first child of the given class, or throw an exception if no node of the target type is found.
     * 
     * <p>
     * The search is done over all children nodes. Traverse is done in a breath-first manner.
     * 
     * <p>
     * Throws an exception if the node of the given class is not found.
     * 
     * @param targetType
     * @return
     */
    public <T extends MatlabNode> T getFirstChildRecursive(Class<T> targetType) {
        // return getFirstChildRecursiveOptional(targetType).orElseThrow(
        // () -> new RuntimeException("Could not find a node of type '" + targetType.getSimpleName()
        // + "':\n" + this));
        return getFirstChildRecursiveOptional(targetType).orElse(null);
    }

    /**
     * Get all children that are an instance of the given class.
     * 
     * @param targetType
     * @return
     */
    @Override
    public <T extends MatlabNode> List<T> getChildren(Class<T> targetType) {
        // It is safe to cast to T, since it returns nodes that implements the given class
        return (List<T>) getChildren().stream()
                .filter(node -> targetType.isInstance(node))
                .map(child -> targetType.cast(child))
                .collect(Collectors.toList());
    }

    /**
     * Helper method that throws an exception if any of the children are NOT of the given class.
     * 
     * @param targetType
     * @return
     */
    public <T extends MatlabNode> List<T> getChildrenAll(Class<T> targetType) {
        return getChildrenStream()
                .map(child -> targetType.cast(child))
                .collect(Collectors.toList());
    }

    /**
     * Helper method that throws an exception if any of the children are NOT of the given class.
     * 
     * @param targetType
     * @return
     */
    public <T extends MatlabNode, T2 extends MatlabNode> List<T> getChildrenAll(Class<T> targetType,
            Class<T2> ignoreType) {
        List<T> children = getChildren(targetType);
        List<T2> ignoreChildren = getChildren(ignoreType);

        if (ignoreType.isAssignableFrom(targetType) || targetType.isAssignableFrom(ignoreType)) {
            throw new IllegalArgumentException("targetType and ignoreType must be disjoint");
        }

        if (children.size() != getNumChildren() - ignoreChildren.size()) {
            String message = "Node " + getClass().getSimpleName()
                    + " contains children that are not of class '" + targetType.getSimpleName() + "':\n"
                    + getChildren();

            throw new RuntimeException(message);
        }

        return children;
    }

    /**
     * Converts the current MatlabNode to the class of the given StatementNode class.
     * 
     * <p>
     * Throws an exception
     * 
     * @param statementClass
     * @return
     */
    /*
    public <T extends StatementNode> T toStatement(Class<T> statementClass) {
    
    }
    */

    /**
     * Returns all statements of the StatementNode class.
     * 
     * <p>
     * If the current node is a StatementNode of the given class, it is included in the list.
     * 
     * @param token
     * @param type
     * @return
     */
    public <T extends StatementNode> List<T> getStatements(Class<T> statementClass) {
        List<T> statements = SpecsFactory.newArrayList();

        getStatements(this, statementClass, statements);

        return statements;
    }

    private static <T extends StatementNode> void getStatements(MatlabNode node, Class<T> type,
            List<T> statements) {

        if (type.isInstance(node)) {
            statements.add(type.cast(node));
        }

        node.getChildren().forEach(child -> getStatements(child, type, statements));
    }

    @Override
    public MatlabNodeIterator getChildrenIterator() {
        return new MatlabNodeIterator(this);
    }

    /**
     * As default, returns false.
     * 
     * @return true if this node should not be in the tree after parsing.
     */
    public boolean isTemporary() {
        return false;
    }

    /**
     * Joins the corresponding code of a list of nodes, according to the given strings.
     * 
     * @param delimiter
     * @param prefix
     * @param suffix
     * @param nodes
     * @return
     */
    protected String joinCode(String delimiter, String prefix, String suffix, List<MatlabNode> nodes) {
        StringJoiner joiner = new StringJoiner(delimiter, prefix, suffix);

        nodes.forEach(arg -> joiner.add(arg.getCode()));

        return joiner.toString();
    }

    /**
     * Cast the current token to the given class, unless it is a parenthesis node. In that case, it ignores the
     * parenthesis and then tries to cast it. If it could not, throws an exception.
     * 
     * @param aClass
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends MatlabNode> Optional<T> to(Class<T> aClass) {
        if (aClass.isInstance(this)) {
            return Optional.of((T) this);
        }

        if (this instanceof ParenthesisNode) {
            if (getNumChildren() != 1) {
                // throw new RuntimeException("Expected to find only one child:\n" + this);
                return Optional.empty();
            }

            return this.getChild(0).to(aClass);
        }

        return Optional.empty();
        // throw new RuntimeException("Could not cast to '" + aClass.getSimpleName() + "':\n" + this);
    }

    /**
     * Returns the node itself, unless the node is an ExpressionNode, in that case returns its only child.
     * <p>
     * Temporary method to be used during the phasing out of the ExpressionNode from the MATLAB AST.
     * 
     * @return
     */
    public MatlabNode normalizeExpr() {
        if (!(this instanceof ExpressionNode)) {
            return this;
        }

        Preconditions.checkArgument(getNumChildren() == 1, "Expecting ExpressionNode to have only one child");
        // return getChild(0);
        return getChild(0).normalizeExpr();
    }

    /**
     * Bypasses parenthesis (and expression nodes, until they are phased out).
     * 
     * @return
     */
    public MatlabNode normalize() {
        return normalize(MatlabNode.BYPASS_SET);
    }

    @Override
    public String getNodeName() {
        String originalName = super.getNodeName();

        if (originalName.endsWith("Node")) {
            return originalName.substring(0, originalName.length() - "Node".length());
        }

        return originalName;
    }

    /**
     * Returns the corresponding code, if a leaf node, or an empty string, if it has children.
     */
    @Override
    public String toContentString() {
        if (hasChildren()) {
            return "";
        }
        return getCode();
    }

    public int getLine() {

        return getAncestorTry(StatementNode.class)
                .map(stmt -> stmt.getLine())
                .orElseThrow(() -> new RuntimeException(
                        "getLine() not implemented for node '" + getClass().getSimpleName() + "'"));
        /*        
        // Get line of first ancestor that is a statement
        Integer line = getAncestorTry(StatementNode.class).map(stmt -> stmt.getLine()).orElse(null);
        
        if (line != null) {
            return line;
        }
        
        throw new RuntimeException("getLine() not implemented for node '" + getClass().getSimpleName() + "'");
        */
    }
}
