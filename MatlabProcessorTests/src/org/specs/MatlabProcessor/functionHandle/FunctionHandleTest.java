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

package org.specs.MatlabProcessor.functionHandle;

import junit.framework.TestCase;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.properties.SpecsProperty;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.FunctionHandleNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.LambdaNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabProcessor.TestUtils;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

public class FunctionHandleTest extends TestCase {
    @Override
    protected void setUp() {
	SpecsSystem.programStandardInit();
	SpecsProperty.ShowStackTrace.applyProperty("true");
    }

    @Test
    public void testFunctionHandles() {
	for (FunctionHandleTestResource resource : FunctionHandleTestResource.values()) {
	    testFunctionHandle(resource.name(), resource.getResource(), resource.getResultResource());
	}
    }

    @Test
    public void testAnonymousHandles() {
	for (FunctionHandleAnonymousResource resource : FunctionHandleAnonymousResource.values()) {
	    testAnonymousHandle(resource.name(), resource.getResource(), resource.getResultResource());
	}
    }

    @Test
    public void testErrors() {
	TestUtils.testErrors(FunctionHandleErrorResource.class);
    }

    private static void testFunctionHandle(String resourceName, String test, String resultResource) {
	System.out.println(resourceName);

	FileNode testToken = new MatlabParser().parse(() -> test);
	String result = SpecsIo.getResource(resultResource);

	ScriptNode script = testToken.getScript();
	// AssignmentSt assignment = script.getStatements(AssignmentSt.class).get(0);
	MatlabNode foundNode = script.getDescendantsStream()
		.filter(n -> n instanceof FunctionHandleNode)
		.findFirst()
		.get();

	Assert.assertEquals(resourceName + ": " + result,
		resourceName + ": " + foundNode.getCode());
    }

    private static void testAnonymousHandle(String resourceName, String test, String resultResource) {
	System.out.println(resourceName);

	FileNode testToken = new MatlabParser().parse(() -> test);
	String result = SpecsIo.getResource(resultResource);

	ScriptNode script = testToken.getScript();
	AssignmentSt assignment = script.getStatements(AssignmentSt.class).get(0);
	MatlabNode foundNode = assignment.getDescendantsStream()
		.filter(n -> n instanceof LambdaNode)
		.findFirst()
		.get();

	Assert.assertEquals(resourceName + ": " + result,
		resourceName + ": " + foundNode.getCode());
    }
}
