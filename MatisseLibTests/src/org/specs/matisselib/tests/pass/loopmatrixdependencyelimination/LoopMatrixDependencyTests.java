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

package org.specs.matisselib.tests.pass.loopmatrixdependencyelimination;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Types.VariableType;
import org.specs.MatlabToC.Functions.MatissePrimitive;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.matisselib.passes.posttype.LoopMatrixDependencyEliminationPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.tests.TestSkeleton;
import org.specs.matisselib.tests.TestUtils;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.collections.MultiMap;

public class LoopMatrixDependencyTests extends TestSkeleton {
    @Test
    public void testAuthorizedValuesForMissingTypes() {
        FunctionBody body = buildBody();

        Map<String, VariableType> types = new HashMap<>();

        MultiMap<String, String> result = LoopMatrixDependencyEliminationPass.getAuthorizedDerivedIndices(body,
                name -> Optional.ofNullable(types.get(name)));

        Assert.assertEquals(MultiMap.newInstance(), result);
    }

    @Test
    public void testAuthorizedValuesSimple() {
        FunctionBody body = buildBody();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$step$1", getNumerics().newInt(1));

        MultiMap<String, String> result = LoopMatrixDependencyEliminationPass.getAuthorizedDerivedIndices(body,
                name -> Optional.ofNullable(types.get(name)));

        Assert.assertEquals(MultiMap.newInstance(), result);
    }

    @Test
    public void testAuthorizedValuesWithDiff() {
        FunctionBody body = buildBody();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$step$1", getNumerics().newInt(1));
        types.put("$one$1", getNumerics().newInt(1));

        MultiMap<String, String> result = LoopMatrixDependencyEliminationPass.getAuthorizedDerivedIndices(body,
                name -> Optional.ofNullable(types.get(name)));

        MultiMap<String, String> expected = new MultiMap<>();
        expected.put("$iter$1", "$expr$1");

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testAuthorizedValuesWithNegativeDiff() {
        FunctionBody body = buildBody();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$step$1", getNumerics().newInt(-1));
        types.put("$one$1", getNumerics().newInt(1));

        MultiMap<String, String> result = LoopMatrixDependencyEliminationPass.getAuthorizedDerivedIndices(body,
                name -> Optional.ofNullable(types.get(name)));

        MultiMap<String, String> expected = new MultiMap<>();

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testSimple() {
        FunctionBody body = buildBody();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$step$1", getNumerics().newInt(1));
        types.put("$one$1", getNumerics().newInt(1));

        applyPass(body, types);

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(
                LoopMatrixDependencyResource.SIMPLE.getResource())),
                TestUtils.normalize(body.toString()));
    }

