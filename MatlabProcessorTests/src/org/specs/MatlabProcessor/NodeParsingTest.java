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

package org.specs.MatlabProcessor;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

/**
 * Just to check it parses, without errors.
 * 
 * @author JoaoBispo
 *
 */
public class NodeParsingTest {

    public static void testParser(String matlabStatement) {
	testParser(matlabStatement, matlabStatement);
    }

    public static void testParser(String matlabStatement, String expectedOutput) {
	// Create node with statement
	List<MatlabNode> nodes = new MatlabParser().parse(matlabStatement).getChildren();

	/*
	if (node.size() != 1) {
	    fail("Generated " + node.size() + " nodes from statement: '" + matlabStatement + "'");
	}
	*/

	StringBuilder builder = new StringBuilder();

	nodes.stream()
		.map(node -> node.getCode())
		.forEach(code -> builder.append(code));

	String generated = builder.toString();

	// Remove new line, if statement has it
	if (generated.endsWith("\n")) {
	    generated = generated.substring(0, generated.length() - "\n".length());
	}

	assertEquals(expectedOutput, generated);
    }

    @Test
    public void test() {
	// Field Access

	testParser("a.b = 0");
	testParser("a = 1 + 2 + 3 + 4");
	testParser("%{\nHey\n%}");
	testParser("function [out1, out2] = fname(a, b, c)\nend");
	testParser("a = func(b)");
	testParser("a = b{3}");
	testParser("a = []");
	testParser("[a, b(1)] = c");
	testParser("ls ./d", "ls './d'");

	// System.out.println(MatlabProcessorUtils.fromMString("for i=1:3:10\nend"));
	// System.out.println("FILE:\n" +
	// MatlabProcessorUtils.fromMFile(IoUtils.getResourceString("for_simple.m"), "none"));

    }
}
