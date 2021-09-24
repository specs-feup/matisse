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

package org.specs.matisselib.tests.pass.elimination.getorfirst;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabToC.Functions.MatissePrimitive;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.ForLoopBuilderResult;
import org.specs.matisselib.passes.posttype.GetOrFirstSimplificationPass;
import org.specs.matisselib.tests.FunctionComposer;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.ResourceProvider;

public class GetOrFirstEliminationTests {
    @Test
    public void testSimple() {
        TypedInstance instance = FunctionComposer.create(getFunctions(), editor -> {
            String n = editor.makeIntegerTemporary("n");

            String one = editor.addMakeIntegerInstruction("one", 1);
            String matrix = editor.addSimpleCallToOutputWithSemantics("matisse_new_array_from_dims", "X", one, n);
            ForLoopBuilderResult loop = editor.makeForLoop(one, one, n);

            BlockEditorHelper loopEditor = loop.getLoopBuilder();
            String iter = loopEditor.addIntItersInstruction("iter");
            VariableType intType = NumericFactory.defaultFactory().newInt();
            loopEditor.addGetOrFirst(matrix, iter, intType);
        });

        test(instance, GetOrFirstEliminationResources.SIMPLE);
    }

    @Test
    public void testInvalid() {
        TypedInstance instance = FunctionComposer.create(getFunctions(), editor -> {
            String n = editor.makeIntegerTemporary("n");
            String m = editor.makeIntegerTemporary("m");

            String one = editor.addMakeIntegerInstruction("one", 1);
            String matrix = editor.addSimpleCallToOutputWithSemantics("matisse_new_array_from_dims", "X", one, n);
            ForLoopBuilderResult loop = editor.makeForLoop(one, one, m);

            BlockEditorHelper loopEditor = loop.getLoopBuilder();
            String iter = loopEditor.addIntItersInstruction("iter");
            VariableType intType = NumericFactory.defaultFactory().newInt();
            loopEditor.addGetOrFirst(matrix, iter, intType);
        });

        test(instance, GetOrFirstEliminationResources.INVALID);
    }

    private void test(TypedInstance instance, ResourceProvider resource) {
        DataStore passData = TestUtils.buildPassData(getFunctions(), instance);
        new GetOrFirstSimplificationPass().apply(instance, passData);

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource)),
                TestUtils.normalize(instance.getFunctionBody().toString()));
    }

    private static Map<String, InstanceProvider> getFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();

        functions.put("matisse_new_array_from_dims", MatissePrimitive.NEW_ARRAY_FROM_DIMS.getMatlabFunction());

        return functions;
    }
}