    @Test
    public void testSimple2() {
        FunctionBody body = buildBody("$one$1", "$one$2", "$one$3");

        Map<String, VariableType> types = new HashMap<>();
        types.put("$step$1", getNumerics().newInt(1));
        types.put("$one$1", getNumerics().newInt(1));
        types.put("$one$2", getNumerics().newInt(1));
        types.put("$one$3", getNumerics().newInt(1));

        applyPass(body, types);

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(
                LoopMatrixDependencyResource.SIMPLE2.getResource())),
                TestUtils.normalize(body.toString()));
    }

    @Test
    public void testSimple2D() {
        FunctionBody body = buildBody2D();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$step$1", getNumerics().newInt(1));
        types.put("$one$1", getNumerics().newInt(1));

        applyPass(body, types);

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(
                LoopMatrixDependencyResource.SIMPLE2D.getResource())),
                TestUtils.normalize(body.toString()));
    }

    @Test
    public void testPartiallyUnmodified() {
        FunctionBody body = buildBody();

        Map<String, VariableType> types = new HashMap<>();
        types.put("$step$1", getNumerics().newInt(1));

        applyPass(body, types);

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(
                LoopMatrixDependencyResource.PARTIALLY_UNMODIFIED.getResource())),
                TestUtils.normalize(body.toString()));
    }

    private static FunctionBody buildBody() {
        return buildBody("$one$1", "$one$1", "$one$1");
    }

    private static FunctionBody buildBody(String i1, String i2, String i3) {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        body.addBlock(block);
        block.addInstruction(new ForInstruction("$start$1", "$step$1", "$end$1", 1, 2));

        SsaBlock loopBlock = new SsaBlock();
        body.addBlock(loopBlock);
        loopBlock.addInstruction(new PhiInstruction("A$2", Arrays.asList("A$1", "A$3"), Arrays.asList(0, 1)));
        loopBlock.addInstruction(new IterInstruction("$iter$1"));
        loopBlock.addInstruction(new SimpleGetInstruction("$value$1", "A$1", Arrays.asList("$iter$1", i1)));
        loopBlock.addInstruction(new UntypedFunctionCallInstruction("minus", Arrays.asList("$expr$1"),
                Arrays.asList("$iter$1", "$one$1")));
        loopBlock.addInstruction(new SimpleGetInstruction("$value$2", "A$1", Arrays.asList("$expr$1", i2)));
        loopBlock
                .addInstruction(new SimpleSetInstruction("A$3", "A$2", Arrays.asList("$iter$1", i3), "$value$1"));

        SsaBlock endBlock = new SsaBlock();
        body.addBlock(endBlock);
        endBlock.addInstruction(new PhiInstruction("A$4", Arrays.asList("A$1", "A$3"), Arrays.asList(0, 1)));

        return body;
    }

    private static FunctionBody buildBody2D() {
        FunctionBody body = new FunctionBody();
        SsaBlock block = new SsaBlock();
        body.addBlock(block);
        block.addInstruction(new ForInstruction("$start$1", "$step$1", "$end$1", 1, 4));

        SsaBlock outerLoopBlock = new SsaBlock();
        body.addBlock(outerLoopBlock);
        outerLoopBlock.addInstruction(new PhiInstruction("A$2", Arrays.asList("A$1", "A$5"), Arrays.asList(0, 3)));
        outerLoopBlock.addInstruction(new IterInstruction("$iter$1"));
        outerLoopBlock.addInstruction(new ForInstruction("$start$1", "$step$1", "$end$2", 2, 3));

        SsaBlock innerLoopBlock = new SsaBlock();
        body.addBlock(innerLoopBlock);
        innerLoopBlock.addInstruction(new PhiInstruction("A$3", Arrays.asList("A$2", "A$4"), Arrays.asList(1, 2)));
        innerLoopBlock.addInstruction(new IterInstruction("$iter$2"));
        innerLoopBlock.addInstruction(new SimpleGetInstruction("$value$1", "A$1", Arrays.asList("$iter$1", "$iter$2")));
        innerLoopBlock.addInstruction(new UntypedFunctionCallInstruction("minus", Arrays.asList("$expr$1"),
                Arrays.asList("$iter$1", "$one$1")));
        innerLoopBlock.addInstruction(new SimpleGetInstruction("$value$2", "A$1", Arrays.asList("$expr$1", "$iter$2")));
        innerLoopBlock
                .addInstruction(
                        new SimpleSetInstruction("A$4", "A$3", Arrays.asList("$iter$1", "$iter$2"), "$value$1"));

        SsaBlock innerEndBlock = new SsaBlock();
        body.addBlock(innerEndBlock);
        innerEndBlock.addInstruction(new PhiInstruction("A$5", Arrays.asList("A$2", "A$4"), Arrays.asList(1, 2)));

        SsaBlock endBlock = new SsaBlock();
        body.addBlock(endBlock);
        endBlock.addInstruction(new PhiInstruction("A$6", Arrays.asList("A$1", "A$5"), Arrays.asList(0, 3)));

        return body;
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
            Map<String, VariableType> types) {

        TestUtils.testTypeTransparentPass(
                new LoopMatrixDependencyEliminationPass(),
                body,
                types,
                getDefaultFunctions());
    }
}
