package org.specs.matisselib.passes.posttype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.helpers.ConventionalLoopVariableAnalysis;
import org.specs.matisselib.helpers.LoopVariable;
import org.specs.matisselib.helpers.UsageMap;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class LoopMatrixCopyEliminationPass implements PostTypeInferencePass {

    public static final String PASS_NAME = "loop_matrix_copy_elimination";

    @Override
    public void apply(TypedInstance instance,
            DataStore passData) {

        Logger logger = PassUtils.getLogger(passData, PASS_NAME);

        if (PassUtils.skipPass(instance, PASS_NAME)) {
            logger.log("Skipping");
            return;
        }

        logger.log("Starting");

        DataProviderService dataProvider = passData.get(ProjectPassServices.DATA_PROVIDER);
        SizeGroupInformation sizes = dataProvider.buildData(CompilerDataProviders.SIZE_GROUP_INFORMATION);

        boolean invalidateSizes = false;

        UsageMap usages = UsageMap.build(instance.getFunctionBody());

        Set<String> arguments = instance.getFlattenedInstructionsOfTypeStream(ArgumentInstruction.class)
                .filter(arg -> !instance.getFunctionType().isInputReference(arg.getArgumentIndex()))
                .map(arg -> arg.getOutput())
                .collect(Collectors.toSet());

        Map<String, String> sizeMatrices = new HashMap<>();
        getValidSizeMatrices(instance, sizeMatrices);

        Map<String, String> derivedMatrices = new HashMap<>();
        Set<String> usedSources = new HashSet<>();

        List<SsaBlock> blocks = instance.getBlocks();
        for (int blockId = 0; blockId < blocks.size(); ++blockId) {
            SsaBlock block = blocks.get(blockId);

            Optional<ForInstruction> forEnd = block.getEndingInstruction()
                    .filter(ForInstruction.class::isInstance)
                    .map(ForInstruction.class::cast);

            if (!forEnd.isPresent()) {
                continue;
            }

            ForInstruction xfor = forEnd.get();
            if (!ConstantUtils.isConstantOne(instance.getVariableType(xfor.getStart()))) {
                logger.log("Loop does not start in 1");
                continue;
            }
            if (!ConstantUtils.isConstantOne(instance.getVariableType(xfor.getInterval()))) {
                logger.log("Loop does not have an interval of 1");
                continue;
            }

            SsaBlock loopBlock = instance.getBlock(xfor.getLoopBlock());
            if (loopBlock.getEndingInstruction().isPresent()) {
                continue;
            }

            for (SsaInstruction instruction : block.getInstructions()) {
                if (instruction instanceof FunctionCallInstruction) {
                    FunctionCallInstruction functionCall = (FunctionCallInstruction) instruction;

                    if (functionCall.getFunctionName().equals("matisse_new_array") &&
                            functionCall.getInputVariables().size() == 1 &&
                            functionCall.getOutputs().size() == 1) {

                        String input = functionCall.getInputVariables().get(0);
                        String output = functionCall.getOutputs().get(0);

                        String source = sizeMatrices.get(input);
                        if (isValidUsage(instance, arguments, source, output)) {
                            derivedMatrices.put(output, source);
                        }
                    } else if (functionCall.getFunctionName().equals("matisse_new_array_from_matrix") &&
                            functionCall.getInputVariables().size() == 1 &&
                            functionCall.getOutputs().size() == 1) {

                        String source = functionCall.getInputVariables().get(0);
                        String output = functionCall.getOutputs().get(0);
                        if (isValidUsage(instance, arguments, source, output)) {
                            derivedMatrices.put(output, source);
                        }
                    }
                }
            }

            if (derivedMatrices.isEmpty()) {
                continue;
            }

            // At this point we have, for the current block (for loop prefix):
            // A list of all "derived matrices".
            // X is "derived from" Y if X is allocated with uninitialized values with the same shape as Y.

            logger.log("Candidates: " + derivedMatrices);

            List<LoopVariable> loopVariables = ConventionalLoopVariableAnalysis.analyzeStandardLoop(instance, blockId,
                    true);
            logger.log("Loop Variables: " + loopVariables);

            for (LoopVariable loopVariable : new ArrayList<>(loopVariables)) {
                Set<SsaInstruction> extraUsages = new HashSet<>();
                int extraBeforeLoopUsages = 0;
                for (SsaInstruction instruction : block.getInstructions()) {
                    if (instruction instanceof PhiInstruction) {
                        continue;
                    }
                    for (String input : instruction.getInputVariables()) {
                        if (input.equals(loopVariable.beforeLoop)) {
                            extraUsages.add(instruction);
                            ++extraBeforeLoopUsages;
                        }
                    }
                }

                if (usages.getUsageCount(loopVariable.beforeLoop) != 2 + extraBeforeLoopUsages
                        || usages.getUsageCount(loopVariable.loopEnd) != 2
                        || usages.getUsageCount(loopVariable.loopStart) != 1) {

                    logger.log("Removing " + loopVariable + " due to incorrect number of usages.");
                    continue;
                }

                Map<String, String> sets = new HashMap<>();
                Set<String> illegalMatrices = new HashSet<>();

                String iter = null;
                for (SsaInstruction instruction : loopBlock.getInstructions()) {
                    if (instruction instanceof IterInstruction) {
                        iter = ((IterInstruction) instruction).getOutput();
                        continue;
                    }
                    if (instruction instanceof SimpleGetInstruction) {
                        SimpleGetInstruction get = (SimpleGetInstruction) instruction;

                        if (get.getIndices().equals(Arrays.asList(iter))) {
                            continue;
                        }

                        // Fallthrough
                    }

                    illegalMatrices.addAll(instruction.getInputVariables());
                }

                for (SsaInstruction instruction : loopBlock.getInstructions()) {
                    if (instruction instanceof SimpleSetInstruction) {
                        SimpleSetInstruction simpleSet = (SimpleSetInstruction) instruction;

                        if (simpleSet.getIndices().equals(Arrays.asList(iter))) {
                            String output = simpleSet.getOutput();
                            String inputMatrix = simpleSet.getInputMatrix();

                            assert output != null;
                            assert inputMatrix != null;
                            sets.put(output, inputMatrix);
                        }
                        continue;
                    }
                }

                Map<String, String> newNames = new HashMap<>();

                logger.log("Finished analysing block");
                if (!sets.containsKey(loopVariable.loopEnd)) {
                    continue;
                }
                if (!sets.get(loopVariable.loopEnd).equals(loopVariable.loopStart)) {
                    continue;
                }
                if (!derivedMatrices.containsKey(loopVariable.beforeLoop)) {
                    logger.log(loopVariable.beforeLoop + " not built in recognizable manner");
                    continue;
                }

                String source = derivedMatrices.get(loopVariable.beforeLoop);
                if (usedSources.contains(source)) {
                    logger.log(source + " already used for different loop variable.");
                    continue;
                }

                // The loop must cover the full range of source
                if (!sizes.areSameValue(sizes.getNumelResult(source), xfor.getEnd())) {
                    continue;
                }

                // Rename beforeLoop to source
                // However, we do not want to apply the rename to the original declaration
                // (otherwise source would be declared twice)

                newNames.put(loopVariable.beforeLoop, source);

                logger.log("Performing rename of " + newNames);
                loopBlock.renameVariables(newNames);
                instance.getBlock(xfor.getEndBlock()).renameVariables(newNames);
                extraUsages.forEach(instruction -> instruction.renameVariables(newNames));

                invalidateSizes = true;
                usedSources.add(source);
            }

        }

        if (invalidateSizes) {
            dataProvider.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);
        }
    }

    private static boolean isValidUsage(TypedInstance instance,
            Set<String> arguments,
            String source,
            String output) {

        return source != null &&
                instance.getVariableType(source).equals(instance.getVariableType(output)) &&
                !output.endsWith("$ret") &&
                !arguments.contains(source);
    }

    private static void getValidSizeMatrices(TypedInstance body, Map<String, String> sizeMatrices) {
        for (SsaInstruction instruction : body.getFlattenedInstructionsIterable()) {
            if (instruction instanceof FunctionCallInstruction) {
                FunctionCallInstruction functionCall = (FunctionCallInstruction) instruction;

                if (functionCall.getFunctionName().equals("size") &&
                        functionCall.getInputVariables().size() == 1 &&
                        functionCall.getOutputs().size() == 1) {

                    String input = functionCall.getInputVariables().get(0);
                    String output = functionCall.getOutputs().get(0);

                    sizeMatrices.put(output, input);
                }
            }
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                // Explicitly invalidated
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
