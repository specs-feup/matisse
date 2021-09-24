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

package org.specs.matisselib.passes.ssa;

import java.util.Arrays;
import java.util.List;

import org.specs.matisselib.passes.TypeNeutralSsaPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

/**
 * Removes branch instructions where both the if and else blocks are empty. <br/>
 * This is most effective if performed after dead code elimination and phi simplification.<br/>
 * TODO: Perhaps, in the future, we could also remove branches where both branches are identical?
 * 
 * @author Luís Reis
 *
 */
public class EmptyBranchEliminationPass extends TypeNeutralSsaPass {

    @Override
    public void apply(FunctionBody source, DataStore data) {
	Preconditions.checkArgument(source != null);
	Preconditions.checkArgument(data != null);

	boolean erasedEmptyBranch;
	do {
	    erasedEmptyBranch = false;

	    List<SsaBlock> blocks = source.getBlocks();
	    block_loop: for (int i = 0; i < blocks.size(); i++) {
		SsaBlock block = blocks.get(i);
		List<SsaInstruction> instructions = block.getInstructions();

		if (instructions.isEmpty()) {
		    continue;
		}

		SsaInstruction lastInstruction = instructions.get(instructions.size() - 1);
		if (!(lastInstruction instanceof BranchInstruction)) {
		    continue;
		}

		BranchInstruction branch = (BranchInstruction) lastInstruction;
		if (!isDeadBranch(branch, blocks)) {
		    continue;
		}

		instructions.remove(instructions.size() - 1);

		int trueBlockId = branch.getTrueBlock();
		int falseBlockId = branch.getFalseBlock();
		int endBlockId = branch.getEndBlock();

		SsaBlock endBlock = source.getBlocks().get(endBlockId);
		for (SsaInstruction sequenceInstruction : endBlock.getInstructions()) {
		    block.addInstruction(sequenceInstruction);
		}

		assert trueBlockId > i;
		assert falseBlockId > i;
		assert endBlockId > i;

		source.renameBlocks(Arrays.asList(endBlockId), Arrays.asList(i));

		source.removeAndRenameBlocks(trueBlockId, falseBlockId, endBlockId);

		break block_loop;
	    }
	} while (erasedEmptyBranch);
    }

    private static boolean isDeadBranch(BranchInstruction branch, List<SsaBlock> blocks) {
	assert branch != null;
	assert blocks != null;

	int trueBlock = branch.getTrueBlock();
	int falseBlock = branch.getFalseBlock();

	// Ignore line instructions
	for (SsaInstruction instruction : blocks.get(trueBlock).getInstructions()) {
	    if (instruction.getInstructionType() != InstructionType.LINE) {
		return false;
	    }
	}
	for (SsaInstruction instruction : blocks.get(falseBlock).getInstructions()) {
	    if (instruction.getInstructionType() != InstructionType.LINE) {
		return false;
	    }
	}

	// See if any phi depends on these blocks.

	boolean foundDependantPhi = blocks
		.stream()
		.flatMap(block -> block.getInstructions().stream())
		.filter(instruction -> instruction instanceof PhiInstruction)
		.map(instruction -> (PhiInstruction) instruction)
		.anyMatch(
			phi -> phi.getSourceBlocks().contains(trueBlock) || phi.getSourceBlocks().contains(falseBlock));

	return !foundDependantPhi;
    }
}
