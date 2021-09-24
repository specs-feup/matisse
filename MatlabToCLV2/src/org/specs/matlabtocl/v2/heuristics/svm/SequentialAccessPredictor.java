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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.NumberInput;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.VariableInput;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.IndexedInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matlabtocl.v2.codegen.ParallelLoopInformation;
import org.specs.matlabtocl.v2.codegen.Reduction;
import org.specs.matlabtocl.v2.ssa.ParallelRegionSettings;

import pt.up.fe.specs.util.collections.MultiMap;

/**
 * <p>
 * Tests that on all X(A, B, C) accesses, variable A is sequential relative to the inner-most iteration variable.
 * Indices B and C are ignored.
 * 
 * <p>
 * So for an outer loop i with an inner loop j:
 * <ul>
 * <li>If A == j, then it is sequential
 * <li>If A == 1, then it is non-sequential
 * <li>If A == i, then it is non-sequential
 * <li>If A == j + 1, then it is sequential
 * <li>If A == i + j, then it is sequential
 * </ul>
 * 
 * <p>
 * For parallel loops, the heuristic is more complicated. The inner-most dimension is not necessarily the one that has
 * sequential execution and we need to know the workgroup size.
 * 
 * <p>
 * We also have the concept of *regionally constant* variables. If a variable is sequential in a loop, then it is
 * regionally constant in all nested loops.
 * 
 * @author Luís Reis
 *
 */
public class SequentialAccessPredictor implements BlacklistHeuristic {
    private static final boolean ENABLE_LOG = false;

    public Set<String> buildBlacklist(FunctionBody body,
            Function<String, Optional<VariableType>> typeGetter,
            ParallelLoopInformation parallelLoop,
            ParallelRegionSettings settings,
            MultiMap<Integer, Reduction> localReductions,
            int blockId,
            int sharedLocalIndex,
            List<Integer> nonOneIndices) {

        Set<String> blockConstant = new HashSet<>();
        Set<String> blockSequential = new HashSet<>();
        Set<String> randomAccess = new HashSet<>();
        Set<String> badAccesses = new HashSet<>();

        Set<String> importedVars = BlockUtils.getVariablesUsedInContainedBlocks(body, blockId);
        importedVars.removeAll(BlockUtils.getVariablesDeclaredInContainedBlocks(body, blockId));
        blockConstant.addAll(importedVars);

        visitAccesses(body, typeGetter, parallelLoop, blockId, 0, sharedLocalIndex, blockConstant,
                blockSequential, randomAccess, true, badAccesses);

        return badAccesses;
    }

