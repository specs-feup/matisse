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

package org.specs.matisselib.ssa.instructions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.specs.matisselib.ssa.InstructionType;

import com.google.common.base.Preconditions;

public final class WhileInstruction extends SsaInstruction {
    private int loopBlock;
    private int endBlock;

    public WhileInstruction(int loopBlock, int endBlock) {
	Preconditions.checkArgument(loopBlock >= 0);
	Preconditions.checkArgument(endBlock >= 0);

	this.loopBlock = loopBlock;
	this.endBlock = endBlock;
    }

    @Override
    public WhileInstruction copy() {
	return new WhileInstruction(this.loopBlock, this.endBlock);
    }

    @Override
    public boolean isEndingInstruction() {
	return true;
    }

    public int getLoopBlock() {
	return this.loopBlock;
    }

    public int getEndBlock() {
	return this.endBlock;
    }

    @Override
    public Optional<Integer> tryGetEndBlock() {
	return Optional.of(getEndBlock());
    }

    @Override
    public String toString() {
	return "while #" + this.loopBlock + ", #" + this.endBlock;
    }

    @Override
    public List<String> getInputVariables() {
	return Arrays.asList();
    }

    @Override
    public List<String> getOutputs() {
	return Arrays.asList();
    }

    @Override
    public InstructionType getInstructionType() {
	return InstructionType.CONTROL_FLOW;
    }

    @Override
    public void renameBlocks(List<Integer> oldNames, List<Integer> newNames) {
	this.loopBlock = tryRenameBlock(oldNames, newNames, this.loopBlock);
	this.endBlock = tryRenameBlock(oldNames, newNames, this.endBlock);
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	Preconditions.checkArgument(newNames != null);
    }

    @Override
    public void removeBlocks(List<Integer> blockIds) {
	if (blockIds.contains(this.loopBlock) || blockIds.contains(this.endBlock)) {
	    throw new IllegalStateException();
	}
    }

    @Override
    public List<Integer> getTargetBlocks() {
	return Arrays.asList(this.loopBlock, this.endBlock);
    }

    @Override
    public void breakBlock(int originalBlock, int startBlock, int endBlock) {
	if (originalBlock != startBlock) {
	    renameBlocks(Arrays.asList(originalBlock), Arrays.asList(startBlock));
	}
    }

    @Override
    public List<Integer> getOwnedBlocks() {
	return getTargetBlocks();
    }
}
