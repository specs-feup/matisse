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

package org.specs.matisselib.tests.pass.elimination.flatten;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilder;
import org.specs.MatlabToC.Functions.MatissePrimitive;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.MFileInstance.PassAwareMatlabToCEngine;
import org.specs.matisselib.DefaultRecipes;
import org.specs.matisselib.ProjectPassCompilationManager;
import org.specs.matisselib.ProjectPassCompilationOptions;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.passes.posttype.VerticalFlattenEliminationPass;
import org.specs.matisselib.providers.MatlabFunctionTable;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.VerticalFlattenInstruction;
import org.specs.matisselib.tests.TestFunctionProviderService;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matisselib.unssa.allocators.DummyVariableAllocator;
import org.suikasoft.jOptions.DataStore.SimpleDataStore;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.StringProvider;

public class FlattenEliminationTests extends TestSkeleton {
    @Test
    public void testSimple() {

        VariableType intType = getNumerics().newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();
        Map<String, VariableType> types = new HashMap<>();

        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addInstruction(new VerticalFlattenInstruction("y$ret", "A$1"));
        body.addBlock(block);

        types.put("A$1", intMatrixType);
        types.put("y$ret", intMatrixType);

        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(FlattenEliminationResource.SIMPLE.getResource())),
                TestUtils.normalize(obtained));
    }

    private static Map<String, InstanceProvider> getDefaultFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();
        functions.put("zeros", MatlabBuiltin.ZEROS.getMatlabFunction());
        functions.put("matisse_new_array", MatissePrimitive.NEW_ARRAY.getMatlabFunction());
        functions.put("matisse_new_array_from_dims", MatissePrimitive.NEW_ARRAY_FROM_DIMS.getMatlabFunction());
        functions.put("size", MatlabBuiltin.SIZE.getMatlabFunction());
        functions.put("numel", MatlabBuiltin.NUMEL.getMatlabFunction());
        functions.put("plus", COperator.Addition.getProvider());
        return functions;
    }

    private static void applyPass(FunctionBody body,
            Map<String, VariableType> types,
            Map<String, InstanceProvider> functions) {

        Map<String, VariableType> modifiedTypes = types == null ? new HashMap<>() : types;
        Map<String, InstanceProvider> modifiedFunctions = functions == null ? new HashMap<>() : functions;

        CommonPassData passData = new CommonPassData("foo");
        SystemFunctionProviderService functionProvider = new TestFunctionProviderService(modifiedFunctions);
        passData.add(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER, functionProvider);

        String rootFile = "dummy.m";
        Map<String, StringProvider> availableFiles = new HashMap<>();
        availableFiles.put(rootFile, () -> "function dummy, end");

        MatlabFunctionTable functionTable = new MatlabFunctionTable();
        functionTable.addPrototypes(MatissePrimitive.class);
        functionTable.addPrototypes(MatlabBuiltin.class);

        ProjectPassCompilationManager manager = new ProjectPassCompilationManager(
                new ProjectPassCompilationOptions()
                        .withPreTypeInferenceRecipe(DefaultRecipes.DefaultMatlabASTTypeInferenceRecipe)
                        .withSsaRecipe(DefaultRecipes.getTestPreTypeInferenceRecipe())
                        .withPostTypeInferenceRecipe(DefaultRecipes.getTestPostTypeInferenceRecipe(true))
                        .withAvailableFiles(availableFiles)
                        .withSystemFunctions(functionTable));
        MFileProvider.setEngine(new PassAwareMatlabToCEngine(manager, functionTable, new DummyVariableAllocator(),
                SsaToCBuilder.DEFAULT_SSA_TO_C_RULES));

        manager.applyPreTypeInferencePasses(rootFile);
        manager.applyTypeInference(new SimpleDataStore("test"));

        TestUtils.testTypeTransparentPass(new VerticalFlattenEliminationPass(), body, modifiedTypes, modifiedFunctions);
    }
}
