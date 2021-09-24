/**
 * Copyright 2017 SPeCS.
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

package org.specs.matisselib.tests.pass.allocationvalueelimination;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.MatlabToC.Functions.MatissePrimitive;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.ForLoopBuilderResult;
import org.specs.matisselib.passes.posttype.AllocationValueEliminationPass;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.tests.FunctionComposer;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matisselib.typeinference.TypedInstance;

import pt.up.fe.specs.util.SpecsIo;

public class AllocationValueEliminationTests {
    @Test
    public void testSimple() {
        TypedInstance instance = FunctionComposer.create(getSystemFunctions(), editor -> {
            String a = editor.addMakeIntegerInstruction("a", 10);
            String x1 = editor.addSimpleCallToOutputWithSemantics("zeros", "X", Arrays.asList(a, a));
            String one = editor.addMakeIntegerInstruction("one", 1);
            String numel = editor.addSimpleCallToOutputWithSemantics("numel", "numel", Arrays.asList(x1));
            ForLoopBuilderResult xfor = editor.makeForLoop(one, one, numel);

            BlockEditorHelper loopEditor = xfor.getLoopBuilder();
            String x2 = loopEditor.makeTemporary("X", loopEditor.getType(x1));
            String x3 = loopEditor.makeTemporary("X", loopEditor.getType(x1));
            String x4 = loopEditor.makeTemporary("X", loopEditor.getType(x1));

            loopEditor.addInstruction(new PhiInstruction(x2, Arrays.asList(x1, x3), Arrays.asList(0, 1)));
            String i = loopEditor.addIntItersInstruction("i");
            loopEditor.addInstruction(new SimpleSetInstruction(x3, x2, Arrays.asList(i), one));

            BlockEditorHelper endEditor = xfor.getEndBuilder();
            endEditor.addInstruction(new PhiInstruction(x4, Arrays.asList(x1, x3), Arrays.asList(0, 1)));
        });

        test(instance, AllocationValueEliminationResource.SIMPLE);
    }

    @Test
    public void testLoop2D() {
        TypedInstance instance = FunctionComposer.create(getSystemFunctions(), editor -> {
            String a = editor.addMakeIntegerInstruction("a", 10);
            String x1 = editor.addSimpleCallToOutputWithSemantics("zeros", "X", Arrays.asList(a, a));
            String one = editor.addMakeIntegerInstruction("one", 1);
            ForLoopBuilderResult xfor = editor.makeForLoop(one, one, a);

            BlockEditorHelper loopEditor = xfor.getLoopBuilder();
            String x2 = loopEditor.makeTemporary("X", loopEditor.getType(x1));
            String x3 = loopEditor.makeTemporary("X", loopEditor.getType(x1));
            String x4 = loopEditor.makeTemporary("X", loopEditor.getType(x1));

            loopEditor.addInstruction(new PhiInstruction(x2, Arrays.asList(x1, x3), Arrays.asList(0, 1)));
            String i = loopEditor.addIntItersInstruction("i");

            ForLoopBuilderResult innerFor = loopEditor.makeForLoop(one, one, a);
            String x5 = loopEditor.makeTemporary("X", loopEditor.getType(x1));
            String x6 = loopEditor.makeTemporary("X", loopEditor.getType(x1));

            BlockEditorHelper innerLoopEditor = innerFor.getLoopBuilder();
            innerLoopEditor.addInstruction(new PhiInstruction(x5, Arrays.asList(x2, x6), Arrays.asList(1, 3)));
            String j = innerLoopEditor.addIntItersInstruction("j");
            innerLoopEditor.addInstruction(new SimpleSetInstruction(x6, x5, Arrays.asList(i, j), one));

            BlockEditorHelper afterInnerLoopEditor = innerFor.getEndBuilder();
            afterInnerLoopEditor.addInstruction(new PhiInstruction(x3, Arrays.asList(x2, x6), Arrays.asList(1, 3)));

            BlockEditorHelper endEditor = xfor.getEndBuilder();
            endEditor.addInstruction(new PhiInstruction(x4, Arrays.asList(x1, x3), Arrays.asList(0, 4)));
        });

        test(instance, AllocationValueEliminationResource.LOOP2D);
    }

    @Test
    public void testNoCover() {
        TypedInstance instance = FunctionComposer.create(getSystemFunctions(), editor -> {
            String a = editor.addMakeIntegerInstruction("a", 10);
            String x1 = editor.addSimpleCallToOutputWithSemantics("zeros", "X", Arrays.asList(a, a));
            String one = editor.addMakeIntegerInstruction("one", 1);
            String two = editor.addMakeIntegerInstruction("two", 2);
            String numel = editor.addSimpleCallToOutputWithSemantics("numel", "numel", Arrays.asList(x1));
            ForLoopBuilderResult xfor = editor.makeForLoop(two, one, numel);

            BlockEditorHelper loopEditor = xfor.getLoopBuilder();
            String x2 = loopEditor.makeTemporary("X", loopEditor.getType(x1));
            String x3 = loopEditor.makeTemporary("X", loopEditor.getType(x1));
            String x4 = loopEditor.makeTemporary("X", loopEditor.getType(x1));

            loopEditor.addInstruction(new PhiInstruction(x2, Arrays.asList(x1, x3), Arrays.asList(0, 1)));
            String i = loopEditor.addIntItersInstruction("i");
            loopEditor.addInstruction(new SimpleSetInstruction(x3, x2, Arrays.asList(i), one));

            BlockEditorHelper endEditor = xfor.getEndBuilder();
            endEditor.addInstruction(new PhiInstruction(x4, Arrays.asList(x1, x3), Arrays.asList(0, 1)));
        });

        test(instance, AllocationValueEliminationResource.NO_COVER);
    }

    private void test(TypedInstance instance, AllocationValueEliminationResource resource) {
        TestUtils.testTypeTransparentPass(new AllocationValueEliminationPass(), instance.getFunctionBody(),
                new HashMap<>(instance.getVariableTypes()), getSystemFunctions());

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource)),
                TestUtils.normalize(instance.getFunctionBody().toString()));
    }

    private Map<String, InstanceProvider> getSystemFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();

        functions.put("matisse_new_array_from_dims", MatissePrimitive.NEW_ARRAY_FROM_DIMS.getMatlabFunction());
        functions.put("numel", MatlabBuiltin.NUMEL.getMatlabFunction());
        functions.put("zeros", MatlabBuiltin.ZEROS.getMatlabFunction());

        return functions;
    }
}
