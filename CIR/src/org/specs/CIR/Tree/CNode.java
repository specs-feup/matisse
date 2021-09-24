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

package org.specs.CIR.Tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.Tree.CNodes.ParenthesisNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Pointer.PointerType;

import pt.up.fe.specs.util.treenode.ATreeNode;

/**
 * @author Joao Bispo
 * 
 */
public abstract class CNode extends ATreeNode<CNode> {

    private static final Collection<Class<? extends CNode>> BYPASS_SET = Arrays.asList(ParenthesisNode.class);

    protected CNode(CNode... children) {
        this(Arrays.asList(children));
    }

    /**
     * @param type
     * @param children
     * @param content
     */
    protected CNode(List<CNode> children) {
        super(children);
    }

    @Override
    public CNode getThis() {
        return this;
    }

    /**
     * 
     * @return the VariableType of this token, or throws an exception if no type is defined for the token
     */
    public VariableType getVariableType() {
        throw new RuntimeException("Not supported for CTokens of type '" + getNodeName() + "' in class '"
                + getClass().getSimpleName() + "'");
    }

    /**
     * 
     * @return the C code of this token
     */
    public String getCode() {
        throw new RuntimeException("Not supported for CTokens of type '" + getNodeName() + "' in class '"
                + getClass().getSimpleName() + "'");
    }

    /**
     * Gets the C code for this token, when used as part of another expression. Unlike getCode(), this expression adds
     * parenthesis when the precedence rules require it.
     * 
     * @param parentPrecedence
     *            The precedence level of the parent expression, usually MemberAccess.
     * @return The generated code.
     */
    public String getCodeForContent(PrecedenceLevel parentPrecedence) {
        if (PrecedenceLevel.requireContentParenthesis(parentPrecedence, getPrecedenceLevel())) {
            return "(" + getCode() + ")";
        }

        return getCode();
    }

    public String getCodeForLeftSideOf(PrecedenceLevel parentPrecedence) {
        if (PrecedenceLevel.requireLeftParenthesis(parentPrecedence, getPrecedenceLevel())) {
            return "(" + getCode() + ")";
        }

        return getCode();
    }

    public String getCodeForRightSideOf(PrecedenceLevel parentPrecedence) {
        if (PrecedenceLevel.requireRightParenthesis(parentPrecedence, getPrecedenceLevel())) {
            return "(" + getCode() + ")";
        }

        return getCode();
    }

    public PrecedenceLevel getPrecedenceLevel() {
        return PrecedenceLevel.Unspecified;
    }

    public String getCodeAsPointer() {
        String prefix = "";
        if (!(getVariableType() instanceof PointerType)) {
            prefix = "&";
        }

        return prefix + getCodeForContent(PrecedenceLevel.PrefixIncrement);
        // return "&" + getCodeForContent(PrecedenceLevel.PrefixIncrement);
    }

    public abstract String toReadableString();

    /**
     * Casts the current node to the given type.
     * 
     * <p>
     * Bypasses certain nodes (e.g., parenthesis), returning their child instead of node itself.
     * 
     * @param type
     * @return
     */
    public <K extends CNode> Optional<K> cast(Class<K> type) {
        CNode normalizedNode = normalize(CNode.BYPASS_SET);
        if (type.isInstance(normalizedNode)) {
            return Optional.of(type.cast(normalizedNode));
        }

        return Optional.empty();
        // return TokenUtils.getToken(this, type, CNode.BYPASS_SET);
    }

    @Override
    public String toContentString() {
        // if (getContent() == null) {
        // return "";
        // }
        // return getContent().toString();

        if (hasChildren()) {
            return "";
        }

        return getCode();

    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
}
