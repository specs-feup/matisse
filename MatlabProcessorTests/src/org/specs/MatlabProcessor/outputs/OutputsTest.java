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

package org.specs.MatlabProcessor.outputs;

import java.util.List;

import junit.framework.TestCase;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.properties.SpecsProperty;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OutputsNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.UnusedVariableNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptNode;
import org.specs.MatlabProcessor.TestUtils;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

public class OutputsTest extends TestCase {
    @Override
    protected void setUp() {
	SpecsSystem.programStandardInit();
	SpecsProperty.ShowStackTrace.applyProperty("true");
    }

    @Test
    public void testOutputs() {
	for (OutputsTestResource resource : OutputsTestResource.values()) {
	    testCase(resource);
	}
    }

    @Test
    public void testErrors() {
	TestUtils.testErrors(OutputsErrorResource.class);
    }

    private static void testCase(OutputsTestResource resource) {
	System.out.println(resource.name());

	FileNode testToken = new MatlabParser().parse(resource);
	ScriptNode script = testToken.getScript();

	StatementNode assignment = script.getStatements().get(0);
	OutputsNode outputs = assignment.getChildren(OutputsNode.class).get(0);
	List<MatlabNode> outputNodes = outputs.getChildren(MatlabNode.class);
	List<String> expectedOutputs = resource.getExpectedOutputs();

	Assert.assertEquals(expectedOutputs.size(), outputNodes.size());

	for (int i = 0; i < expectedOutputs.size(); ++i) {
	    String expected = expectedOutputs.get(i);
	    if (expected.equals("~")) {
		// Make sure it's UnusedVariableNode, not Operator ~
		Assert.assertEquals(UnusedVariableNode.class, outputNodes.get(i).getClass());
	    } else {
		Assert.assertEquals(expected, outputNodes.get(i).getCode());
	    }
	}
    }
}
