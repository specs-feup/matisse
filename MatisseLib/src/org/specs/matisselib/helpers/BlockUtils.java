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

package org.specs.matisselib.helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.BreakInstruction;
import org.specs.matisselib.ssa.instructions.ContinueInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matisselib.unssa.ControlFlowGraph;

import com.google.common.base.Preconditions;

public class BlockUtils {
    private BlockUtils() {
    }

    public static int getBlockEnd(FunctionBody functionBody, int blockId) {
        return functionBody
                .getBlock(blockId)
                .getEndingInstruction()
                .flatMap(end -> end.tryGetEndBlock())
                .map(end -> getBlockEnd(functionBody, end))
                .orElse(blockId);
    }

    public static boolean hasNestedSideEffects(TypedInstance instance, int blockId) {
        return hasNestedSideEffects(instance.getFunctionBody(), blockId);
    }

    public static boolean hasNestedSideEffects(FunctionBody functionBody, int blockId) {
        Set<Integer> visitedBlocks = new HashSet<>();
        Queue<Integer> pendingBlocks = new LinkedList<>();

        pendingBlocks.add(blockId);

        while (!pendingBlocks.isEmpty()) {
            int currentBlockId = pendingBlocks.poll();

            boolean changed = visitedBlocks.add(currentBlockId);
            assert changed : "Block ownership semantics violation. Block " + currentBlockId
                    + " owned by multiple other blocks, at:\n" + functionBody;

            SsaBlock block = functionBody.getBlock(currentBlockId);
            if (block.hasSideEffects()) {
                return true;
            }

            Optional<SsaInstruction> end = block.getEndingInstruction();
            if (end.isPresent()) {
                pendingBlocks.addAll(end.get().getOwnedBlocks());
            }
        }

        return false;
    }

    public static boolean isSimpleSection(TypedInstance instance, int blockId) {
        return isSimpleSection(instance.getFunctionBody(), blockId);
    }

