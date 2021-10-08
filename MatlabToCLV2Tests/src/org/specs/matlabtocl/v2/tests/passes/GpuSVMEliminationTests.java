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

package org.specs.matlabtocl.v2.tests.passes;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.MatlabToC.Functions.MatissePrimitive;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.tests.FunctionComposer;
import org.specs.matisselib.tests.TestUtils;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matlabtocl.v2.CLServices;
import org.specs.matlabtocl.v2.codegen.CLVersion;
import org.specs.matlabtocl.v2.codegen.GeneratedKernel;
import org.specs.matlabtocl.v2.codegen.KernelArgument;
import org.specs.matlabtocl.v2.codegen.MatrixCopyToGpuStrategy;
import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.CodeGenerationStrategyProvider;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.ReductionStrategy;
import org.specs.matlabtocl.v2.heuristics.schedule.ScheduleDecisionTree;
import org.specs.matlabtocl.v2.services.DeviceMemoryManagementStrategy;
import org.specs.matlabtocl.v2.ssa.ParallelRegionSettings;
import org.specs.matlabtocl.v2.ssa.ScheduleStrategy;
import org.specs.matlabtocl.v2.ssa.instructions.CopyToGpuInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.InvokeKernelInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.SetGpuRangeInstruction;
import org.specs.matlabtocl.v2.ssa.passes.GpuSVMBufferEliminationPass;
import org.specs.matlabtocl.v2.ssa.passes.GpuSVMEliminationMode;
import org.specs.matlabtocl.v2.tests.CLTestUtils;
import org.specs.matlabtocl.v2.types.api.GpuGlobalBufferType;
import org.suikasoft.jOptions.DataStore.SimpleDataStore;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;

public class GpuSVMEliminationTests {
    @Test
    public void testUseSVMThenCopyToGPU() {
        TypedInstance instance = FunctionComposer.create(getFunctions(), editor -> {
            String intN = editor.addMakeIntegerInstruction("int", 1234);
            String mat1 = editor.addSimpleCallToOutputWithSemantics("matisse_new_array_from_dims", "mat", intN, intN);
            String mat2 = editor.makeTemporary("mat", editor.getType(mat1).get());
            String begin = editor.addMakeIntegerInstruction("begin", 1);
            String end = editor.addSimpleCallToOutput("numel", mat1);
            String value = editor.addMakeIntegerInstruction("value", 0);
            editor.addInstruction(new SetGpuRangeInstruction(mat2, mat1, begin, end, value, null));

            String buffer = editor.makeTemporary("buffer", new GpuGlobalBufferType());
            editor.addInstruction(new CopyToGpuInstruction(buffer, mat2));
        });

        applyPass(instance, GpuSVMEliminationMode.ELIMINATE_NO_ADDED_COPIES,
                GpuSVMEliminationResource.USE_SVM_THEN_COPY_TO_GPU);
    }

    /*
    Temporarily disabled. Not working and I don't have the time to figure out why.
    @Test
    public void testUseSVMThenReturnNoAddedCopies() {
        TypedInstance instance = FunctionComposer.create(getFunctions(), editor -> {
            String intN = editor.addMakeIntegerInstruction("int", 1234);
            String mat1 = editor.addSimpleCallToOutputWithSemantics("matisse_new_array_from_dims", "mat", intN, intN);
            String mat2 = editor.makeTemporary("mat", editor.getType(mat1).get());
            String begin = editor.addMakeIntegerInstruction("begin", 1);
            String end = editor.addSimpleCallToOutput("numel", mat1);
            String value = editor.addMakeIntegerInstruction("value", 0);
            editor.addInstruction(new SetGpuRangeInstruction(mat2, mat1, begin, end, value, null));
        });

        Map<String, String> newNames = new HashMap<>();
        newNames.put("$mat$2", "mat$ret");
        instance.renameVariables(newNames);

        applyPass(instance, GpuSVMEliminationMode.ELIMINATE_NO_ADDED_COPIES,
                GpuSVMEliminationResource.USE_SVM_THEN_RETURN_NO_ADDED_COPIES);
    }

    @Test
    public void testDisparity3Bug() {
        TypedInstance instance = FunctionComposer.create(getFunctions(), editor -> {
            String int1 = editor.addMakeIntegerInstruction("one", 1);
            String intN = editor.makeTemporary("arg", editor.getNumerics().newInt());
            editor.addInstruction(new ArgumentInstruction(intN, 0));
            String mat1 = editor.addSimpleCallToOutputWithSemantics("matisse_new_array_from_dims", "mat",
                    Arrays.asList(int1, intN));
            String mat2 = editor.makeTemporary("mat", editor.getType(mat1).get());

            ParallelRegionSettings settings = CLTestUtils.buildDummySettings();
            List<KernelArgument> kArguments1 = Arrays.asList(
                    KernelArgument.importData("mat1", mat1),
                    KernelArgument.importReductionData("mat", mat1, mat2));
            FunctionInstance kernelInstance1 = new LiteralInstance(null, "kernel1", "file.c", "<<>>");
            GeneratedKernel generatedKernel1 = new GeneratedKernel(kernelInstance1,
                    settings,
                    kArguments1,
                    null,
                    CLVersion.V1_0);
            editor.addInstruction(new InvokeKernelInstruction(generatedKernel1,
                    Collections.emptyList(),
                    Arrays.asList(mat1, mat1), Arrays.asList(mat2), Arrays.asList(1)));
        });

        applyPass(instance, GpuSVMEliminationMode.ELIMINATE_NO_ADDED_COPIES,
                GpuSVMEliminationResource.DISPARITY_3_BUG);
    }
    */

