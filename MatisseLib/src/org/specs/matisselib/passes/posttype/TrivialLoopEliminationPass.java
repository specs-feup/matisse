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

package org.specs.matisselib.passes.posttype;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.BreakInstruction;
import org.specs.matisselib.ssa.instructions.ContinueInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class TrivialLoopEliminationPass extends SizeAwareInstructionRemovalPass<ForInstruction> {

    private static final boolean ENABLE_LOG = false;

    public TrivialLoopEliminationPass() {
        super(ForInstruction.class);
    }

    @Override
    protected boolean canEliminate(FunctionBody body,
            ForInstruction instruction,
            Function<String, Optional<VariableType>> typeGetter,
            SizeGroupInformation sizes) {

        log("At " + body.getName() + ", checking " + instruction);

        String start = instruction.getStart();
        String interval = instruction.getInterval();
        String end = instruction.getEnd();

        if (sizes.isEmptyRange(start, interval, end)) {
            log("Is empty range");
            return true;
        }

        if (sizes.isSingleIteration(start, interval, end)) {
            if (isSimpleLoop(body, instruction.getLoopBlock())) {
                log("Simple loop");
                return true;
            }
            log("Not a simple loop");
            return false;
        }
        log("Not a single iteration");
        return false;
    }

    private boolean isSimpleLoop(FunctionBody body, int blockId) {
        SsaBlock block = body.getBlock(blockId);
        for (SsaInstruction instruction : block.getInstructions()) {
            if (instruction instanceof BreakInstruction || instruction instanceof ContinueInstruction) {
                return false;
            }

            for (int ownedId : instruction.getOwnedBlocks()) {
                if (!isSimpleLoop(body, ownedId)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    protected void removeInstruction(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            SsaBlock block,
            int blockId,
            int instructionId,
            ForInstruction instruction,
            SizeGroupInformation sizes,
            DataStore passData) {

        String start = instruction.getStart();
        String interval = instruction.getInterval();
        String end = instruction.getEnd();

        log("Eliminating for: " + instruction);

        boolean singleIter = sizes.isSingleIteration(start, interval, end);
        if (singleIter) {
            log("Reason: Single iteration");
            removeSingleIter(body, block, blockId, instructionId, instruction, typeGetter);
        } else {
            log("Reason: Zero iterations");
            removeEmptyFor(body, block, blockId, instructionId, instruction);
        }
    }

    private static void removeSingleIter(FunctionBody body,
            SsaBlock block,
            int blockId,
            int instructionId,
            ForInstruction xfor,
            Function<String, Optional<VariableType>> typeGetter) {

        // Remove original for
        block.removeInstructionAt(instructionId);

        int forContent = xfor.getLoopBlock();
        int afterFor = xfor.getEndBlock();

        Map<String, String> renames = new HashMap<>();

        for (SsaInstruction instruction : body.getBlock(forContent).getInstructions()) {
            if (instruction instanceof PhiInstruction) {
                PhiInstruction phi = (PhiInstruction) instruction;
                int varIndex = phi.getSourceBlocks().indexOf(blockId);
                assert varIndex >= 0 : "Malformed phi node: " + phi;

                block.addAssignment(phi.getOutput(), phi.getInputVariables().get(varIndex));
                continue;
            }
            if (instruction instanceof IterInstruction) {
                String iter = instruction.getOutputs().get(0);
                String start = xfor.getStart();

                if (typeGetter.apply(iter).get().equals(typeGetter.apply(start).get())) {
                    // Loose equals, not strict equals
                    renames.put(iter, start);
                } else {
                    // Different types, we'll want to preserve whichever casts are needed.
                    block.addAssignment(iter, start);
                }
                continue;
            }
            block.addInstruction(instruction);
        }
        int forEndId = BlockUtils.getBlockEnd(body, forContent);
        int endInsertionId;
        if (forEndId == forContent) {
            endInsertionId = blockId;
        } else {
            endInsertionId = forEndId;
        }
        SsaBlock endInsertionBlock = body.getBlock(endInsertionId);

        for (SsaInstruction instruction : body.getBlock(afterFor).getInstructions()) {
            if (instruction instanceof PhiInstruction) {
                PhiInstruction phi = (PhiInstruction) instruction;
                int varIndex = phi.getSourceBlocks().indexOf(forEndId);
                assert varIndex >= 0 : "Malformed phi node: " + phi;

                endInsertionBlock.addAssignment(phi.getOutput(), phi.getInputVariables().get(varIndex));
                continue;
            }
            endInsertionBlock.addInstruction(instruction);
        }

        // Remove instructions, so they don't appear multiple times.
        body.getBlock(forContent).getInstructions().clear();
        body.getBlock(afterFor).getInstructions().clear();

        body.renameBlocks(Arrays.asList(afterFor, forContent), Arrays.asList(endInsertionId, blockId));
        body.renameVariables(renames);
    }

    private static void removeEmptyFor(
            FunctionBody body,
            SsaBlock block,
            int blockId,
            int instructionId,
            ForInstruction xfor) {

        // Remove original for
        block.removeInstructionAt(instructionId);

        // Move instructions from end of block to end of current block
        int targetBlock = xfor.getEndBlock();
        for (SsaInstruction instruction : body.getBlock(targetBlock).getInstructions()) {
            if (instruction instanceof PhiInstruction) {
                PhiInstruction phi = (PhiInstruction) instruction;
                int varIndex = phi.getSourceBlocks().indexOf(blockId);
                assert varIndex >= 0 : "Malformed phi node: " + phi;

                block.addAssignment(phi.getOutput(), phi.getInputVariables().get(varIndex));
                continue;
            }
            block.addInstruction(instruction);
        }

        // Remove instructions, so they don't appear multiple times.
        body.getBlock(targetBlock).getInstructions().clear();

        body.renameBlocks(Arrays.asList(targetBlock), Arrays.asList(blockId));
    }

    @Override
    protected void afterPass(FunctionBody body, DataStore passData, boolean performedElimination) {

        if (performedElimination) {
            DataProviderService dataProvider = passData.get(ProjectPassServices.DATA_PROVIDER);

            dataProvider.invalidate(CompilerDataProviders.CONTROL_FLOW_GRAPH);
            dataProvider.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);
        }
    }

    private static void log(String message) {
        if (TrivialLoopEliminationPass.ENABLE_LOG) {
            System.out.print("[trivial_loop_elimination] ");
            System.out.println(message);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH, // Explicitly invalidated
                CompilerDataProviders.SIZE_GROUP_INFORMATION); // Explicitly invalidated
    }

}
