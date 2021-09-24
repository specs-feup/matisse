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

package org.specs.matisselib.tests.pass.loopgetsimplifier.boundscheckmotion;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.matisselib.PreTypeInferenceServices;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.passes.posttype.loopgetsimplifier.BoundsCheckMotionPass;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.services.log.NullLogService;
import org.specs.matisselib.services.scalarbuilderinfo.Z3ScalarValueInformationBuilderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.MatrixGetInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.tests.TestFunctionProviderService;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matisselib.typeinference.TypedInstance;

import pt.up.fe.specs.util.SpecsIo;

public class BoundsCheckMotionTests extends TestSkeleton {
    @Test
    public void testSimple() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addInstruction(new ArgumentInstruction("$numel", 1));
        block.addAssignment("$one", 1);
        block.addInstruction(new ForInstruction("$one", "$one", "$numel", 1, 2));
        body.addBlock(block);

        SsaBlock loopContent = new SsaBlock();
        loopContent.addInstruction(new IterInstruction("$iter"));
        loopContent.addInstruction(new MatrixGetInstruction("o$1", "A$1", Arrays.asList("$iter")));
        body.addBlock(loopContent);

        SsaBlock endBlock = new SsaBlock();
        body.addBlock(endBlock);

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("o$1", intType);
        types.put("$numel", intType);
        types.put("$iter", intType);
        types.put("$one", int1Type);
        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(BoundsCheckMotionResource.SIMPLE.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testSimple2D() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addInstruction(new ArgumentInstruction("$numel", 1));
        block.addAssignment("$one", 1);
        block.addInstruction(new ForInstruction("$one", "$one", "$numel", 1, 4));
        body.addBlock(block);

        SsaBlock loopContent = new SsaBlock();
        loopContent.addInstruction(new IterInstruction("$iter$1"));
        loopContent.addInstruction(new ForInstruction("$one", "$one", "$numel", 2, 3));
        body.addBlock(loopContent);

        SsaBlock loopContent2 = new SsaBlock();
        loopContent2.addInstruction(new IterInstruction("$iter$2"));
        loopContent2.addInstruction(new MatrixGetInstruction("o$1", "A$1", Arrays.asList("$iter$1", "$iter$2")));
        body.addBlock(loopContent2);

        body.addBlock(new SsaBlock());
        body.addBlock(new SsaBlock());

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("o$1", intType);
        types.put("$numel", intType);
        types.put("$iter", intType);
        types.put("$one", int1Type);
        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(BoundsCheckMotionResource.SIMPLE2D.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testWithConstant() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addInstruction(new ArgumentInstruction("$numel", 1));
        block.addAssignment("$one", 1);
        block.addInstruction(new ForInstruction("$one", "$one", "$numel", 1, 2));
        body.addBlock(block);

        SsaBlock loopContent = new SsaBlock();
        loopContent.addInstruction(new IterInstruction("$iter"));
        loopContent.addInstruction(new MatrixGetInstruction("o$1", "A$1", Arrays.asList("$iter", "$one")));
        body.addBlock(loopContent);

        SsaBlock endBlock = new SsaBlock();
        body.addBlock(endBlock);

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("o$1", intType);
        types.put("$numel", intType);
        types.put("$iter", intType);
        types.put("$one", int1Type);
        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(BoundsCheckMotionResource.WITH_CONSTANT.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testDerived() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType);

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addInstruction(new ArgumentInstruction("$numel", 1));
        block.addAssignment("$one", 1);
        block.addInstruction(new ForInstruction("$one", "$one", "$numel", 1, 2));
        body.addBlock(block);

        SsaBlock loopContent = new SsaBlock();
        loopContent.addInstruction(new IterInstruction("$iter"));
        loopContent.addInstruction(new TypedFunctionCallInstruction("plus",
                FunctionTypeBuilder.newInline().addInput(intType).addInput(intType).returning(intType).build(),
                Arrays.asList("$plus"), Arrays.asList("$iter", "$one")));
        loopContent.addInstruction(new MatrixGetInstruction("o$1", "A$1", Arrays.asList("$plus", "$one")));
        body.addBlock(loopContent);

        SsaBlock endBlock = new SsaBlock();
        body.addBlock(endBlock);

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("o$1", intType);
        types.put("$numel", intType);
        types.put("$plus", intType);
        types.put("$iter", intType);
        types.put("$one", int1Type);
        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(BoundsCheckMotionResource.DERIVED.getResource())),
                TestUtils.normalize(obtained));
    }

    private static Map<String, InstanceProvider> getDefaultFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();
        functions.put("numel", MatlabBuiltin.NUMEL.getMatlabFunction());
        functions.put("size", MatlabBuiltin.SIZE.getMatlabFunction());
        functions.put("ge", MatlabOp.GreaterThanOrEqual.getMatlabFunction());
        return functions;
    }

    private static FunctionBody applyPass(FunctionBody body,
            Map<String, VariableType> types,
            Map<String, InstanceProvider> functions) {

        CommonPassData passData = new CommonPassData("foo");
        SystemFunctionProviderService functionProvider = new TestFunctionProviderService(functions);
        passData.add(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER, functionProvider);
        passData.add(ProjectPassServices.SCALAR_VALUE_INFO_BUILDER_PROVIDER,
                new Z3ScalarValueInformationBuilderService());
        passData.add(PreTypeInferenceServices.LOG, new NullLogService());

        ProviderData providerData = ProviderData.newInstance("test-data");
        TypedInstance instance = new TypedInstance(new FunctionIdentification("test.m"),
                Collections.emptyList(),
                body,
                () -> "",
                providerData);
        for (String variableName : types.keySet()) {
            instance.addVariable(variableName, types.get(variableName));
        }

        new BoundsCheckMotionPass().apply(instance, passData);

        types.clear();
        types.putAll(instance.getVariableTypes());

        return instance.getFunctionBody();
    }
}
