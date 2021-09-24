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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabNode.MatlabNode;

import pt.up.fe.specs.util.SpecsCollections;

/**
 * Contains a list of comments.
 * 
 * <p>
 * TODO: Currently, contains a list of comment statements. Consider if it should store only a list of strings.
 * 
 * @author JoaoBispo
 *
 */
public class CommentBlockSt extends CommentSt {

    private boolean isCommentBlock;

    /**
     * TOM Compatibility.
     * 
     * @param data
     * @param children
     */
    CommentBlockSt(StatementData data, Collection<? extends MatlabNode> children) {
	this(data.getLine(), SpecsCollections.cast(new ArrayList<>(children), CommentSt.class));
    }

    CommentBlockSt(int lineNumber, Collection<? extends CommentSt> children) {
	super(new StatementData(lineNumber, true), children);

	// As default, is true
	isCommentBlock = true;
    }

    @Override
    protected MatlabNode copyPrivate() {
	CommentBlockSt commentBlock = new CommentBlockSt(getData().getLine(), Collections.emptyList());
	commentBlock.setCommentBlock(isCommentBlock());

	return commentBlock;
    }

    @Override
    public List<String> getCommentLines() {
	return getChildren(CommentSt.class).stream()
		.map(child -> child.getCommentLines())
		.reduce(new ArrayList<>(), SpecsCollections::add);
    }

    @Override
    public boolean isCommentBlock() {
	return isCommentBlock;
    }

    /**
     * Sets if the node should print the comments line-by-line, or as a comment block.
     * 
     * @param isCommentBlock
     */
    public void setCommentBlock(boolean isCommentBlock) {
	this.isCommentBlock = isCommentBlock;
    }
}
