/**
 * Copyright 2016 SPeCS.
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

package org.specs.matlabtocl.v2.tests.passes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.BranchBuilderResult;
import org.specs.matisselib.helpers.ForLoopBuilderResult;
import org.specs.matisselib.services.DefaultDataProviderService;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.tests.FunctionComposer;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.ssa.instructions.CompleteReductionInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.CopyToGpuInstruction;
import org.specs.matlabtocl.v2.ssa.passes.SetReductionBufferOptimizerPass;
import org.specs.matlabtocl.v2.types.api.GpuGlobalBufferType;
import org.suikasoft.jOptions.DataStore.SimpleDataStore;

import pt.up.fe.specs.util.SpecsIo;

public class SetReductionBufferOptimizerTests {
    @Test
    public void testSimple() {
        TypedInstance instance = FunctionComposer.create(getFunctions(), editor -> {
            NumericFactory numerics = NumericFactory.defaultFactory();
            DynamicMatrixType matType = DynamicMatrixType.newInstance(numerics.newFloat());

            editor.makeTemporary("arg", new GpuGlobalBufferType());
            editor.makeTemporary("new_buf", new GpuGlobalBufferType());
            editor.makeTemporary("mat", matType);

            editor.addInstruction(new ArgumentInstruction("arg$1", 0));
            editor.addInstruction(new ArgumentInstruction("arg$2", 1));
            editor.addInstruction(new CompleteReductionInstruction("$mat$1", ReductionType.MATRIX_SET, "arg$1",
                    matType, null, "arg$2"));
            editor.addInstruction(new CopyToGpuInstruction("$new_buf$1", "$mat$1"));
            editor.addAssignment("$stuff$1", "$new_buf$1");
        });

        test(instance, SequentialSetReductionBufferOptimizerResources.SIMPLE);
    }

    @Test
    public void testLoop() {
        TypedInstance instance = FunctionComposer.create(getFunctions(), editor -> {
            NumericFactory numerics = NumericFactory.defaultFactory();
            DynamicMatrixType matType = DynamicMatrixType.newInstance(numerics.newFloat());

            editor.makeTemporary("buf", new GpuGlobalBufferType());
            editor.makeTemporary("buf", new GpuGlobalBufferType());
            editor.makeTemporary("buf", new GpuGlobalBufferType());
            editor.makeTemporary("result", new GpuGlobalBufferType());
            editor.makeTemporary("m1", matType);
            editor.makeTemporary("m2", matType);
            editor.makeTemporary("m3", matType);
            editor.makeTemporary("m4", matType);
            editor.makeTemporary("m5", matType);

            editor.addInstruction(new ArgumentInstruction("$m$1", 0));
            editor.addInstruction(new ArgumentInstruction("$buf$1", 1));
            editor.addInstruction(new ArgumentInstruction("arg$1", 2));
            editor.addMakeIntegerInstruction("one", 1);
            editor.addInstruction(new CompleteReductionInstruction("$m$2", ReductionType.MATRIX_SET, "$buf$1",
                    matType, null, "$m$1"));
            ForLoopBuilderResult forLoop = editor.makeForLoop("$one$1", "$one$1", "$arg$1");
            BlockEditorHelper loop = forLoop.getLoopBuilder();
            loop.addInstruction(new PhiInstruction("$m$3", Arrays.asList("$m$2", "$m$4"), Arrays.asList(0, 1)));
            loop.addInstruction(new CopyToGpuInstruction("$buf$2", "$m$3"));
            loop.addAssignment("$test$1", "$buf$2"); // Ensure buffer is used
            loop.addInstruction(new CompleteReductionInstruction("$m$4", ReductionType.MATRIX_SET, "$buf$2",
                    matType, null, "$m$3"));
            BlockEditorHelper afterLoop = forLoop.getEndBuilder();
            afterLoop.addInstruction(new PhiInstruction("$m$5", Arrays.asList("$m$2", "$m$4"), Arrays.asList(0, 1)));
            afterLoop.addInstruction(new CopyToGpuInstruction("$buf$3", "$m$5"));
            afterLoop.addAssignment("$result$1", "$buf$3");
        });

        test(instance, LoopSetReductionBufferOptimizerResources.SIMPLE);
    }

    @Test
    public void testMidIf() {
        TypedInstance instance = FunctionComposer.create(getFunctions(), editor -> {
            NumericFactory numerics = NumericFactory.defaultFactory();
            DynamicMatrixType matType = DynamicMatrixType.newInstance(numerics.newFloat());

            editor.makeTemporary("buf", new GpuGlobalBufferType());
            editor.makeTemporary("buf", new GpuGlobalBufferType());
            editor.makeTemporary("buf", new GpuGlobalBufferType());
            editor.makeTemporary("result", new GpuGlobalBufferType());
            editor.makeTemporary("m1", matType);
            editor.makeTemporary("m2", matType);
            editor.makeTemporary("m3", matType);
            editor.makeTemporary("m4", matType);
            editor.makeTemporary("m5", matType);

            editor.addInstruction(new ArgumentInstruction("$m$1", 0));
            editor.addInstruction(new ArgumentInstruction("$buf$1", 1));
            editor.addInstruction(new ArgumentInstruction("arg$1", 2));
            editor.addMakeIntegerInstruction("one", 1);
            editor.addInstruction(new CompleteReductionInstruction("$m$2", ReductionType.MATRIX_SET, "$buf$1",
                    matType, null, "$m$1"));
            ForLoopBuilderResult forLoop = editor.makeForLoop("$one$1", "$one$1", "$arg$1");
            BlockEditorHelper loop = forLoop.getLoopBuilder();
            loop.addInstruction(new PhiInstruction("$m$3", Arrays.asList("$m$2", "$m$4"), Arrays.asList(0, 5)));

            BranchBuilderResult branch = loop.makeBranch("$one$1");
            BlockEditorHelper branchEnd = branch.getEndBuilder();
            branchEnd.addInstruction(new CopyToGpuInstruction("$buf$2", "$m$3"));
            branchEnd.addAssignment("$test$1", "$buf$2"); // Ensure buffer is used
            branchEnd.addInstruction(new CompleteReductionInstruction("$m$4", ReductionType.MATRIX_SET, "$buf$2",
                    matType, null, "$m$3"));
            BlockEditorHelper afterLoop = forLoop.getEndBuilder();
            afterLoop.addInstruction(new PhiInstruction("$m$5", Arrays.asList("$m$2", "$m$4"), Arrays.asList(0, 5)));
            afterLoop.addInstruction(new CopyToGpuInstruction("$buf$3", "$m$5"));
            afterLoop.addAssignment("$result$1", "$buf$3");
        });

        test(instance, LoopSetReductionBufferOptimizerResources.MID_IF);
    }

    @Test
    public void testMidLoop() {
        TypedInstance instance = FunctionComposer.create(getFunctions(), editor -> {
            NumericFactory numerics = NumericFactory.defaultFactory();
            DynamicMatrixType matType = DynamicMatrixType.newInstance(numerics.newFloat());

            editor.makeTemporary("buf", new GpuGlobalBufferType());
            editor.makeTemporary("buf", new GpuGlobalBufferType());
            editor.makeTemporary("buf", new GpuGlobalBufferType());
            editor.makeTemporary("result", new GpuGlobalBufferType());
            editor.makeTemporary("m1", matType);
            editor.makeTemporary("m2", matType);
            editor.makeTemporary("m3", matType);
            editor.makeTemporary("m4", matType);
            editor.makeTemporary("m5", matType);

            editor.addInstruction(new ArgumentInstruction("$m$1", 0));
            editor.addInstruction(new ArgumentInstruction("$buf$1", 1));
            editor.addInstruction(new ArgumentInstruction("arg$1", 2));
            editor.addMakeIntegerInstruction("one", 1);
            editor.addInstruction(new CompleteReductionInstruction("$m$2", ReductionType.MATRIX_SET, "$buf$1",
                    matType, null, "$m$1"));
            ForLoopBuilderResult forLoop = editor.makeForLoop("$one$1", "$one$1", "$arg$1");
            BlockEditorHelper loop = forLoop.getLoopBuilder();
            loop.addInstruction(new PhiInstruction("$m$3", Arrays.asList("$m$2", "$m$4"), Arrays.asList(0, 4)));

            ForLoopBuilderResult innerForLoop = loop.makeForLoop("$one$1", "$one$1", "$one$1");
            BlockEditorHelper innerLoopEnd = innerForLoop.getEndBuilder();
            innerLoopEnd.addInstruction(new CopyToGpuInstruction("$buf$2", "$m$3"));
            innerLoopEnd.addAssignment("$test$1", "$buf$2"); // Ensure buffer is used
            innerLoopEnd.addInstruction(new CompleteReductionInstruction("$m$4", ReductionType.MATRIX_SET, "$buf$2",
                    matType, null, "$m$3"));
            BlockEditorHelper afterLoop = forLoop.getEndBuilder();
            afterLoop.addInstruction(new PhiInstruction("$m$5", Arrays.asList("$m$2", "$m$4"), Arrays.asList(0, 4)));
            afterLoop.addInstruction(new CopyToGpuInstruction("$buf$3", "$m$5"));
            afterLoop.addAssignment("$result$1", "$buf$3");
        });

        test(instance, LoopSetReductionBufferOptimizerResources.MID_LOOP);
    }

    private Map<String, InstanceProvider> getFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();

        return functions;
    }

    private static void test(TypedInstance instance, SequentialSetReductionBufferOptimizerResources testResource) {
        SimpleDataStore dataStore = new SimpleDataStore("test");
        dataStore.add(ProjectPassServices.DATA_PROVIDER, new DefaultDataProviderService(instance, dataStore));
        new SetReductionBufferOptimizerPass().apply(instance, dataStore);

        String expected = SpecsIo.getResource(testResource);
        String obtained = instance.getFunctionBody().toString();

        Assert.assertEquals(TestUtils.normalize(expected), TestUtils.normalize(obtained));
    }

    private static void test(TypedInstance instance, LoopSetReductionBufferOptimizerResources testResource) {
        SimpleDataStore dataStore = new SimpleDataStore("test");
        dataStore.add(ProjectPassServices.DATA_PROVIDER, new DefaultDataProviderService(instance, dataStore));
        new SetReductionBufferOptimizerPass().apply(instance, dataStore);

        String expected = SpecsIo.getResource(testResource);
        String obtained = instance.getFunctionBody().toString();

        Assert.assertEquals(TestUtils.normalize(expected), TestUtils.normalize(obtained));
    }
}
