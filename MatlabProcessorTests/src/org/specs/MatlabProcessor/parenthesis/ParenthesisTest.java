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

package org.specs.MatlabProcessor.parenthesis;

import junit.framework.TestCase;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.properties.SpecsProperty;

import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptNode;
import org.specs.MatlabProcessor.TestUtils;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

public class ParenthesisTest extends TestCase {
    @Override
    protected void setUp() {
	SpecsSystem.programStandardInit();
	SpecsProperty.ShowStackTrace.applyProperty("true");
    }

    public void testParenthesis() {
	for (ParenthesisTestResource resource : ParenthesisTestResource.values()) {
	    testParenthesis(resource.name(), resource.getResource(), resource.getResultResource());
	}
    }

    @Test
    public void testErrors() {
	TestUtils.testErrors(ParenthesisErrorResource.class);
    }

    private static void testParenthesis(String resourceName, String test, String resultResource) {
	System.out.println(resourceName);

	FileNode testToken = new MatlabParser().parse(() -> test);
	String result = SpecsIo.getResource(resultResource);

	ScriptNode script = testToken.getScript();

	TestUtils.compareCode(resourceName, result, script);
    }

}
