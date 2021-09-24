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
 * Literal code. Can be used to represents constants, or even several C instructions.
 * 
 * @author JoaoBispo
 *
 */
public class VerbatimNode extends CNode {

    private final String literal;
    private final VariableType variableType;
    private final PrecedenceLevel precedenceLevel;

    VerbatimNode(String string, VariableType variableType, PrecedenceLevel precedenceLevel) {
        literal = string;
        this.variableType = variableType;
        this.precedenceLevel = precedenceLevel;
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.util.treenode.ATreeNode#copyPrivate()
     */
    @Override
    protected CNode copyPrivate() {
        return new VerbatimNode(getLiteral(), variableType, precedenceLevel);
    }

    public String getLiteral() {
        return literal;
    }

    @Override
    public VariableType getVariableType() {
        return variableType;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Tree.CToken#getCode()
     */
    @Override
    public String getCode() {
        return getLiteral();

    }

    @Override
    public String toReadableString() {
        return getLiteral();
    }

    @Override
    public PrecedenceLevel getPrecedenceLevel() {
        return precedenceLevel;
    }
}
