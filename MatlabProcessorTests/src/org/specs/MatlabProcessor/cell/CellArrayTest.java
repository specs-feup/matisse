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

package org.specs.MatlabProcessor.cell;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabIR.Exceptions.CodeParsingException;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CellNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

import junit.framework.TestCase;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.properties.SpecsProperty;

public class CellArrayTest extends TestCase {
    @Override
    protected void setUp() {
	SpecsSystem.programStandardInit();
	SpecsProperty.ShowStackTrace.applyProperty("true");
    }

    @Test
    public void testResults() {
	for (CellArrayResource resource : CellArrayResource.values()) {
	    testResult(resource.name(), resource.getResource(), resource.getResultResource());
	}
    }

    @Test
    public void testErrors() {
	for (CellArrayErrorResource resource : CellArrayErrorResource.values()) {
	    System.out.println(resource.name());

	    SpecsProperty.LoggingLevel.applyProperty("901");
	    try {
		new MatlabParser().parse(resource);
		// MatlabProcessorUtils.fromMFile(IoUtils.getResource(resource),
		// "script");
		Assert.fail("Expected CodeParsingException");
	    } catch (CodeParsingException e) {
		// Good
	    }
	    SpecsProperty.LoggingLevel.applyProperty("700");
	}
    }

    @Test
    public void testValid() {
	for (CellArrayValidParseResource resource : CellArrayValidParseResource.values()) {
	    System.out.println(resource.name());

	    String expectedResult = SpecsIo.getResource(resource.getResultResource());

	    FileNode testToken = new MatlabParser().parse(resource);
	    ScriptNode script = testToken.getScript();

	    StatementNode accessCall = script.getStatements().get(1);
	    MatlabNode child = accessCall.getChild(0);

	    String resourceName = resource.name();
	    Assert.assertEquals(resourceName + ": " + expectedResult.replace("\r\n", "\n").trim(),
		    resourceName + ": " + child.toString().replace("\r\n", "\n").trim());
	}
    }

    private static void testResult(String resourceName, String test, String resultResource) {
	System.out.println(resourceName);

	FileNode testToken = new MatlabParser().parse(() -> test);
	// FileNode testToken = new MatlabParser().parse(() -> test);

	String result = "{" + SpecsIo.getResource(resultResource) + "}";

	ScriptNode script = testToken.getScript();
	AssignmentSt assignment = script.getStatements(AssignmentSt.class).get(0);

	MatlabNode right = assignment.getRightHand().normalize();

	CellNode cellArray = (CellNode) right;

	Assert.assertEquals(resourceName + ": " + result,
		resourceName + ": " + cellArray.getCode());
    }
}
