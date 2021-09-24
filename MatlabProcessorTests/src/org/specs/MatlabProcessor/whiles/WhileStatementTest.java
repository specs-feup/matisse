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

package org.specs.MatlabProcessor.whiles;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.BlockSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.WhileSt;
import org.specs.MatlabProcessor.TestUtils;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

import junit.framework.TestCase;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;

public class WhileStatementTest extends TestCase {
    @Override
    protected void setUp() {
	SpecsSystem.programStandardInit();
    }

    @Test
    public void testResults() {
	for (WhileStatementTestResource resource : WhileStatementTestResource.values()) {
	    testResult(resource.name(), resource.getResource(), resource.getResultResource());
	}
    }

    private static void testResult(String resourceName, String test, String resultResource) {
	System.out.println(resourceName);

	FileNode testToken = new MatlabParser().parse(() -> test);
	String result = SpecsIo.getResource(resultResource);

	ScriptNode script = testToken.getScript();
	BlockSt block = script.getStatements(BlockSt.class).get(0);

	StringBuilder output = new StringBuilder();
	for (StatementNode statement : block.getChildren(StatementNode.class)) {
	    if (statement instanceof WhileSt) {
		output.append("Condition: ");
		output.append(((WhileSt) statement).getCondition().getCode());
		output.append("\nBody: ");
	    } else {
		output.append(statement.getCode());
	    }
	}

	Assert.assertEquals(resourceName + ": " + TestUtils.clean(result),
		resourceName + ": " + TestUtils.clean(output.toString()));
    }
}
