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

package org.specs.MatlabProcessor.dynamicFields;

import junit.framework.TestCase;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ExpressionNode;
import org.specs.MatlabProcessor.TestUtils;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

public class DynamicAccessTest extends TestCase {
    @Override
    protected void setUp() {
	SpecsSystem.programStandardInit();
    }

    @Test
    public void testResults() {
	for (DynamicAccessTestResource resource : DynamicAccessTestResource.values()) {
	    testResult(resource, resource.getResultResource());
	}
    }

    private static void testResult(DynamicAccessTestResource resource, String resultResource) {
	String resourceName = resource.name();
	System.out.println(resource.name());

	FileNode testToken = new MatlabParser().parse(resource);
	String result = SpecsIo.getResource(resultResource);

	ScriptNode script = testToken.getScript();
	StatementNode statement = script.getStatements().get(0);

	Assert.assertEquals(resource.getTotalStatementChildren(), statement.getNumChildren());
	MatlabNode outputNode = statement.getChild(resource.getAccessIndex());
	if (outputNode instanceof ExpressionNode) {
	    SpecsLogs.warn("Found expression node.");
	    outputNode = ((ExpressionNode) outputNode).getChild(0);
	}
	String output = outputNode.toString();

	Assert.assertEquals(resourceName + ": " + TestUtils.clean(result),
		resourceName + ": " + TestUtils.clean(output.toString()));
    }
}
