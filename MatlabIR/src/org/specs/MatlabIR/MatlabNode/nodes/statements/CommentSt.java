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

import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;

public abstract class CommentSt extends StatementNode {

    protected CommentSt(StatementData data, Collection<? extends MatlabNode> children) {
	super(data, children);
    }

    @Override
    public String getStatementCode() {
	StringJoiner joiner = getJoiner();

	getCommentLines().forEach(line -> joiner.add(line));

	return joiner.toString();
    }

    private StringJoiner getJoiner() {
	if (isCommentBlock()) {
	    // Joiner for block comment
	    return new StringJoiner("\n", "%{\n", "\n%}");
	}

	// Joiner for single comment on each line
	return new StringJoiner("\n%", "%", "");
    }

    public abstract List<String> getCommentLines();

    public String getCommentString() {
	return getCommentLines().stream().collect(Collectors.joining("\n"));
    }

    public abstract boolean isCommentBlock();

    @Override
    public String toContentString() {
	return getCommentString();
    }
}
