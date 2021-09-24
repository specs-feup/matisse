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
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.String.StringType;

/**
 * @author Joao Bispo
 *
 */
public class StringNode extends CNode {

    private final String string;
    private final int charBitSize;

    /**
     * Creates a constant string.
     * 
     * @param string
     * @param charBitSize
     */
    StringNode(String string, int charBitSize) {
        this.string = string;
        this.charBitSize = charBitSize;
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.util.treenode.ATreeNode#copyPrivate()
     */
    @Override
    protected CNode copyPrivate() {
        return new StringNode(getString(), charBitSize);
    }

    /*
    public StringToken(String string, NumericFactory numerics) {
    this(string, numerics.newChar().getBits());
    }
    */

    public String getString() {
        return string;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Tree.CToken#getVariableType()
     */
    @Override
    public VariableType getVariableType() {
        return StringType.create(getString(), charBitSize, true);
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Tree.CToken#getCode()
     */
    @Override
    public String getCode() {
        // Prints a string in C
        return "\"" + getString() + "\"";
    }

    @Override
    public String toReadableString() {
        return getCode();
    }
}
