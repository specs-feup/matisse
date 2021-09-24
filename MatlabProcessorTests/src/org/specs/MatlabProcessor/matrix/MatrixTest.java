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

package org.specs.MatlabProcessor.matrix;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabIR.Exceptions.CodeParsingException;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatrixNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

import junit.framework.TestCase;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;

public class MatrixTest extends TestCase {
    @Override
    protected void setUp() {
	SpecsSystem.programStandardInit();
    }

    @Test
    public void testResults() {
	for (MatrixResource resource : MatrixResource.values()) {
	    testResult(resource.name(), resource.getResource(), resource.getResultResource());
	}
    }

    @Test
    public void testErrors() {
	for (MatrixErrorResource resource : MatrixErrorResource.values()) {
	    try {
		new MatlabParser().parse(resource);
		Assert.fail("Expected CodeParsingException");
	    } catch (CodeParsingException e) {

	    }
	}
    }

    @Test
    public void testOutputs() {
	String resource = "matrix/simple_outputs.m";
	Assert.assertNotNull(new MatlabParser().parse(() -> resource));
    }

    private static void testResult(String resourceName, String test, String resultResource) {
	System.out.println(resourceName);

	FileNode testToken = new MatlabParser().parse(() -> test);
	String result = "[" + SpecsIo.getResource(resultResource) + "]";

	ScriptNode script = testToken.getScript();
	AssignmentSt assignment = script.getStatements(AssignmentSt.class).get(0);

	MatlabNode right = assignment.getRightHand().normalize();

	MatrixNode matrix = (MatrixNode) right;

	Assert.assertEquals(resourceName + ": " + result, resourceName + ": " + matrix.getCode());
    }
}