    /**
     * A group of blocks starting at a given block ID is simple if it has no nested breaks or continues.
     * 
     * @param functionBody
     *            The body of the function to check
     * @param blockId
     *            The ID to start the analysis at.
     * @return Whether the section is simple
     */
    public static boolean isSimpleSection(FunctionBody functionBody, int blockId) {
        for (SsaInstruction instruction : functionBody.getBlock(blockId).getInstructions()) {
            if (instruction instanceof BreakInstruction || instruction instanceof ContinueInstruction) {
                return false;
            }

            for (int ownedBlockId : instruction.getOwnedBlocks()) {
                if (!isSimpleSection(functionBody, ownedBlockId)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static void renameVariablesNested(FunctionBody functionBody, int blockId, Map<String, String> newNames) {
        Set<Integer> visitedBlocks = new HashSet<>();
        Queue<Integer> pendingBlocks = new LinkedList<>();

        pendingBlocks.add(blockId);

        while (!pendingBlocks.isEmpty()) {
            int currentBlockId = pendingBlocks.poll();

            boolean changed = visitedBlocks.add(currentBlockId);
            assert changed : "Block ownership semantics violation. Block " + currentBlockId
                    + " owned by multiple other blocks, at:\n" + functionBody;

            SsaBlock block = functionBody.getBlock(currentBlockId);

            for (SsaInstruction instruction : block.getInstructions()) {
                instruction.renameVariables(newNames);

                pendingBlocks.addAll(instruction.getOwnedBlocks());
            }
        }
    }

    public static int getAfterPhiInsertionPoint(SsaBlock block) {
        int lastPhi = -1;

        List<SsaInstruction> instructions = block.getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            SsaInstruction instruction = instructions.get(i);

            if (instruction instanceof PhiInstruction) {
                lastPhi = i;
                continue;
            }
            if (instruction.getInstructionType() != InstructionType.DECORATOR
                    && instruction.getInstructionType() != InstructionType.LINE) {
                break;
            }
        }

        return lastPhi + 1;
    }

    public static List<PhiInstruction> getPhiNodes(SsaBlock block) {
        List<PhiInstruction> phis = new ArrayList<>();

        for (SsaInstruction instruction : block.getInstructions()) {
            if (instruction instanceof PhiInstruction) {
                phis.add((PhiInstruction) instruction);
                continue;
            }
            if (instruction.getInstructionType() != InstructionType.DECORATOR
                    && instruction.getInstructionType() != InstructionType.LINE) {
                break;
            }
        }

        return phis;
    }

    public static Set<String> getVariablesDeclaredInContainedBlocks(TypedInstance instance, int blockId) {
        return getVariablesDeclaredInContainedBlocks(instance.getFunctionBody(), blockId);
    }

    public static Set<String> getVariablesDeclaredInContainedBlocks(FunctionBody body, int blockId) {
        Set<String> declaredVariables = new HashSet<>();

        for (SsaInstruction instruction : body.getBlock(blockId).getInstructions()) {
            declaredVariables.addAll(instruction.getOutputs());

            for (int ownedBlock : instruction.getOwnedBlocks()) {
                declaredVariables.addAll(getVariablesDeclaredInContainedBlocks(body, ownedBlock));
            }
        }

        return declaredVariables;
    }

    public static Set<String> getVariablesUsedInContainedBlocks(FunctionBody body, int blockId) {
        Set<String> declaredVariables = new HashSet<>();

        for (SsaInstruction instruction : body.getBlock(blockId).getInstructions()) {
            declaredVariables.addAll(instruction.getInputVariables());

            for (int ownedBlock : instruction.getOwnedBlocks()) {
                declaredVariables.addAll(getVariablesUsedInContainedBlocks(body, ownedBlock));
            }
        }

        return declaredVariables;
    }

    public static void computeForLoopIterationsAndSizes(
            FunctionBody body,
            List<Integer> chosenNesting,
            List<String> loopSizes,
            List<String> iters) {

        for (int loopBlockId : chosenNesting) {
            SsaBlock block = body.getBlock(loopBlockId);
            ForInstruction xfor2 = (ForInstruction) block.getEndingInstruction().get();
            String end = xfor2.getEnd();

            loopSizes.add(end);

            String iter = null;
            for (SsaInstruction instruction : body.getBlock(xfor2.getLoopBlock()).getInstructions()) {
                if (instruction instanceof IterInstruction) {
                    iter = ((IterInstruction) instruction).getOutput();
                    break;
                }
            }

            iters.add(iter);
        }
    }

    /**
     * Returns true if and only if the program can only access testBlock by passing through sourceBlock. If testBlock ==
     * sourceBlock, returns true if and only if there is a cycle involving this block.
     */
    public static boolean covers(ControlFlowGraph cfg, int sourceBlock, int testBlock) {
        Queue<Integer> blocksToCheck = new LinkedList<>();
        Set<Integer> visitedBlocks = new HashSet<>();

        blocksToCheck.add(testBlock);
        while (!blocksToCheck.isEmpty()) {
            int blockId = blocksToCheck.poll();
            if (!visitedBlocks.add(blockId)) {
                continue;
            }

            if (blockId == 0) {
                return false;
            }

            for (int antecedent : cfg.getAntecedentsOf(blockId)) {
                if (antecedent != sourceBlock) {
                    blocksToCheck.add(antecedent);
                }
            }
        }

        return true;
    }

    public static boolean covers(ControlFlowGraph cfg, InstructionLocation source, InstructionLocation test) {
        Preconditions.checkArgument(!source.equals(test));

        return (source.getBlockId() == test.getBlockId() && source.getInstructionId() <= test.getInstructionId()) ||
                covers(cfg, source.getBlockId(), test.getBlockId());
    }

    public static List<BlockDescendants> getBlockNesting(FunctionBody body) {
        List<Set<Integer>> descendants = new ArrayList<>();

        for (int blockId = 0; blockId < body.getBlocks().size(); ++blockId) {
            SsaBlock block = body.getBlock(blockId);

            Set<Integer> children = new HashSet<>();

            block.getEndingInstruction()
                    .ifPresent(endingInstruction -> {
                        for (int ownedBlock : endingInstruction.getOwnedBlocks()) {
                            if (ownedBlock != endingInstruction.tryGetEndBlock().orElse(-1)) {
                                children.add(ownedBlock);
                            }
                        }
                    });

            descendants.add(children);
        }

        // Convert set of children into set of descendants
        for (int blockId = 0; blockId < body.getBlocks().size(); ++blockId) {
            for (Set<Integer> descendant : descendants) {
                if (descendant.contains(blockId)) {
                    descendant.addAll(descendants.get(blockId));
                }
            }
        }

        return descendants.stream()
                .map(BlockDescendants::new)
                .collect(Collectors.toList());
    }

    /**
     * Indicates whether a variable is used after a given point, without passing by its definition again first.
     * 
     * @param cfg
     *            The Control Flow Graph to use
     * @param variable
     *            The variable to test
     * @param body
     *            The body of the function
     * @param blockId
     *            The block index where to start the search
     * @param instructionId
     *            The instruction index where to start the search
     * @return True if the variable is never used after the specified point without passing by its definition first.
     */
    public static boolean isBufferDeadAfter(ControlFlowGraph cfg, String variable, FunctionBody body,
            int blockId,
            int instructionId,
            Logger logger) {

        logger.log("> isBufferDead? " + variable + ": " + blockId + ", " + instructionId);

        Set<Integer> markedToVisit = new HashSet<>();
        Queue<InstructionLocation> toVisit = new LinkedList<>();
        toVisit.add(new InstructionLocation(blockId, instructionId));

        visitor: while (!toVisit.isEmpty()) {
            InstructionLocation startingLocation = toVisit.poll();
            logger.log("> Visiting " + startingLocation);

            int currentBlockId = startingLocation.getBlockId();
            int startingInstructionId = startingLocation.getInstructionId();
            SsaBlock currentBlock = body.getBlock(currentBlockId);

            for (int i = startingInstructionId; i < currentBlock.getInstructions().size(); ++i) {
                SsaInstruction instruction = currentBlock.getInstructions().get(i);

                if (instruction.getInputVariables().contains(variable)) {
                    // Found a variable use
                    logger.log("! Found use at " + instruction);
                    return false;
                }

                if (instruction.getOutputs().contains(variable)) {
                    // Found the variable definition
                    logger.log("> Reached definition.");
                    continue visitor;
                }
            }

            for (int successorId : cfg.getSuccessorsOf(currentBlockId)) {
                if (markedToVisit.contains(successorId)) {
                    continue;
                }

                markedToVisit.add(successorId);
                toVisit.add(new InstructionLocation(successorId, 0));
            }
        }

        logger.log("> No uses of " + variable);
        return true;
    }
}
