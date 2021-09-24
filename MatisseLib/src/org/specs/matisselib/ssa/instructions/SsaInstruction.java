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
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.specs.matisselib.ssa.InstructionLifetimeInformation;
import org.specs.matisselib.ssa.InstructionType;

public abstract class SsaInstruction {
    public abstract SsaInstruction copy();

    public abstract List<String> getInputVariables();

    public abstract List<String> getOutputs();

    public abstract InstructionType getInstructionType();

    public abstract boolean isEndingInstruction();

    public abstract void renameBlocks(List<Integer> oldNames, List<Integer> newNames);

    protected static int tryRenameBlock(List<Integer> oldNames, List<Integer> newNames, int previousId) {
        int newId = oldNames.indexOf(previousId);
        if (newId < 0) {
            return previousId;
        }

        return newNames.get(newId);
    }

    /**
     * A special case for instructions that violate normal SSA semantics. Usually, in an instruction A = foo B, A does
     * not interfere with B because A is only live at the *exit* of foo, and B is only live at the *entry* of foo.
     * However, for some instructions (e.g. function call of a matrix multiplication) A and B *do* interfere. Those
     * (rare) cases are described in this function.
     * 
     * @return
     */
    public Set<String> getEntryInterferentOutputs() {
        // For most functions, there is no possible interference.

        return Collections.emptySet();
    }

    public InstructionLifetimeInformation getLifetimeInformation() {
        return InstructionLifetimeInformation.NO_SPECIAL_REQUIREMENTS;
    }

    /**
     * Gets global variables <em>directly</em> referenced by this instruction. Note that an instruction can return empty
     * for this method even if {@link #dependsOnGlobalState()} returns true, e.g., for function calls.
     * 
     * @return
     */
    public Set<String> getReferencedGlobals() {
        return Collections.emptySet();
    }

    public boolean dependsOnGlobalState() {
        // This is the usual case.

        return false;
    }

    public abstract void renameVariables(Map<String, String> newNames);

    protected static void renameVariableList(Map<String, String> newNames, List<String> list) {
        ListIterator<String> iterator = list.listIterator();
        while (iterator.hasNext()) {
            String oldName = iterator.next();
            String newName = newNames.get(oldName);

            if (newName != null) {
                iterator.set(newName);
            }
        }
    }

    /**
     * Updates the instruction with the information that the given blocks no longer exist.
     * 
     * @param blockIds
     *            The blocks to be removed.
     */
    public abstract void removeBlocks(List<Integer> blockIds);

    /**
     * For control flow structure instructions (such as branch and while but not break), this gets the list of blocks
     * that are "covered" by this instruction. For other instructions, this is an empty list.
     * 
     * @return
     */
    public abstract List<Integer> getTargetBlocks();

    /**
     * For control flow instructions (such as branch and while but not break), this gets the end block (that is, the
     * block that will be executed after the section is over). For other instructions, this returns empty.
     * 
     * @return The end block, or empty if not applicable.
     */
    public abstract Optional<Integer> tryGetEndBlock();

    /**
     * Updates the instruction to know that the block previously known as originalBlock is now more than one: The start
     * is startBlock and the end is endBlock
     * 
     * @param originalBlock
     * @param startBlock
     * @param endBlock
     */
    public abstract void breakBlock(int originalBlock, int startBlock, int endBlock);

    public abstract List<Integer> getOwnedBlocks();
}
