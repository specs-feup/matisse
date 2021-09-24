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

package org.specs.matisselib.tests.pass.simpleaccesssimplifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.matisselib.passes.posttype.SimpleAccessSimplificationPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;

public class SimpleAccessSimplifierTests extends TestSkeleton {
    @Test
    public void testSimple() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, Arrays.asList(1, 2));

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = buildSimpleGetBody("$one", "x$1");

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("$one", int1Type);
        types.put("x$1", intType);
        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(SimpleAccessSimplifierResource.SIMPLE.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testSimpleSet() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, Arrays.asList(1, 2));

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = buildSimpleSetBody("$one", "x$1");

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("y$1", intMatrixType);
        types.put("$one", int1Type);
        types.put("x$1", intType);
        types.put("$value$1", intType);
        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(SimpleAccessSimplifierResource.SIMPLE_SET.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testSimpleSet2() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, Arrays.asList(1, 2));

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = buildSimpleSetBody("$one", "x$1", "$one", "$one");

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("y$1", intMatrixType);
        types.put("$one", int1Type);
        types.put("x$1", intType);
        types.put("$value$1", intType);
        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(SimpleAccessSimplifierResource.SIMPLE_SET.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testSimpleSet3() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, Arrays.asList(-1, -1));

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = buildSimpleSetBody("x$1", "$one", "$one", "$one");

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("y$1", intMatrixType);
        types.put("$one", int1Type);
        types.put("x$1", intType);
        types.put("$value$1", intType);
        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(SimpleAccessSimplifierResource.SIMPLE_SET.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testSimpleSet4() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, Arrays.asList(-1, -1));

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = buildSimpleSetBody("x$1", "x$2", "$one", "$one");

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("y$1", intMatrixType);
        types.put("$one", int1Type);
        types.put("x$1", intType);
        types.put("x$2", intType);
        types.put("$value$1", intType);
        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(SimpleAccessSimplifierResource.SIMPLE_SET2.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testInvalid() {

        VariableType intType = getNumerics().newInt();
        VariableType int1Type = getNumerics().newInt(1);
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, Arrays.asList(2, 2));

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = buildSimpleGetBody("$one", "x$1");

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("$one", int1Type);
        types.put("x$1", intType);
        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(SimpleAccessSimplifierResource.INVALID.getResource())),
                TestUtils.normalize(obtained));
    }

    @Test
    public void testInvalid2() {

        VariableType intType = getNumerics().newInt();
        VariableType intMatrixType = DynamicMatrixType.newInstance(intType, Arrays.asList(2, 2));

        Map<String, InstanceProvider> functions = getDefaultFunctions();

        FunctionBody body = buildSimpleGetBody("x$1", "x$2");

        Map<String, VariableType> types = new HashMap<>();
        types.put("A$1", intMatrixType);
        types.put("x$2", intType);
        types.put("x$1", intType);
        applyPass(body, types, functions);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(SimpleAccessSimplifierResource.INVALID2.getResource())),
                TestUtils.normalize(obtained));
    }

    private static FunctionBody buildSimpleGetBody(String... args) {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addInstruction(new ArgumentInstruction("x$1", 1));
        block.addAssignment("$one", 1);
        block.addInstruction(new SimpleGetInstruction("y$1", "A$1", Arrays.asList(args)));
        body.addBlock(block);
        return body;
    }

    private static FunctionBody buildSimpleSetBody(String... args) {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        block.addInstruction(new ArgumentInstruction("A$1", 0));
        block.addInstruction(new ArgumentInstruction("x$1", 1));
        block.addAssignment("$one", 1);
        block.addAssignment("$value$1", 2);
        block.addInstruction(new SimpleSetInstruction("y$1", "A$1", Arrays.asList(args), "$value$1"));
        body.addBlock(block);
        return body;
    }

    private static Map<String, InstanceProvider> getDefaultFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();
        return functions;
    }

    private static void applyPass(FunctionBody body,
            Map<String, VariableType> types,
            Map<String, InstanceProvider> functions) {

        TestUtils.testTypeTransparentPass(new SimpleAccessSimplificationPass(), body, types, functions);
    }
}
