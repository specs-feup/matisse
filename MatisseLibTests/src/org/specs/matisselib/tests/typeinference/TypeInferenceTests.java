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

package org.specs.matisselib.tests.typeinference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabToC.MatlabToCUtils;
import org.specs.matisselib.DefaultRecipes;
import org.specs.matisselib.MatlabRecipe;
import org.specs.matisselib.ProjectPassCompilationManager;
import org.specs.matisselib.ProjectPassCompilationOptions;
import org.specs.matisselib.passes.ssa.DeadCodeEliminationPass;
import org.specs.matisselib.passes.ssa.LineInformationEliminationPass;
import org.specs.matisselib.providers.MatlabFunctionTable;
import org.specs.matisselib.ssa.SsaRecipe;
import org.specs.matisselib.ssa.SsaRecipeBuilder;
import org.specs.matisselib.tests.NullPrintStream;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matisselib.typeinference.PostTypeInferenceRecipe;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.DataStore.SimpleDataStore;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.StringProvider;

public class TypeInferenceTests extends TestSkeleton {
    @Test
    public void testSimple() {
        TypesMap types = new TypesMap();
        TypedInstance instance = applyTypeInference(TypeInferenceResources.SIMPLE, types);

        FunctionType type = instance.getFunctionType();
        Assert.assertEquals("double", type.getOutputTypes().get(0).code().getReturnType());
        Assert.assertEquals(1.1, (double) ((ScalarType) type.getOutputTypes().get(0)).scalar().getConstant(), 0.0001);
    }

    private static ProjectPassCompilationManager buildManager(MatlabRecipe preTypeInferenceRecipe,
            SsaRecipe ssaRecipe,
            Map<String, StringProvider> availableFiles) {

        return buildManager(preTypeInferenceRecipe, ssaRecipe, availableFiles, new MatlabFunctionTable());
    }

    private static ProjectPassCompilationManager buildManager(MatlabRecipe preTypeInferenceRecipe,
            SsaRecipe ssaRecipe,
            Map<String, StringProvider> availableFiles,
            MatlabFunctionTable functions) {

        ProjectPassCompilationManager manager = new ProjectPassCompilationManager(
                new ProjectPassCompilationOptions()
                        .withPreTypeInferenceRecipe(preTypeInferenceRecipe)
                        .withSsaRecipe(ssaRecipe)
                        .withPostTypeInferenceRecipe(PostTypeInferenceRecipe.empty())
                        .withAvailableFiles(availableFiles)
                        .withSystemFunctions(functions));
        return manager;
    }

    @Test
    public void testImplicitCall() {
        TypesMap types = new TypesMap();
        TypedInstance instance = applyTypeInference(TypeInferenceResources.IMPLICIT, types);

        FunctionType type = instance.getFunctionType();
        Assert.assertEquals("double", type.getOutputTypes().get(0).code().getReturnType());
        Assert.assertEquals(123, (double) ((ScalarType) type.getOutputTypes().get(0)).scalar().getConstant(), 0.0001);
    }

    @Test
    public void testForLoop() {
        TypesMap types = new TypesMap();
        TypedInstance instance = applyTypeInference(TypeInferenceResources.FOR_LOOP, types);

        FunctionType type = instance.getFunctionType();
        Assert.assertEquals(null, ((ScalarType) type.getOutputTypes().get(0)).scalar().getConstant());
        Assert.assertEquals(null, ((ScalarType) type.getOutputTypes().get(1)).scalar().getConstant());
    }

    @Test
    public void testTooManyOutputs() {
        MatlabRecipe preTypeInferenceRecipe = MatlabRecipe.empty();
        SsaRecipe ssaRecipe = SsaRecipe.empty();
        Map<String, StringProvider> availableFiles = new HashMap<>();
        availableFiles.put("main.m", () -> "function x = main()\n [x, y] = 1.1;\nend");

        ProjectPassCompilationManager manager = buildManager(preTypeInferenceRecipe, ssaRecipe,
                availableFiles);
        manager.setErrorReportStream(NullPrintStream.INSTANCE);

        manager.applyPreTypeInferencePasses("main.m");

        try {
            manager.applyTypeInference(new SimpleDataStore("test"));

            Assert.fail();
        } catch (RuntimeException e) {
            // Expected exception
        }
    }

