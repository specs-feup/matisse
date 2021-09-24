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

import org.specs.CIR.Language.ReservedWord;
import org.specs.CIR.Tree.CNode;

/**
 * Reserved word in C.
 * 
 * @author Joao Bispo
 *
 */
public class ReservedWordNode extends CNode {

    private final ReservedWord reservedWord;

    /**
     * @param type
     * @param content
     * @param children
     */
    ReservedWordNode(ReservedWord word) {
        reservedWord = word;
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.util.treenode.ATreeNode#copyPrivate()
     */
    @Override
    protected CNode copyPrivate() {
        return new ReservedWordNode(getReservedWord());
    }

    public ReservedWord getReservedWord() {
        return reservedWord;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Tree.CToken#getCode()
     */
    @Override
    public String getCode() {
        return getReservedWord().getLiteral();
    }

    @Override
    public String toReadableString() {
        return getCode();
    }
}
