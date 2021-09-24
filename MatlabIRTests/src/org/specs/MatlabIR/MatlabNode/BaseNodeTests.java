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

package org.specs.MatlabIR.MatlabNode;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentBlockSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;

public class BaseNodeTests {

    @Test
    public void test() {

	List<String> comments = Arrays.asList("comment1", "comment2");
	CommentBlockSt commentBlockSt = StatementFactory.newCommentBlockStatementFromStrings(
		comments, 0);

	CommentBlockSt commentBlockSt2 = StatementFactory.newCommentBlockStatement(
		Arrays.asList(commentBlockSt, commentBlockSt), 0);

	List<String> comments2 = Arrays.asList("comment1", "comment2", "comment1", "comment2");
	assertEquals(comments2, commentBlockSt2.getCommentLines());
    }

    @Test
    public void nodeUtils() {
	assertEquals(Arrays.asList("b"), MatlabNodeUtils.getVariableNames(MatlabNodeFactory.newIdentifier("b")));
    }
}
