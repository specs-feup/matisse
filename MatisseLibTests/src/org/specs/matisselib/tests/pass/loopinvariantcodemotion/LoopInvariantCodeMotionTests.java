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

package org.specs.matisselib.tests.pass.loopinvariantcodemotion;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.ForLoopBuilderResult;
import org.specs.matisselib.passes.posttype.LoopInvariantCodeMotionPass;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.tests.FunctionComposer;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;

public class LoopInvariantCodeMotionTests {
    @Test
    public void testSimple() {
        TypedInstance instance = FunctionComposer.create(getFunctions(), editor -> {
            String one = editor.addMakeIntegerInstruction("one", 1);
            ForLoopBuilderResult loop = editor.makeForLoop(one, one, one);
            loop.getLoopBuilder().addMakeIntegerInstruction("number", 123);
        });

        applyTest(instance, LoopInvariantCodeMotionResources.SIMPLE);
    }

    @Test
    public void testResize() {
        TypedInstance instance = FunctionComposer.create(getFunctions(), editor -> {
            String one = editor.addMakeIntegerInstruction("one", 1);
            String alpha = editor.addMakeIntegerInstruction("alpha", 3);
            String y1 = editor.addSimpleCallToOutputWithSemantics("zeros", "y", Arrays.asList("$one$1", "$one$1"));
            ForLoopBuilderResult loop = editor.makeForLoop(one, one, alpha);
            BlockEditorHelper loopEditor = loop.getLoopBuilder();
            String y2 = editor.makeTemporary("y", editor.getType(y1));
            loopEditor.addInstruction(new PhiInstruction(y2,
                    Arrays.asList(y1, "$y$3"), Arrays.asList(0, 1)));
            String iter = loopEditor.addIntItersInstruction("iter");

            String value = loopEditor.addSimpleCallToOutputWithSemantics("numel", "value", Arrays.asList(y2));
            String y3 = loopEditor.addSet(y2, Arrays.asList(iter), value);
            assert y3.equals("$y$3");
        });

        applyTest(instance, LoopInvariantCodeMotionResources.RESIZE);
    }

    @Test
    public void testNoResize() {
        TypedInstance instance = FunctionComposer.create(getFunctions(), editor -> {
            String one = editor.addMakeIntegerInstruction("one", 1);
            String alpha = editor.addMakeIntegerInstruction("alpha", 3);
            String y1 = editor.addSimpleCallToOutputWithSemantics("zeros", "y", Arrays.asList("$one$1", "$alpha$1"));
            ForLoopBuilderResult loop = editor.makeForLoop(one, one, alpha);
            BlockEditorHelper loopEditor = loop.getLoopBuilder();
            String y2 = editor.makeTemporary("y", editor.getType(y1));
            loopEditor.addInstruction(new PhiInstruction(y2,
                    Arrays.asList(y1, "$y$3"), Arrays.asList(0, 1)));
            String iter = loopEditor.addIntItersInstruction("iter");

            String value = loopEditor.addSimpleCallToOutputWithSemantics("numel", "value", Arrays.asList(y2));
            String y3 = loopEditor.addSimpleSet(y2, Arrays.asList(iter), value);
            assert y3.equals("$y$3");
        });

        applyTest(instance, LoopInvariantCodeMotionResources.NO_RESIZE);
    }

    private void applyTest(TypedInstance instance, LoopInvariantCodeMotionResources resource) {
        DataStore passData = TestUtils.buildPassData(getFunctions(), instance);

        new LoopInvariantCodeMotionPass().apply(instance, passData);

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource)),
                TestUtils.normalize(instance.getFunctionBody().toString()));
    }

    private Map<String, InstanceProvider> getFunctions() {
        HashMap<String, InstanceProvider> functions = new HashMap<>();
        functions.put("zeros", MatlabBuiltin.ZEROS.getMatlabFunction());
        functions.put("numel", MatlabBuiltin.NUMEL.getMatlabFunction());
        return functions;
    }
}
