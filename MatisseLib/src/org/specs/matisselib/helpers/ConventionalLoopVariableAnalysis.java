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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.exceptions.NoForLoopError;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.BreakInstruction;
import org.specs.matisselib.ssa.instructions.ContinueInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.WhileInstruction;
import org.specs.matisselib.typeinference.TypedInstance;

public class ConventionalLoopVariableAnalysis {
    /**
     * Computes all conventional variables in a loop. If a loop starts with a phi instruction "$start = phi
     * #beforeLoop:$beforeLoop, #loopEnd:$loopEnd", and after the loop there is a phi instruction "$afterLoop = phi
     * #beforeLoop:$beforeLoop, #loopEnd:$loopEnd", then [$start, $beforeLoop, $loopEnd, $afterLoop] is a conventional
     * loop variable if all those variables have the same loose variable type.
     * 
     * @param body
     *            The function to analyze
     * @param typeGetter
     *            A function that retrieves the type of the given variable
     * @param containerBlockId
     *            The ID of the block ending with the for instruction.
     * @return The set of loop variables
     */
    static List<LoopVariable> computeConventionalVariables(FunctionBody body,
            Function<String, Optional<VariableType>> typeGetter,
            int containerBlockId,
            boolean enforceSameTypes) {

        SsaBlock startBlock = body.getBlock(containerBlockId);
        ForInstruction xfor = startBlock.getEndingInstruction()
                .filter(ForInstruction.class::isInstance)
                .map(ForInstruction.class::cast)
                .orElseThrow(() -> new NoForLoopError("No for loop in block #" + containerBlockId));

        int loopBlockId = xfor.getLoopBlock();
        SsaBlock loopBlock = body.getBlock(loopBlockId);
        int afterLoopId = xfor.getEndBlock();
        int loopEndId = BlockUtils.getBlockEnd(body, loopBlockId);

        List<String> beforeLoop = new ArrayList<>();
        List<String> loopStart = new ArrayList<>();
        List<String> loopEnd = new ArrayList<>();

        for (SsaInstruction instruction : loopBlock.getInstructions()) {
            if (instruction instanceof PhiInstruction) {
                PhiInstruction phi = (PhiInstruction) instruction;

                List<Integer> sourceBlocks = phi.getSourceBlocks();
                if (sourceBlocks.size() != 2) {
                    continue;
                }

                int beforeLoopIndex = sourceBlocks.indexOf(containerBlockId);
                int loopEndIndex = sourceBlocks.indexOf(loopEndId);

                if (beforeLoopIndex < 0 || loopEndIndex < 0) {
                    continue;
                }

                String beforeLoopVar = phi.getInputVariables().get(beforeLoopIndex);
                String loopEndVar = phi.getInputVariables().get(loopEndIndex);

                beforeLoop.add(beforeLoopVar);
                loopStart.add(phi.getOutput());
                loopEnd.add(loopEndVar);
            }
        }

        assert beforeLoop.size() == loopStart.size();
        assert loopStart.size() == loopEnd.size();

        List<LoopVariable> loopVariables = new ArrayList<>();
        Set<Integer> usedIndices = new HashSet<>();

        SsaBlock afterLoopBlock = body.getBlock(afterLoopId);
        for (SsaInstruction instruction : afterLoopBlock.getInstructions()) {
            if (instruction instanceof PhiInstruction) {
                PhiInstruction phi = (PhiInstruction) instruction;

                List<Integer> sourceBlocks = phi.getSourceBlocks();
                if (sourceBlocks.size() != 2) {
                    continue;
                }

                int beforeLoopIndex = sourceBlocks.indexOf(containerBlockId);
                int loopEndIndex = sourceBlocks.indexOf(loopEndId);

                if (beforeLoopIndex < 0 || loopEndIndex < 0) {
                    continue;
                }

                String beforeLoopVar = phi.getInputVariables().get(beforeLoopIndex);
                String loopEndVar = phi.getInputVariables().get(loopEndIndex);

                String afterLoopVar = phi.getOutput();

                for (int i = 0; i < beforeLoop.size(); ++i) {
                    if (!beforeLoop.get(i).equals(beforeLoopVar)) {
                        continue;
                    }
                    if (!loopEnd.get(i).equals(loopEndVar)) {
                        continue;
                    }
                    String loopStartVar = loopStart.get(i);

                    Optional<VariableType> type = typeGetter.apply(beforeLoopVar);
                    if (enforceSameTypes && !type.equals(typeGetter.apply(loopStartVar))) {
                        continue;
                    }
                    if (enforceSameTypes && !type.equals(typeGetter.apply(loopEndVar))) {
                        continue;
                    }
                    if (enforceSameTypes && !type.equals(typeGetter.apply(afterLoopVar))) {
                        continue;
                    }

                    usedIndices.add(i);
                    loopVariables.add(new LoopVariable(beforeLoopVar, loopStartVar, loopEndVar, afterLoopVar));
                    break;
                }
            }
        }

        for (int i = 0; i < beforeLoop.size(); ++i) {
            if (usedIndices.contains(i)) {
                continue;
            }

            String beforeLoopVar = beforeLoop.get(i);
            String loopStartVar = loopStart.get(i);
            String loopEndVar = loopEnd.get(i);

            Optional<VariableType> type = typeGetter.apply(beforeLoopVar);
            if (enforceSameTypes && !type.equals(typeGetter.apply(loopStartVar))) {
                continue;
            }
            if (enforceSameTypes && !type.equals(typeGetter.apply(loopEndVar))) {
                continue;
            }

            loopVariables.add(new LoopVariable(beforeLoopVar, loopStartVar, loopEndVar, null));
        }

        return loopVariables;
    }

