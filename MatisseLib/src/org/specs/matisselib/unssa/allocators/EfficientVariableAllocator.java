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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.VariableInput;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.ssa.instructions.MultiSetInstruction;
import org.specs.matisselib.ssa.instructions.ParallelCopyInstruction;
import org.specs.matisselib.ssa.instructions.ReadGlobalInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.WriteGlobalInstruction;
import org.specs.matisselib.unssa.ControlFlowGraph;
import org.specs.matisselib.unssa.ControlFlowGraphBuilder;
import org.specs.matisselib.unssa.InterferenceGraph;
import org.specs.matisselib.unssa.InterferenceGraphBuilder;
import org.specs.matisselib.unssa.VariableAllocation;
import org.specs.matisselib.unssa.VariableAllocator;

/**
 * The preferred variable allocator for anything other than test purposes.
 *
 */
public class EfficientVariableAllocator implements VariableAllocator {

    private final DummyVariableAllocator BASE_ALLOCATOR = new DummyVariableAllocator();

    @Override
    public VariableAllocation performAllocation(FunctionBody functionBody, Predicate<List<String>> canMerge) {
        VariableAllocation allocation = this.BASE_ALLOCATOR.performAllocation(functionBody, canMerge);

        ControlFlowGraph cfg = ControlFlowGraphBuilder.build(functionBody);
        InterferenceGraph interferenceGraph = InterferenceGraphBuilder.build(functionBody, cfg);

        for (List<String> group : allocation.getVariableGroups()) {
            interferenceGraph.mergeGroup(group, group.get(0));
        }

        for (SsaInstruction instruction : functionBody.getFlattenedInstructionsIterable()) {
            visitHighPriorityInstruction(canMerge, allocation, interferenceGraph, instruction);
        }

        for (SsaInstruction instruction : functionBody.getFlattenedInstructionsIterable()) {

            visitMediumPriorityInstruction(canMerge, allocation, interferenceGraph, instruction);
        }

        return allocation;
    }

    protected void visitHighPriorityInstruction(Predicate<List<String>> canMerge, VariableAllocation allocation,
            InterferenceGraph interferenceGraph, SsaInstruction instruction) {
        // We'll start with the basics:
        // Whenever we have an assignment with at least one temporary, try to fuse the two variables.
        // Additionally, if the assignment refers to the same underlying variable (A$1 and A$2, for instance), then
        // merge it.
        // In the case of parallel copies, those are variables created by the out-of-SSA algorithm. We'll eliminate
        // those at every opportunity we get.
        if (instruction instanceof AssignmentInstruction) {
            AssignmentInstruction assignment = (AssignmentInstruction) instruction;

            if (!(assignment.getInput() instanceof VariableInput)) {
                return;
            }

            String inputName = ((VariableInput) assignment.getInput()).getName();
            String outputName = assignment.getOutput();

            int output$index = outputName.indexOf('$');
            int input$index = inputName.indexOf('$');

            if (output$index == 0 || input$index == 0
                    || outputName.substring(0, output$index).equals(inputName.substring(0, input$index))) {

                tryMerge(canMerge, interferenceGraph, allocation, outputName, inputName);
            }
        } else if (instruction instanceof ParallelCopyInstruction) {
            ParallelCopyInstruction parallelCopy = (ParallelCopyInstruction) instruction;

            for (int i = 0; i < parallelCopy.getInputVariables().size(); ++i) {
                String input = parallelCopy.getInputVariables().get(i);
                String output = parallelCopy.getOutputs().get(i);

                tryMerge(canMerge, interferenceGraph, allocation, output, input);
            }
        } else if (instruction instanceof SimpleSetInstruction) {
            SimpleSetInstruction simpleSet = (SimpleSetInstruction) instruction;
            String input = simpleSet.getInputMatrix();
            String output = simpleSet.getOutput();

            tryMerge(canMerge, interferenceGraph, allocation, output, input);
        } else if (instruction instanceof MultiSetInstruction) {
            MultiSetInstruction multiSet = (MultiSetInstruction) instruction;
            String input = multiSet.getInputMatrix();
            String output = multiSet.getOutput();

            tryMerge(canMerge, interferenceGraph, allocation, output, input);
        }
    }

