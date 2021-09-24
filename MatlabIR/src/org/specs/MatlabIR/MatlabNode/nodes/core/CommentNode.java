/**
 * Copyright 2015 SPeCS.
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

package org.specs.MatlabIR.MatlabNode.nodes.core;

import java.util.Collection;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * A MatLab comment.
 * 
 * <p>
 * The content is a String.
 * 
 * @author JoaoBispo
 *
 */
public class CommentNode extends MatlabNode {

    private final String comment;

    CommentNode(String comment) {
	this.comment = comment;
    }

    CommentNode(Object content, Collection<MatlabNode> children) {
	this((String) content);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new CommentNode(getString());
    }

    /**
     * 
     * @return the text of the comment
     */
    public String getString() {
	return comment;
    }

    @Override
    public String getCode() {
	return "%" + getString();
    }
}
