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

package org.specs.matlabtocl.v2.codegen.loopconverters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.DefaultReportService;
import org.specs.matisselib.PreTypeInferenceServices;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.helpers.ConventionalLoopVariableAnalysis;
import org.specs.matisselib.helpers.ForLoopHierarchy;
import org.specs.matisselib.helpers.ForLoopHierarchy.BlockData;
import org.specs.matisselib.helpers.LoopVariable;
import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.IndexedInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.LineInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matisselib.unssa.allocators.EfficientVariableAllocator;
import org.specs.matlabtocl.v2.codegen.GeneratedCodeSegment;
import org.specs.matlabtocl.v2.codegen.GeneratedKernel;
import org.specs.matlabtocl.v2.codegen.KernelCodeGenerator;
import org.specs.matlabtocl.v2.codegen.ParallelLoopInformation;
import org.specs.matlabtocl.v2.codegen.Reduction;
import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.CodeGenerationStrategyProvider;
import org.specs.matlabtocl.v2.codegen.reductionvalidators.GeneralReductionFormatValidationResult;
import org.specs.matlabtocl.v2.codegen.reductionvalidators.GeneralReductionFormatValidator;
import org.specs.matlabtocl.v2.codegen.reductionvalidators.SumReductionValidator;
import org.specs.matlabtocl.v2.helpers.ParallelUtils;
import org.specs.matlabtocl.v2.heuristics.schedule.ScheduleDecisionTree;
import org.specs.matlabtocl.v2.heuristics.schedule.ScheduleMethod;
import org.specs.matlabtocl.v2.heuristics.schedule.SchedulePredictorContext;
import org.specs.matlabtocl.v2.heuristics.schedule.ScheduleRuleChecker;
import org.specs.matlabtocl.v2.heuristics.svm.CoalescedAccessPredictor;
import org.specs.matlabtocl.v2.services.KernelInstanceSink;
import org.specs.matlabtocl.v2.ssa.ParallelRegionInstance;
import org.specs.matlabtocl.v2.ssa.ParallelRegionSettings;
import org.specs.matlabtocl.v2.ssa.ScheduleStrategy;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.collections.MultiMap;
import pt.up.fe.specs.util.reporting.Reporter;

public class KernelBuilder implements LoopConverter {
    private static final boolean ENABLE_LOG = false;

