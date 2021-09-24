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

package org.specs.matisselib.tests.passmanager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;
import org.specs.matisselib.MatlabRecipe;
import org.specs.matisselib.MatlabRecipeBuilder;
import org.specs.matisselib.ProjectPassCompilationManager;
import org.specs.matisselib.ProjectPassCompilationOptions;
import org.specs.matisselib.passes.ast.CommentInserterPass;
import org.specs.matisselib.passes.ast.CommentRemoverPass;
import org.specs.matisselib.passes.ast.FunctionReferencerPass;
import org.specs.matisselib.passes.ssa.ConvertToCssaPass;
import org.specs.matisselib.passes.ssa.SsaCommentInserterPass;
import org.specs.matisselib.providers.MatlabFunctionTable;
import org.specs.matisselib.ssa.SsaRecipe;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matisselib.typeinference.PostTypeInferenceRecipe;
import org.specs.matisselib.typeinference.PostTypeInferenceRecipeBuilder;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.StringProvider;

public class ProjectPassCompilationManagerTests {
    @Test
    public void testSimple() {
        MatlabRecipeBuilder preBuilder = new MatlabRecipeBuilder();
        preBuilder.addPass(new CommentRemoverPass());

        MatlabRecipe preTypeInferenceRecipe = preBuilder.getRecipe();
        SsaRecipe ssaRecipe = SsaRecipe.empty();
        PostTypeInferenceRecipe postTypeInferenceRecipe = PostTypeInferenceRecipe.empty();
        Map<String, StringProvider> availableFiles = new HashMap<>();

        String file = SpecsIo.getResource(ProjectPassCompilationManagerSourceResources.DUMMY);
        availableFiles.put("dummy.m", () -> file);

        try (ProjectPassCompilationManager passManager = new ProjectPassCompilationManager(
                new ProjectPassCompilationOptions()
                        .withPreTypeInferenceRecipe(preTypeInferenceRecipe)
                        .withSsaRecipe(ssaRecipe)
                        .withPostTypeInferenceRecipe(postTypeInferenceRecipe)
                        .withAvailableFiles(availableFiles)
                        .withSystemFunctions(new MatlabFunctionTable()))) {
            passManager.applyPreTypeInferencePasses("dummy.m");

            FileNode node = new MatlabParser().parse(ProjectPassCompilationManagerOutputResources.DUMMY_NO_COMMENTS);

            FunctionIdentification rootFunction = new FunctionIdentification("dummy.m", "dummy");
            String dummyCode = passManager.getFunctionNode(rootFunction).get().getCode();
            Assert.assertEquals(node.getFunctionWithName("dummy").get().getCode(), dummyCode);

            String expectedLog = SpecsIo.getResource(ProjectPassCompilationManagerOutputResources.TEST1_LOG);

            String log = passManager.getPassLog();
            Assert.assertEquals(TestUtils.normalize(expectedLog), TestUtils.normalize(log));

        }
    }

    @Test
    public void testInsertedComments() {
        MatlabRecipeBuilder preBuilder = new MatlabRecipeBuilder();
        preBuilder.addPass(new CommentRemoverPass());
        preBuilder.addPass(new CommentInserterPass(" Comment 1"));
        preBuilder.addPass(new FunctionReferencerPass(new FunctionIdentification("dummy.m", "aux")));
        preBuilder.addPass(new CommentInserterPass(" Comment 2"));

        MatlabRecipe preTypeInferenceRecipe = preBuilder.getRecipe();
        SsaRecipe ssaRecipe = SsaRecipe.empty();
        PostTypeInferenceRecipe postTypeInferenceRecipe = PostTypeInferenceRecipe.empty();
        Map<String, StringProvider> availableFiles = new HashMap<>();

        String file = SpecsIo.getResource(ProjectPassCompilationManagerSourceResources.DUMMY);
        availableFiles.put("dummy.m", () -> file);

        try (ProjectPassCompilationManager passManager = new ProjectPassCompilationManager(
                new ProjectPassCompilationOptions()
                        .withPreTypeInferenceRecipe(preTypeInferenceRecipe)
                        .withSsaRecipe(ssaRecipe)
                        .withPostTypeInferenceRecipe(postTypeInferenceRecipe)
                        .withAvailableFiles(availableFiles)
                        .withSystemFunctions(new MatlabFunctionTable()))) {
            passManager.applyPreTypeInferencePasses("dummy.m");

            FileNode node = new MatlabParser()
                    .parse(ProjectPassCompilationManagerOutputResources.DUMMY_INSERTED_COMMENTS);

            FunctionIdentification rootFunction = new FunctionIdentification("dummy.m", "dummy");
            String dummyCode = passManager.getFunctionNode(rootFunction).get().getCode();
            Assert.assertEquals(node.getFunctionWithName("dummy").get().getCode(), dummyCode);
            FunctionIdentification auxFunction = new FunctionIdentification("dummy.m", "aux");
            String auxCode = passManager.getFunctionNode(auxFunction).get().getCode();
            Assert.assertEquals(node.getFunctionWithName("aux").get().getCode(), auxCode);

            String expectedLog = SpecsIo.getResource(ProjectPassCompilationManagerOutputResources.TEST2_LOG);

            String log = passManager.getPassLog();
            Assert.assertEquals(TestUtils.normalize(expectedLog), TestUtils.normalize(log));
        }
    }

