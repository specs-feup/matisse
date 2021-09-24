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

package org.specs.MatlabProcessor.functionDeclarations;

import java.io.File;

import junit.framework.TestCase;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.io.SimpleFile;
import pt.up.fe.specs.util.properties.SpecsProperty;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;
import org.specs.MatlabProcessor.TestUtils;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

public class FunctionDeclarationTest extends TestCase {
    @Override
    protected void setUp() {
	SpecsSystem.programStandardInit();
	SpecsProperty.ShowStackTrace.applyProperty("true");
    }

    @Test
    public void testFunctionDeclarations() {
	for (FunctionDeclarationTestResource resource : FunctionDeclarationTestResource.values()) {
	    testFunctionDeclaration(resource.name(), resource.getResource(), resource.getResultResource());
	}
    }

    @Test
    public void testErrors() {
	TestUtils.testErrors(FunctionDeclarationErrorResource.class);
    }

    private static void testFunctionDeclaration(String resourceName, String test, String resultResource) {

	// Create file to simulate M-file
	SimpleFile file = SimpleFile.newInstance(new File(test).getName(), SpecsIo.getResource(test));
	FileNode testToken = new MatlabParser().parse(file);

	String result = SpecsIo.getResource(resultResource);

	FunctionNode function = testToken.getMainFunction();
	FunctionDeclarationSt functionDeclaration = function.getDeclarationNode();
	String functionName = functionDeclaration.getNameNode().getName();
	String outputs = functionDeclaration.getOutputs().getCode();
	String inputs = functionDeclaration.getInputs().getCode();

	StringBuilder output = new StringBuilder();
	output.append(functionName);
	output.append('\n');
	output.append(outputs);
	output.append('\n');
	output.append(inputs);
	output.append('\n');
	for (StatementNode stmt : function.getStatements(StatementNode.class)) {
	    if (stmt instanceof FunctionDeclarationSt) {
		continue;
	    }
	    output.append(stmt.getCode());
	}

	Assert.assertEquals(resourceName + ": " + TestUtils.clean(result),
		resourceName + ": " + TestUtils.clean(output.toString()));
    }
}