    /**
     * A loop is standard if:
     * <ul>
     * <li>All its variables are standard
     * <li>Has no breaks/continues
     * <li>No phi blocks after the loop reference any variables inside the loop other than the standard ones.
     * </ul>
     * 
     * @return A list of loop variables if the loop is standard, empty set otherwise
     */
    public static List<LoopVariable> analyzeStandardLoop(FunctionBody body,
            Function<String, Optional<VariableType>> typeGetter,
            int containerBlockId,
            boolean enforceSameTypes) {

        List<LoopVariable> variables = computeConventionalVariables(body, typeGetter, containerBlockId,
                enforceSameTypes);

        SsaBlock startBlock = body.getBlock(containerBlockId);
        ForInstruction xfor = startBlock.getEndingInstruction()
                .filter(ForInstruction.class::isInstance)
                .map(ForInstruction.class::cast)
                .orElseThrow(() -> new NoForLoopError("No for loop in block #" + containerBlockId));

        int loopBlockId = xfor.getLoopBlock();
        SsaBlock loopBlock = body.getBlock(loopBlockId);
        int afterLoopId = xfor.getEndBlock();
        SsaBlock afterLoop = body.getBlock(afterLoopId);

        for (SsaInstruction instruction : loopBlock.getInstructions()) {
            if (instruction instanceof PhiInstruction) {
                String output = ((PhiInstruction) instruction).getOutput();
                if (!variables.stream().anyMatch(standardVar -> standardVar.loopStart.equals(output))) {
                    // Found non-standard variable
                    return Collections.emptyList();
                }
            }
        }

        Queue<Integer> pendingBlocks = new LinkedList<>();
        pendingBlocks.add(loopBlockId);

        while (!pendingBlocks.isEmpty()) {
            int blockId = pendingBlocks.poll();
            SsaBlock block = body.getBlock(blockId);

            for (SsaInstruction instruction : block.getInstructions()) {
                if (instruction instanceof BreakInstruction || instruction instanceof ContinueInstruction) {
                    return Collections.emptyList();
                }
                if (instruction instanceof ForInstruction || instruction instanceof WhileInstruction) {
                    continue;
                }

                pendingBlocks.addAll(instruction.getTargetBlocks());
            }
        }

        for (SsaInstruction instruction : afterLoop.getInstructions()) {
            if (instruction instanceof PhiInstruction) {
                String output = ((PhiInstruction) instruction).getOutput();
                if (!variables.stream()
                        .anyMatch(standardVar -> output.equals(standardVar.getAfterLoop().orElse(null)))) {
                    // Found non-standard variable
                    return Collections.emptyList();
                }
            }
        }

        return variables;

    }

    public static List<LoopVariable> analyzeStandardLoop(TypedInstance instance,
            int containerBlockId,
            boolean enforceSameTypes) {

        return analyzeStandardLoop(instance.getFunctionBody(), instance::getVariableType, containerBlockId,
                enforceSameTypes);
    }
}
