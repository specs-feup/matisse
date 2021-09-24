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

package org.specs.MatlabProcessor.blockComment;

import org.junit.Test;
import org.specs.MatlabIR.Exceptions.CodeParsingException;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentSt;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

import junit.framework.TestCase;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.properties.SpecsProperty;

public class BlockCommentTest extends TestCase {

    @Override
    public void setUp() {
	SpecsSystem.programStandardInit();
	SpecsProperty.ShowStackTrace.applyProperty("true");
    }

    @Test
    public void testResults() {
	// System.out.println(MatlabProcessorUtils.fromMFile("for i = [1], end", "dummy"));
	// Tests
	for (BlockCommentTestResource resource : BlockCommentTestResource.values()) {
	    testResult(resource.name(), resource.getResource(), resource.getResultResource());

	    // assertTrue(result);
	}

    }

    @Test
    public void testError() {
	SpecsProperty.LoggingLevel.applyProperty("901");
	// Test errors
	for (BlockCommentErrorResource resource : BlockCommentErrorResource.values()) {
	    testError(resource.name(), resource.getResource());
	}
	SpecsProperty.LoggingLevel.applyProperty("700");
    }

    public void testResult(String resourceName, String test, String result) {
	// Parse resource
	// FileNode testToken = new MatlabParser().parse(() -> test);
	FileNode testToken = new MatlabParser().parse(() -> test);

	// Parse result
	FileNode resultToken = new MatlabParser().parse(() -> result);
	// FileNode resultToken = MatlabProcessorUtils.fromMFile(IoUtils.getResource(result), "script");

	ScriptNode script = testToken.getScript();

	// Remove comments statements from test token
	script.removeChildren(CommentSt.class);

	// Check if equal
	assertEquals("Failed on test '" + resourceName + "'", resultToken.getCode(), testToken.getCode());
    }

    public void testError(String resourceName, String test) {

	// It passes the test if it fails
	// Parse resource
	try {
	    new MatlabParser().parse(() -> test);
	    // new MatlabParser().parse(() -> test);
	    fail("Expected CodeParsingException");
	} catch (CodeParsingException ex) {
	    // Success
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Expected CodeParsingException, got " + e.getClass().getSimpleName());
	}

    }

}
