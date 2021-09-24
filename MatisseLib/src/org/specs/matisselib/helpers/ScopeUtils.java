/**
 * Copyright 2016 SPeCS.
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

package org.specs.matisselib.helpers;

import java.util.HashSet;
import java.util.Set;

import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypedInstance;

public class ScopeUtils {
    private ScopeUtils() {
    }

    /**
     * Returns the set of variables declared in a block, or any of that block's nested blocks.
     * 
     * @param body
     *            The function to check
     * @param blockId
     *            The outer most block to check
     * @return The set of declared variables
     */
    public static Set<String> getDeclaredVariables(FunctionBody body, int blockId) {
	Set<String> declaredVariables = new HashSet<>();

	addDeclarations(declaredVariables, body, blockId);

	return declaredVariables;
    }

    private static void addDeclarations(Set<String> declaredVariables, FunctionBody body, int blockId) {
	SsaBlock block = body.getBlock(blockId);

	for (SsaInstruction instruction : block.getInstructions()) {
	    declaredVariables.addAll(instruction.getOutputs());

	    instruction.getOwnedBlocks()
		    .forEach(ownedBlockId -> addDeclarations(declaredVariables, body, ownedBlockId));
	}
    }

    /**
     * Returns the set of variables declared in a block, or any of that block's nested blocks.
     * 
     * @param instance
     *            The function to check
     * @param blockId
     *            The outer most block to check
     * @return The set of declared variables
     */
    public static Set<String> getDeclaredVariables(TypedInstance instance, int blockId) {
	return getDeclaredVariables(instance.getFunctionBody(), blockId);
    }
}