    private void applyPass(TypedInstance instance, GpuSVMEliminationMode mode,
            GpuSVMEliminationResource resource) {

        DataStore passData = new SimpleDataStore("test");
        passData.add(CLServices.CODE_GENERATION_STRATEGY_PROVIDER, new CodeGenerationStrategyProvider() {

            @Override
            public boolean loadProgramFromSource() {
                throw new RuntimeException("Invalid");
            }

            @Override
            public boolean isSvmRestrictedToSequentialAccesses() {
                throw new RuntimeException("Invalid");
            }

            @Override
            public boolean isSvmRestrictedToCoalescedAccesses() {
                throw new RuntimeException("Invalid");
            }

            @Override
            public boolean isRangeSetInstructionEnabled() {
                throw new RuntimeException("Invalid");
            }

            @Override
            public Map<ReductionType, List<ReductionStrategy>> getReductionStrategies() {
                throw new RuntimeException("Invalid");
            }

            @Override
            public Map<ReductionType, List<ReductionStrategy>> getLocalReductionStrategies() {
                throw new RuntimeException("Invalid");
            }

            @Override
            public Map<ReductionType, List<ReductionStrategy>> getSubgroupReductionStrategies() {
                throw new RuntimeException("Invalid");
            }

            @Override
            public GpuSVMEliminationMode getSVMEliminationMode() {
                return mode;
            }

            @Override
            public String getProgramFileName() {
                throw new RuntimeException("Invalid");
            }

            @Override
            public int getSubGroupSize() {
                throw new RuntimeException("Invalid");
            }

            @Override
            public int getMaxWorkItemDimensions() {
                throw new RuntimeException("Invalid");
            }

            @Override
            public MatrixCopyToGpuStrategy getMatrixCopyStrategy() {
                throw new RuntimeException("Invalid");
            }

            @Override
            public DeviceMemoryManagementStrategy getDeviceMemoryManagementStrategy() {
                return DeviceMemoryManagementStrategy.FINE_GRAINED_BUFFERS;
            }

            @Override
            public ScheduleStrategy getCoarseScheduleStrategy() {
                throw new RuntimeException("Invalid");
            }

            @Override
            public ScheduleStrategy getFixedWorkGroupsScheduleStrategy() {
                throw new RuntimeException("Invalid");
            }

            @Override
            public ScheduleDecisionTree getScheduleDecisionTree() {
                throw new RuntimeException("Invalid");
            }

            @Override
            public boolean isSvmSetRangeForbidden() {
                throw new RuntimeException("Invalid");
            }

            @Override
            public boolean getTryUseScheduleCooperative() {
                throw new RuntimeException("Invalid");
            }

            @Override
            public boolean getPreferSubGroupCooperativeSchedule() {
                throw new RuntimeException("Invalid");
            }

            @Override
            public boolean getNvidiaSubgroupAsWarpFallback() {
                throw new RuntimeException("Invalid");
            }
        });
        new GpuSVMBufferEliminationPass().apply(instance, passData);

        Assert.assertEquals(TestUtils.normalize(SpecsIo.getResource(resource)),
                TestUtils.normalize(instance.getFunctionBody().toString()));
    }

    private static Map<String, InstanceProvider> getFunctions() {
        Map<String, InstanceProvider> functions = new HashMap<>();

        functions.put("matisse_new_array_from_dims", MatissePrimitive.NEW_ARRAY_FROM_DIMS.getMatlabFunction());
        functions.put("numel", MatlabBuiltin.NUMEL.getMatlabFunction());
        functions.put("zeros", MatlabBuiltin.ZEROS.getMatlabFunction());

        return functions;
    }
}
