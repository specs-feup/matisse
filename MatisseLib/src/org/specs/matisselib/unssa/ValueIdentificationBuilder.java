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

package org.specs.matisselib.unssa;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.Input;
import org.specs.matisselib.ssa.NumberInput;
import org.specs.matisselib.ssa.UndefinedInput;
import org.specs.matisselib.ssa.VariableInput;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.ParallelCopyInstruction;
import org.specs.matisselib.ssa.instructions.ReadGlobalInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.WriteGlobalInstruction;

import com.google.common.base.Preconditions;

public class ValueIdentificationBuilder {
    private ValueIdentificationBuilder() {
    }

    public static ValueIdentification build(FunctionBody body, ControlFlowGraph cfg) {
        Preconditions.checkArgument(body != null);
        Preconditions.checkArgument(cfg != null);

        Set<Integer> includedBlocks = new HashSet<>();
        Queue<Integer> blocksToVisit = new LinkedList<>();
        includedBlocks.add(0);
        blocksToVisit.add(0);

        Set<String> globals = new HashSet<>();
        for (SsaInstruction instruction : body.getFlattenedInstructionsIterable()) {
            globals.addAll(instruction.getReferencedGlobals());
        }
        boolean callsFunctionsReferencingGlobals = false;
        for (SsaInstruction instruction : body.getFlattenedInstructionsIterable()) {
            if (instruction instanceof FunctionCallInstruction && instruction.dependsOnGlobalState()) {
                callsFunctionsReferencingGlobals = true;
                break;
            }
        }
        Set<String> safeGlobals;
        if (!callsFunctionsReferencingGlobals) {
            Set<String> writtenGlobals = new HashSet<>();
            for (SsaInstruction instruction : body.getFlattenedInstructionsIterable()) {
                if (instruction instanceof WriteGlobalInstruction) {
                    globals.addAll(instruction.getReferencedGlobals());
                }
            }

            safeGlobals = new HashSet<>(globals);
            safeGlobals.removeAll(writtenGlobals);
        } else {
            safeGlobals = Collections.emptySet();
        }

        ValueIdentification values = new ValueIdentification();

        for (String global : safeGlobals) {
            values.setValueTag(global, values.newValueTag());
        }

        while (!blocksToVisit.isEmpty()) {
            int blockId = blocksToVisit.poll();

            for (SsaInstruction instruction : body.getBlock(blockId).getInstructions()) {
                if (instruction instanceof ParallelCopyInstruction) {
                    ParallelCopyInstruction parallelCopy = (ParallelCopyInstruction) instruction;

                    List<String> inputVariables = parallelCopy.getInputVariables();
                    List<String> outputVariables = parallelCopy.getOutputs();
                    for (int i = 0; i < inputVariables.size(); i++) {
                        String input = inputVariables.get(i);
                        String output = outputVariables.get(i);

                        values.setValueTag(output, values.getValueTag(input).get());
                    }
                    continue;
                }

                if (instruction instanceof AssignmentInstruction) {
                    AssignmentInstruction assignment = (AssignmentInstruction) instruction;
                    Input input = assignment.getInput();
                    if (input instanceof VariableInput) {
                        String inputName = ((VariableInput) input).getName();

                        values.setValueTag(assignment.getOutput(), values.getValueTag(inputName).get());
                        continue;
                    }
                    if (input instanceof UndefinedInput) {
                        values.setValueTag(assignment.getOutput(), 0);
                        continue;
                    }
                    if (input instanceof NumberInput) {
                        NumberInput numberInput = (NumberInput) input;
                        values.setValueTag(assignment.getOutput(), values.valueTagForConstant(numberInput.getNumber()));
                        continue;
                    }
                }

                if (instruction instanceof ReadGlobalInstruction) {
                    ReadGlobalInstruction readGlobal = (ReadGlobalInstruction) instruction;

                    String global = readGlobal.getGlobal();
                    String out = readGlobal.getOutput();

                    values.getValueTag(global).ifPresent(tag -> {
                        values.setValueTag(out, tag);
                    });
                    continue;
                }

                for (String output : instruction.getOutputs()) {
                    values.setValueTag(output, values.newValueTag());
                }
            }

            for (int successor : cfg.getSuccessorsOf(blockId)) {
                if (!includedBlocks.contains(successor)) {
                    includedBlocks.add(successor);
                    blocksToVisit.add(successor);
                }
            }
        }

        return values;
    }
}
