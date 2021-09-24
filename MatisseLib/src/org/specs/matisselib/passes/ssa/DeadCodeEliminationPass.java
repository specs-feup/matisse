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

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.passes.TypeNeutralSsaPass;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

/**
 * Performs dead code elimination, by removing all instructions that do nothing. This pass assumes that all outputs are
 * used and that all blocks/instructions are reachable.
 * 
 * @author Luís Reis
 *
 */
public class DeadCodeEliminationPass extends TypeNeutralSsaPass {

    @Override
    public void apply(FunctionBody source, DataStore data) {
        Preconditions.checkArgument(source != null);

        Queue<Integer> pendingVariables = new PriorityQueue<>();
        Map<String, Integer> variableAssignmentIndices = new HashMap<>();
        List<SsaInstruction> instructions = source.getBlocks()
                .stream()
                .flatMap(block -> block.getInstructions().stream())
                .collect(Collectors.toList());

        for (int currentInstruction = 0; currentInstruction < instructions.size(); ++currentInstruction) {
            SsaInstruction instruction = instructions.get(currentInstruction);

            for (String output : instruction.getOutputs()) {
                variableAssignmentIndices.put(output, currentInstruction);
            }

            assert instruction.getOutputs() != null;
            assert instruction.getOutputs().stream().allMatch(m -> m != null) : "Found null output at " + instruction;
            if (instruction.getInstructionType() != InstructionType.NO_SIDE_EFFECT
                    || instruction.getOutputs().stream().anyMatch(m -> m.endsWith("$ret"))) {
                pendingVariables.add(currentInstruction);
            }
        }

        // Consider a directed graph where each node is an instruction and each edge is a data dependency.
        // We start from a set of nodes that can not be removed and find out every reachable node from those
        // using a breadth-first search (though the order is not important).

        boolean[] isUsed = new boolean[instructions.size()];
        while (!pendingVariables.isEmpty()) {
            int instructionIndex = pendingVariables.poll();
            isUsed[instructionIndex] = true;

            SsaInstruction instruction = instructions.get(instructionIndex);
            for (String output : instruction.getInputVariables()) {
                assert variableAssignmentIndices.containsKey(output) : "Use of undeclared variable " + output + ", in "
                        + instruction + "\n"
                        + "dependencies: " + instruction.getInputVariables() + "\n"
                        + source;

                int sourceIndex = variableAssignmentIndices.get(output);
                if (!isUsed[sourceIndex]) {
                    isUsed[sourceIndex] = true;
                    pendingVariables.add(sourceIndex);
                }
            }
        }

        // All instructions not marked as used can be removed.
        int currentInstruction = 0;
        for (SsaBlock block : source.getBlocks()) {
            ListIterator<SsaInstruction> instructionIterator = block.getInstructions().listIterator();
            while (instructionIterator.hasNext()) {
                instructionIterator.next();

                if (!isUsed[currentInstruction]) {
                    instructionIterator.remove();
                }

                ++currentInstruction;
            }
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
