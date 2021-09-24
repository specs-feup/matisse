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

package org.specs.matisselib.tests.ast.whilesimplifier;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.MatisseInit;
import org.specs.matisselib.MatlabAstPassManager;
import org.specs.matisselib.MatlabRecipe;
import org.specs.matisselib.MatlabRecipeBuilder;
import org.specs.matisselib.passes.ast.WhileSimplifierPass;
import org.specs.matisselib.passmanager.PreTypeInferenceManagerV2;
import org.specs.matisselib.tests.TestUtils;
import org.suikasoft.jOptions.Interfaces.DataView;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.StringProvider;

public class WhileSimplifierTests {
    @Test
    public void testSimple() {
	test(WhileSimplifierResource.NESTED);
    }

    private static void test(WhileSimplifierResource resource) {
	Map<String, StringProvider> availableFiles = getProjectFiles(resource);
	MatlabRecipeBuilder builder = new MatlabRecipeBuilder();
	builder.addPass(new WhileSimplifierPass());
	MatlabRecipe recipe = builder.getRecipe();
	// MatlabAstPassManager m = new PreTypeInferencePassManager(recipe, availableFiles, passLog);
	MatlabAstPassManager m = new PreTypeInferenceManagerV2(recipe, new MatisseInit().newPassData(availableFiles,
		DataView.empty()));

	m.applyPreTypeInferencePasses("nested.m");

	MatlabNode functionNode = m.getFunctionNode(new FunctionIdentification("nested.m", "nested")).get();
	String obtained = functionNode.getCode();

	Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getExpectedResource())),
		TestUtils.normalize(obtained));

    }

    private static Map<String, StringProvider> getProjectFiles(WhileSimplifierResource resource) {
	Map<String, StringProvider> availableFiles = new HashMap<>();
	availableFiles.put("nested.m", () -> SpecsIo.getResource(resource));
	return availableFiles;
    }
}
