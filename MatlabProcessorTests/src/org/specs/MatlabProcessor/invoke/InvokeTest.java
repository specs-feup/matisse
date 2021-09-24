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

package org.specs.MatlabProcessor.invoke;

import junit.framework.TestCase;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.InvokeNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptNode;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

public class InvokeTest extends TestCase {
    @Override
    protected void setUp() {
	SpecsSystem.programStandardInit();
    }

    @Test
    public void testResults() {
	for (InvokeTestResource resource : InvokeTestResource.values()) {
	    testResult(resource.name(), resource.getResource(), resource.getResultResource());
	}
    }

    private static void testResult(String resourceName, String test, String resultResource) {
	System.out.println(resourceName);

	FileNode testToken = new MatlabParser().parse(() -> test);
	String result = SpecsIo.getResource(resultResource);

	ScriptNode script = testToken.getScript();
	StatementNode statement = script.getChildren(StatementNode.class).get(0);
	InvokeNode invoke = statement.getChild(InvokeNode.class, 0);

	Assert.assertEquals(resourceName + ": " + result,
		resourceName + ": " + invoke.getCode());
    }
}