    public Optional<GeneratedCodeSegment> generateCode(
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

        log("Parallel loop=" + parallelLoop.loopDeclarationBlockIds);

        DefaultReportService defaultReportService = (DefaultReportService) reporter;

        int line = -1;

        int declarationBlockId = parallelLoop.loopDeclarationBlockIds.get(0);
        SsaBlock block = parallelInstance.getBody().getBlock(declarationBlockId);
        List<SsaInstruction> instructions = block.getInstructions();
        for (int i = instructions.size() - 1; i >= 0; i--) {
            SsaInstruction instruction = instructions.get(i);

            if (instruction instanceof LineInstruction) {
                line = ((LineInstruction) instruction).getLine();
                break;
            }
        }

        ParallelRegionSettings kernelSettings = parallelSettings.copy();

        MultiMap<Integer, Reduction> localReductions = new MultiMap<>();
        if (kernelSettings.schedule == ScheduleStrategy.AUTO ||
                kernelSettings.schedule == ScheduleStrategy.COOPERATIVE ||
                kernelSettings.schedule == ScheduleStrategy.SUBGROUP_COOPERATIVE) {

            // Compute local reductions

            int innerLoop = SpecsCollections.last(parallelLoop.loopDeclarationBlockIds);
            ForInstruction kernelFor = (ForInstruction) parallelInstance.getBody()
                    .getBlock(innerLoop)
                    .getEndingInstruction()
                    .get();

            ForLoopHierarchy innerLoops = ForLoopHierarchy.identifyLoops(parallelInstance.getBody(),
                    kernelFor.getLoopBlock());
            log("innerLoops=" + innerLoops);

            Set<Integer> blocks = new HashSet<>();

            for (int i = innerLoops.getForLoops().size() - 1; i >= 0; --i) {
                BlockData blockData = innerLoops.getForLoops().get(i);
                int blockId = blockData.getBlockId();
                if (blockData.getNesting().stream().anyMatch(blocks::contains)) {
                    continue;
                }

                ForInstruction xfor = parallelInstance.getBody()
                        .getBlock(blockId)
                        .getEndingInstruction()
                        .map(ForInstruction.class::cast)
                        .get();
                int loopBlockId = xfor.getLoopBlock();

                String iterVariable = parallelInstance.getBody()
                        .getBlock(loopBlockId)
                        .getInstructions()
                        .stream()
                        .filter(IterInstruction.class::isInstance)
                        .map(IterInstruction.class::cast)
                        .findFirst()
                        .map(iter -> iter.getOutput())
                        .orElseThrow(() -> new RuntimeException(
                                "No iter variable, at block=" + loopBlockId + ", instance=" + parallelInstance));

                List<LoopVariable> loopVariables = ConventionalLoopVariableAnalysis.analyzeStandardLoop(
                        parallelInstance.getBody(),
                        parallelInstance.getTypeGetter(), blockId, true);

                List<Reduction> candidateReductions = new ArrayList<>();

                log("At #" + blockId + ", inner loop Variables: " + loopVariables);
                for (LoopVariable lv : loopVariables) {
                    Optional<GeneralReductionFormatValidationResult> tryResult = GeneralReductionFormatValidator.test(
                            parallelInstance.getBody(), Arrays.asList(blockId),
                            Arrays.asList(lv));
                    if (!tryResult.isPresent()) {
                        log(lv.loopStart + " not in general reduction format");
                        candidateReductions.clear();
                        break;
                    }
                    GeneralReductionFormatValidationResult result = tryResult.get();

                    Optional<ReductionType> reductionType = new SumReductionValidator()
                            .verifyReduction(parallelInstance, blockId, Arrays.asList(blockId),
                                    Arrays.asList(iterVariable),
                                    result.getReductionNames(),
                                    result.getConstructionInstructions(),
                                    result.getMidUsageInstructions(),
                                    passData.get(ProjectPassServices.SCALAR_VALUE_INFO_BUILDER_PROVIDER));
                    if (!reductionType.isPresent()) {
                        log("Unsupported reduction type.");
                        candidateReductions.clear();
                        break;
                    }

                    // TODO: Ensure result of reductions is only used in matrix sets.
                    // Either that, or perform some sort of broadcast or keep the result available on all threads.
                    // Do the same for the initial value.

                    VariableType underlyingType = parallelInstance.getType(lv.beforeLoop).get();
                    Reduction reduction = new Reduction(Arrays.asList(lv), reductionType.get(),
                            underlyingType,
                            result.getReductionNames());
                    candidateReductions.add(reduction);
                }

                if (!candidateReductions.isEmpty()) {
                    blocks.add(blockId);

                    localReductions.put(blockId, candidateReductions);
                }
            }
        }
        ForInstruction xfor = parallelInstance.getBody()
                .getBlock(parallelLoop.loopDeclarationBlockIds.get(0))
                .getEndingInstruction()
                .map(ForInstruction.class::cast)
                .get();
        if (kernelSettings.schedule == ScheduleStrategy.AUTO) {
            if (codeGenerationStrategyProvider.getTryUseScheduleCooperative()) {
                log("Determining whether to use schedule(cooperative)");

                List<Integer> nonOneIndices = ParallelUtils.getNonOneIndices(containerInstance::getVariableType,
                        kernelSettings.localSizes);
                int sharedLocalIndex = ParallelUtils.getSharedLocalIndex(nonOneIndices);

                ParallelRegionSettings directSettings = kernelSettings.copy();
                directSettings.schedule = ScheduleStrategy.DIRECT;
                Set<String> blackList = new CoalescedAccessPredictor().buildBlacklist(
                        parallelInstance.getBody(),
                        parallelInstance.getTypeGetter(),
                        parallelLoop,
                        directSettings,
                        new MultiMap<>(),
                        xfor.getLoopBlock(),
                        sharedLocalIndex,
                        nonOneIndices);

                if (blackList.isEmpty()) {
                    log("No point in using schedule(cooperative), as all accesses are coalesced anyway");
                } else {
                    log("Found non-coalesced accesses");

                    ParallelRegionSettings modifiedSettings = kernelSettings.copy();
                    modifiedSettings.schedule = ScheduleStrategy.COOPERATIVE;

                    Set<String> modifiedBlacklist = new CoalescedAccessPredictor().buildBlacklist(
                            parallelInstance.getBody(),
                            parallelInstance.getTypeGetter(),
                            parallelLoop,
                            modifiedSettings,
                            localReductions,
                            xfor.getLoopBlock(),
                            sharedLocalIndex,
                            nonOneIndices);
                    if (modifiedBlacklist.isEmpty()) {
                        // Those accesses become coalesced
                        log("Using schedule(cooperative) would make those accesses coalesced");

                        if (allNonTrivialWorkIsDistributed(parallelInstance, xfor.getLoopBlock(), localReductions)) {
                            if (codeGenerationStrategyProvider.getPreferSubGroupCooperativeSchedule()) {
                                log("Using schedule(subgroup_cooperative)");
                                kernelSettings.schedule = ScheduleStrategy.SUBGROUP_COOPERATIVE;
                            } else {
                                log("Using schedule(cooperative)");
                                kernelSettings.schedule = ScheduleStrategy.COOPERATIVE;
                            }
                        } else {
                            log("Not using schedule(cooperative), as some non-trivial work would be duplicated");
                        }
                    } else {
                        log("Using schedule(cooperative) wouldn't help, as accesses would remain non-coalesced: "
                                + modifiedBlacklist);
                    }
                }
            }
        }

        if (kernelSettings.schedule != ScheduleStrategy.COOPERATIVE &&
                kernelSettings.schedule != ScheduleStrategy.SUBGROUP_COOPERATIVE) {
            localReductions.clear();
        }

        if (kernelSettings.schedule == ScheduleStrategy.AUTO) {
            // For prediction purposes, pretend we're using direct mode.
            ParallelRegionSettings predictorSettings = kernelSettings.copy();
            predictorSettings.schedule = ScheduleStrategy.DIRECT;
            predictorSettings.scheduleNames = new ArrayList<>();
            addDefaultLocalSizes(parallelInstance, parallelLoop, makeTemporary, providerData, block, predictorSettings);

            int blockId = xfor.getLoopBlock();

            ScheduleDecisionTree scheduleDecisionTree = codeGenerationStrategyProvider.getScheduleDecisionTree();
            SchedulePredictorContext context = new SchedulePredictorContext();
            context.providerData = providerData;
            context.body = parallelInstance.getBody();
            context.parallelLoop = parallelLoop;
            context.settings = predictorSettings;
            context.typeGetter = parallelInstance.getTypeGetter();
            context.localReductions = localReductions;
            context.blockId = blockId;
            context.wideScope = passData.get(PreTypeInferenceServices.WIDE_SCOPE);
            context.typedInstanceProvider = passData.get(ProjectPassServices.TYPED_INSTANCE_PROVIDER);

            ScheduleMethod method = scheduleDecisionTree.decide(context, new ScheduleRuleChecker());
            assert method.getSchedule() != ScheduleStrategy.AUTO;

            kernelSettings.schedule = method.getSchedule();
            kernelSettings.scheduleNames = new ArrayList<>();
            for (int size : method.getParameters()) {
                VariableType distributionParameterType = providerData.getNumerics().newInt(size);
                String var = makeTemporary.apply("distribution_parameter", distributionParameterType);
                parallelInstance.addType(var, distributionParameterType);

                int insertionPoint = BlockUtils.getAfterPhiInsertionPoint(block);
                block.insertInstruction(insertionPoint, AssignmentInstruction.fromInteger(var, size));
                kernelSettings.scheduleNames.add(var);
            }

            if (kernelSettings.schedule != ScheduleStrategy.COOPERATIVE) {
                localReductions.clear();
            }
        }

        if (kernelSettings.schedule != ScheduleStrategy.DIRECT) {
            for (int i = kernelSettings.scheduleNames.size(); i < parallelLoop.loopDeclarationBlockIds
                    .size(); ++i) {
                log("Adding extra schedule parameter with default value 1");

                VariableType type = providerData.getNumerics().newInt(1);
                String artificialName = makeTemporary.apply("extra_parameter",
                        type);
                parallelInstance.addType(artificialName, type);

                int insertionPoint = BlockUtils.getAfterPhiInsertionPoint(block);
                block.insertInstruction(insertionPoint, AssignmentInstruction.fromInteger(artificialName, 1));
                if (kernelSettings.schedule.isPrefixParameterType()) {
                    kernelSettings.scheduleNames.add(0, artificialName);
                } else {
                    kernelSettings.scheduleNames.add(artificialName);
                }
            }
        }
        addDefaultLocalSizes(parallelInstance, parallelLoop, makeTemporary, providerData, block, kernelSettings);

        System.out.println("LR=" + localReductions);
        GeneratedKernel kernel = KernelCodeGenerator.buildImplementation(
                codeGenerationStrategyProvider,
                parallelInstance,
                parallelLoop,
                new EfficientVariableAllocator(),
                kernelSettings,
                localReductions,
                passData,
                defaultReportService.withLineNumber(line),
                kernelSink.generateNextId());

        kernelSink.addKernel(kernel);

        return Optional.of(kernel);
    }

