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

package org.specs.matlabtocl.v2.ssa.passes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.ConventionalLoopVariableAnalysis;
import org.specs.matisselib.helpers.LoopVariable;
import org.specs.matisselib.helpers.UsageUtils;
import org.specs.matisselib.passes.TypeTransparentSsaPass;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.unssa.ControlFlowGraph;
import org.specs.matlabtocl.v2.ssa.instructions.CompleteReductionInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.CopyToGpuInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Removes useless copy_to_gpu from complete_reduction MATRIX_SET results. Instead of copy_to_gpu, directly use the GPU
 * buffer from complete_reduction
 * 
 * @author Luís Reis
 *
 */
public class SetReductionBufferOptimizerPass extends TypeTransparentSsaPass {

    private static final boolean ENABLE_LOG = false;

    @Override
    public void apply(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            DataStore passData) {

        log("Starting");

        ControlFlowGraph cfg = passData
                .get(ProjectPassServices.DATA_PROVIDER)
                .buildData(CompilerDataProviders.CONTROL_FLOW_GRAPH);
        Map<String, String> completedSetReductions = new HashMap<>();

        for (SsaInstruction instruction : body.getFlattenedInstructionsIterable()) {
            if (instruction instanceof CompleteReductionInstruction) {
                CompleteReductionInstruction reduction = (CompleteReductionInstruction) instruction;

                completedSetReductions.put(reduction.getOutput(), reduction.getBuffer());
            }
        }

        for (;;) {
            boolean optimized = optimizeSequential(body, cfg, completedSetReductions);
            optimized |= optimizeLoop(body, typeGetter, cfg, completedSetReductions);

            if (!optimized) {
                break;
            }
        }
    }

    private boolean optimizeLoop(FunctionBody source,
            Function<String, Optional<VariableType>> typeGetter,
            ControlFlowGraph cfg,
            Map<String, String> completedSetReductions) {

        for (int blockId = 0; blockId < source.getBlocks().size(); ++blockId) {
            SsaBlock block = source.getBlock(blockId);
            Optional<ForInstruction> maybeFor = block.getEndingInstruction()
                    .filter(ForInstruction.class::isInstance)
                    .map(ForInstruction.class::cast);
            if (!maybeFor.isPresent()) {
                continue;
            }
            ForInstruction xfor = maybeFor.get();

            List<LoopVariable> loopVariables = ConventionalLoopVariableAnalysis.analyzeStandardLoop(source, typeGetter,
                    blockId, true);
            int activeBlockId = xfor.getLoopBlock();
            for (int instructionId = 0; instructionId < source.getBlock(activeBlockId).getInstructions()
                    .size(); instructionId++) {
                SsaInstruction instruction = source.getBlock(activeBlockId).getInstructions().get(instructionId);

                if (instruction instanceof BranchInstruction) {
                    BranchInstruction branch = (BranchInstruction) instruction;

                    activeBlockId = branch.getEndBlock();
                    instructionId = -1;
                    continue;
                }

                if (instruction instanceof ForInstruction) {
                    // Handle inner loops.
                    ForInstruction innerFor = (ForInstruction) instruction;

                    activeBlockId = innerFor.getEndBlock();
                    instructionId = -1;
                    continue;
                }

                if (instruction instanceof CopyToGpuInstruction) {
                    log("Found copy to gpu instruction");
                    CopyToGpuInstruction copy = (CopyToGpuInstruction) instruction;
                    String matrix = copy.getInput();
                    String buffer = copy.getOutput();

                    LoopVariable loopVariable = loopVariables
                            .stream()
                            .filter(v -> v.loopStart.equals(matrix))
                            .findFirst()
                            .orElse(null);
                    if (loopVariable == null) {
                        log("No matching loop variable for " + matrix);
                        continue;
                    }

                    String sourceBuffer = completedSetReductions.get(loopVariable.beforeLoop);
                    if (sourceBuffer == null) {
                        log("No source buffer for " + loopVariable.beforeLoop);
                        continue;
                    }

                    if (UsageUtils.isVariableUsedAfter(source, cfg,
                            new InstructionLocation(activeBlockId, instructionId),
                            sourceBuffer)) {

                        log("Variable " + source + " is reused");
                        continue;
                    }

                    if (!buffer.equals(completedSetReductions.get(loopVariable.loopEnd))) {
                        log("Buffer does not match ending loop");
                        continue;
                    }

                    log("Optimizing: " + buffer + "->" + sourceBuffer);

                    // Remove copy_to_gpu
                    source.getBlock(activeBlockId).removeInstructionAt(instructionId);
                    --instructionId;

                    if (loopVariable.getAfterLoop().isPresent()) {
                        completedSetReductions.put(loopVariable.getAfterLoop().get(), buffer);
                    }

                    // Change names of buffers
                    Map<String, String> newNames = new HashMap<>();
                    newNames.put(buffer, sourceBuffer);
                    renameVariables(source, completedSetReductions, newNames);

                    return true;
                }
            }
        }

        return false;
    }

    private void renameVariables(FunctionBody source,
            Map<String, String> completedSetReductions,
            Map<String, String> newNames) {

        source.renameVariables(newNames);

        for (String key : completedSetReductions.keySet()) {
            String oldValue = completedSetReductions.get(key);
            String newValue = newNames.getOrDefault(oldValue, oldValue);
            completedSetReductions.put(key, newValue);
        }
    }

    private boolean optimizeSequential(FunctionBody source, ControlFlowGraph cfg,
            Map<String, String> completedSetReductions) {

        boolean optimized = false;

        // Deal with sequential transfers
        for (int blockId = 0; blockId < source.getBlocks().size(); ++blockId) {
            SsaBlock block = source.getBlock(blockId);
            for (int instructionId = 0; instructionId < block.getInstructions().size(); ++instructionId) {
                SsaInstruction instruction = block.getInstructions().get(instructionId);

                if (instruction instanceof CopyToGpuInstruction) {
                    CopyToGpuInstruction copy = (CopyToGpuInstruction) instruction;
                    String matrix = copy.getInput();
                    String buffer = copy.getOutput();

                    String sourceBuffer = completedSetReductions.get(matrix);
                    if (sourceBuffer == null) {
                        continue;
                    }

                    if (UsageUtils.isVariableUsedAfter(source, cfg,
                            new InstructionLocation(blockId, instructionId),
                            sourceBuffer)) {

                        continue;
                    }

                    optimized = true;

                    // Remove copy_to_gpu
                    block.removeInstructionAt(instructionId);
                    --instructionId;

                    // Change names of buffers
                    Map<String, String> newNames = new HashMap<>();
                    newNames.put(buffer, sourceBuffer);
                    renameVariables(source, completedSetReductions, newNames);
                }
            }
        }

        return optimized;
    }

    public static void log(String message) {
        if (ENABLE_LOG) {
            System.out.print("[set_reduction_buffer_optimizer] ");
            System.out.println(message);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }

}
