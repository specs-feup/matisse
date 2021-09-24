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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.specs.matisselib.ssa.InstructionType;

import com.google.common.base.Preconditions;

public class BranchInstruction extends SsaInstruction {
    private String condition;
    private int trueBlock;
    private int falseBlock;
    private int endBlock;

    public BranchInstruction(String condition, int trueBlock, int falseBlock, int endBlock) {
	Preconditions.checkArgument(condition != null);
	Preconditions.checkArgument(trueBlock >= 0);
	Preconditions.checkArgument(falseBlock >= 0);
	Preconditions.checkArgument(endBlock >= 0);

	this.condition = condition;
	this.trueBlock = trueBlock;
	this.falseBlock = falseBlock;
	this.endBlock = endBlock;
    }

    @Override
    public BranchInstruction copy() {
	return new BranchInstruction(this.condition, this.trueBlock, this.falseBlock, this.endBlock);
    }

    @Override
    public String toString() {
	return "branch " + this.condition + ", #" + this.trueBlock + ", #" + this.falseBlock + ", #" + this.endBlock;
    }

    @Override
    public boolean isEndingInstruction() {
	return true;
    }

    public String getConditionVariable() {
	return this.condition;
    }

    @Override
    public List<String> getInputVariables() {
	return Arrays.asList(this.condition);
    }

    @Override
    public List<String> getOutputs() {
	return Collections.emptyList();
    }

    @Override
    public InstructionType getInstructionType() {
	return InstructionType.CONTROL_FLOW;
    }

    @Override
    public void renameBlocks(List<Integer> oldNames, List<Integer> newNames) {
	this.trueBlock = tryRenameBlock(oldNames, newNames, this.trueBlock);
	this.falseBlock = tryRenameBlock(oldNames, newNames, this.falseBlock);
	this.endBlock = tryRenameBlock(oldNames, newNames, this.endBlock);
    }

    public int getTrueBlock() {
	return this.trueBlock;
    }

    public int getFalseBlock() {
	return this.falseBlock;
    }

    public int getEndBlock() {
	return this.endBlock;
    }

    @Override
    public Optional<Integer> tryGetEndBlock() {
	return Optional.of(getEndBlock());
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	Preconditions.checkArgument(newNames != null);

	this.condition = newNames.getOrDefault(this.condition, this.condition);
    }

    @Override
    public void removeBlocks(List<Integer> blockIds) {
	if (blockIds.contains(this.trueBlock) || blockIds.contains(this.falseBlock)
		|| blockIds.contains(this.endBlock)) {
	    throw new IllegalStateException("Attempting to delete blocks " + blockIds
		    + ", but branch still references at least one of those blocks: " + this);
	}
    }

    @Override
    public List<Integer> getTargetBlocks() {
	return Arrays.asList(this.trueBlock, this.falseBlock, this.endBlock);

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