    @Test
    public void testUserFunctionCall() {
        MatlabRecipe preTypeInferenceRecipe = MatlabRecipe.empty();
        SsaRecipe ssaRecipe = SsaRecipe.empty();
        Map<String, StringProvider> availableFiles = new HashMap<>();
        availableFiles.put("main.m",
                () -> "function x = main()\n x = f(1.1);\nend\n function y = f(x)\ny = x;\nend");

        ProjectPassCompilationManager manager = buildManager(preTypeInferenceRecipe, ssaRecipe,
                availableFiles);
        manager.applyPreTypeInferencePasses("main.m");

        TypedInstance instance = manager.applyTypeInference(new SimpleDataStore("test"));

        FunctionType type = instance.getFunctionType();
        Assert.assertEquals("double", type.getOutputTypes().get(0).code().getReturnType());
    }

    @Test
    public void testSystemFunctionCall() {
        MatlabRecipe preTypeInferenceRecipe = DefaultRecipes.DefaultMatlabASTTypeInferenceRecipe;
        SsaRecipe ssaRecipe = SsaRecipe.empty();
        Map<String, StringProvider> availableFiles = new HashMap<>();
        availableFiles.put("main.m",
                () -> "function x = main()\n x = 1.1 + 2;\nend");

        MatlabFunctionTable functions = MatlabToCUtils.buildMatissePrototypeTable();

        ProjectPassCompilationManager manager = buildManager(preTypeInferenceRecipe, ssaRecipe,
                availableFiles, functions);
        manager.applyPreTypeInferencePasses("main.m");

        DataStore setupTable = DataStore.newInstance("Hello-world");
        setupTable.set(CirKeys.DEFAULT_REAL, getNumerics().newDouble());
        TypedInstance instance = manager.applyTypeInference(setupTable);

        FunctionType type = instance.getFunctionType();
        Assert.assertEquals("double", type.getOutputTypes().get(0).code().getReturnType());
        Assert.assertEquals(3.1, (float) ((ScalarType) type.getOutputTypes().get(0)).scalar().getConstant(), 0.001);

    }

    @Test
    public void testInstanceCache() {
        MatlabRecipe preTypeInferenceRecipe = MatlabRecipe.empty();
        SsaRecipe ssaRecipe = SsaRecipe.empty();
        Map<String, StringProvider> availableFiles = new HashMap<>();
        availableFiles.put("main.m",
                () -> SpecsIo.getResource(TypeInferenceResources.CACHE_M));

        ProjectPassCompilationManager manager = buildManager(preTypeInferenceRecipe, ssaRecipe,
                availableFiles);
        manager.applyPreTypeInferencePasses("main.m");

        manager.applyTypeInference(new SimpleDataStore("test"));

        String obtainedLog = manager.getPassLog();
        String expectedLog = SpecsIo.getResource(TypeInferenceResources.CACHE_LOG);

        Assert.assertEquals(TestUtils.normalize(expectedLog.trim()), TestUtils.normalize(obtainedLog.trim()));
    }

    @Test
    public void testBranch() {
        TypesMap types = new TypesMap();
        TypedInstance instance = applyTypeInference(TypeInferenceResources.BRANCH, types);

        FunctionType type = instance.getFunctionType();

        Assert.assertEquals("double", type.getOutputTypes().get(0).code().getReturnType());
        Assert.assertEquals(1.1, ((ScalarType) type.getOutputTypes().get(0)).scalar().getConstant());
    }

    @Test
    public void testWhileLoop() {
        TypeInferenceResources resource = TypeInferenceResources.WHILE_LOOP;
        TypesMap types = new TypesMap();

        TypedInstance instance = applyTypeInference(resource, types);

        FunctionType type = instance.getFunctionType();

        VariableType output = type.getOutputTypes().get(0);
        Assert.assertEquals("double", output.code().getReturnType());
        Assert.assertNull(((ScalarType) output).scalar().getConstant());
    }

    @Test
    public void testScalarGet() {
        TypeInferenceResources resource = TypeInferenceResources.MATRIX_SCALAR_GET;

        TypesMap types = new TypesMap();
        ScalarType floatType = getNumerics().newDouble();
        VariableType matrixType = DynamicMatrixType.newInstance(floatType);
        types.addSymbol(Arrays.asList("A"), matrixType);

        TypedInstance instance = applyTypeInference(resource, types);

        FunctionType type = instance.getFunctionType();

        Assert.assertEquals("double", type.getOutputTypes().get(0).code().getReturnType());
        Assert.assertNull(((ScalarType) type.getOutputTypes().get(0)).scalar().getConstant());
    }

    @Test
    public void testScalarSet() {
        TypeInferenceResources resource = TypeInferenceResources.MATRIX_SCALAR_SET;

        TypesMap types = new TypesMap();
        ScalarType floatType = getNumerics().newDouble();
        VariableType matrixType = DynamicMatrixType.newInstance(floatType);
        types.addSymbol(Arrays.asList("A"), matrixType);

        TypedInstance instance = applyTypeInference(resource, types);

        FunctionType type = instance.getFunctionType();

        VariableType outputType = type.getOutputTypes().get(0);
        Assert.assertTrue(outputType instanceof DynamicMatrixType);
        Assert.assertEquals("double", ((DynamicMatrixType) outputType).getElementType().code().getReturnType());
    }