    protected void visitMediumPriorityInstruction(Predicate<List<String>> canMerge, VariableAllocation allocation,
            InterferenceGraph interferenceGraph, SsaInstruction instruction) {
        // Iterate again, looking for extra (but potentially less efficient) opportunities
        // We keep this in a separate loop because we want to ensure the most promising cases are taken care of first.
        if (instruction instanceof TypedFunctionCallInstruction) {
            TypedFunctionCallInstruction call = (TypedFunctionCallInstruction) instruction;

            FunctionType fType = call.getFunctionType();
            Map<String, Integer> byRefInputs = new HashMap<>();
            for (int i = 0; i < fType.getArgumentsTypes().size(); ++i) {
                if (fType.isInputReference(i)) {
                    byRefInputs.put(fType.getArgumentsNames().get(i), i);
                }
            }

            if (!byRefInputs.isEmpty()) {
                List<String> outputAsInputNames = fType.getOutputAsInputNames();
                for (int outputIndex = 0; outputIndex < outputAsInputNames.size(); outputIndex++) {
                    String outputName = outputAsInputNames.get(outputIndex);
                    if (byRefInputs.containsKey(outputName)) {
                        int inputIndex = byRefInputs.get(outputName);

                        tryMerge(canMerge, interferenceGraph, allocation, call.getOutputs().get(outputIndex),
                                call.getInputVariables().get(inputIndex));
                    }
                }
            }

        } else if (instruction instanceof MatrixSetInstruction) {
            MatrixSetInstruction set = (MatrixSetInstruction) instruction;
            String input = set.getInputMatrix();
            String output = set.getOutput();

            tryMerge(canMerge, interferenceGraph, allocation, output, input);
        } else if (instruction instanceof ReadGlobalInstruction) {
            ReadGlobalInstruction read = (ReadGlobalInstruction) instruction;

            tryMerge(canMerge, interferenceGraph, allocation, read.getGlobal(), read.getOutput());
        } else if (instruction instanceof WriteGlobalInstruction) {
            WriteGlobalInstruction read = (WriteGlobalInstruction) instruction;

            tryMerge(canMerge, interferenceGraph, allocation, read.getGlobal(), read.getSsaVariable());
        }
    }

    protected static boolean tryMerge(
            Predicate<List<String>> canMerge,
            InterferenceGraph interferenceGraph,
            VariableAllocation allocation,
            String variable1,
            String variable2) {

        int group1 = allocation.getGroupIdForVariable(variable1);
        int group2 = allocation.getGroupIdForVariable(variable2);

        if (group1 == group2) {
            // Already combined
            return false;
        }

        if (canMerge(canMerge, interferenceGraph, allocation, group1, group2)) {
            int newGroup = allocation.merge(group1, group2);

            List<String> newGroupList = allocation.getVariableGroups().get(newGroup);
            interferenceGraph.mergeGroup(newGroupList, newGroupList.get(0));
            return true;
        }

        return false;
    }

    private static boolean canMerge(
            Predicate<List<String>> canMerge,
            InterferenceGraph interferenceGraph,
            VariableAllocation allocation,
            int groupId1,
            int groupId2) {

        List<String> group1 = allocation.getVariableGroups().get(groupId1);
        String sampleVariable1 = group1.get(0);
        List<String> group2 = allocation.getVariableGroups().get(groupId2);
        String sampleVariable2 = group2.get(0);

        if (interferenceGraph.hasInterference(sampleVariable1, sampleVariable2)) {
            return false;
        }

        List<String> combinedGroup = new ArrayList<>(group1);
        combinedGroup.addAll(group2);

        return canMerge.test(combinedGroup);
    }

}
