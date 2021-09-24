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

package org.specs.matlabtocl.v2.ssa.instructions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.specs.matisselib.ssa.InOrderBlockTraversalInstruction;
import org.specs.matisselib.ssa.InstructionType;
import org.specs.matlabtocl.v2.ssa.ParallelRegionSettings;

public final class ParallelBlockInstruction extends InOrderBlockTraversalInstruction {

    private ParallelRegionSettings settings;
    private int contentBlock;
    private int endBlock;

    public ParallelBlockInstruction(ParallelRegionSettings settings, int contentBlock, int endBlock) {
        this.settings = settings.copy();
        this.contentBlock = contentBlock;
        this.endBlock = endBlock;
    }

    @Override
    public ParallelBlockInstruction copy() {
        return new ParallelBlockInstruction(this.settings, this.contentBlock, this.endBlock);
    }

    public ParallelRegionSettings getSettings() {
        return settings;
    }

    public int getContentBlock() {
        return this.contentBlock;
    }

    public int getEndBlock() {
        return this.endBlock;
    }

    @Override
    public List<String> getInputVariables() {
        return settings.getInputVariables();
    }

    @Override
    public List<String> getOutputs() {
        return Collections.emptyList();
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.HAS_SIDE_EFFECT;
    }

    @Override
    public void renameBlocks(List<Integer> oldNames, List<Integer> newNames) {
        this.contentBlock = tryRenameBlock(oldNames, newNames, this.contentBlock);
        this.endBlock = tryRenameBlock(oldNames, newNames, this.endBlock);
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
        settings.renameVariables(newNames);
    }

    @Override
    public void removeBlocks(List<Integer> blockIds) {
        if (blockIds.contains(this.contentBlock) || blockIds.contains(this.endBlock)) {
            throw new IllegalStateException();
        }
    }

    @Override
    public List<Integer> getTargetBlocks() {
        return Arrays.asList(this.contentBlock, this.endBlock);
    }

    @Override
    public Optional<Integer> tryGetEndBlock() {
        return Optional.of(this.endBlock);
    }

    @Override
    public void breakBlock(int originalBlock, int startBlock, int endBlock) {
        if (this.contentBlock == originalBlock) {
            this.contentBlock = startBlock;
        }
        if (this.endBlock == originalBlock) {
            this.endBlock = startBlock;
        }
    }

    @Override
    public String toString() {
        return "parallel_block" + settings + " #" + this.contentBlock + ", #" + this.endBlock;
    }
}
