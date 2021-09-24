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

import java.util.stream.Collectors;

import org.specs.CIR.Tree.CNode;

import pt.up.fe.specs.util.utilities.StringLines;

/**
 * Represents a comment.
 * 
 * @author Joao Bispo
 *
 */
public class CommentNode extends CNode {

    private final String comment;

    /**
     * @param type
     * @param content
     * @param children
     */
    CommentNode(String comment) {
        this.comment = comment;
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.util.treenode.ATreeNode#copyPrivate()
     */
    @Override
    protected CNode copyPrivate() {
        // Strings are immutable
        return new CommentNode(getComment());
    }

    public String getComment() {
        return comment;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Tree.CToken#getCode()
     */
    @Override
    public String getCode() {
        StringLines comments = StringLines.newInstance(getComment());

        return comments.stream().collect(Collectors.joining("\n//", "// ", ""));

    }

    @Override
    public String toReadableString() {
        return getCode();
    }
}
