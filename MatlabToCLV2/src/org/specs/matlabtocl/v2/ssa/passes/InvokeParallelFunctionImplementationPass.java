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

package org.specs.matlabtocl.v2.ssa.passes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.specs.CIR.FunctionInstance.OutputData;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.PreTypeInferenceServices;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockDescendants;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.helpers.ConventionalLoopVariableAnalysis;
import org.specs.matisselib.helpers.ForLoopHierarchy;
import org.specs.matisselib.helpers.ForLoopHierarchy.BlockData;
import org.specs.matisselib.helpers.LoopVariable;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.passes.posttype.InstructionRemovalPass;
import org.specs.matisselib.passes.ssa.BlockReorderingPass;
import org.specs.matisselib.passes.ssa.SsaValidatorPass;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.services.ScalarValueInformationBuilderService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.services.TypedInstanceProviderService;
import org.specs.matisselib.services.WideScopeService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.Input;
import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.NumberInput;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.VariableInput;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.BreakInstruction;
import org.specs.matisselib.ssa.instructions.BuiltinVariableInstruction;
import org.specs.matisselib.ssa.instructions.CommentInstruction;
import org.specs.matisselib.ssa.instructions.ContinueInstruction;
import org.specs.matisselib.ssa.instructions.EndInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.GetOrFirstInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.LineInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.WhileInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matlabtocl.v2.CLServices;
import org.specs.matlabtocl.v2.codegen.ArgumentRole;
import org.specs.matlabtocl.v2.codegen.GeneratedCodeSegment;
import org.specs.matlabtocl.v2.codegen.GeneratedKernel;
import org.specs.matlabtocl.v2.codegen.GeneratedSetRange;
import org.specs.matlabtocl.v2.codegen.KernelArgument;
import org.specs.matlabtocl.v2.codegen.ParallelLoopInformation;
import org.specs.matlabtocl.v2.codegen.Reduction;
import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.codegen.loopconverters.KernelBuilder;
import org.specs.matlabtocl.v2.codegen.loopconverters.LoopConverter;
import org.specs.matlabtocl.v2.codegen.loopconverters.SetRangeBuilder;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.CodeGenerationStrategyProvider;
import org.specs.matlabtocl.v2.codegen.reductionvalidators.GeneralReductionFormatValidationResult;
import org.specs.matlabtocl.v2.codegen.reductionvalidators.GeneralReductionFormatValidator;
import org.specs.matlabtocl.v2.codegen.reductionvalidators.MatrixConstructionReductionValidator;
import org.specs.matlabtocl.v2.codegen.reductionvalidators.ReductionValidator;
import org.specs.matlabtocl.v2.codegen.reductionvalidators.SumReductionValidator;
import org.specs.matlabtocl.v2.helpers.ParallelUtils;
import org.specs.matlabtocl.v2.heuristics.svm.CoalescedAccessPredictor;
import org.specs.matlabtocl.v2.heuristics.svm.SequentialAccessPredictor;
import org.specs.matlabtocl.v2.loopproperties.SerialDimensionLoopProperty;
import org.specs.matlabtocl.v2.services.DeviceMemoryManagementStrategy;
import org.specs.matlabtocl.v2.services.KernelInstanceSink;
import org.specs.matlabtocl.v2.services.ParallelRegionSource;
import org.specs.matlabtocl.v2.ssa.ParallelRegionInstance;
import org.specs.matlabtocl.v2.ssa.ParallelRegionSettings;
import org.specs.matlabtocl.v2.ssa.ScheduleStrategy;
import org.specs.matlabtocl.v2.ssa.instructions.AllocateGlobalReductionBufferInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.AllocateLocalBufferInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.CompleteReductionInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.ComputeGlobalSizeInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.ComputeGroupSizeInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.CopyToGpuInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.InvokeKernelInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.InvokeParallelFunctionInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.SetGpuRangeInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.UseWorkGroupSizeInstruction;
import org.specs.matlabtocl.v2.types.api.CLBridgeType;
import org.specs.matlabtocl.v2.types.api.GpuGlobalBufferType;
import org.specs.matlabtocl.v2.types.api.GpuLocalBufferType;
import org.specs.matlabtocl.v2.types.api.WorkSizeType;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.collect.Iterables;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.reporting.Reporter;

