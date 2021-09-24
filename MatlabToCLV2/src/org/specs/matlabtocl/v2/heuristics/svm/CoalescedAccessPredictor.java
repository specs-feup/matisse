/**
 * Copyright 2017 SPeCS.
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

package org.specs.matlabtocl.v2.heuristics.svm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.NumberInput;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.VariableInput;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.IndexedInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matlabtocl.v2.codegen.ParallelLoopInformation;
import org.specs.matlabtocl.v2.codegen.Reduction;
import org.specs.matlabtocl.v2.ssa.ParallelRegionSettings;

import pt.up.fe.specs.util.collections.MultiMap;

public class CoalescedAccessPredictor implements BlacklistHeuristic {
    private static final boolean ENABLE_LOG = false;

    public static class BadAccess {
        public String matrix;
        public InstructionLocation location;

        public BadAccess(String matrix, InstructionLocation location) {
            this.matrix = matrix;
            this.location = location;
        }

        @Override
        public String toString() {
            return matrix + "@" + location;
        }
    }

    public Set<String> buildBlacklist(FunctionBody body,
            Function<String, Optional<VariableType>> typeGetter,
            ParallelLoopInformation parallelLoop,
            ParallelRegionSettings settings,
            MultiMap<Integer, Reduction> localReductions,
            int blockId,
            int sharedLocalIndex,
            List<Integer> nonOneIndices) {

        log("----------------------");
        log("Predicting coalescence of " + body.getName() + " with schedule " + settings.schedule);
        log("Non one indices: " + nonOneIndices);
        log("Shared local index: " + sharedLocalIndex);

        List<BadAccess> badAccesses = computeBadAccesses(body,
                typeGetter,
                parallelLoop,
                settings,
                localReductions,
                blockId,
                sharedLocalIndex, nonOneIndices);

        log("Bad accesses: " + badAccesses);

        return badAccesses.stream()
                .map(access -> access.matrix)
                .collect(Collectors.toSet());
    }

    public List<BadAccess> computeBadAccesses(FunctionBody body,
            Function<String, Optional<VariableType>> typeGetter,
            ParallelLoopInformation parallelLoop,
            ParallelRegionSettings settings,
            MultiMap<Integer, Reduction> localReductions,
            int blockId,
            int sharedLocalIndex,
            List<Integer> nonOneIndices) {

        Set<String> sequentialVariables = new HashSet<>();
        Set<String> localConstantVariables = new HashSet<>();
        List<BadAccess> badAccesses = new ArrayList<>();

        Set<String> importedVars = BlockUtils.getVariablesUsedInContainedBlocks(body, blockId);
        importedVars.removeAll(BlockUtils.getVariablesDeclaredInContainedBlocks(body, blockId));
        localConstantVariables.addAll(importedVars);

        visitAccesses(body, typeGetter, parallelLoop, settings, blockId, 0,
                sharedLocalIndex,
                nonOneIndices,
                false,
                false,
                localReductions,
                sequentialVariables,
                localConstantVariables, badAccesses);
        return badAccesses;
    }

    private static void visitAccesses(FunctionBody body,
            Function<String, Optional<VariableType>> typeGetter,
            ParallelLoopInformation parallelLoop,
            ParallelRegionSettings settings,
            int blockId,
            int depth,
            int indexDepth,
            List<Integer> nonOneIndices,
            boolean isLocallyConstantLoop,
            boolean isLocallySequentialLoop,
            MultiMap<Integer, Reduction> localReductions,
            Set<String> sequentialVariables,
            Set<String> localConstantVariables,
            List<BadAccess> badAccesses) {

        SsaBlock block = body.getBlock(blockId);
        List<SsaInstruction> instructions = block.getInstructions();
        for (int instructionId = 0; instructionId < instructions.size(); instructionId++) {
            SsaInstruction instruction = instructions.get(instructionId);
            InstructionLocation instructionLocation = new InstructionLocation(blockId, instructionId);

            if (instruction instanceof IterInstruction) {
                String output = ((IterInstruction) instruction).getOutput();

                log("Checking iter " + output);
                if (depth < parallelLoop.loopDeclarationBlockIds.size()) {
                    int mainLocalIteration = parallelLoop.loopDeclarationBlockIds.size() - indexDepth - 1;
                    if (depth == mainLocalIteration && nonOneIndices.size() <= 1) {
                        if (settings.schedule.isLocalConstantIndex()) {
                            log("Iter " + output + " is LC");
                            localConstantVariables.add(output);
                        } else if (settings.schedule.isCoalescenceFriendly()) {
                            log("Iter " + output + " is LS");
                            sequentialVariables.add(output);
                        } else {
                            log("Unclear how to handle iteration of " + settings.schedule);
                        }
                    } else if (nonOneIndices.size() == 1) {
                        log("Iter " + output + " is LC because local_size is 1");
                        localConstantVariables.add(output);
                    } else {
                        log("Iter " + output + " is not LS/LC because of its local size.");
                    }
                } else if (isLocallyConstantLoop) {
                    if (settings.schedule.distributesLoop()) {
                        log("Loop iter " + output + " is LS due to distributive schedule");
                        sequentialVariables.add(output);
                    } else {
                        log("Loop iter " + output + " is LC because loop is LC");
                        localConstantVariables.add(output);
                    }
                } else if (isLocallySequentialLoop) {
                    log("Loop iter " + output + " is LS because loop is LS");
                    sequentialVariables.add(output);
                }
                continue;
            }
            if (instruction instanceof IndexedInstruction) {
                IndexedInstruction indexedInstruction = (IndexedInstruction) instruction;
                String inputMatrix = indexedInstruction.getInputMatrix();
                List<String> indices = indexedInstruction.getIndices();

                if (indices.size() == 0) {
                    continue;
                }
                String firstIndex = indices.get(0);
                if (!sequentialVariables.contains(firstIndex) && !localConstantVariables.contains(firstIndex)) {
                    log("Index " + firstIndex + " not sequential nor locally constant");
                    log("Seq=" + sequentialVariables + ", local constants=" + localConstantVariables);
                    badAccesses.add(new BadAccess(inputMatrix, instructionLocation));
                    continue;
                }
                for (int i = 1; i < indices.size(); ++i) {
                    String index = indices.get(i);
                    if (!localConstantVariables.contains(index)) {
                        log("Index " + index + " not in local constants: " + localConstantVariables + "\n\tseq = "
                                + sequentialVariables);
                        badAccesses.add(new BadAccess(inputMatrix, instructionLocation));
                        break;
                    }
                }
                continue;
            }
            if (instruction instanceof ForInstruction) {
                ForInstruction innerFor = (ForInstruction) instruction;
                String start = innerFor.getStart();
                String interval = innerFor.getInterval();
                Optional<VariableType> intervalType = typeGetter.apply(interval);
                boolean isInnerForConstant = localConstantVariables.contains(start)
                        && localConstantVariables.contains(interval); // i = LC:LC:?, i is LC
                boolean isInnerForSequential = sequentialVariables.contains(start)
                        && ConstantUtils.isConstantOne(intervalType); // i = S:1:?, i is S

                if (settings.schedule.distributesLoop() && localReductions.containsKey(blockId)) {
                    assert isInnerForConstant;

                    isInnerForConstant = false;
                    isInnerForSequential = true;
                }

                visitAccesses(body,
                        typeGetter,
                        parallelLoop,
                        settings,
                        innerFor.getLoopBlock(),
                        depth + 1,
                        indexDepth,
                        nonOneIndices,
                        isInnerForConstant,
                        isInnerForSequential,
                        localReductions,
                        sequentialVariables,
                        localConstantVariables,
                        badAccesses);

                visitAccesses(body, typeGetter, parallelLoop, settings,
                        innerFor.getEndBlock(), depth,
                        indexDepth,
                        nonOneIndices,
                        isInnerForConstant, isInnerForSequential,
                        localReductions, sequentialVariables, localConstantVariables,
                        badAccesses);
                continue;
            }
            if (instruction instanceof AssignmentInstruction) {
                AssignmentInstruction assignment = (AssignmentInstruction) instruction;

                String output = assignment.getOutput();
                if (assignment.getInput() instanceof VariableInput) {
                    String input = ((VariableInput) assignment.getInput()).getName();

                    if (sequentialVariables.contains(input)) {
                        sequentialVariables.add(output);
                    }
                    if (localConstantVariables.contains(input)) {
                        localConstantVariables.add(output);
                    }

                    continue;
                } else if (assignment.getInput() instanceof NumberInput) {
                    localConstantVariables.add(output);
                }
            }
            if (instruction instanceof TypedFunctionCallInstruction) {
                TypedFunctionCallInstruction functionCall = (TypedFunctionCallInstruction) instruction;
                if (functionCall.getFunctionName().equals("plus")) {

                    // LC + LC = LC
                    // S + LC = S
                    // LC + S = S
                    // S + S = :(

                    if (functionCall.getOutputs().size() != 1 || functionCall.getInputVariables().size() != 2) {
                        log("Unrecognized function format: " + instruction);
                        continue;
                    }

                    String output = functionCall.getOutputs().get(0);
                    String left = functionCall.getInputVariables().get(0);
                    String right = functionCall.getInputVariables().get(1);

                    boolean isLeftLocallyConstant = localConstantVariables.contains(left);
                    boolean isLeftSequential = sequentialVariables.contains(left);
                    boolean isRightLocallyConstant = localConstantVariables.contains(right);
                    boolean isRightSequential = sequentialVariables.contains(right);

                    boolean resultLocallyConstant = false;
                    boolean resultSequential = false;

                    if (isLeftLocallyConstant) {
                        if (isRightLocallyConstant) {
                            resultLocallyConstant = true;
                        } else if (isRightSequential) {
                            resultSequential = true;
                        }
                    } else if (isLeftSequential && isRightLocallyConstant) {
                        resultSequential = true;
                    }

                    assert !(resultSequential && resultLocallyConstant);

                    if (resultSequential) {
                        sequentialVariables.add(output);
                    }
                    if (resultLocallyConstant) {
                        localConstantVariables.add(output);
                    }

                    continue;
                } else if (functionCall.getFunctionName().equals("minus")) {

                    // LC - LC = LC
                    // S - LC = S
                    // LC - S = :(
                    // S - S = LC

                    if (functionCall.getOutputs().size() != 1 || functionCall.getInputVariables().size() != 2) {
                        log("Unrecognized function format: " + instruction);
                        continue;
                    }

                    String output = functionCall.getOutputs().get(0);
                    String left = functionCall.getInputVariables().get(0);
                    String right = functionCall.getInputVariables().get(1);

                    boolean isLeftLocallyConstant = localConstantVariables.contains(left);
                    boolean isLeftSequential = sequentialVariables.contains(left);
                    boolean isRightLocallyConstant = localConstantVariables.contains(right);
                    boolean isRightSequential = sequentialVariables.contains(right);

                    boolean resultLocallyConstant = false;
                    boolean resultSequential = false;

                    if (isLeftSequential) {
                        if (isRightLocallyConstant) {
                            resultSequential = true;
                        } else if (isRightSequential) {
                            resultLocallyConstant = true;
                        }
                    } else if (isLeftLocallyConstant && isRightLocallyConstant) {
                        resultLocallyConstant = true;
                    }

                    assert !(resultSequential && resultLocallyConstant);

                    if (resultSequential) {
                        sequentialVariables.add(output);
                    }
                    if (resultLocallyConstant) {
                        localConstantVariables.add(output);
                    }

                    continue;
                } else if (!functionCall.getFunctionType().dependsOnGlobalState()) {
                    boolean allInputsLocallyConstant = true;
                    for (String input : functionCall.getInputVariables()) {
                        if (!localConstantVariables.contains(input)) {
                            allInputsLocallyConstant = false;
                            break;
                        }
                    }

                    if (allInputsLocallyConstant) {
                        for (String output : functionCall.getOutputs()) {
                            localConstantVariables.add(output);
                        }
                    }
                }
            }

            if (instruction.getOwnedBlocks().size() != 0) {
                for (int ownedBlock : instruction.getOwnedBlocks()) {
                    visitAccesses(body, typeGetter, parallelLoop, settings, ownedBlock,

                            Integer.MAX_VALUE, indexDepth, nonOneIndices,
                            false, false, localReductions,
                            sequentialVariables, localConstantVariables, badAccesses);
                }
                continue;
            }

            log("Unhandled instruction: " + instruction);
        }
    }

    private static void log(String message) {
        if (ENABLE_LOG) {
            System.out.print("[coalesced_access_predictor] ");
            System.out.println(message);
        }
    }
}
