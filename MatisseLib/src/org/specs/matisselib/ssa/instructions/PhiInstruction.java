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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.specs.matisselib.ssa.InstructionType;

import com.google.common.base.Preconditions;

public final class PhiInstruction extends SsaInstruction {

    private String output;
    private final List<String> inputs;
    private final List<Integer> sourceBlocks;

    public PhiInstruction(String output, List<String> inputs, List<Integer> sourceBlocks) {
	Preconditions.checkArgument(output != null);
	Preconditions.checkArgument(inputs != null);
	Preconditions.checkArgument(sourceBlocks != null);
	Preconditions.checkArgument(inputs.size() > 0);
	Preconditions.checkArgument(inputs.size() == sourceBlocks.size());

	this.output = output;
	this.inputs = new ArrayList<>(inputs);
	this.sourceBlocks = new ArrayList<>(sourceBlocks);
    }

    @Override
    public PhiInstruction copy() {
	return new PhiInstruction(this.output, this.inputs, this.sourceBlocks);
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();

	builder.append(this.output);
	builder.append(" = phi ");

	for (int i = 0; i < this.inputs.size(); ++i) {
	    String input = this.inputs.get(i);
	    int sourceBlock = this.sourceBlocks.get(i);

	    if (i != 0) {
		builder.append(", ");
	    }

	    builder.append("#");
	    builder.append(sourceBlock);
	    builder.append(":");
	    builder.append(input);
	}

	return builder.toString();
    }

    @Override
    public List<String> getInputVariables() {
	return Collections.unmodifiableList(this.inputs);
    }

    public void setInputVariable(int i, String name) {
	Preconditions.checkArgument(i >= 0 && i < this.inputs.size());
	Preconditions.checkArgument(name != null);

	this.inputs.set(i, name);
    }

    public void setOutputVariable(String name) {
	Preconditions.checkArgument(name != null);

	this.output = name;
    }

    @Override
    public List<String> getOutputs() {
	return Arrays.asList(this.output);
    }

    public String getOutput() {
	return this.output;
    }

    public List<Integer> getSourceBlocks() {
	return Collections.unmodifiableList(this.sourceBlocks);
    }

    @Override
    public InstructionType getInstructionType() {
	return InstructionType.NO_SIDE_EFFECT;
    }

    @Override
    public void renameBlocks(List<Integer> oldNames, List<Integer> newNames) {
	for (int i = 0; i < this.sourceBlocks.size(); ++i) {
	    this.sourceBlocks.set(i, tryRenameBlock(oldNames, newNames, this.sourceBlocks.get(i)));
	}
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	Preconditions.checkArgument(newNames != null);

	this.output = newNames.getOrDefault(this.output, this.output);

	renameVariableList(newNames, this.inputs);
    }

    @Override
    public void removeBlocks(List<Integer> blockIds) {
	for (int i : blockIds) {
	    int index = this.sourceBlocks.indexOf(i);
	    if (index < 0) {
		continue;
	    }

	    assert index == this.sourceBlocks.lastIndexOf(i);

	    this.inputs.remove(index);
	    this.sourceBlocks.remove(index);
	}
    }

    @Override
    public boolean isEndingInstruction() {
	return false;
    }

    @Override
    public List<Integer> getTargetBlocks() {
	return Collections.emptyList();
    }

    @Override
    public Optional<Integer> tryGetEndBlock() {
	return Optional.empty();
    }

    @Override
    public void breakBlock(int originalBlock, int startBlock, int endBlock) {
	renameBlocks(Arrays.asList(originalBlock), Arrays.asList(endBlock));
    }

    @Override
    public List<Integer> getOwnedBlocks() {
	return Collections.emptyList();
    }
}
