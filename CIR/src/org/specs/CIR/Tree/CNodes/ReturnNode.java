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

import java.util.Optional;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Void.VoidType;

/**
 * Return of a function.
 * 
 * @author JoaoBispo
 *
 */
public class ReturnNode extends CNode {

    ReturnNode(CNode returnToken) {
        super(returnToken);
    }

    ReturnNode() {
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.util.treenode.ATreeNode#copyPrivate()
     */
    @Override
    protected CNode copyPrivate() {
        return new ReturnNode();
    }

    /**
     * 
     * @return the token inside the return. It might be empty.
     */
    public Optional<CNode> getReturnToken() {
        // Check if it has any children
        if (getNumChildren() == 0) {
            return Optional.empty();
        }

        if (getNumChildren() > 1) {
            throw new RuntimeException("Return token has more than one child:\n" + this);
        }

        return Optional.of(getChildren().get(0));
    }

    @Override
    public VariableType getVariableType() {
        return getReturnToken().
        // If return has token, return type of token
                map(token -> token.getVariableType()).
                // Otherwise, return void
                orElse(VoidType.newInstance());
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Tree.CToken#getCode()
     */
    @Override
    public String getCode() {

        String returnExpression = getReturnToken().
        // If has token, return token code
                map(token -> token.getCode()).
                // If empty, return empty string
                orElse("");

        return "\nreturn " + returnExpression;
    }

    @Override
    public String toReadableString() {
        return "\nreturn " + getReturnToken()
                .map(token -> token.toReadableString())
                .orElse("");
    }
}
