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

package pt.up.fe.specs.matisse.weaver.joinpoints.java.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.BlockSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentSt;

public class SelectionUtils {

    public static List<CommentSt> getSectionNodes(List<StatementNode> statements) {
	List<CommentSt> sections = new ArrayList<>();
	for (StatementNode statement : statements) {

	    // Call again if block
	    if (statement instanceof BlockSt) {
		sections.addAll(getSectionNodes(((BlockSt) statement).getStatements()));
	    }

	    // Ignore if not comment
	    if (!(statement instanceof CommentSt)) {
		continue;
	    }

	    CommentSt comment = (CommentSt) statement;
	    // Ignore if does not start with @
	    if (!comment.getCommentString().startsWith("@")) {
		continue;
	    }

	    sections.add(comment);
	}

	return sections;
    }

    public static List<CommentSt> getSectionNodes(MatlabNode root) {
	// System.out.println("CHILDREN:" + root.getChildrenStream().collect(Collectors.toList()));
	// System.out.println("DESCENDANTS:" + root.getDescendantsStream());
	List<CommentSt> sections = root.getChildrenStream()
		// List<CommentSt> sections = root.getDescendantsStream()
		.filter(node -> node instanceof CommentSt)
		.map(node -> (CommentSt) node)
		.filter(comment -> comment.getCommentString().startsWith("@"))
		.collect(Collectors.toList());
	return sections;
    }

}
