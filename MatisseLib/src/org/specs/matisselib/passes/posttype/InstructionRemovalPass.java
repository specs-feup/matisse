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

package org.specs.matisselib.passes.posttype;

import java.util.List;

import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

public abstract class InstructionRemovalPass<T extends SsaInstruction> implements PostTypeInferencePass {
    private final Class<T> clazz;

    protected InstructionRemovalPass(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * This function is public so that unit tests can call it directly.
     */
    @Override
    public void apply(TypedInstance instance,
            DataStore passData) {
        /*
         * We'll keep trying to remove the instructions until none are left
         */
        while (tryRemoveInstruction(instance, passData)) {
            // Do nothing.
        }
    }

    private boolean tryRemoveInstruction(TypedInstance instance,
            DataStore passData) {

        List<SsaBlock> blocks = instance.getBlocks();
        for (int blockId = 0; blockId < blocks.size(); blockId++) {
            SsaBlock block = blocks.get(blockId);

            List<SsaInstruction> instructions = block.getInstructions();
            for (int instructionId = 0; instructionId < instructions.size(); instructionId++) {
                SsaInstruction instruction = instructions.get(instructionId);

                if (this.clazz.isInstance(instruction)) {
                    T castInstruction = this.clazz.cast(instruction);
                    if (canEliminate(instance, castInstruction)) {

                        removeInstruction(instance,
                                block,
                                blockId,
                                instructionId,
                                castInstruction,
                                passData);

                        return true;
                    }
                }
            }
        }

        return false;
    }

    protected boolean canEliminate(TypedInstance instance, T instruction) {

        return true;
    }

    protected abstract void removeInstruction(TypedInstance instance,
            SsaBlock block,
            int blockId,
            int instructionId,
            T instruction,
            DataStore passData);
}
