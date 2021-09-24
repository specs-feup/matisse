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

package org.specs.matisselib.tests.pass.removetypestrings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilder;
import org.specs.MatlabToC.Functions.MatissePrimitive;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.MFileInstance.MatlabToCEngine;
import org.specs.MatlabToC.MFileInstance.PassAwareMatlabToCEngine;
import org.specs.matisselib.DefaultRecipes;
import org.specs.matisselib.ProjectPassCompilationManager;
import org.specs.matisselib.ProjectPassCompilationOptions;
import org.specs.matisselib.passes.posttype.RemoveTypeStringsPass;
import org.specs.matisselib.providers.MatlabFunctionTable;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.StringInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matisselib.unssa.allocators.EfficientVariableAllocator;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.StringProvider;

public class RemoveTypeStringsTests {
    @Test
    public void testNone() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addAssignment("y$ret", 1);
        body.addBlock(block);

        applyPass(body, null, null);

        RemoveTypeStringsResource resource = RemoveTypeStringsResource.NONE;
        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    private static void performTest(String functionName, RemoveTypeStringsResource resource) {

        DataStore setup = DataStore.newInstance("foo");
        NumericFactory numerics = new NumericFactory(setup);

        VariableType intType = numerics.newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);
        StringType stringType = StringType.create("uint32", 32);

        Map<String, InstanceProvider> functions = getDefaultFunctions();
        InstanceProvider function = functions.get(functionName);

        ProviderData data = ProviderData.newInstance(Arrays.asList(intType, intType, stringType), setup);
        FunctionType functionType = function.getType(data);

        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new StringInstruction("$3", "uint32"));
        block.addInstruction(new TypedFunctionCallInstruction(functionName, functionType, "A$1", "$1", "$2", "$3"));
        block.addInstruction(AssignmentInstruction.fromVariable("A$2", "A$1"));
        body.addBlock(block);

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("$3", stringType);
        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testOnes() {
        performTest("ones", RemoveTypeStringsResource.ONES);
    }

    @Test
    public void testZeros() {
        performTest("zeros", RemoveTypeStringsResource.ZEROS);
    }

    @Test
    public void testEye() {
        performTest("eye", RemoveTypeStringsResource.EYE);
    }

    @Test
    public void testShape() {

        DataStore settings = DataStore.newInstance("foo");
        NumericFactory numerics = new NumericFactory(settings);

        Map<String, InstanceProvider> functions = getDefaultFunctions();
        buildDummyEngine(settings, functions);

        VariableType intType = numerics.newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);
        StringType stringType = StringType.create("uint32", 32);

        InstanceProvider function = functions.get("zeros");

        ProviderData data = ProviderData.newInstance(Arrays.asList(intMatrixType, stringType), settings);
        FunctionType functionType = function.getType(data);

        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new StringInstruction("$2", "uint32"));
        block.addInstruction(new TypedFunctionCallInstruction("zeros", functionType, "A$1", "$1", "$2"));
        block.addInstruction(AssignmentInstruction.fromVariable("A$2", "A$1"));
        body.addBlock(block);

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("$1", intMatrixType);
        types.put("$2", stringType);
        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(RemoveTypeStringsResource.SHAPE)),
                TestUtils.normalize(obtained));
    }

    private static void buildDummyEngine(DataStore settings, Map<String, InstanceProvider> functions) {
        MatlabFunctionTable functionTable = new MatlabFunctionTable();
        for (String functionName : functions.keySet()) {
            functionTable.addBuilder(functionName, functions.get(functionName));
        }

        Map<String, StringProvider> availableFiles = new HashMap<>();
        availableFiles.put("dummy.m", StringProvider.newInstance("function dummy, end"));

        ProjectPassCompilationManager manager = new ProjectPassCompilationManager(
                new ProjectPassCompilationOptions()
                        .withPreTypeInferenceRecipe(DefaultRecipes.DefaultMatlabASTTypeInferenceRecipe)
                        .withSsaRecipe(DefaultRecipes.getTestPreTypeInferenceRecipe())
                        .withPostTypeInferenceRecipe(DefaultRecipes.getTestPostTypeInferenceRecipe(true))
                        .withAvailableFiles(availableFiles)
                        .withSystemFunctions(functionTable));
        manager.applyPreTypeInferencePasses("dummy.m");
        manager.applyTypeInference(settings);

        MatlabToCEngine engine = new PassAwareMatlabToCEngine(
                manager,
                functionTable,
                new EfficientVariableAllocator(),
                SsaToCBuilder.DEFAULT_SSA_TO_C_RULES);
        MFileProvider.setEngine(engine);
    }

    private static Map<String, InstanceProvider> getDefaultFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();

        for (MatissePrimitive function : MatissePrimitive.values()) {
            functions.put(function.getName(), function.getMatlabFunction());
        }
        for (MatlabBuiltin function : MatlabBuiltin.values()) {
            functions.put(function.getName(), function.getMatlabFunction());
        }

        return functions;
    }

    private static void applyPass(FunctionBody body,
            Map<String, VariableType> types,
            Map<String, InstanceProvider> functions) {

        TestUtils.testTypeTransparentPass(new RemoveTypeStringsPass(), body, types, functions);
    }
}
