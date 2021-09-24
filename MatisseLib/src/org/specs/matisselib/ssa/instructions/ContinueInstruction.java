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

public class ContinueInstruction extends SsaInstruction {

    @Override
    public ContinueInstruction copy() {
	return new ContinueInstruction();
    }

    @Override
    public boolean isEndingInstruction() {
	return true;
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
    public String toString() {
	return "continue";
    }

    @Override
    public InstructionType getInstructionType() {
	return InstructionType.CONTROL_FLOW;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	Preconditions.checkArgument(newNames != null);
    }

    @Override
    public void renameBlocks(List<Integer> oldNames, List<Integer> newNames) {
	// Do nothing
    }

    @Override
    public void removeBlocks(List<Integer> blockIds) {
	// Do nothing
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
	// Do nothing
    }

    @Override
    public List<Integer> getOwnedBlocks() {
	return Collections.emptyList();
    }
}
