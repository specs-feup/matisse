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

package org.specs.matisselib.tests.ast.elseifunroll;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;
import org.specs.matisselib.MatisseInit;
import org.specs.matisselib.MatlabAstPassManager;
import org.specs.matisselib.MatlabRecipe;
import org.specs.matisselib.passes.ast.ElseIfUnrollerPass;
import org.specs.matisselib.passmanager.PreTypeInferenceManagerV2;
import org.specs.matisselib.tests.TestUtils;
import org.suikasoft.jOptions.Interfaces.DataView;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.StringProvider;

public class ElseIfUnrollerTests {
    @Test
    public void testNoOp() {
	test(ElseIfUnrollerResource.NOOP);
    }

    @Test
    public void testSimple() {
	test(ElseIfUnrollerResource.SIMPLE);
    }

    @Test
    public void testNested() {
	test(ElseIfUnrollerResource.NESTED);
    }

    private static void test(ElseIfUnrollerResource resource) {
	Map<String, StringProvider> availableFiles = getProjectFiles(resource);
	MatlabRecipe recipe = MatlabRecipe.fromSinglePass(new ElseIfUnrollerPass());
	// MatlabAstPassManager m = new PreTypeInferencePassManager(recipe, availableFiles, passLog);
	MatlabAstPassManager m = new PreTypeInferenceManagerV2(recipe, new MatisseInit().newPassData(availableFiles,
		DataView.empty()));

	String resourceName = resource.name().toLowerCase();
	m.applyPreTypeInferencePasses(resourceName + ".m");

	MatlabNode expected = new MatlabParser().parse(SpecsIo.getResource(resource.getExpectedResource()))
		.getMainFunction();
	MatlabNode obtained = m.getFunctionNode(new FunctionIdentification(resourceName + ".m", resourceName)).get();

	TestUtils.assertTreesEqual(expected, obtained, true);

    }

    private static Map<String, StringProvider> getProjectFiles(ElseIfUnrollerResource resource) {
	Map<String, StringProvider> availableFiles = new HashMap<>();

	String resourceName = resource.name().toLowerCase();
	availableFiles.put(resourceName + ".m", () -> SpecsIo.getResource(resource));

	return availableFiles;
    }
}
