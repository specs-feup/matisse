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

package org.specs.MatlabProcessor.expressions;

import junit.framework.TestCase;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.properties.SpecsProperty;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ExpressionNode;
import org.specs.MatlabProcessor.TestUtils;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

public class ExpressionTest extends TestCase {
    @Override
    protected void setUp() {
	SpecsSystem.programStandardInit();
	SpecsProperty.ShowStackTrace.applyProperty("true");
    }

    public void testExpressions() {
	for (ExpressionTestResource resource : ExpressionTestResource.values()) {
	    testExpression(resource.name(), resource.getResource(), resource.getResultResource());
	}
    }

    private static void testExpression(String resourceName, String test, String resultResource) {
	FileNode testToken = new MatlabParser().parse(() -> test);
	String result = SpecsIo.getResource(resultResource);

	ScriptNode script = testToken.getScript();
	StatementNode statement = script.getStatements().get(0);
	MatlabNode expression = statement.getChild(0);
	if (expression instanceof ExpressionNode) {
	    SpecsLogs.warn("Found ExpressionNode.");
	    expression = expression.getChild(0);
	}

	TestUtils.compareTrees(resourceName, result, expression);
    }
}
