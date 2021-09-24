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
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;

public class VariableNode extends CNode {

    private final Variable variable;

    VariableNode(Variable variable) {
        this.variable = variable;
    }

    VariableNode(String name, VariableType type, boolean isGlobal) {
        this(new Variable(name, type, isGlobal));
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.util.treenode.ATreeNode#copyPrivate()
     */
    @Override
    protected CNode copyPrivate() {
        return new VariableNode(getVariable());
    }

    public Variable getVariable() {
        return variable;
    }

    public String getVariableName() {
        return getVariable().getName();
    }

    @Override
    public VariableType getVariableType() {
        return getVariable().getType();
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Tree.CToken#getCode()
     */
    @Override
    public String getCode() {

        if (getVariableType().pointer().isByReference()) {
            return "*" + getVariable().getName();
        }

        return getVariable().getName();
    }

    @Override
    public PrecedenceLevel getPrecedenceLevel() {
        if (getVariableType().pointer().isByReference()) {
            return PrecedenceLevel.PrefixIncrement;
        }
        return PrecedenceLevel.Atom;
    }

    @Override
    public String getCodeAsPointer() {
        if (getVariableType().pointer().isByReference()) {
            return getVariable().getName();
        }
        return super.getCodeAsPointer();
    }

    @Override
    public String toReadableString() {
        return getVariable().getName();
    }
}