    // @Test
    public void testDeadBranch() {
        TypeInferenceResources resource = TypeInferenceResources.DEAD_BRANCH;

        TypesMap types = new TypesMap();

        TypedInstance instance = applyTypeInference(resource, types);

        ScalarType returnType = (ScalarType) instance.getFunctionType().getCReturnType();
        Assert.assertEquals("1", returnType.scalar()
                .getConstantString());
    }

    @Test
    public void testMultipleInstantiation() {
        TypeInferenceResources resource = TypeInferenceResources.MULTIPLE_INSTANTIATION;

        TypesMap types = new TypesMap();

        SsaRecipeBuilder ssaRecipeBuilder = new SsaRecipeBuilder();
        ssaRecipeBuilder.addPass(new DeadCodeEliminationPass());
        ssaRecipeBuilder.addPass(new LineInformationEliminationPass());
        SsaRecipe ssaRecipe = ssaRecipeBuilder.getRecipe();
        TypedInstance instance = applyTypeInference(resource, types, ssaRecipe);

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(TypeInferenceResources.MULTIPLE_INSTANTIATION_RESULT)),
                TestUtils.normalize(instance.toString()));
    }

    @Test
    public void testBigInt() {
        TypeInferenceResources resource = TypeInferenceResources.BIG_INT;
        TypesMap types = new TypesMap();

        SsaRecipeBuilder ssaRecipeBuilder = new SsaRecipeBuilder();
        ssaRecipeBuilder.addPass(new LineInformationEliminationPass());
        SsaRecipe ssaRecipe = ssaRecipeBuilder.getRecipe();
        TypedInstance instance = applyTypeInference(resource, types, ssaRecipe);

        Assert.assertEquals("DOUBLE (4.294967295E9)", instance.getFunctionType().getOutputTypes().get(0).toString());
    }

    @Test
    public void testDoubleIf() {
        TypeInferenceResources resource = TypeInferenceResources.DOUBLE_IF;
        TypesMap types = new TypesMap();
        types.addSymbol("x", getNumerics().newInt());

        SsaRecipeBuilder ssaRecipeBuilder = new SsaRecipeBuilder();
        ssaRecipeBuilder.addPass(new LineInformationEliminationPass());
        SsaRecipe ssaRecipe = ssaRecipeBuilder.getRecipe();
        TypedInstance instance = applyTypeInference(resource, types, ssaRecipe);

        Assert.assertEquals("INT (1)", instance.getFunctionType().getOutputTypes().get(0).toString());
        Assert.assertEquals("INT", instance.getFunctionType().getOutputTypes().get(1).toString());
    }

    @Test
    public void testNestedIf() {
        TypeInferenceResources resource = TypeInferenceResources.NESTED_IF;
        TypesMap types = new TypesMap();
        types.addSymbol("x", getNumerics().newInt());

        SsaRecipeBuilder ssaRecipeBuilder = new SsaRecipeBuilder();
        ssaRecipeBuilder.addPass(new LineInformationEliminationPass());
        SsaRecipe ssaRecipe = ssaRecipeBuilder.getRecipe();
        TypedInstance instance = applyTypeInference(resource, types, ssaRecipe);

        Assert.assertEquals("INT", instance.getFunctionType().getOutputTypes().get(0).toString());
    }

    private static TypedInstance applyTypeInference(TypeInferenceResources resource, TypesMap types) {
        return applyTypeInference(resource, types, SsaRecipe.empty());
    }

    private static TypedInstance applyTypeInference(TypeInferenceResources resource, TypesMap types,
            SsaRecipe ssaRecipe) {

        MatlabRecipe preTypeInferenceRecipe = DefaultRecipes.DefaultMatlabASTTypeInferenceRecipe;
        Map<String, StringProvider> availableFiles = new HashMap<>();
        availableFiles.put(resource.getResourceName(),
                () -> SpecsIo.getResource(resource));

        MatlabFunctionTable functions = MatlabToCUtils.buildMatissePrototypeTable();
        ProjectPassCompilationManager manager = buildManager(preTypeInferenceRecipe, ssaRecipe,
                availableFiles, functions);
        manager.applyPreTypeInferencePasses(resource.getResourceName());

        manager.setDefaultTypes(types);
        TypedInstance instance = manager.applyTypeInference(new SimpleDataStore("test"));
        return instance;
    }
}