public class InvokeParallelFunctionImplementationPass
        extends InstructionRemovalPass<InvokeParallelFunctionInstruction> {

    public static final String PASS_NAME = "invoke_parallel_implementation";

    private static final List<ReductionValidator> REDUCTION_VALIDATORS = Arrays.asList(
            new SumReductionValidator(),
            new MatrixConstructionReductionValidator());
    private static final List<LoopConverter> LOOP_CONVERTERS = Arrays.asList(
            new SetRangeBuilder(),
            new KernelBuilder());

    public InvokeParallelFunctionImplementationPass() {

        super(InvokeParallelFunctionInstruction.class);
    }

    @Override
    protected void removeInstruction(TypedInstance containerInstance,
            SsaBlock containerBlock,
            int containerBlockId,
            int containerInstructionId,
            InvokeParallelFunctionInstruction invoke,
            DataStore passData) {

        Logger logger = PassUtils.getLogger(passData, PASS_NAME);

        if (PassUtils.skipPass(containerInstance, PASS_NAME)) {
            logger.log("Skipping " + containerInstance.getFunctionIdentification().getName());
            return;
        }

        logger.log("Starting");

        DataProviderService dataProvider = passData.get(ProjectPassServices.DATA_PROVIDER);
        dataProvider.invalidate(CompilerDataProviders.CONTROL_FLOW_GRAPH);
        dataProvider.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);

        ParallelRegionSource source = passData.get(CLServices.PARALLEL_REGION_SOURCE);
        KernelInstanceSink kernelInstanceSink = passData
                .get(CLServices.KERNEL_INSTANCE_SINK);
        TypedInstanceProviderService instanceProviderService = passData
                .get(ProjectPassServices.TYPED_INSTANCE_PROVIDER);
        WideScopeService wideScopeService = passData
                .get(PreTypeInferenceServices.WIDE_SCOPE);
        ScalarValueInformationBuilderService scalarBuilderService = passData
                .get(ProjectPassServices.SCALAR_VALUE_INFO_BUILDER_PROVIDER);
        SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

        CodeGenerationStrategyProvider codeGenerationStrategyProvider = passData
                .get(CLServices.CODE_GENERATION_STRATEGY_PROVIDER);

        ProviderData providerData = containerInstance.getProviderData();

        ParallelRegionInstance parallelInstance = source.getById(invoke.getRegionId());
        FunctionBody body = parallelInstance.getBody();

        ForLoopHierarchy forLoops = ForLoopHierarchy.identifyLoops(body);
        List<BlockDescendants> blockNesting = BlockUtils.getBlockNesting(body);

        logger.log("Blocks ending in fors: " + forLoops);

        // We now have a list of loops that are candidates for parallelization.
        // But we need to find out if they are actually parallelizable first.

        List<ParallelLoopInformation> parallelLoops = getParallelizableLoops(parallelInstance,
                providerData,
                codeGenerationStrategyProvider,
                wideScopeService,
                instanceProviderService,
                scalarBuilderService,
                forLoops,
                blockNesting,
                logger);

        logger.log("Parallel loops: " + parallelLoops);

        Set<Integer> blocksSoFar = new HashSet<>();
        List<ParallelLoopInformation> chosenLoops = new ArrayList<>();

        // Let's choose which loops we'll *actually* generate code for.
        for (ParallelLoopInformation parallelLoop : parallelLoops) {
            int blockId = parallelLoop.loopDeclarationBlockIds.get(0);

            if (forLoops.anyParentIn(blockId, blocksSoFar)) {
                logger.log(
                        "Not extracting loop at " + blockId + " because a parent loop is already being parallelized.");
            } else {
                blocksSoFar.add(blockId);
                chosenLoops.add(parallelLoop);
            }
        }

        logger.log("Choosing to parallelize loops: " + chosenLoops);

        Reporter reporter = providerData.getReportService();
        List<GeneratedCodeSegment> generatedCodeSegments = chosenLoops
                .stream()
                .map(parallelLoop -> generateCode(containerInstance,
                        parallelInstance,
                        parallelLoop,
                        parallelInstance.getParallelSettings(),
                        codeGenerationStrategyProvider,
                        kernelInstanceSink,
                        passData,
                        containerInstance::makeTemporary,
                        providerData,
                        reporter))
                .collect(Collectors.toList());

        replaceLoopsWithCorrespondingCalls(containerInstance,
                body,
                systemFunctions,
                codeGenerationStrategyProvider,
                chosenLoops,
                generatedCodeSegments,
                logger);

        new BlockReorderingPass().apply(body, passData);

        int numBlocks = body.getBlocks().size();
        if (numBlocks == 1) {
            containerBlock.replaceInstructionAt(containerInstructionId, body.getBlock(0).getInstructions());
        } else {
            SsaBlock newEndBlock = new SsaBlock();
            int newEndBlockId = containerInstance.addBlock(newEndBlock);
            containerInstance.breakBlock(containerBlockId, containerBlockId, newEndBlockId);

            Map<Integer, Integer> newBlockNames = new HashMap<>();
            for (int i = 1; i < numBlocks - 1; ++i) {
                newBlockNames.put(i, containerInstance.addBlock(new SsaBlock()));
            }
            newBlockNames.put(0, containerBlockId);
            newBlockNames.put(numBlocks - 1, newEndBlockId);

            body.renameBlocks(newBlockNames);

            for (int i = 1; i < numBlocks - 1; ++i) {
                int newBlockName = newBlockNames.get(i);
                SsaBlock block = containerInstance.getBlock(newBlockName);
                block.addInstructions(body.getBlock(i).getInstructions());
            }

            // Adjust the container block
            newEndBlock.addInstructions(body.getBlock(numBlocks - 1).getInstructions());
            List<SsaInstruction> movedInstructions = containerBlock
                    .getInstructions()
                    .subList(containerInstructionId + 1, containerBlock.getInstructions().size());
            newEndBlock.addInstructions(movedInstructions);
            movedInstructions.clear();
            containerBlock.removeInstructionAt(containerInstructionId);
            containerBlock.addInstructions(body.getBlock(0).getInstructions());

            new SsaValidatorPass("invoke-parallel-validator").apply(containerInstance, passData);
            new BlockReorderingPass().apply(containerInstance, passData);
        }
    }

    public GeneratedCodeSegment generateCode(
            TypedInstance containerInstance,
            ParallelRegionInstance parallelInstance,
            ParallelLoopInformation parallelLoop,
            ParallelRegionSettings parallelSettings,
            CodeGenerationStrategyProvider codeGenerationStrategyProvider,
            KernelInstanceSink kernelSink,
            DataStore passData,
            BiFunction<String, VariableType, String> makeTemporary,
            ProviderData providerData,
            Reporter reporter) {

        return LOOP_CONVERTERS.stream()
                .map(builder -> builder.generateCode(containerInstance,
                        parallelInstance, parallelLoop,
                        parallelSettings,
                        codeGenerationStrategyProvider,
                        kernelSink,
                        passData, makeTemporary, providerData, reporter))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseThrow(() -> new NotImplementedException("Could not generate code"));

    }

    static class OuterLoopPreludeInformation {
        boolean isValid;
        List<PhiInstruction> phis = new ArrayList<>();
    }

    private static void replaceLoopsWithCorrespondingCalls(TypedInstance containerInstance,
            FunctionBody body,
            SystemFunctionProviderService systemFunctions,
            CodeGenerationStrategyProvider codeStrategyProvider,
            List<ParallelLoopInformation> chosenLoops,
            List<GeneratedCodeSegment> generatedKernels,
            Logger logger) {

        replaceLoopsFromBlock(containerInstance, body, systemFunctions, codeStrategyProvider, chosenLoops,
                generatedKernels, 0, logger);
    }

    private static void replaceLoopsFromBlock(TypedInstance containerInstance,
            FunctionBody body,
            SystemFunctionProviderService systemFunctions,
            CodeGenerationStrategyProvider codeStrategyProvider,
            List<ParallelLoopInformation> chosenLoops,
            List<GeneratedCodeSegment> generatedCodeSegments,
            int currentBlockId,
            Logger logger) {

        SsaBlock block = body.getBlock(currentBlockId);

        block.getEndingInstruction().ifPresent(endInstruction -> {
            Optional<ParallelLoopInformation> potentialParallelLoop = chosenLoops
                    .stream()
                    .filter(loop -> loop.loopDeclarationBlockIds.get(0) == currentBlockId)
                    .findAny();

            if (potentialParallelLoop.isPresent()) {

                logger.log("PLIs=" + potentialParallelLoop);

                ParallelLoopInformation parallelLoop = potentialParallelLoop.get();
                ForInstruction forInstruction = (ForInstruction) endInstruction;

                // We'll process the end block first, because we will copy the
                // contents of that block to the end of this one later.
                // We want to be able to deal with cases where there are
                // two consecutive parallel for loops
                // for end; for end;

                replaceLoopsFromBlock(containerInstance,
                        body,
                        systemFunctions,
                        codeStrategyProvider,
                        chosenLoops,
                        generatedCodeSegments,
                        forInstruction.getEndBlock(),
                        logger);

                GeneratedCodeSegment segment = generatedCodeSegments.get(chosenLoops.indexOf(parallelLoop));

                block.removeLastInstruction();

                addCodeSegmentCall(containerInstance, body, block, currentBlockId,
                        systemFunctions,
                        codeStrategyProvider,
                        parallelLoop,
                        forInstruction, segment, logger);

                // Now copy target block.
                int blockEndIndex = forInstruction.getEndBlock();
                for (SsaInstruction instruction : body.getBlock(blockEndIndex).getInstructions()) {
                    if (instruction instanceof PhiInstruction) {
                        PhiInstruction phi = (PhiInstruction) instruction;

                        assert parallelLoop.reductions.stream()
                                .anyMatch(
                                        reduction -> reduction.getLoopVariables().get(0)
                                                .getAfterLoop()
                                                .get()
                                                .equals(phi.getOutput())) : phi.getOutput()
                                                        + " does not match any variable in " + parallelLoop.reductions
                                                        + "\n" + body;

                        continue;
                    }
                    block.addInstruction(instruction.copy());
                }

                body.renameBlocks(Arrays.asList(blockEndIndex), Arrays.asList(currentBlockId));
            } else {
                for (int ownedBlockId : endInstruction.getOwnedBlocks()) {
                    replaceLoopsFromBlock(containerInstance,
                            body,
                            systemFunctions,
                            codeStrategyProvider,
                            chosenLoops,
                            generatedCodeSegments,
                            ownedBlockId,
                            logger);
                }
            }
        });
    }

    private static void addCodeSegmentCall(TypedInstance instance,
            FunctionBody body,
            SsaBlock block,
            int blockId,
            SystemFunctionProviderService systemFunctions,
            CodeGenerationStrategyProvider codeStrategyProvider,
            ParallelLoopInformation parallelLoop,
            ForInstruction forInstruction,
            GeneratedCodeSegment codeSegment,
            Logger logger) {

        if (codeSegment instanceof GeneratedKernel) {
            addKernelCall(instance, body, block, blockId, systemFunctions, codeStrategyProvider, parallelLoop,
                    forInstruction, (GeneratedKernel) codeSegment, logger);
        } else if (codeSegment instanceof GeneratedSetRange) {
            addSetRangeCall(instance, body, block, blockId, systemFunctions, codeStrategyProvider, parallelLoop,
                    forInstruction,
                    (GeneratedSetRange) codeSegment);
        } else {
            throw new NotImplementedException(codeSegment);
        }
    }

    private static void addSetRangeCall(TypedInstance instance,
            FunctionBody body,
            SsaBlock block, int blockId,
            SystemFunctionProviderService systemFunctions,
            CodeGenerationStrategyProvider codeStrategyProvider,
            ParallelLoopInformation parallelLoop,
            ForInstruction forInstruction,
            GeneratedSetRange codeSegment) {

        DeviceMemoryManagementStrategy memoryStrategy = codeStrategyProvider.getDeviceMemoryManagementStrategy();

        Reduction reduction = parallelLoop.reductions.get(0);

        BlockEditorHelper editor = new BlockEditorHelper(body, instance.getProviderData(), systemFunctions,
                instance::getVariableType, instance::makeTemporary,
                blockId);

        String input;
        if (memoryStrategy == DeviceMemoryManagementStrategy.COPY_BUFFERS) {
            input = editor.makeTemporary("buffer", new GpuGlobalBufferType());
            editor.addInstruction(new CopyToGpuInstruction(input, reduction.getInitialName()));
        } else if (memoryStrategy == DeviceMemoryManagementStrategy.FINE_GRAINED_BUFFERS) {
            input = reduction.getInitialName();
        } else {
            throw new NotImplementedException(memoryStrategy);
        }

        String begin = editor.addMakeIntegerInstruction("one", 1);
        String end = codeSegment.getEnd();
        String value = codeSegment.getValue();
        VariableType patternType = MatrixUtils.getElementType(reduction.getUnderlyingType());
        String castValue = editor.makeTemporary("pattern", patternType);
        editor.addAssignment(castValue, value);

        if (memoryStrategy == DeviceMemoryManagementStrategy.COPY_BUFFERS) {
            editor.addInstruction(
                    new SetGpuRangeInstruction(input, begin, end, castValue, codeSegment.getParallelSettings()));
            editor.addInstruction(new CompleteReductionInstruction(reduction.getFinalName(), ReductionType.MATRIX_SET,
                    input, reduction.getUnderlyingType(), null, reduction.getInitialName()));
        } else if (memoryStrategy == DeviceMemoryManagementStrategy.FINE_GRAINED_BUFFERS) {
            String output = reduction.getFinalName();

            editor.addInstruction(new SetGpuRangeInstruction(output, input, begin, end, castValue,
                    codeSegment.getParallelSettings()));
        } else {
            throw new NotImplementedException(memoryStrategy);
        }
    }

    private static void addKernelCall(TypedInstance instance,
            FunctionBody body,
            SsaBlock block,
            int blockId,
            SystemFunctionProviderService systemFunctions,
            CodeGenerationStrategyProvider codeStrategyProvider,
            ParallelLoopInformation parallelLoop,
            ForInstruction forInstruction,
            GeneratedKernel kernel,
            Logger logger) {

        DeviceMemoryManagementStrategy memoryStrategy = codeStrategyProvider.getDeviceMemoryManagementStrategy();
        boolean svmRestrictSequential = codeStrategyProvider.isSvmRestrictedToSequentialAccesses();
        boolean svmRestrictCoalesced = codeStrategyProvider.isSvmRestrictedToCoalescedAccesses();

        BlockEditorHelper editor = new BlockEditorHelper(body, instance.getProviderData(), systemFunctions,
                instance::getVariableType, instance::makeTemporary,
                blockId);

        int depth = parallelLoop.loopDeclarationBlockIds.size();
        ParallelRegionSettings settings = kernel.getParallelSettings();

        List<String> forEnds = new ArrayList<>();
        List<String> forVars = new ArrayList<>();
        List<Integer> loopBlocks = new ArrayList<>();
        forEnds.add(forInstruction.getEnd());
        forVars.addAll(forInstruction.getInputVariables());

        {
            int currentBlock = forInstruction.getLoopBlock();
            for (int i = 1; i < depth; ++i) {
                loopBlocks.add(currentBlock);

                ForInstruction innerFor = (ForInstruction) body.getBlock(currentBlock).getEndingInstruction().get();
                forEnds.add(innerFor.getEnd());
                forVars.addAll(innerFor.getInputVariables());

                currentBlock = innerFor.getLoopBlock();
            }

            loopBlocks.add(currentBlock);
        }

        Map<String, SsaInstruction> intermediates = new HashMap<>();

        for (int i = 0; i < depth - 1; ++i) {
            SsaBlock currentBlock = body.getBlock(loopBlocks.get(i));

            for (SsaInstruction instruction : currentBlock.getInstructions()) {
                for (String output : instruction.getOutputs()) {
                    intermediates.put(output, instruction);
                }
            }
        }

        Queue<String> pendingCopyOutVars = new LinkedList<>();
        pendingCopyOutVars.addAll(forVars);

        Set<String> variablesToCopyOut = new HashSet<>();
        while (!pendingCopyOutVars.isEmpty()) {
            String var = pendingCopyOutVars.poll();

            SsaInstruction instruction = intermediates.get(var);
            if (instruction != null) {
                variablesToCopyOut.add(var);

                Iterable<String> inputs = Iterables.concat(instruction.getInputVariables(), instruction.getOutputs());

                for (String input : inputs) {
                    if (!pendingCopyOutVars.contains(input) && !variablesToCopyOut.contains(input)) {
                        pendingCopyOutVars.add(input);
                    }
                }
            }
        }

        logger.log("Copy out: " + variablesToCopyOut);

        for (int i = 0; i < depth - 1; ++i) {
            SsaBlock currentBlock = body.getBlock(loopBlocks.get(i));

            for (SsaInstruction instruction : currentBlock.getInstructions()) {
                if (instruction.getOutputs().stream().anyMatch(variablesToCopyOut::contains)) {
                    editor.addInstruction(instruction.copy());
                }
            }
        }

        // Prepare the kernel arguments.
        List<String> outputs = new ArrayList<>();
        List<Integer> outputSources = new ArrayList<>();
        List<String> arguments = new ArrayList<>();
        List<String> numUnadjustedThreads = new ArrayList<>();
        List<String> numTasks = new ArrayList<>();
        List<String> numGroups = new ArrayList<>();
        List<String> globalSizes = new ArrayList<>();

        if (settings.schedule == ScheduleStrategy.COOPERATIVE
                || settings.schedule == ScheduleStrategy.SUBGROUP_COOPERATIVE) {
            assert forEnds.size() == 1;
        }

        for (int i = 0; i < depth; ++i) {
            String tasksInDim = forEnds.get(forEnds.size() - i - 1);
            numTasks.add(tasksInDim);

            String numGroupsInDim = instance.makeTemporary("num_groups", WorkSizeType.BASE_TYPE);
            String globalSize = instance.makeTemporary("global_size", WorkSizeType.BASE_TYPE);

            String workgroupSize = settings.localSizes.get(i);

            String numThreadsInDim;
            if (settings.schedule == ScheduleStrategy.DIRECT ||
                    settings.schedule == ScheduleStrategy.COOPERATIVE ||
                    settings.schedule == ScheduleStrategy.SUBGROUP_COOPERATIVE) {
                editor.addInstruction(
                        new ComputeGroupSizeInstruction(numGroupsInDim, tasksInDim, workgroupSize));
                numThreadsInDim = globalSize;
            } else if (settings.schedule == ScheduleStrategy.COARSE_GLOBAL_ROTATION
                    || settings.schedule == ScheduleStrategy.COARSE_SEQUENTIAL) {
                assert i < settings.scheduleNames.size();

                if (i == depth - 1 && i != settings.scheduleNames.size() - 1) {
                    throw new NotImplementedException("Too many coarse factors.");
                }

                String coarseFactor = settings.scheduleNames.get(forEnds.size() - i - 1);

                numThreadsInDim = editor.makeTemporary("num_threads_in_dim", WorkSizeType.BASE_TYPE);
                editor.addInstruction(
                        new ComputeGroupSizeInstruction(numThreadsInDim, tasksInDim, coarseFactor));
                editor.addInstruction(
                        new ComputeGroupSizeInstruction(numGroupsInDim, numThreadsInDim, workgroupSize));
            } else if (settings.schedule == ScheduleStrategy.FIXED_WORK_GROUPS_SEQUENTIAL ||
                    settings.schedule == ScheduleStrategy.FIXED_WORK_GROUPS_GLOBAL_ROTATION) {
                assert i < settings.scheduleNames.size();

                String numWorkGroupsInDim;
                if (i == depth - 1 && i != settings.scheduleNames.size() - 1) {
                    throw new NotImplementedException("Too many work group IDs.");
                }

                numWorkGroupsInDim = settings.scheduleNames.get(i);

                editor.addInstruction(new UseWorkGroupSizeInstruction(numGroupsInDim, numWorkGroupsInDim));

                numThreadsInDim = editor.makeTemporary("num_threads_in_dim", WorkSizeType.BASE_TYPE);
                editor.addInstruction(
                        new ComputeGlobalSizeInstruction(numThreadsInDim, numWorkGroupsInDim, workgroupSize));
            } else {
                throw new NotImplementedException(settings.schedule);
            }

            editor.addInstruction(new ComputeGlobalSizeInstruction(globalSize, workgroupSize, numGroupsInDim));

            numUnadjustedThreads.add(numThreadsInDim);
            numGroups.add(numGroupsInDim);
            globalSizes.add(globalSize);
        }

        String totalSize;
        if (depth == 1) {
            totalSize = globalSizes.get(0);
        } else {
            totalSize = null;
        }

        Map<String, String> reductionBuffers = new HashMap<>();
        Map<String, ArgumentRole> reductionBufferRoles = new HashMap<>();

        for (KernelArgument kernelArgument : kernel.getArguments()) {

            switch (kernelArgument.role) {
            case IMPORTED_DATA: {
                if (mayUseSharedMemory(instance, kernel, kernelArgument, memoryStrategy) &&
                        shouldUseSharedMemory(instance, body, kernel, kernelArgument, forInstruction, parallelLoop,
                                svmRestrictSequential,
                                svmRestrictCoalesced, logger)) {
                    arguments.add(kernelArgument.referencedVariable);
                    if (kernelArgument.referencedReduction != null) {
                        outputs.add(kernelArgument.referencedReduction);
                        outputSources.add(arguments.size() - 1);
                    }
                } else {
                    String gpuBuffer = instance.makeTemporary(kernelArgument.name + "_gpu",
                            new GpuGlobalBufferType());
                    if (kernelArgument.referencedReduction != null) {
                        reductionBuffers.put(kernelArgument.referencedReduction, gpuBuffer);
                    }
                    arguments.add(gpuBuffer);

                    editor.addInstruction(
                            new CopyToGpuInstruction(gpuBuffer, kernelArgument.referencedVariable));
                }
                break;
            }
            case IMPORTED_NUMEL: {
                String argName = instance.makeTemporary(kernelArgument.name + "_numel",
                        CLBridgeType.getBridgeTypeFor((CLNativeType) kernelArgument.clType));
                arguments.add(argName);

                editor.addInstruction(
                        new EndInstruction(argName, kernelArgument.referencedVariable, 0, 1));
                break;
            }
            case IMPORTED_DIM: {
                String argName = instance.makeTemporary(
                        kernelArgument.name + "_dim" + (kernelArgument.dim + 1),
                        CLBridgeType.getBridgeTypeFor((CLNativeType) kernelArgument.clType));
                arguments.add(argName);

                editor.addInstruction(
                        new EndInstruction(argName, kernelArgument.referencedVariable, kernelArgument.dim,
                                Integer.MAX_VALUE));
                break;
            }
            case IMPORTED_VALUE: {
                arguments.add(kernelArgument.referencedVariable);

                break;
            }
            case GLOBAL_PER_WORK_ITEM_BUFFER: {
                String bufferName = instance.makeTemporary(
                        NameUtils.getSuggestedName(kernelArgument.referencedVariable) + "_global",
                        new GpuGlobalBufferType());
                assert kernelArgument.referencedReduction != null;
                reductionBuffers.put(kernelArgument.referencedReduction, bufferName);
                reductionBufferRoles.put(bufferName, ArgumentRole.GLOBAL_PER_WORK_ITEM_BUFFER);
                arguments.add(bufferName);

                editor.addInstruction(
                        new AllocateGlobalReductionBufferInstruction(bufferName, kernelArgument.clType,
                                totalSize));
                break;
            }
            case GLOBAL_PER_WORK_GROUP_BUFFER: {
                String bufferName = instance.makeTemporary(
                        NameUtils.getSuggestedName(kernelArgument.referencedVariable) + "_global",
                        new GpuGlobalBufferType());
                assert kernelArgument.referencedReduction != null;
                reductionBuffers.put(kernelArgument.referencedReduction, bufferName);
                reductionBufferRoles.put(bufferName, ArgumentRole.GLOBAL_PER_WORK_GROUP_BUFFER);
                arguments.add(bufferName);

                assert numGroups.size() == 1;
                // FIXME
                editor.addInstruction(
                        new AllocateGlobalReductionBufferInstruction(bufferName, kernelArgument.clType,
                                numGroups.get(0)));
                break;
            }
            case LOCAL_REDUCTION_BUFFER: {
                String bufferName = instance.makeTemporary(
                        NameUtils.getSuggestedName(kernelArgument.referencedVariable) + "_local",
                        new GpuLocalBufferType());
                arguments.add(bufferName);

                assert numGroups.size() == 1;
                // FIXME
                editor.addInstruction(
                        new AllocateLocalBufferInstruction(bufferName, kernelArgument.clType,
                                settings.localSizes.get(0)));
                break;
            }
            case NUM_TASKS: {
                arguments.add(numTasks.get(numTasks.size() - kernelArgument.dim - 1));
                break;
            }
            default:
                throw new UnsupportedOperationException("Argument role: " + kernelArgument.role);
            }
        }

        editor.addInstruction(new InvokeKernelInstruction(kernel, globalSizes, arguments, outputs, outputSources));

        for (Reduction reduction : parallelLoop.reductions) {
            String finalName = reduction
                    .getLoopVariables()
                    .get(0)
                    .getAfterLoop().get();

            if (!reductionBuffers.containsKey(finalName)) {
                assert memoryStrategy == DeviceMemoryManagementStrategy.FINE_GRAINED_BUFFERS;
                continue;
            }
            String bufferName = reductionBuffers.get(finalName);
            ArgumentRole role = reductionBufferRoles.get(bufferName);

            String reductionSize;
            if (reduction.getReductionType() == ReductionType.MATRIX_SET) {
                reductionSize = null;
            } else if (role == ArgumentRole.GLOBAL_PER_WORK_GROUP_BUFFER) {
                reductionSize = numGroups.get(0);
            } else {
                reductionSize = globalSizes.get(0);
            }

            editor.addInstruction(new CompleteReductionInstruction(
                    finalName,
                    reduction.getReductionType(),
                    bufferName,
                    reduction.getUnderlyingType(),
                    reductionSize,
                    reduction.getLoopVariables().get(0).beforeLoop));
        }
    }

    private static boolean shouldUseSharedMemory(TypedInstance containerInstance,
            FunctionBody body,
            GeneratedKernel kernel,
            KernelArgument kernelArgument,
            ForInstruction outerFor,
            ParallelLoopInformation parallelLoop,
            boolean svmRestrictSequential,
            boolean svmRestrictCoalesced,
            Logger logger) {

        Set<String> badAccesses = new HashSet<>();

        List<String> localSizes = kernel.getParallelSettings().localSizes;

        List<Integer> nonOneIndices = ParallelUtils.getNonOneIndices(containerInstance::getVariableType, localSizes);
        int indexDepth = ParallelUtils.getSharedLocalIndex(nonOneIndices);

        if (svmRestrictCoalesced) {
            Set<String> nonCoalescedAccesses = new CoalescedAccessPredictor().buildBlacklist(body,
                    containerInstance::getVariableType,
                    parallelLoop,
                    kernel.getParallelSettings(),
                    kernel.getLocalReductions(),
                    outerFor.getLoopBlock(),
                    indexDepth,
                    nonOneIndices);
            logger.log("Predicted Non Coalesced Accesses: " + nonCoalescedAccesses);

            badAccesses.addAll(nonCoalescedAccesses);
        }
        if (svmRestrictSequential) {
            Set<String> nonSequentialAccesses = new SequentialAccessPredictor().buildBlacklist(body,
                    containerInstance::getVariableType,
                    parallelLoop,
                    kernel.getParallelSettings(),
                    kernel.getLocalReductions(),
                    outerFor.getLoopBlock(),
                    indexDepth,
                    nonOneIndices);
            logger.log("Predicted Non Sequential Accesses: " + nonSequentialAccesses);

            badAccesses.addAll(nonSequentialAccesses);
        }

        if (kernelArgument.referencedReduction == null) {
            if (badAccesses.contains(kernelArgument.referencedVariable)) {
                return false;
            }
        } else {
            Reduction referencedReduction = parallelLoop.reductions.stream()
                    .filter(reduction -> reduction.getFinalName().equals(kernelArgument.referencedReduction))
                    .findFirst()
                    .get();

            if (referencedReduction.getNames().stream().anyMatch(name -> badAccesses.contains(name))) {
                return false;
            }
        }

        return true;
    }

    private static boolean mayUseSharedMemory(TypedInstance instance,
            GeneratedKernel kernel,
            KernelArgument kernelArgument,
            DeviceMemoryManagementStrategy memoryStrategy) {

        switch (memoryStrategy) {
        case COPY_BUFFERS:
            return false;
        case FINE_GRAINED_BUFFERS:
            break;
        default:
            throw new NotImplementedException(memoryStrategy);
        }

        String sourceVar = kernelArgument.referencedVariable;
        if (!MatrixUtils.usesDynamicAllocation(instance.getVariableType(sourceVar))) {
            return false;
        }

        List<KernelArgument> relevantArguments = kernel.getArguments().stream()
                .filter(argument -> argument.role == ArgumentRole.IMPORTED_DATA
                        && kernelArgument.referencedVariable.equals(argument.referencedVariable))
                .collect(Collectors.toList());

        return relevantArguments.size() == 1 || relevantArguments.stream().allMatch(arg -> arg.isReadOnly);
    }

    private static List<ParallelLoopInformation> getParallelizableLoops(ParallelRegionInstance parallelInstance,
            ProviderData providerData,
            CodeGenerationStrategyProvider codeGenerationStrategyProvider,
            WideScopeService wideScopeService,
            TypedInstanceProviderService instanceProviderService,
            ScalarValueInformationBuilderService scalarBuilderService,
            ForLoopHierarchy forLoops,
            List<BlockDescendants> blockNesting,
            Logger logger) {

        List<ParallelLoopInformation> parallelLoops = new ArrayList<>();
        Set<List<Integer>> visitedLoops = new HashSet<>();

        for (BlockData blockData : forLoops.getForLoops()) {
            visitParallelizableLoops(parallelInstance, providerData,
                    codeGenerationStrategyProvider,
                    wideScopeService, instanceProviderService,
                    scalarBuilderService,
                    forLoops,
                    blockNesting,
                    visitedLoops,
                    Arrays.asList(blockData.getBlockId()),
                    parallelLoops,
                    logger);
        }
        return parallelLoops;

    }

    private static void visitParallelizableLoops(ParallelRegionInstance parallelInstance,
            ProviderData providerData,
            CodeGenerationStrategyProvider codeGenerationStrategyProvider,
            WideScopeService wideScopeService,
            TypedInstanceProviderService instanceProviderService,
            ScalarValueInformationBuilderService scalarBuilderService,
            ForLoopHierarchy forLoops,
            List<BlockDescendants> blockNesting,
            Set<List<Integer>> visitedLoops,
            List<Integer> loopsToVisit,
            List<ParallelLoopInformation> parallelLoops,
            Logger logger) {

        logger.log("Visiting " + loopsToVisit);

        if (visitedLoops.contains(loopsToVisit)) {
            return;
        }
        visitedLoops.add(loopsToVisit);

        // Visit outer loops first
        int outerMostLoopToVisit = loopsToVisit.get(0);
        for (int blockId = 0; blockId < blockNesting.size(); ++blockId) {
            if (blockNesting.get(blockId).getDescendants().contains(outerMostLoopToVisit)) {
                visitParallelizableLoops(parallelInstance, providerData, codeGenerationStrategyProvider,
                        wideScopeService,
                        instanceProviderService,
                        scalarBuilderService, forLoops, blockNesting, visitedLoops, Arrays.asList(blockId),
                        parallelLoops,
                        logger);
            }
        }

        // Then visit loop groups that contain this
        // First by adding prefix loops ([B] -> [A, B])
        Optional<BlockData> outerMostBlockData = forLoops.getBlockData(outerMostLoopToVisit);
        if (!outerMostBlockData.isPresent()) {
            logger.log("Can't handle case: Missing block data for " + outerMostLoopToVisit);
            return;
        }
        List<Integer> outerMostNesting = outerMostBlockData.get().getNesting();
        if (!outerMostNesting.isEmpty()) {
            List<Integer> childLoopToVisit = new ArrayList<>(loopsToVisit);
            childLoopToVisit.add(0, outerMostNesting.get(0));
            visitParallelizableLoops(parallelInstance, providerData, codeGenerationStrategyProvider, wideScopeService,
                    instanceProviderService,
                    scalarBuilderService, forLoops, blockNesting, visitedLoops, childLoopToVisit,
                    parallelLoops,
                    logger);
        }
        // Then by adding suffix loops ([A] -> [A, B], [A, C])
        int innerMostLoopToVisit = SpecsCollections.last(loopsToVisit);
        for (int directChild : forLoops.getDirectChildLoops(innerMostLoopToVisit)) {
            List<Integer> childLoopToVisit = new ArrayList<>(loopsToVisit);
            childLoopToVisit.add(directChild);
            visitParallelizableLoops(parallelInstance, providerData, codeGenerationStrategyProvider, wideScopeService,
                    instanceProviderService,
                    scalarBuilderService, forLoops, blockNesting, visitedLoops, childLoopToVisit,
                    parallelLoops,
                    logger);
        }

        // Don't parallelize if container is already parallel
        for (ParallelLoopInformation parallelLoop : parallelLoops) {
            if (blockNesting.get(parallelLoop.loopDeclarationBlockIds.get(0)).getDescendants()
                    .contains(outerMostLoopToVisit)) {

                // Ancestor was already parallelized. Skip.
                return;
            }

            if (parallelLoop.loopDeclarationBlockIds.get(0) == outerMostLoopToVisit) {
                // Ancestor was already parallelized. Skip.
                return;
            }
        }

        if (loopsToVisit.size() > codeGenerationStrategyProvider.getMaxWorkItemDimensions()) {
            return;
        }

        // Now see if the current combination is valid.
        getParallelLoopInformation(parallelInstance, providerData, wideScopeService, instanceProviderService, forLoops,
                loopsToVisit, scalarBuilderService, logger)
                        .ifPresent(parallelLoops::add);
    }

    static class PendingBlockInfo {
        int blockId;
        boolean allowBreak;

        PendingBlockInfo(int blockId, boolean allowBreak) {
            this.blockId = blockId;
            this.allowBreak = allowBreak;
        }
    }

    static class CandidateReduction {
        String initialName;
        String finalLoopName;
        String finalName;
    }

    private static Optional<ParallelLoopInformation> getParallelLoopInformation(ParallelRegionInstance parallelInstance,
            ProviderData providerData,
            WideScopeService wideScopeService,
            TypedInstanceProviderService instanceProviderService,
            ForLoopHierarchy forLoops,
            List<Integer> blocks,
            ScalarValueInformationBuilderService scalarBuilderService,
            Logger logger) {

        logger.log("Testing loops: " + blocks);

        FunctionBody body = parallelInstance.getBody();

        boolean isValid = true;
        List<List<LoopVariable>> loopVariables = new ArrayList<>();
        for (int innerBlockId : blocks) {
            List<LoopVariable> innerLoopVariables = ConventionalLoopVariableAnalysis.analyzeStandardLoop(body,
                    parallelInstance::getType, innerBlockId, true);

            ForInstruction xfor = (ForInstruction) body.getBlock(innerBlockId).getEndingInstruction()
                    .get();

            if (xfor.hasProperty(SerialDimensionLoopProperty.class)) {
                logger.log("Loop has [serial_dimension] property, can't parallelize.");
                return Optional.empty();
            }

            String start = xfor.getStart();
            Optional<VariableType> startType = parallelInstance.getType(start);
            if (!ConstantUtils.isConstantOne(startType)) {
                logger.log(
                        "Only loops starting at 1 are supported, got " + start + ", of type " + startType.orElse(null));
                return Optional.empty();
            }
            if (!ConstantUtils.isConstantOne(parallelInstance.getType(xfor.getInterval()))) {
                logger.log("Only loops with interval of 1 are supported, got " + xfor.getInterval());
                return Optional.empty();
            }

            if (innerLoopVariables.isEmpty()) {
                logger.log("Loop is not standard");
                return Optional.empty();
            }

            if (loopVariables.isEmpty()) {
                loopVariables.add(new ArrayList<>(innerLoopVariables));
            } else {

                List<LoopVariable> lastLoopVariables = SpecsCollections.last(loopVariables);

                if (innerLoopVariables.size() != lastLoopVariables.size()) {
                    logger.log("Mismatch in number of loop variables");
                    return Optional.empty();
                }

                List<LoopVariable> orderedInnerLoopVariables = new ArrayList<>();

                for (LoopVariable lv : lastLoopVariables) {
                    String outerLoopStart = lv.loopStart;
                    String outerLoopEnd = lv.loopEnd;

                    Optional<LoopVariable> foundVar = innerLoopVariables.stream()
                            .filter(var -> var.beforeLoop.equals(outerLoopStart) &&
                                    var.getAfterLoop().isPresent() &&
                                    var.getAfterLoop().get().equals(outerLoopEnd))
                            .findAny();
                    if (!foundVar.isPresent()) {
                        logger.log("Inner loop does not modify variable " + outerLoopStart + ".");
                        break;
                    }

                    orderedInnerLoopVariables.add(foundVar.get());
                }

                if (orderedInnerLoopVariables.size() != lastLoopVariables.size()) {
                    return Optional.empty();
                }

                loopVariables.add(orderedInnerLoopVariables);
            }
        }

        assert loopVariables.size() == blocks.size() : "Found " + loopVariables.size() + " sets of variables for "
                + blocks.size() + " nested loops.";

        logger.log("Attempting to parallelize: " + blocks);

        List<String> iterDependentVars = new ArrayList<>();
        List<String> iterVars = new ArrayList<>();
        for (int innerBlockId : blocks) {
            boolean isInnerMost = innerBlockId == SpecsCollections.last(blocks);

            if (isInnerMost) {
                ForInstruction loop = (ForInstruction) body
                        .getBlock(innerBlockId)
                        .getEndingInstruction()
                        .get();

                int loopBlockId = loop.getLoopBlock();

                Map<String, SsaInstruction> declarations = new HashMap<>();

                Queue<Integer> pendingBlocks = new LinkedList<>();
                pendingBlocks.add(loopBlockId);

                String iterVar = null;

                while (!pendingBlocks.isEmpty()) {
                    int currentBlockId = pendingBlocks.poll();

                    SsaBlock block = body.getBlock(currentBlockId);

                    for (SsaInstruction instruction : block.getInstructions()) {
                        // We already validated breaks/continues in ConventionalLoopVariableAnalysis

                        if (!isInstructionWhitelisted(body, providerData, wideScopeService, instanceProviderService,
                                parallelInstance::getType,
                                instruction, true, logger)) {
                            logger.log("Can't parallelize loops " + blocks + " due to " + instruction);
                            return Optional.empty();
                        }

                        for (String output : instruction.getOutputs()) {
                            declarations.put(output, instruction);
                        }

                        if (instruction instanceof IterInstruction) {
                            if (currentBlockId == loopBlockId) {
                                iterVar = ((IterInstruction) instruction).getOutput();
                            }
                        }

                        // TODO: Make sure we don't perform any implicit matrix allocations in the loop.
                        // This can happen when two matrix SSA variables are assigned to the same final one.

                        for (int ownedBlock : instruction.getOwnedBlocks()) {
                            pendingBlocks.add(ownedBlock);
                        }
                    }
                }

                iterVars.add(iterVar);
            } else {
                boolean result = validateIntermediateLoopInstructions(body, innerBlockId,
                        iterVars, iterDependentVars, parallelInstance::getType, logger);
                if (!result) {
                    isValid = false;
                    break;
                }
            }
        }

        if (!isValid) {
            return Optional.empty();
        }

        List<Reduction> reductions = new ArrayList<>();

        for (int varId = 0; varId < loopVariables.get(0).size(); ++varId) {
            int capturedVarId = varId;

            List<LoopVariable> loopVariable = loopVariables.stream()
                    .map(loop -> loop.get(capturedVarId))
                    .collect(Collectors.toList());

            Optional<String> output = loopVariable.get(0).getAfterLoop();
            if (!output.isPresent()) {
                // If Dead Code Elimination didn't catch this, then this means that the loop most likely has
                // loop carried dependencies.
                logger.log("Can't parallelize loops " + blocks
                        + ": Reduction result (" + loopVariable + ") doesn't seem to be used.");
                return Optional.empty();
            }

            Optional<GeneralReductionFormatValidationResult> possibleResult = GeneralReductionFormatValidator.test(
                    parallelInstance.getBody(),
                    blocks,
                    loopVariable);
            if (!possibleResult.isPresent()) {
                return Optional.empty();
            }
            GeneralReductionFormatValidationResult result = possibleResult.get();

            assert iterVars.size() == blocks.size() : iterVars + " iter variables for " + blocks.size()
                    + " blocks.";

            Optional<ReductionType> potentialReductionType = getReductionType(parallelInstance,
                    blocks.get(0),
                    iterVars,
                    blocks,
                    loopVariable,
                    result,
                    scalarBuilderService,
                    logger);

            if (!potentialReductionType.isPresent()) {
                logger.log("Can't parallelize loops " + blocks + ": Can't apply reduction to " + output.get());
                return Optional.empty();
            }

            ReductionType reductionType = potentialReductionType.get();

            Reduction reduction = new Reduction(loopVariable,
                    reductionType,
                    parallelInstance.getType(loopVariable.get(0).beforeLoop).get(),
                    result.getReductionNames());
            logger.log("Found reduction for: " + loopVariables);
            reductions.add(reduction);
        }

        List<SsaInstruction> phis = body.getBlock(
                ((ForInstruction) body.getBlock(blocks.get(0))
                        .getEndingInstruction()
                        .get()).getEndBlock())
                .getInstructions()
                .stream()
                .filter(PhiInstruction.class::isInstance)
                .collect(Collectors.toList());

        return Optional.of(new ParallelLoopInformation(blocks, reductions));
    }

    private static boolean validateIntermediateLoopInstructions(FunctionBody body,
            int blockId,
            List<String> iterVars,
            List<String> iterDependentVars,
            Function<String, Optional<VariableType>> typeGetter,
            Logger logger) {

        logger.log("Analysing intermediate loop: " + blockId);

        ForInstruction loop = (ForInstruction) body
                .getBlock(blockId)
                .getEndingInstruction()
                .get();

        int loopBlockId = loop.getLoopBlock();
        boolean foundIterVar = false;

        SsaBlock block = body.getBlock(loopBlockId);
        for (SsaInstruction instruction : block.getInstructions()) {
            if (instruction.getInstructionType() == InstructionType.LINE ||
                    instruction.getInstructionType() == InstructionType.DECORATOR) {
                continue;
            }

            if (instruction instanceof PhiInstruction) {
                iterDependentVars.add(((PhiInstruction) instruction).getOutput());
                continue;
            }

            if (instruction instanceof IterInstruction) {
                foundIterVar = true;

                String output = ((IterInstruction) instruction).getOutput();
                iterDependentVars.add(output);
                iterVars.add(output);
                continue;
            }

            if (instruction instanceof ForInstruction) {
                ForInstruction xfor = (ForInstruction) instruction;
                if (iterDependentVars.contains(xfor.getStart()) ||
                        iterDependentVars.contains(xfor.getInterval()) ||
                        iterDependentVars.contains(xfor.getEnd())) {

                    logger.log("Inner for iteration range depends on iteration");
                    return false;
                }

                // FIXME: Handle "end" block.

                continue;
            }

            if (instruction instanceof AssignmentInstruction) {
                AssignmentInstruction assignment = (AssignmentInstruction) instruction;

                String output = assignment.getOutput();
                boolean isOutputScalar = ScalarUtils.isScalar(typeGetter.apply(output).orElse(null));
                if (!isOutputScalar) {
                    logger.log("Assigning non-scalar value in intermediate loop: " + instruction);
                    return false;
                }

                Input input = assignment.getInput();
                if (input instanceof NumberInput) {
                    continue;
                }
                if (input instanceof VariableInput) {
                    if (iterDependentVars.contains(((VariableInput) input).getName())) {
                        iterDependentVars.add(output);
                    }
                    continue;
                }
            }

            if (instruction instanceof FunctionCallInstruction) {
                FunctionCallInstruction functionCall = (FunctionCallInstruction) instruction;

                if (!functionCall.getOutputs().stream()
                        .allMatch(var -> ScalarUtils.isScalar(typeGetter.apply(var).orElse(null)))) {

                    logger.log("Non-scalar output in intermediate loop: " + instruction);
                    return false;
                }

                String functionName = functionCall.getFunctionName();
                if (functionName.equals("size") && functionCall.getInputVariables().size() == 2 &&
                        functionCall.getOutputs().size() == 1) {

                    if (functionCall.getInputVariables().stream().anyMatch(iterDependentVars::contains)) {
                        iterDependentVars.add(functionCall.getOutputs().get(0));
                    }

                    continue;
                }

                if ((functionName.equals("plus") || functionName.equals("minus") || functionName.equals("eq"))
                        && functionCall.getOutputs().size() == 1 && functionCall.getInputVariables().size() == 2) {

                    String in1 = functionCall.getInputVariables().get(0);
                    String in2 = functionCall.getInputVariables().get(1);
                    String out = functionCall.getOutputs().get(0);

                    boolean isOutputScalar = ScalarUtils.isScalar(typeGetter.apply(out).orElse(null));
                    if (!isOutputScalar) {
                        logger.log("Assigning non-scalar value in intermediate loop: " + instruction);
                        return false;
                    }

                    if (iterDependentVars.contains(in1) || iterDependentVars.contains(in2)) {
                        iterDependentVars.add(out);
                    }

                    continue;
                }
            }

            logger.log("Unsupported instruction: " + instruction);
            return false;
        }

        if (!foundIterVar) {
            iterVars.add(null);
        }

        return true;
    }

    private static Optional<ReductionType> getReductionType(ParallelRegionInstance parallelInstance,
            int outerBlockId,
            List<String> iterVariables,
            List<Integer> blocks,
            List<LoopVariable> loopVariable,
            GeneralReductionFormatValidationResult result,
            ScalarValueInformationBuilderService scalarBuilderService,
            Logger logger) {

        assert iterVariables.size() == blocks.size() : iterVariables + " iter variables for " + blocks.size()
                + " blocks.";

        for (ReductionValidator validator : InvokeParallelFunctionImplementationPass.REDUCTION_VALIDATORS) {
            logger.log("Testing reduction type: " + validator.getClass().getSimpleName());
            Optional<ReductionType> reduction = validator.verifyReduction(
                    parallelInstance,
                    outerBlockId,
                    blocks,
                    iterVariables,
                    result.getReductionNames(),
                    result.getConstructionInstructions(),
                    result.getMidUsageInstructions(),
                    scalarBuilderService);
            if (reduction.isPresent()) {
                logger.log("Valid reduction");
                return reduction;
            }
        }

        logger.log("Couldn't find any applicable reduction type");
        return Optional.empty();
    }

    private static final List<Class<? extends SsaInstruction>> INNERMOST_WHITELISTED_INSTRUCTIONS = Arrays.asList(
            BuiltinVariableInstruction.class,
            CommentInstruction.class,
            LineInstruction.class,
            PhiInstruction.class,
            AssignmentInstruction.class,
            SimpleSetInstruction.class,
            SimpleGetInstruction.class,
            GetOrFirstInstruction.class,
            IterInstruction.class,

            BranchInstruction.class,
            ForInstruction.class,
            WhileInstruction.class,
            ContinueInstruction.class);

    // STUB
    private static final List<String> INNERMOST_WHITELISTED_FUNCTIONS = Arrays.asList(
            "cos",
            "ldivide",
            "le",
            "lt",
            "eq",
            "exp",
            "ge",
            "gt",
            "log",
            "minus",
            "mod",
            "mrdivide",
            "mtimes",
            "plus",
            "single",
            "sqrt",
            "times",
            "uint8",
            "uint32",
            "uint64",
            "uminus",
            "uplus",
            "rem",

            "MATISSE_cl_global_id");

    private static boolean isInstructionWhitelisted(FunctionBody body,
            ProviderData providerData,
            WideScopeService wideScopeService,
            TypedInstanceProviderService instanceProviderService,
            Function<String, Optional<VariableType>> typeGetter,
            SsaInstruction instruction,
            boolean allowBreak,
            Logger logger) {

        if (InvokeParallelFunctionImplementationPass.INNERMOST_WHITELISTED_INSTRUCTIONS
                .contains(instruction.getClass())) {
            return true;
        }

        if (instruction instanceof BreakInstruction) {
            return allowBreak;
        }

        if (instruction instanceof TypedFunctionCallInstruction) {

            // FIXME: Stub
            TypedFunctionCallInstruction functionCall = (TypedFunctionCallInstruction) instruction;

            String functionName = functionCall.getFunctionName();
            Optional<FunctionIdentification> identification = wideScopeService.getUserFunction(functionName);
            if (identification.isPresent()) {

                List<VariableType> inputTypes = functionCall.getInputVariables()
                        .stream()
                        .map(name -> typeGetter.apply(name).get())
                        .collect(Collectors.toList());
                List<OutputData> outputData = functionCall.getOutputs()
                        .stream()
                        .map(name -> typeGetter.apply(name).get())
                        .map(type -> new OutputData(type, true))
                        .collect(Collectors.toList());

                ProviderData newData = providerData.create(inputTypes);
                newData.setOutputData(outputData);

                TypedInstance instance = instanceProviderService.getTypedInstance(identification.get(), newData);
                return isValidKernelUserFunction(instance, logger);

            }

            if (InvokeParallelFunctionImplementationPass.INNERMOST_WHITELISTED_FUNCTIONS
                    .contains(functionCall.getFunctionName())) {
                return true;
            }

            if (functionCall.getFunctionName().equals("size") && functionCall.getInputVariables().size() == 2) {
                return true;
            }
        }

        return false;
    }

    private static boolean isValidKernelUserFunction(TypedInstance instance, Logger logger) {
        logger.log("Testing instance " + instance.getFunctionBody().getName());

        // FIXME
        return true;
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                // Explicitly invalidated
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