    @Test
    public void testResource() {
        PostTypeInferenceRecipeBuilder recipeBuilder = new PostTypeInferenceRecipeBuilder();
        recipeBuilder.addPass(new SsaCommentInserterPass("Added by recipe"));
        PostTypeInferenceRecipe postTypeInferenceRecipe = recipeBuilder.getRecipe();

        HashMap<String, StringProvider> availableFiles = new HashMap<>();
        availableFiles.put("foo.m", () -> "function foo()\nend");

        try (ProjectPassCompilationManager passManager = new ProjectPassCompilationManager(
                buildOptions(postTypeInferenceRecipe, availableFiles))) {

            passManager.applyPreTypeInferencePasses("foo.m");
            passManager.applyTypeInference(DataStore.newInstance("setup"));
            passManager.applyPostTypeInferencePasses();

            ProviderData providerData = ProviderData.newInstance(Arrays.asList(), DataStore.newInstance("setup"));
            TypedInstance instance = passManager.getInstanceFromResource(
                    ProjectPassCompilationManagerSourceResources.RESOURCE, providerData);

            // Make sure all applicable passes were run

            String expectedLog = SpecsIo.getResource(ProjectPassCompilationManagerOutputResources.TESTRESOURCE_LOG);
            Assert.assertEquals(TestUtils.normalize(expectedLog), TestUtils.normalize(passManager.getPassLog()));
            Assert.assertEquals("% Added by recipe", TestUtils.normalize(instance.toString().split("\n")[2]));
        }
    }

    private static ProjectPassCompilationOptions buildOptions(PostTypeInferenceRecipe postTypeInferenceRecipe,
            HashMap<String, StringProvider> availableFiles) {
        return new ProjectPassCompilationOptions()
                .withPreTypeInferenceRecipe(MatlabRecipe.empty())
                .withSsaRecipe(SsaRecipe.empty())
                .withPostTypeInferenceRecipe(postTypeInferenceRecipe)
                .withAvailableFiles(availableFiles)
                .withSystemFunctions(new MatlabFunctionTable());
    }

    @Test
    public void testResource2() {
        PostTypeInferenceRecipeBuilder recipeBuilder = new PostTypeInferenceRecipeBuilder();
        recipeBuilder.addPass(new SsaCommentInserterPass("Added by recipe"));
        recipeBuilder.addPass(new ConvertToCssaPass());
        PostTypeInferenceRecipe postTypeInferenceRecipe = recipeBuilder.getRecipe();

        HashMap<String, StringProvider> availableFiles = new HashMap<>();
        availableFiles.put("foo.m", () -> "function foo()\nend");

        try (ProjectPassCompilationManager passManager = new ProjectPassCompilationManager(
                buildOptions(postTypeInferenceRecipe, availableFiles))) {
            passManager.applyPreTypeInferencePasses("foo.m");
            passManager.applyTypeInference(DataStore.newInstance("setup"));
            passManager.applyPostTypeInferencePasses();

            ProviderData providerData = ProviderData.newInstance(Arrays.asList(), DataStore.newInstance("setup"));
            TypedInstance instance = passManager.getInstanceFromResource(
                    ProjectPassCompilationManagerSourceResources.RESOURCE, providerData);

            // Make sure all applicable passes were run

            String expectedLog = SpecsIo.getResource(ProjectPassCompilationManagerOutputResources.TESTRESOURCE_LOG2);
            Assert.assertEquals(TestUtils.normalize(expectedLog), TestUtils.normalize(passManager.getPassLog()));
            Assert.assertEquals("% Added by recipe", TestUtils.normalize(instance.toString().split("\n")[2]));

        }
    }

    @Test
    public void testResourceSubfunctions() {
        PostTypeInferenceRecipeBuilder recipeBuilder = new PostTypeInferenceRecipeBuilder();
        recipeBuilder.addPass(new SsaCommentInserterPass("Added by recipe"));
        recipeBuilder.addPass(new ConvertToCssaPass());
        PostTypeInferenceRecipe postTypeInferenceRecipe = recipeBuilder.getRecipe();

        HashMap<String, StringProvider> availableFiles = new HashMap<>();
        availableFiles.put("foo.m", () -> "function foo()\nend");

        try (ProjectPassCompilationManager passManager = new ProjectPassCompilationManager(
                buildOptions(postTypeInferenceRecipe, availableFiles))) {
            passManager.applyPreTypeInferencePasses("foo.m");
            passManager.applyTypeInference(DataStore.newInstance("setup"));
            passManager.applyPostTypeInferencePasses();

            ProviderData providerData = ProviderData.newInstance(Arrays.asList(), DataStore.newInstance("setup"));

            passManager.getInstanceFromResource(
                    ProjectPassCompilationManagerSourceResources.RESOURCE_SUBFUNCTIONS, providerData);

            // If the subfunction isn't loaded, then getInstanceFromResource will throw an exception.
        }
    }
}
