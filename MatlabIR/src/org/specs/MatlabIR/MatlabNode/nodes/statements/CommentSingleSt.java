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

package org.specs.MatlabIR.MatlabNode.nodes.statements;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CommentNode;

/**
 * A Comment.
 * 
 * <p>
 * TODO: Store string directly in CommentSt, instead of using nodes.
 * 
 * @author JoaoBispo
 *
 */
public class CommentSingleSt extends CommentSt {

    /**
     * TOM compatibility.
     * 
     * @param data
     * @param children
     */
    CommentSingleSt(StatementData data, List<MatlabNode> children) {
	this(data.getLine(), (CommentNode) children.get(0));
    }

    private CommentSingleSt(int lineNumber, List<MatlabNode> children) {
	super(new StatementData(lineNumber, true), children);
    }

    CommentSingleSt(int lineNumber, CommentNode comment) {
	this(lineNumber, Arrays.asList(comment));
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new CommentSingleSt(getData().getLine(), Collections.emptyList());
    }

    /**
     * 
     * @return a string with the comment
     */
    public String getComment() {
	return getCommentNode().getString();
    }

    /**
     * TODO: Instead of using a CommentNode, store the comment in the statement.
     * 
     * @return
     */
    private CommentNode getCommentNode() {
	return getFirstChild(CommentNode.class);
    }

    @Override
    public List<String> getCommentLines() {
	return Arrays.asList(getCommentNode().getString());
    }

    @Override
    public boolean isCommentBlock() {
	return false;
    }

}
