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

package org.specs.matlabtocl.v2.ssa.passes;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.helpers.ConventionalLoopVariableAnalysis;
import org.specs.matisselib.helpers.LoopVariable;
import org.specs.matisselib.helpers.UsageMap;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.passes.ssa.DeadCodeEliminationPass;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.ssa.instructions.CompleteReductionInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * When we have:
 * 
 * <pre>
 * A = complete_reduction buffer, MATRIX_SET, initial
 * for
 *   A_loop = phi A, B
 *   ...
 *   B = complete_reduction buffer, MATRIX_SET, A, A_loop
 *   &lt; no `buffer` usages here &gt;
 * C = phi A, B
 * </pre>
 * 
 * Where A and B are not used outside of the phi instruction and the loop is "conventional", convert it into:
 * 
 * <pre>
 * A = complete_reduction buffer, MATRIX_SET, initial
 * for
 *   A_loop = phi A, B
 *   ...
 *   B = complete_reduction buffer, MATRIX_SET, A_loop
 * end
 * C = complete_reduction buffer, MATRIX_SET, initial
 * </pre>
 * 
 * So that {@link DeadCodeEliminationPass} can erase A, A_loop and B.
 * 
 * @author Luís Reis
 *
 */
public class DelayReductionCopyOptimizationPass implements PostTypeInferencePass {

    private static final boolean ENABLE_LOG = false;

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        log("Starting " + instance.getFunctionIdentification().getName());

        SizeGroupInformation sizeGroupInfo = PassUtils.getData(passData, CompilerDataProviders.SIZE_GROUP_INFORMATION);

        Map<String, CompleteReductionInstruction> completeReductions = new HashMap<>();
        UsageMap usages = UsageMap.build(instance.getFunctionBody());

        // No point in using the full-blown size group information when something this simple will do.
        Map<String, String> aliasedSizes = new HashMap<>();

        for (CompleteReductionInstruction instruction : instance
                .getFlattenedInstructionsOfTypeIterable(CompleteReductionInstruction.class)) {

            if (instruction.getReductionType() != ReductionType.MATRIX_SET) {
                continue;
            }

            String output = instruction.getOutput();

            int outputUses = usages.getUsageCount(output);
            if (outputUses != 2) {
                log("Excluding " + instruction + ": output not used twice: "
                        + usages.getUsageCount(output) + " (output uses: " + outputUses + ")");
                continue;
            }

            log("Found reduction: " + instruction);
            aliasedSizes.put(output, instruction.getInitialValue());
            completeReductions.put(output, instruction);
        }

        List<SsaBlock> blocks = instance.getBlocks();
        for (int blockId = 0; blockId < blocks.size(); blockId++) {
            SsaBlock block = blocks.get(blockId);

            Optional<ForInstruction> xfor = block.getEndingInstruction()
                    .filter(ForInstruction.class::isInstance)
                    .map(ForInstruction.class::cast);
            if (xfor.isPresent()) {
                List<LoopVariable> loopVariables = ConventionalLoopVariableAnalysis.analyzeStandardLoop(instance,
                        blockId, true);

                for (LoopVariable loopVariable : loopVariables) {
                    CompleteReductionInstruction startReduction = completeReductions.get(loopVariable.beforeLoop);
                    if (startReduction == null) {
                        log("No start reduction for " + loopVariable.beforeLoop);
                        continue;
                    }

                    CompleteReductionInstruction endReduction = completeReductions.get(loopVariable.loopEnd);
                    if (endReduction == null) {
                        log("No end reduction");
                        continue;
                    }

                    // We only care that the shapes are the same, the exact values do not matter.
                    String initialValue = endReduction.getInitialValue();
                    if (!sizeGroupInfo.areSameSize(initialValue, loopVariable.loopStart)) {
                        log("Initial size mismatch for " + loopVariable.beforeLoop + " vs " + loopVariable.loopStart);
                        continue;
                    }

                    String buffer = endReduction.getBuffer();
                    if (!buffer.equals(startReduction.getBuffer())) {
                        log("Buffer mismatch");
                        continue;
                    }

                    if (isVariableUsedAfter(instance, xfor.get().getLoopBlock(), endReduction, buffer)) {
                        continue;
                    }

                    if (loopVariable.getAfterLoop().isPresent()) {
                        String var = loopVariable.getAfterLoop().get();
                        log("Optimize var");

                        CompleteReductionInstruction reduction = optimize(instance, xfor.get().getEndBlock(), var,
                                startReduction);
                        completeReductions.put(var, reduction);
                    }
                }
            }
        }
    }

    private CompleteReductionInstruction optimize(TypedInstance instance,
            int endBlockId,
            String var,
            CompleteReductionInstruction startReduction) {

        SsaBlock endBlock = instance.getBlock(endBlockId);
        for (ListIterator<SsaInstruction> iterator = endBlock.getInstructions().listIterator(); iterator.hasNext();) {
            SsaInstruction instruction = iterator.next();
            if (instruction instanceof PhiInstruction) {
                PhiInstruction phi = (PhiInstruction) instruction;

                if (phi.getOutput().equals(var)) {
                    log("Removing " + phi);
                    iterator.remove();

                    break;
                }
            }
        }

        int insertionPoint = BlockUtils.getAfterPhiInsertionPoint(endBlock);
        CompleteReductionInstruction instruction = new CompleteReductionInstruction(var, ReductionType.MATRIX_SET,
                startReduction.getBuffer(),
                startReduction.getUnderlyingType(), startReduction.getNumGroups(),
                startReduction.getInitialValue());
        endBlock.insertInstruction(insertionPoint, instruction);

        return instruction;
    }

    private boolean isVariableUsedAfter(TypedInstance instance,
            int loopBlockId,
            CompleteReductionInstruction endReduction,
            String buffer) {

        List<SsaInstruction> instructions = instance.getBlock(loopBlockId).getInstructions();
        for (int instructionId = 0; instructionId < instructions.size(); instructionId++) {
            SsaInstruction instruction = instructions.get(instructionId);
            if (instruction == endReduction) {
                return isVariableUsedIn(instance, loopBlockId, instructionId + 1, buffer);
            }

            Optional<Integer> maybeEndBlock = instruction.tryGetEndBlock();
            if (maybeEndBlock.isPresent()) {
                instructionId = 0;
                loopBlockId = maybeEndBlock.get();
                instructions = instance.getBlock(loopBlockId).getInstructions();
            }
        }

        log("Don't know how to deal with this case.");
        return true; // Conservative value
    }

    private boolean isVariableUsedIn(TypedInstance instance, int blockId, int startInstructionId, String var) {
        SsaBlock block = instance.getBlock(blockId);

        List<SsaInstruction> instructions = block.getInstructions();
        for (int instructionId = startInstructionId; instructionId < instructions.size(); instructionId++) {
            SsaInstruction instruction = instructions.get(instructionId);
            if (instruction.getInputVariables().contains(var)) {
                log("Used at " + instruction);
                return true;
            }

            for (int ownedBlock : instruction.getOwnedBlocks()) {
                if (isVariableUsedIn(instance, ownedBlock, 0, var)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void log(String message) {
        if (ENABLE_LOG) {
            System.out.print("[delay_reduction_copy] ");
            System.out.println(message);
        }
    }

}
