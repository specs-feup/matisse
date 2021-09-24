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

package org.specs.MatlabToC;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;
import org.specs.MatlabToC.Passes.CommentCompactor;

import pt.up.fe.specs.util.SpecsStrings;

public class CommentCompactorTest {

    @Test
    public void test() {
	StringBuilder builder = new StringBuilder();

	int initialComments = 10000;
	for (int i = 0; i < initialComments; i++) {
	    builder.append("% Comment ").append(i).append("\n");
	}

	String mFile = builder.append("%Line1\n"
		+ "%Line2\n"
		+ "%Line3\n"
		+ "function [a] = f(b)\n"
		+ "  a = 0;\n"
		+ "end"
		).toString();

	long tic = System.nanoTime();
	FileNode node = new MatlabParser().parse(mFile);
	long toc = System.nanoTime();
	System.out.println("PARSING:" + SpecsStrings.parseTime(toc - tic));

	// System.out.println("BEFORE:\n" + node);
	tic = System.nanoTime();
	new CommentCompactor().apply(node, new CommonPassData("test_data"));
	toc = System.nanoTime();
	System.out.println("PASS:" + SpecsStrings.parseTime(toc - tic));

	assertEquals(initialComments + 3, node.getChild(0, 0).getNumChildren());
	// System.out.println("NUM CHILD:" + node.getChildren().get(0).numChildren());
	// System.out.println("AFTER:\n" + node);
	// fail("Not yet implemented");

    }
}