    private static void visitAccesses(FunctionBody body,
            Function<String, Optional<VariableType>> typeGetter,
            ParallelLoopInformation parallelLoop,
            int blockId,
            int depth,
            int indexDepth,
            Set<String> blockConstant,
            Set<String> blockSequential,
            Set<String> randomAccess,
            boolean isOuterLoop,
            Set<String> badAccesses) {

        SsaBlock block = body.getBlock(blockId);
        for (SsaInstruction instruction : block.getInstructions()) {
            if (instruction instanceof IterInstruction) {
                String output = ((IterInstruction) instruction).getOutput();

                if (depth < parallelLoop.loopDeclarationBlockIds.size()) {
                    blockConstant.add(output);
                } else {
                    blockSequential.add(output);
                }
                continue;
            }
            if (instruction instanceof IndexedInstruction) {
                IndexedInstruction indexedInstruction = (IndexedInstruction) instruction;
                List<String> outputs = indexedInstruction.getOutputs();
                randomAccess.addAll(outputs);

                if (isOuterLoop) {
                    continue;
                }

                String inputMatrix = indexedInstruction.getInputMatrix();
                List<String> indices = indexedInstruction.getIndices();

                if (indices.size() == 0) {
                    continue;
                }
                String firstIndex = indices.get(0);
                if (blockSequential.contains(firstIndex) || blockConstant.contains(firstIndex)) {
                    log("Index " + firstIndex + " seems to be sequential or constant, at " + instruction);
                } else {
                    log("Index " + firstIndex + " not sequential or constant, at " + instruction);
                    log("Sequential indices: " + blockSequential);
                    log("Constant indices: " + blockSequential);
                    badAccesses.add(inputMatrix);
                    continue;
                }

                for (int i = 1; i < indices.size(); ++i) {
                    String index = indices.get(i);
                    if (!blockConstant.contains(index)) {
                        log("Index " + index + " is not constant, at " + instruction);
                        badAccesses.add(inputMatrix);
                        break;
                    }
                }

                continue;
            }
            if (instruction instanceof ForInstruction) {
                ForInstruction innerFor = (ForInstruction) instruction;

                Set<String> innerBlockConstant = new HashSet<>(blockConstant);
                innerBlockConstant.addAll(randomAccess);
                Set<String> innerSequential = new HashSet<>();
                Set<String> innerRandom = new HashSet<>();
                boolean innerIsOuterLoop;
                if (depth < parallelLoop.loopDeclarationBlockIds.size() - 1) {
                    // We are still dealing with parallel loops.

                    assert isOuterLoop;
                    assert blockSequential.isEmpty();

                    innerIsOuterLoop = true;
                } else {
                    innerBlockConstant.addAll(blockSequential);

                    innerIsOuterLoop = false;
                }

                visitAccesses(body, typeGetter, parallelLoop, innerFor.getLoopBlock(), depth + 1,
                        indexDepth,
                        innerBlockConstant,
                        innerSequential,
                        innerRandom,
                        innerIsOuterLoop,
                        badAccesses);
                visitAccesses(body, typeGetter, parallelLoop, innerFor.getEndBlock(), depth, indexDepth,
                        blockConstant, blockSequential, randomAccess,
                        isOuterLoop,
                        badAccesses);
                continue;
            }
            if (instruction instanceof AssignmentInstruction) {
                AssignmentInstruction assignment = (AssignmentInstruction) instruction;

                String output = assignment.getOutput();
                if (assignment.getInput() instanceof VariableInput) {
                    String input = ((VariableInput) assignment.getInput()).getName();

                    if (blockSequential.contains(input)) {
                        blockSequential.add(output);
                    } else if (blockConstant.contains(input)) {
                        blockConstant.add(output);
                    } else if (randomAccess.contains(input)) {
                        randomAccess.add(output);
                    } else {
                        assert false;
                    }

                    continue;
                } else if (assignment.getInput() instanceof NumberInput) {
                    blockConstant.add(output);
                }
            }
            if (instruction instanceof FunctionCallInstruction) {
                FunctionCallInstruction functionCall = (FunctionCallInstruction) instruction;
                if (functionCall.getFunctionName().equals("plus")) {

                    if (functionCall.getOutputs().size() != 1 || functionCall.getInputVariables().size() != 2) {
                        log("Unrecognized function format: " + instruction);
                        continue;
                    }

                    String output = functionCall.getOutputs().get(0);
                    String left = functionCall.getInputVariables().get(0);
                    String right = functionCall.getInputVariables().get(1);

                    boolean resultBlockConstant = false;
                    boolean resultBlockSequential = false;

                    if (!randomAccess.contains(left) && !randomAccess.contains(right)) {
                        if (blockConstant.contains(left)) {
                            if (blockSequential.contains(right)) {
                                resultBlockSequential = true;
                            } else {
                                resultBlockConstant = true;
                            }
                        } else {
                            assert blockSequential.contains(left) : "Variable " + left
                                    + " not constant, sequential nor random!?";

                            if (blockConstant.contains(right)) {
                                resultBlockSequential = true;
                            }
                        }
                    }

                    assert !resultBlockConstant || !resultBlockSequential;

                    if (resultBlockConstant) {
                        blockConstant.add(output);
                    } else if (resultBlockSequential) {
                        blockSequential.add(output);
                    } else {
                        randomAccess.add(output);
                    }

                    continue;
                } else if (functionCall.getFunctionName().equals("minus")) {

                    if (functionCall.getOutputs().size() != 1 || functionCall.getInputVariables().size() != 2) {
                        log("Unrecognized function format: " + instruction);
                        continue;
                    }

                    String output = functionCall.getOutputs().get(0);
                    String left = functionCall.getInputVariables().get(0);
                    String right = functionCall.getInputVariables().get(1);

                    boolean resultBlockConstant = false;
                    boolean resultBlockSequential = false;

                    if (!randomAccess.contains(left) && !randomAccess.contains(right)) {
                        if (blockSequential.contains(left)) {
                            if (blockSequential.contains(right)) {
                                resultBlockConstant = true;
                            } else {
                                resultBlockSequential = true;
                            }
                        } else {
                            if (blockConstant.contains(right)) {
                                resultBlockConstant = true;
                            }
                        }
                    }

                    assert !resultBlockConstant || !resultBlockSequential;

                    if (resultBlockConstant) {
                        blockConstant.add(output);
                    } else if (resultBlockSequential) {
                        blockSequential.add(output);
                    } else {
                        randomAccess.add(output);
                    }

                    continue;
                } else {
                    if (functionCall.getInputVariables().stream().allMatch(blockConstant::contains)) {
                        for (String output : functionCall.getOutputs()) {
                            blockConstant.add(output);
                        }
                        continue;
                    }
                }
            }

            if (instruction.getOwnedBlocks().size() != 0) {
                for (int ownedBlock : instruction.getOwnedBlocks()) {
                    visitAccesses(body, typeGetter, parallelLoop, ownedBlock,
                            Integer.MAX_VALUE, indexDepth,
                            blockConstant, blockSequential, randomAccess, isOuterLoop, badAccesses);
                }
                continue;
            }

            log("Unhandled instruction: " + instruction);
            randomAccess.addAll(instruction.getOutputs());
        }
    }

    private static void log(String message) {
        if (ENABLE_LOG) {
            System.out.print("[coalesced_access_predictor] ");
            System.out.println(message);
        }
    }
}
