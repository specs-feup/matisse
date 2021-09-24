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

package org.specs.matisselib.tests.pass.loopinterchange;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;
import org.specs.MatlabToC.Functions.MatissePrimitive;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.matisselib.DefaultRecipes;
import org.specs.matisselib.ProjectPassCompilationManager;
import org.specs.matisselib.ProjectPassCompilationOptions;
import org.specs.matisselib.passes.posttype.ConvertMatrixAccessesPass;
import org.specs.matisselib.passes.posttype.LoopInterchangePass;
import org.specs.matisselib.passes.posttype.LoopInvariantCodeMotionPass;
import org.specs.matisselib.passes.posttype.loopinterchange.MemoryLoopInterchangeFormat;
import org.specs.matisselib.passes.ssa.BlockReorderingPass;
import org.specs.matisselib.passes.ssa.SsaValidatorPass;
import org.specs.matisselib.providers.MatlabFunctionTable;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matisselib.typeinference.PostTypeInferenceRecipeBuilder;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.StringProvider;

public class LoopInterchangeTests extends TestSkeleton {
    @Test
    public void testSimple() {
        runTest(LoopInterchangeResource.BASIC);
    }

    @Test
    public void testAfterLoop() {
        runTest(LoopInterchangeResource.AFTERLOOP);
    }

    @Test
    public void testBeforeLoop() {
        runTest(LoopInterchangeResource.BEFORELOOP);
    }

    @Test
    public void testIf() {
        runTest(LoopInterchangeResource.XIF);
    }

    private static void runTest(LoopInterchangeResource resource) {
        FileNode file = new MatlabParser().parse(resource);

        Map<String, InstanceProvider> functions = getDefaultFunctions();
        MatlabFunctionTable systemFunctions = new MatlabFunctionTable();
        for (String function : functions.keySet()) {
            systemFunctions.addBuilder(function, functions.get(function));
        }

        PostTypeInferenceRecipeBuilder recipeBuilder = new PostTypeInferenceRecipeBuilder();

        recipeBuilder.addPass(new ConvertMatrixAccessesPass());
        recipeBuilder.addPass(new LoopInvariantCodeMotionPass());
        recipeBuilder.addPass(new LoopInterchangePass("loop_interchange", MemoryLoopInterchangeFormat.class));
        recipeBuilder.addPass(new SsaValidatorPass("test-validator"));
        recipeBuilder.addPass(new BlockReorderingPass());

        String fileName = resource.name().toLowerCase(Locale.UK) + ".m";

        Map<String, StringProvider> availableFiles = new HashMap<>();
        availableFiles.put(fileName, StringProvider.newInstance(resource));

        DataStore setupTable = DataStore.newInstance("test");
        TypesMap types = new TypesMap();

        NumericFactory numerics = getNumerics();
        ScalarType scalarType = numerics.newDouble();
        DynamicMatrixType matrixType = DynamicMatrixType.newInstance(scalarType);

        for (String input : file.getMainFunction().getDeclarationNode().getInputs().getNames()) {
            boolean isMatrix = Character.isUpperCase(input.charAt(0));

            types.addSymbol(input, isMatrix ? matrixType : scalarType);
        }

        try (ProjectPassCompilationManager manager = new ProjectPassCompilationManager(
                new ProjectPassCompilationOptions()
                        .withZ3Enabled(true)
                        .withPreTypeInferenceRecipe(DefaultRecipes.DefaultMatlabASTTypeInferenceRecipe)
                        .withSsaRecipe(DefaultRecipes.getTestPreTypeInferenceRecipe())
                        .withPostTypeInferenceRecipe(recipeBuilder.getRecipe())
                        .withAvailableFiles(availableFiles)
                        .withSystemFunctions(systemFunctions)
                        .withDefaultTypes(types))) {

            manager.applyPreTypeInferencePasses(fileName);

            manager.getFunctionNode(new FunctionIdentification(fileName));

            TypedInstance instance = manager.applyTypeInference(setupTable);

            manager.applyPostTypeInferencePasses();

            String expected = SpecsIo.getResource(resource.getExpectedResource());
            String actual = instance.toString();

            Assert.assertEquals(TestUtils.normalize(expected), TestUtils.normalize(actual));

        }
    }

    private static Map<String, InstanceProvider> getDefaultFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();

        for (MatissePrimitive function : MatissePrimitive.values()) {
            functions.put(function.getName(), function.getMatlabFunction());
        }
        for (MatlabBuiltin function : MatlabBuiltin.values()) {
            functions.put(function.getName(), function.getMatlabFunction());
        }
        for (MatlabOp function : MatlabOp.values()) {
            functions.put(function.getName(), function.getMatlabFunction());
        }

        return functions;
    }
}
