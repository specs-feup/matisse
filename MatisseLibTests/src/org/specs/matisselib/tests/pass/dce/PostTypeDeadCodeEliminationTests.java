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

package org.specs.matisselib.tests.pass.dce;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabToC.MatlabToCUtils;
import org.specs.matisselib.DefaultRecipes;
import org.specs.matisselib.ProjectPassCompilationManager;
import org.specs.matisselib.ProjectPassCompilationOptions;
import org.specs.matisselib.passes.ssa.DeadCodeEliminationPass;
import org.specs.matisselib.passes.ssa.LineInformationEliminationPass;
import org.specs.matisselib.providers.MatlabFunctionTable;
import org.specs.matisselib.ssa.SsaPass;
import org.specs.matisselib.ssa.SsaRecipe;
import org.specs.matisselib.ssa.SsaRecipeBuilder;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matisselib.typeinference.PostTypeInferenceRecipe;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.StringProvider;

public class PostTypeDeadCodeEliminationTests extends TestSkeleton {
    @Test
    public void testSimple() {
        runTest(DeadCodeEliminationResource.POST_TYPE_SIMPLE);
    }

    @Test
    public void testProbe() {
        runTest(DeadCodeEliminationResource.POST_TYPE_PROBE);
    }

    private static void runTest(DeadCodeEliminationResource resource) {
        Map<String, StringProvider> availableFiles = new HashMap<>();
        availableFiles.put(resource.getResourceName(), StringProvider.newInstance(SpecsIo.getResource(resource)));

        MatlabFunctionTable systemFunctions = MatlabToCUtils.buildMatissePrototypeTable();

        SsaRecipeBuilder ssaRecipeBuilder = new SsaRecipeBuilder();
        for (SsaPass pass : DefaultRecipes.getTestPreTypeInferenceRecipe().getPasses()) {
            ssaRecipeBuilder.addPass(pass);
        }
        ssaRecipeBuilder.addPass(new LineInformationEliminationPass());
        SsaRecipe ssaRecipe = ssaRecipeBuilder.getRecipe();

        DataStore setup = DataStore.newInstance("foo");
        TypesMap types = new TypesMap();
        types.addSymbol(Arrays.asList("x"), getNumerics().newDouble());

        try (ProjectPassCompilationManager manager = new ProjectPassCompilationManager(
                new ProjectPassCompilationOptions()
                        .withPreTypeInferenceRecipe(DefaultRecipes.DefaultMatlabASTTypeInferenceRecipe)
                        .withSsaRecipe(ssaRecipe)
                        .withPostTypeInferenceRecipe(
                                PostTypeInferenceRecipe.fromSinglePass(new DeadCodeEliminationPass()))
                        .withAvailableFiles(availableFiles)
                        .withSystemFunctions(systemFunctions)
                        .withDefaultTypes(types))) {

            manager.applyPreTypeInferencePasses(resource.getResourceName());
            TypedInstance root = manager.applyTypeInference(setup);
            manager.applyPostTypeInferencePasses();

            String body = root.toString();

            Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getExpectedResource())),
                    TestUtils.normalize(body.toString()));

        }
    }
}