    private void addDefaultLocalSizes(ParallelRegionInstance parallelInstance,
            ParallelLoopInformation parallelLoop,
            BiFunction<String, VariableType, String> makeTemporary,
            ProviderData providerData,
            SsaBlock block,
            ParallelRegionSettings kernelSettings) {
        for (int i = kernelSettings.localSizes.size(); i < parallelLoop.loopDeclarationBlockIds.size(); ++i) {
            int localSize = i == 0 ? 128 : 1;

            log("Adding extra local size parameter with default value");

            ScalarType type = providerData.getNumerics().newLong();
            type = type.scalar().setConstant(localSize);

            String workgroupSize = makeTemporary.apply("workgroup_size", type);
            parallelInstance.addType(workgroupSize, type);

            int insertionPoint = BlockUtils.getAfterPhiInsertionPoint(block);
            block.insertInstruction(insertionPoint, AssignmentInstruction.fromInteger(workgroupSize, localSize));
            if (kernelSettings.schedule.isPrefixLocalSize()) {
                kernelSettings.localSizes.add(0, workgroupSize);
            } else {
                kernelSettings.localSizes.add(workgroupSize);
            }
        }

    }

    private boolean allNonTrivialWorkIsDistributed(ParallelRegionInstance parallelInstance,
            int blockId,
            MultiMap<Integer, Reduction> localReductions) {

        List<SsaBlock> blocks = parallelInstance.getBody().getBlocks();
        SsaBlock block = blocks.get(blockId);
        for (SsaInstruction instruction : block.getInstructions()) {
            if (instruction.getInstructionType() == InstructionType.DECORATOR ||
                    instruction.getInstructionType() == InstructionType.LINE) {
                continue;
            }
            if (instruction instanceof IndexedInstruction) {
                continue;
            }
            if (instruction instanceof IterInstruction) {
                continue;
            }
            if (instruction instanceof AssignmentInstruction) {
                continue;
            }
            if (instruction instanceof PhiInstruction) {
                continue;
            }
            if (instruction instanceof ForInstruction) {
                ForInstruction xfor = (ForInstruction) instruction;
                int loopBlockId = xfor.getLoopBlock();

                if (localReductions.containsKey(blockId)) {
                    return allNonTrivialWorkIsDistributed(parallelInstance, loopBlockId, localReductions) &&
                            allNonTrivialWorkIsDistributed(parallelInstance, xfor.getEndBlock(), localReductions);
                }
            }
            if (instruction instanceof FunctionCallInstruction) {
                FunctionCallInstruction functionCall = (FunctionCallInstruction) instruction;

                String functionName = functionCall.getFunctionName();
                switch (functionName) {
                case "minus":
                case "mtimes":
                case "numel":
                case "plus":
                case "times":
                    continue;
                }
            }

            log("Bad: " + instruction);
            return false;
        }

        return true;
    }

    private static void log(String message) {
        if (ENABLE_LOG) {
            System.out.print("[kernel_builder] ");
            System.out.println(message);
        }
    }
}
