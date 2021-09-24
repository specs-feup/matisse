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

package org.specs.matisselib.ssa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.CommentInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

import com.google.common.base.Preconditions;

public class SsaBlock {
    private final List<SsaInstruction> instructions = new ArrayList<>();

    public void addInstruction(SsaInstruction instruction) {
        Preconditions.checkArgument(instruction != null);

        this.instructions.add(instruction);
    }

    public void addInstructions(List<SsaInstruction> instructions) {
        Preconditions.checkArgument(instructions != null);

        this.instructions.addAll(instructions);
    }

    public void prependInstruction(SsaInstruction instruction) {
        Preconditions.checkArgument(instruction != null);

        this.instructions.add(0, instruction);
    }

    public void prependInstructions(List<SsaInstruction> instructions) {
        Preconditions.checkArgument(instructions != null);

        this.instructions.addAll(0, instructions);
    }

    public void insertInstruction(int index, SsaInstruction instruction) {
        Preconditions.checkArgument(index >= 0 && index <= this.instructions.size());
        Preconditions.checkArgument(instruction != null);

        this.instructions.add(index, instruction);
    }

    public void insertInstructions(int index, List<SsaInstruction> instructions) {
        Preconditions.checkArgument(index >= 0 && index <= this.instructions.size());
        Preconditions.checkArgument(instructions != null);

        this.instructions.addAll(index, instructions);
    }

    public List<SsaInstruction> getInstructions() {
        return this.instructions;
    }

    public boolean isEmpty() {
        return this.instructions.isEmpty();
    }

    @Override
    public String toString() {
        return this.instructions
                .stream()
                .map(instruction -> instruction.toString())
                .collect(Collectors.joining("\n"));
    }

    public void renameVariables(Map<String, String> newNames) {
        Preconditions.checkArgument(newNames != null);

        for (SsaInstruction instruction : getInstructions()) {
            instruction.renameVariables(newNames);
        }
    }

    public void replaceInstructionAt(int instructionId, List<SsaInstruction> instructionsToInsert) {
        this.instructions.remove(instructionId);
        this.instructions.addAll(instructionId, instructionsToInsert);
    }

    public void replaceInstructionAt(int instructionId, SsaInstruction newInstruction) {
        this.instructions.set(instructionId, newInstruction);
    }

    public SsaInstruction removeLastInstruction() {
        Preconditions.checkState(this.instructions.size() > 0);

        return this.instructions.remove(this.instructions.size() - 1);
    }

    public void removeInstructionsFrom(int instructionId) {
        Preconditions.checkArgument(instructionId >= 0);
        Preconditions.checkArgument(instructionId < this.instructions.size());

        while (this.instructions.size() > instructionId) {
            this.instructions.remove(this.instructions.size() - 1);
        }
    }

    /**
     * Gets the "ending instruction" of the block. An instruction is "ending" if it can only appear as the last
     * instruction of a block. For instance, sets are not ending instructions, but branches and breaks are.
     * 
     * @return The ending instruction of the block, or empty if there isn't any.
     * @see SsaInstruction#isEndingInstruction()
     */
    public Optional<SsaInstruction> getEndingInstruction() {
        if (this.instructions.size() == 0) {
            return Optional.empty();
        }

        SsaInstruction lastInstruction = this.instructions.get(this.instructions.size() - 1);
        if (lastInstruction.isEndingInstruction()) {
            return Optional.of(lastInstruction);
        }
        return Optional.empty();
    }

    public boolean hasSideEffects() {
        return this.instructions
                .stream()
                .map(instruction -> instruction.getInstructionType())
                .anyMatch(
                        type -> type == InstructionType.HAS_SIDE_EFFECT
                                || type == InstructionType.HAS_VALIDATION_SIDE_EFFECT);
    }

    public boolean usesVariable(String variable) {
        Preconditions.checkArgument(variable != null);

        for (SsaInstruction instruction : this.instructions) {
            if (instruction.getInputVariables().contains(variable)) {
                return true;
            }
        }

        return false;
    }

    public SsaBlock copy() {
        SsaBlock newBlock = new SsaBlock();

        for (SsaInstruction instruction : getInstructions()) {
            newBlock.addInstruction(instruction.copy());
        }

        return newBlock;
    }

    public void removeInstructionAt(int instructionId) {
        this.instructions.remove(instructionId);
    }

    // Helper operations

    public void addAssignment(String variable, int value) {
        addInstruction(AssignmentInstruction.fromInteger(variable, value));
    }

    public void addAssignment(String out, String in) {
        addInstruction(AssignmentInstruction.fromVariable(out, in));
    }

    public void addComment(String content) {
        addInstruction(new CommentInstruction(content));
    }

    public void breakBlock(int originalBlock, int startBlock, int endBlock) {
        for (SsaInstruction instruction : this.instructions) {
            instruction.breakBlock(originalBlock, startBlock, endBlock);
        }
    }

    public void renameBlocks(List<Integer> oldNames, List<Integer> newNames) {
        for (SsaInstruction instruction : this.instructions) {
            instruction.renameBlocks(oldNames, newNames);
        }
    }

    public void removeInstructions(List<SsaInstruction> instructionsToRemove) {
        instructions.removeAll(instructionsToRemove);
    }
}
