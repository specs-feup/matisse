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

package org.specs.matisselib.tests.ast.returnremover;

import static org.specs.matisselib.tests.TestUtils.assertTreesEqual;

import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;
import org.specs.matisselib.PreTypeInferenceServices;
import org.specs.matisselib.passes.ast.ReturnRemoverPass;
import org.specs.matisselib.services.WideScopeService;
import org.specs.matisselib.services.naming.CommonNamingService;
import org.specs.matisselib.services.widescope.MockWideScopeService;

import junit.framework.TestCase;
import pt.up.fe.specs.util.SpecsSystem;

public class ReturnRemoverTests extends TestCase {
    private static FileNode getProcessedTokenFromResourceFile(ReturnRemoverTestFile file) {
	return new MatlabParser().parse(file);
    }

    @Override
    public void setUp() {
	SpecsSystem.programStandardInit();
    }

    @Test
    public void testRemoveReturnOnSequence() {
	testFile(ReturnRemoverTestFile.BASIC_SEQUENCE_NO_RETURN, ReturnRemoverTestFile.BASIC_SEQUENCE);
    }

    @Test
    public void testIfReturn() {
	testFile(ReturnRemoverTestFile.IF_RETURN_NO_RETURN, ReturnRemoverTestFile.IF_RETURN);
    }

    @Test
    public void testForReturn() {
	testFile(ReturnRemoverTestFile.FOR_RETURN_NO_RETURN, ReturnRemoverTestFile.FOR_RETURN);
    }

    @Test
    public void testIfElseReturn() {
	testFile(ReturnRemoverTestFile.IF_ELSE_RETURN_NO_RETURN, ReturnRemoverTestFile.IF_ELSE_RETURN);
    }

    @Test
    public void testSwitchReturn() {
	testFile(ReturnRemoverTestFile.SWITCH_RETURN_NO_RETURN, ReturnRemoverTestFile.SWITCH_RETURN);
    }

    @Test
    public void testRootComment() {
	testFile(ReturnRemoverTestFile.ROOT_COMMENT_NO_RETURN, ReturnRemoverTestFile.ROOT_COMMENT);
    }

    @Test
    public void testIfInFor() {
	testFile(ReturnRemoverTestFile.IF_IN_FOR_NO_RETURN, ReturnRemoverTestFile.IF_IN_FOR_RETURN);
    }

    private static void testFile(ReturnRemoverTestFile result, ReturnRemoverTestFile source) {
	FileNode token = getProcessedTokenFromResourceFile(source);
	FileNode expectedToken = getProcessedTokenFromResourceFile(result);

	FunctionNode function = token.getMainFunction();
	String functionName = function.getFunctionName();

	CommonPassData data = new CommonPassData("setup-name");
	WideScopeService wideScope = new MockWideScopeService(token)
		.withFunctionIdentification(new FunctionIdentification(functionName + ".m"));
	data.add(PreTypeInferenceServices.WIDE_SCOPE, wideScope);
	data.add(PreTypeInferenceServices.COMMON_NAMING, new CommonNamingService(wideScope, token));

	ReturnRemoverPass returnRemover = new ReturnRemoverPass();
	returnRemover.apply(function, data);

	assertTreesEqual(expectedToken, token, false);
    }
}
