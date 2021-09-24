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

package org.specs.matisselib.tests.ast.inputrenamer;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.MatisseInit;
import org.specs.matisselib.MatlabAstPassManager;
import org.specs.matisselib.MatlabRecipe;
import org.specs.matisselib.MatlabRecipeBuilder;
import org.specs.matisselib.passes.ast.InputRenamerPass;
import org.specs.matisselib.passmanager.PreTypeInferenceManagerV2;
import org.specs.matisselib.tests.TestUtils;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.Interfaces.DataView;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.StringProvider;

public class InputRenamerTests {
    @Test
    public void testSimple() {
	test(InputRenamerResource.SIMPLE);
	testV2(InputRenamerResource.SIMPLE);
    }

    private static void test(InputRenamerResource resource) {
	Map<String, StringProvider> availableFiles = getProjectFiles(resource);
	MatlabRecipe recipe = getRecipe(false);
	// MatlabAstPassManager m = new PreTypeInferencePassManager(recipe, availableFiles, passLog);
	MatlabAstPassManager m = new PreTypeInferenceManagerV2(recipe, new MatisseInit().newPassData(availableFiles,
		DataView.empty()));

	m.applyPreTypeInferencePasses("f.m");

	String obtained = m.getFunctionNode(new FunctionIdentification("f.m", "f")).get().getCode();

	Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getExpectedResource())),
		TestUtils.normalize(obtained));

    }

    private static Map<String, StringProvider> getProjectFiles(InputRenamerResource resource) {
	Map<String, StringProvider> availableFiles = new HashMap<>();
	availableFiles.put("f.m", () -> SpecsIo.getResource(resource));
	return availableFiles;
    }

    private static MatlabRecipe getRecipe(boolean isV2) {
	MatlabRecipeBuilder recipeBuilder = new MatlabRecipeBuilder();
	if (isV2) {
	    recipeBuilder.addPass(InputRenamerPass.newInstanceV2());
	} else {
	    recipeBuilder.addPass(new InputRenamerPass());
	}

	MatlabRecipe recipe = recipeBuilder.getRecipe();
	return recipe;
    }

    private static void testV2(InputRenamerResource resource) {
	Map<String, StringProvider> availableFiles = getProjectFiles(resource);
	MatlabRecipe recipe = getRecipe(true);

	DataStore data = new MatisseInit().newPassData(availableFiles, DataView.empty());
	MatlabAstPassManager m = new PreTypeInferenceManagerV2(recipe, data);
	// PassManagerData managerData = m.applyPasses("f.m", recipe, data);
	m.applyPreTypeInferencePasses("f.m");

	// String obtained = m.getUnit(new FunctionIdentification("f.m", "f"), managerData).getCode();
	String obtained = m.getPassManager().getUnit(new FunctionIdentification("f.m", "f"), m.getPassData()).getCode();

	Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getExpectedResource())),
		TestUtils.normalize(obtained));
    }
}
