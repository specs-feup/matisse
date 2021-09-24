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

package org.specs.matisselib.tests.pass.allocationsimplifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.MatlabToC.Functions.MatissePrimitive;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.passes.posttype.AllocationSimplifierPass;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.tests.TestFunctionProviderService;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class AllocationSimplifierTests extends TestSkeleton {
    @Test
    public void testSimple() {
        AllocationSimplifierResource resource = AllocationSimplifierResource.SIMPLE;
        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("$1", 0));
        block.addInstruction(new UntypedFunctionCallInstruction("zeros", Arrays.asList("$2"), Arrays.asList("$1")));
        block.addInstruction(new UntypedFunctionCallInstruction("matisse_new_array_from_matrix", Arrays.asList("$3"),
                Arrays.asList("$2")));

        runTest(resource, block, true, true, true);
    }

    @Test
    public void testChained() {
        AllocationSimplifierResource resource = AllocationSimplifierResource.CHAINED;
        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("$1", 0));
        block.addInstruction(new UntypedFunctionCallInstruction("zeros", Arrays.asList("$2"), Arrays.asList("$1")));
        block.addInstruction(new UntypedFunctionCallInstruction("matisse_new_array_from_matrix", Arrays.asList("$3"),
                Arrays.asList("$2")));
        block.addInstruction(new UntypedFunctionCallInstruction("matisse_new_array_from_matrix", Arrays.asList("$4"),
                Arrays.asList("$3")));

        runTest(resource, block, true, true, true, true);
    }

    @Test
    public void testValues() {
        AllocationSimplifierResource resource = AllocationSimplifierResource.VALUES;
        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("$1", 0));
        block.addInstruction(new ArgumentInstruction("$2", 1));
        block.addInstruction(
                new UntypedFunctionCallInstruction("zeros", Arrays.asList("$3"), Arrays.asList("$1", "$2")));
        block.addInstruction(new UntypedFunctionCallInstruction("matisse_new_array_from_matrix", Arrays.asList("$4"),
                Arrays.asList("$3")));
        block.addInstruction(new UntypedFunctionCallInstruction("matisse_new_array_from_matrix", Arrays.asList("$5"),
                Arrays.asList("$4")));

        runTest(resource, block, false, false, true, true, true);
    }

    @Test
    public void testNoDoubleZeros() {
        AllocationSimplifierResource resource = AllocationSimplifierResource.NO_DOUBLE_ZEROS;
        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("$1", 0));
        block.addInstruction(new UntypedFunctionCallInstruction("zeros", Arrays.asList("$2"), Arrays.asList("$1")));
        block.addInstruction(new UntypedFunctionCallInstruction("zeros", Arrays.asList("$3"), Arrays.asList("$2")));

        runTest(resource, block, true, true, true);
    }

    @Test
    public void testZerosSize() {
        AllocationSimplifierResource resource = AllocationSimplifierResource.ZEROS_SIZE;
        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("$1", 0));
        block.addInstruction(new UntypedFunctionCallInstruction("zeros", Arrays.asList("$2"), Arrays.asList("$1")));
        block.addInstruction(new UntypedFunctionCallInstruction("size", Arrays.asList("$size"), Arrays.asList("$2")));
        block.addInstruction(new UntypedFunctionCallInstruction("zeros", Arrays.asList("$3"), Arrays.asList("$size")));

        runTest(resource, block, true, true, true);
    }

    @Test
    public void testNewArraySize() {
        AllocationSimplifierResource resource = AllocationSimplifierResource.NEW_ARRAY_SIZE;
        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("$1", 0));
        block.addInstruction(new UntypedFunctionCallInstruction("zeros", Arrays.asList("$2"), Arrays.asList("$1")));
        block.addInstruction(new UntypedFunctionCallInstruction("size", Arrays.asList("$size"), Arrays.asList("$2")));
        block.addInstruction(
                new UntypedFunctionCallInstruction("matisse_new_array", Arrays.asList("$3"), Arrays.asList("$size")));

        runTest(resource, block, false, true, true);
    }

    private static void runTest(AllocationSimplifierResource resource, SsaBlock block, boolean... isMatrix) {

        VariableType intType = getNumerics().newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        Map<String, VariableType> types = new HashMap<>();

        FunctionBody body = new FunctionBody();
        body.addBlock(block);

        for (int i = 1; i <= isMatrix.length; ++i) {
            types.put("$" + i, isMatrix[i - 1] ? intMatrixType : intType);
        }

        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource.getResource())),
                TestUtils.normalize(obtained));
    }

    private static Map<String, InstanceProvider> getDefaultFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();
        functions.put("zeros", new InstanceProvider() {

            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                throw new NotImplementedException("Test zeros");
            }

            @Override
            public FunctionType getType(ProviderData data) {
                return FunctionTypeBuilder
                        .newInline()
                        .addInputs(data.getInputTypes())
                        .returning(DynamicMatrixType.newInstance(NumericFactory.defaultFactory().newInt()))
                        .build();
            }
        });
        functions.put("matisse_new_array_from_matrix", MatissePrimitive.NEW_ARRAY_FROM_MATRIX.getMatlabFunction());
        functions.put("matisse_new_array", MatissePrimitive.NEW_ARRAY.getMatlabFunction());
        functions.put("matisse_new_array_from_dims", MatissePrimitive.NEW_ARRAY_FROM_DIMS.getMatlabFunction());
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

        ProviderData providerData = ProviderData.newInstance("test");

        Map<String, Integer> lastTemporaries = body.getLastTemporaries();

        new AllocationSimplifierPass();
        AllocationSimplifierPass.apply(body,
                providerData,
                name -> {
                    if (modifiedTypes.containsKey(name)) {
                        return Optional.of(modifiedTypes.get(name));
                    }
                    return Optional.empty();
                }, (semantics, type) -> {
                    String name = body.makeTemporary(semantics);
                    modifiedTypes.put(name, type);
                    lastTemporaries.put(semantics, lastTemporaries.getOrDefault(semantics, 0) + 1);
                    return name;
                }, passData);

        if (!lastTemporaries.equals(body.getLastTemporaries())) {
            Assert.fail("Allocated temporary variable through body.makeTemporary() instead of type-aware function");
        }
    }
}
