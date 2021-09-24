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

package org.specs.MatlabToC.Passes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentSingleSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNodePass.APasses.AllStatementsPass;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Transforms a sequence of comments into a single comment.
 * 
 * @author JoaoBispo
 *
 */
public class CommentCompactor extends AllStatementsPass {

    private static final int TEST_THRESHOLD = 2;

    @Override
    protected void applyOnStatement(MatlabNodeIterator iterator, DataStore data) {

	// Check if there are as many comments as the threshold
	for (int i = 0; i < CommentCompactor.TEST_THRESHOLD; i++) {
	    // If appears something that is not a Comment Statement, just return
	    if (!iterator.nextOld(CommentSingleSt.class).isPresent()) {
		return;
	    }
	}

	// Go back in the iterator as many times as the threshold
	iterator.back(CommentCompactor.TEST_THRESHOLD);

	// Add comments to the list while the next node is a Comment statement
	List<CommentSingleSt> comments = new ArrayList<>();
	while (iterator.hasNext()) {
	    Optional<CommentSingleSt> next = iterator.nextOld(CommentSingleSt.class);

	    // No comment found, stop
	    if (!next.isPresent()) {
		break;
	    }

	    // Remove Comment
	    iterator.remove();
	    // Add it to list
	    comments.add(next.get());
	}

	// Use line number of first comment
	int lineNumber = comments.get(0).getData().getLine();

	// Build Comment block
	MatlabNode commentBlock = StatementFactory.newCommentBlockStatement(comments, lineNumber);

	// Add new comment block before the last returned statement that was not a comment
	iterator.back(1);
	iterator.add(commentBlock);

    }

}
