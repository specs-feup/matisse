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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;

public abstract class ControlFlowIndependentInstruction extends SsaInstruction {

    @Override
    public final boolean isEndingInstruction() {
	return false;
    }

    @Override
    public final void removeBlocks(List<Integer> blockIds) {
	// Intentionally left blank
    }

    @Override
    public final List<Integer> getTargetBlocks() {
	// No target blocks
	return Collections.emptyList();
    }

    @Override
    public final Optional<Integer> tryGetEndBlock() {
	// No action needed
	return Optional.empty();
    }

    @Override
    public final void breakBlock(int originalBlock, int startBlock, int endBlock) {
	// Intentionally left blank
    }

    @Override
    public final void renameBlocks(List<Integer> oldNames, List<Integer> newNames) {
	Preconditions.checkArgument(oldNames != null);
	Preconditions.checkArgument(newNames != null);
	Preconditions.checkArgument(oldNames.size() == newNames.size());
    }

    @Override
    public List<Integer> getOwnedBlocks() {
	return Collections.emptyList();
    }
}
