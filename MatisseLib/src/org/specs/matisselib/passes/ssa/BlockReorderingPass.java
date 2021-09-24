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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.passes.TypeNeutralSsaPass;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Ensures that block order is in a "canonical" format. Also removes unused blocks.
 * 
 * @author Luís Reis
 *
 */
public class BlockReorderingPass extends TypeNeutralSsaPass {

    @Override
    public void apply(FunctionBody source, DataStore data) {
        List<Integer> missingBlocks = new ArrayList<>();
        for (int i = 0; i < source.getBlocks().size(); ++i) {
            missingBlocks.add(i);
        }

        List<Integer> blockOrder = new ArrayList<>();
        Stack<Integer> blocksToVisit = new Stack<>();
        blocksToVisit.push(0);

        while (!blocksToVisit.isEmpty()) {
            int blockId = blocksToVisit.pop();
            missingBlocks.remove((Integer) blockId);
            blockOrder.add(blockId);

            SsaBlock block = source.getBlock(blockId);
            block.getEndingInstruction()
                    .map(end -> end.getOwnedBlocks())
                    .ifPresent(ownedBlocks -> {
                        for (int i = ownedBlocks.size() - 1; i >= 0; i--) {
                            int target = ownedBlocks.get(i);
                            assert missingBlocks.contains(target) : "Block " + target
                                    + " is either owned by multiple other blocks, or does not exist.\nIn " + source;

                            blocksToVisit.push(target);
                        }
                    });
        }

        List<Integer> oldNames = new ArrayList<>();
        List<Integer> newNames = new ArrayList<>();
        int currentName = 0;
        for (int block : blockOrder) {
            oldNames.add(block);
            newNames.add(currentName++);
        }

        // Update blocks (elimination of dead code, reordering of live code)
        List<SsaBlock> newBlocks = new ArrayList<>();
        for (int block : blockOrder) {
            newBlocks.add(source.getBlock(block));
        }
        source.setBlocks(newBlocks);

        // Update instructions
        for (SsaInstruction instruction : source.getFlattenedInstructionsIterable()) {
            instruction.removeBlocks(missingBlocks);
            instruction.renameBlocks(oldNames, newNames);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
