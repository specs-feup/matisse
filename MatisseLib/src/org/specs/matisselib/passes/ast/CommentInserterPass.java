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

package org.specs.matisselib.passes.ast;

import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentSingleSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNodePass.AMatlabNodePass;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

/**
 * Insers a comment at the beginning of a node.
 *
 */
public class CommentInserterPass extends AMatlabNodePass {

    private final String comment;

    public CommentInserterPass(String comment) {
	Preconditions.checkArgument(comment != null);

	this.comment = comment;
    }

    @Override
    public MatlabNode apply(MatlabNode rootNode, DataStore data) {
	List<CommentSingleSt> comments = StatementFactory.newCommentStatements(comment, -1);
	for (int i = comments.size() - 1; i >= 0; --i) {
	    rootNode.addChild(0, comments.get(i));
	}

	return rootNode;
    }

    @Override
    public String getName() {
	return "CommentInserterPass";
    }

}
