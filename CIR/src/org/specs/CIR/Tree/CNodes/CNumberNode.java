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
import org.specs.CIR.Types.ATypes.CNative.CNativeUtils;
import org.specs.CIR.TypesOld.CNumber;
import org.specs.CIRTypes.Language.CLiteral;

/**
 * Represents a C number.
 * 
 * @author JoaoBispo
 *
 */
public class CNumberNode extends CNode {

    private final CNumber cNumber;

    CNumberNode(CNumber cNumber) {
        this.cNumber = cNumber;
    }

    /*
    CNumberNode(int number) {
    this(CLiteral.newInteger(number));
    }
    */

    CNumberNode(Number number, VariableType type) {
        this(CLiteral.newInstance(number, CNativeUtils.toCNative(type)));
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.util.treenode.ATreeNode#copyPrivate()
     */
    @Override
    protected CNode copyPrivate() {
        // Using the same CNumber since they are immutable.
        return new CNumberNode(getCNumber());
    }

    public CNumber getCNumber() {
        return cNumber;
    }

    @Override
    public VariableType getVariableType() {
        return getCNumber().getType();
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Tree.CToken#getCode()
     */
    @Override
    public String getCode() {
        return getCNumber().toCString();
    }

    @Override
    public PrecedenceLevel getPrecedenceLevel() {
        return PrecedenceLevel.Atom;
    }

    @Override
    public String toReadableString() {
        return getCNumber().toCString();
    }

}
