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

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import org.specs.matisselib.PassUtils;
import org.specs.matisselib.passes.TypeNeutralSsaPass;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.LineInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.EndDirectiveInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.ParallelBlockInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.ParallelDirectiveInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class ParallelBlockBuilderPass extends TypeNeutralSsaPass {

    public static final String PASS_NAME = "parallel_block_builder";

    @Override
    public void apply(FunctionBody source, DataStore data) {
        Logger logger = PassUtils.getLogger(data, PASS_NAME);

        logger.log("Starting");

        List<SsaBlock> blocks = source.getBlocks();

        getFunctionWideDirective(source, blocks, logger).ifPresent(index -> {
            List<SsaInstruction> instructions = source.getBlock(0).getInstructions();
            ParallelDirectiveInstruction instruction = (ParallelDirectiveInstruction) instructions.get(index);

            logger.log("Removing parallel directive: " + instructions.get(index));
            instructions.remove((int) index);

            buildFunctionWideBlock(instruction, source, logger);
        });

        logger.log("Creating blocks for regions");
        while (buildRegionBlocks(source, logger)) {
        }
    }

    private static boolean buildRegionBlocks(FunctionBody source, Logger logger) {
        List<SsaBlock> blocks = source.getBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            SsaBlock block = blocks.get(i);
            List<SsaInstruction> instructions = block.getInstructions();
            for (int j = 0; j < instructions.size(); j++) {
                SsaInstruction instruction = instructions.get(j);

                if (instruction instanceof ParallelDirectiveInstruction) {
                    // Found parallel directive
                    logger.log("Found parallel directive instruction at #" + i + ":" + j);
                    createBlockStartingAt((ParallelDirectiveInstruction) instruction, source, i, j, logger);
                    return true;
                }
            }
        }

        return false;
    }

    private static void createBlockStartingAt(ParallelDirectiveInstruction start,
            FunctionBody source,
            int startBlockId,
            int startInstructionId,
            Logger logger) {
        SsaBlock startBlock = source.getBlock(startBlockId);

        int endBlockId = startBlockId;

        SsaBlock block = source.getBlock(endBlockId);
        for (int endInstructionId = startInstructionId; endInstructionId < block.getInstructions()
                .size(); ++endInstructionId) {
            SsaInstruction instruction = block.getInstructions().get(endInstructionId);

            if (instruction instanceof EndDirectiveInstruction) {
                logger.log("Found end directive. Block goes from #" + startBlockId + ":" + startInstructionId +
                        " to #" + endBlockId + ":" + endInstructionId);

                SsaBlock inParallelStartBlock = new SsaBlock();
                SsaBlock newEndBlock = new SsaBlock();
                int inParallelStartBlockId = source.addBlock(inParallelStartBlock);
                int newEndBlockId = source.addBlock(newEndBlock);

                source.breakBlock(startBlockId, startBlockId, inParallelStartBlockId);
                if (endBlockId == startBlockId) {
                    endBlockId = inParallelStartBlockId;
                }
                source.breakBlock(endBlockId, endBlockId, newEndBlockId);

                // [Start] - [In Parallel Start Block] - ... - [End] - [New End]

                startBlock.getInstructions().set(startInstructionId,
                        new ParallelBlockInstruction(start.getSettings(), inParallelStartBlockId, newEndBlockId));

                // Migrate instructions
                int lastStartBlockInstructionToMigrate;
                if (endBlockId == inParallelStartBlockId) {
                    lastStartBlockInstructionToMigrate = endInstructionId;
                } else {
                    lastStartBlockInstructionToMigrate = startBlock.getInstructions().size();
                }
                List<SsaInstruction> instructionsToMigrateIn = startBlock.getInstructions()
                        .subList(startInstructionId + 1, lastStartBlockInstructionToMigrate);
                inParallelStartBlock.addInstructions(instructionsToMigrateIn);
                List<SsaInstruction> instructionsToMigrateOut = block.getInstructions()
                        .subList(endInstructionId, block.getInstructions().size());
                newEndBlock.addInstructions(instructionsToMigrateOut);

                if (endBlockId == inParallelStartBlockId) {
                    block.getInstructions().subList(startInstructionId + 1, block.getInstructions().size()).clear();
                } else {
                    instructionsToMigrateIn.clear();
                    instructionsToMigrateOut.clear();
                }

                newEndBlock.getInstructions().remove(0);

                return;
            } else if (instruction.isEndingInstruction()) {
                endBlockId = instruction.tryGetEndBlock().get();
                block = source.getBlock(endBlockId);
                endInstructionId = 0;
            }
        }

        // TODO: Proper error messages
        throw new RuntimeException("Could not find matching end instruction");
    }

    private static void buildFunctionWideBlock(ParallelDirectiveInstruction start, FunctionBody source,
            Logger logger) {
        SsaBlock prefixBlock = new SsaBlock();
        int firstParallelBlock = source.addBlock(prefixBlock);
        int endBlockId = source.addBlock(new SsaBlock());

        source.renameBlocks(Arrays.asList(0), Arrays.asList(firstParallelBlock));

        SsaBlock entryBlock = source.getBlock(0);
        ListIterator<SsaInstruction> it = entryBlock.getInstructions().listIterator();

        boolean canMigrateInstructions = true;
        String reasonNotToMigrate = null;
        while (it.hasNext()) {
            SsaInstruction instruction = it.next();
            if (canMigrateInstructions
                    && (instruction.getInstructionType() == InstructionType.DECORATOR
                            || instruction.getInstructionType() == InstructionType.LINE)) {
                continue;
            }
            if (instruction instanceof ArgumentInstruction) {
                if (!canMigrateInstructions) {
                    logger.log("Error: Can't move argument instruction, due to " + reasonNotToMigrate);
                    System.err.println(source);
                    throw new RuntimeException();
                }
                continue;
            }

            if (canMigrateInstructions) {
                canMigrateInstructions = false;
                reasonNotToMigrate = instruction.toString();
            }

            if (!(instruction instanceof AssignmentInstruction) || !start.getSettings().getInputVariables()
                    .contains(((AssignmentInstruction) instruction).getOutput())) {
                it.remove();
                prefixBlock.addInstruction(instruction);
            }
        }

        logger.log("Adding function-wide block");
        entryBlock.addInstruction(new ParallelBlockInstruction(start.getSettings(), firstParallelBlock, endBlockId));
    }

    private static Optional<Integer> getFunctionWideDirective(
            FunctionBody source,
            List<SsaBlock> blocks,
            Logger logger) {
        // First, we'll search for function-wide directives
        SsaBlock entryBlock = blocks.get(0);
        int currentLine = -1;
        List<SsaInstruction> instructions = entryBlock.getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            SsaInstruction instruction = instructions.get(i);
            if (instruction instanceof LineInstruction) {
                currentLine = ((LineInstruction) instruction).getLine();
                continue;
            }

            if (!(instruction instanceof ParallelDirectiveInstruction)) {
                continue;
            }

            if (currentLine != -1 && currentLine < source.getFirstLine()) {
                logger.log("Found function-wide directive");
                return Optional.of(i);
            }
        }

        return Optional.empty();
    }

}
