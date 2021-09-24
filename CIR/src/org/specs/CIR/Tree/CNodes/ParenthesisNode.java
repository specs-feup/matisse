/**
 * Copyright 2014 SPeCS.
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

package org.specs.CIR.Tree.CNodes;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;

/**
 * @author Joao Bispo
 *
 */
public class ParenthesisNode extends CNode {

    /**
     * @param type
     * @param content
     * @param children
     */
    ParenthesisNode(CNode token) {
        super(token);
    }

    /**
     * Empty constructor
     */
    private ParenthesisNode() {
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.util.treenode.ATreeNode#copyPrivate()
     */
    @Override
    protected CNode copyPrivate() {
        return new ParenthesisNode();
    }

    /**
     * 
     * @return the token inside the parenthesis
     */
    public CNode getToken() {
        // Only has one children
        return getChildren().get(0);
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Tree.CToken#getVariableType()
     */
    @Override
    public VariableType getVariableType() {
        return getToken().getVariableType();
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Tree.CToken#getCode()
     */
    @Override
    public String getCode() {
        return "(" + getToken().getCode() + ")";
    }

    @Override
    public PrecedenceLevel getPrecedenceLevel() {
        return PrecedenceLevel.Atom;
    }

    @Override
    public String toReadableString() {
        return "(" + getToken().toReadableString() + ")";
    }
}
