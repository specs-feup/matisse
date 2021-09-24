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

package org.specs.matisselib.unssa.allocators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.unssa.VariableAllocation;
import org.specs.matisselib.unssa.VariableAllocator;

import com.google.common.base.Preconditions;

/**
 * <p>
 * Performs a simple variable allocation that merges as few variables as possible.
 * </p>
 * <p>
 * This is meant to be used for test purposes, though other allocators may use this as a starting point.
 * </p>
 * 
 * @author Luís Reis
 *
 */
public class DummyVariableAllocator implements VariableAllocator {
    @Override
    public VariableAllocation performAllocation(FunctionBody body, Predicate<List<String>> canMerge) {
        Preconditions.checkArgument(body != null);

        Set<String> variablesToAllocate = new HashSet<>();
        VariableAllocation alloc = new VariableAllocation();

        List<Set<String>> groups = performGroupAllocation(body, variablesToAllocate);

        for (Set<String> group : groups) {
            alloc.addVariableGroup(new ArrayList<>(group));
            variablesToAllocate.removeAll(group);
        }

        for (String var : variablesToAllocate) {
            alloc.addIsolatedVariable(var);
        }

        return alloc;
    }

    private static List<Set<String>> performGroupAllocation(FunctionBody body, Set<String> variablesToAllocate) {
        List<Set<String>> groups = new ArrayList<>();

        for (SsaBlock block : body.getBlocks()) {
            for (SsaInstruction instruction : block.getInstructions()) {
                if (instruction instanceof ArgumentInstruction) {
                    ArgumentInstruction arg = (ArgumentInstruction) instruction;

                    String output = arg.getOutput();
                    String underlyingName = NameUtils.getSuggestedName(output);

                    if (body.isByRef(underlyingName)) {
                        Set<String> group = new HashSet<>();
                        group.add(output);
                        group.add(underlyingName + "$ret");
                        groups.add(group);
                    } else {
                        variablesToAllocate.add(output);
                    }
                } else if (instruction instanceof PhiInstruction) {
                    PhiInstruction phi = (PhiInstruction) instruction;
                    // All variables in the phi node are assigned to the same group

                    Set<String> group = new HashSet<>();
                    group.addAll(phi.getInputVariables());
                    group.addAll(phi.getOutputs());

                    for (Set<String> existentGroup : groups) {
                        for (String groupVariable : existentGroup) {
                            if (group.contains(groupVariable)) {
                                System.err.println(body);
                                System.err.println(groupVariable);
                                throw new UnsupportedOperationException(
                                        "Failed to create variable. Check if conversion to CSSA is working.");
                            }
                        }
                    }

                    groups.add(group);
                } else {
                    variablesToAllocate.addAll(instruction.getOutputs());
                    variablesToAllocate.addAll(instruction.getReferencedGlobals());
                }
            }
        }
        return groups;
    }
}
