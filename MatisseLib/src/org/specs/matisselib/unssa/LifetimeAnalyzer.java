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

import java.util.List;
import java.util.stream.Collectors;

import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InstructionLifetimeInformation;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.ReadGlobalInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.WriteGlobalInstruction;

import com.google.common.base.Preconditions;

public class LifetimeAnalyzer {
    private LifetimeAnalyzer() {
    }

    public static LifetimeInformation analyze(FunctionBody functionBody) {
        Preconditions.checkArgument(functionBody != null);

        ControlFlowGraph cfg = ControlFlowGraphBuilder.build(functionBody);

        return analyze(functionBody, cfg);
    }

    public static LifetimeInformation analyze(FunctionBody functionBody, ControlFlowGraph cfg) {
        Preconditions.checkArgument(functionBody != null);
        Preconditions.checkArgument(cfg != null);

        int rows = functionBody.getBlocks().size();
        List<Integer> columns = functionBody.getBlocks()
                .stream()
                .map(block -> block.getInstructions().size())
                .collect(Collectors.toList());

        LifetimeInformation info = new LifetimeInformation(rows, columns);

        List<SsaBlock> blocks = functionBody.getBlocks();
        int lastBlockId = blocks.size() - 1;
        SsaBlock lastBlock = blocks.get(lastBlockId);
        int lastBlockSize = lastBlock.getInstructions().size();

        // Handle
        functionBody
                .getFlattenedInstructionsStream()
                .flatMap(instruction -> instruction.getReferencedGlobals().stream())
                .forEach(global -> markVariableAtExit(info, functionBody, cfg, global, lastBlockId, lastBlockSize - 1,
                        -1));

        // Handle local variables
        functionBody
                .getFlattenedInstructionsStream()
                .flatMap(instruction -> instruction.getOutputs().stream())
                .filter(output -> output.endsWith("$ret"))
                .distinct()
                .forEach(
                        returnedVariable -> markVariableAtExit(info,
                                functionBody,
                                cfg,
                                returnedVariable,
                                lastBlockId,
                                lastBlockSize - 1,
                                -1));

        // Handle for iter instructions
        for (int blockId = 0; blockId < blocks.size(); ++blockId) {
            SsaBlock block = blocks.get(blockId);

            for (SsaInstruction instruction : block.getInstructions()) {
                if (instruction.getLifetimeInformation() == InstructionLifetimeInformation.BLOCK_LIFETIME) {
                    for (String name : instruction.getOutputs()) {
                        int endBlockId = BlockUtils.getBlockEnd(functionBody, blockId);

                        int instructionId = blocks.get(endBlockId).getInstructions().size() - 1;
                        markVariableAtExit(info, functionBody, cfg, name, endBlockId, instructionId, -1);
                    }
                }
            }
        }

        // Handle read_global instructions
        for (int blockId = 0; blockId < blocks.size(); ++blockId) {
            SsaBlock block = blocks.get(blockId);

            List<SsaInstruction> instructions = block.getInstructions();
            for (int instructionId = 0; instructionId < instructions.size(); instructionId++) {
                SsaInstruction instruction = instructions.get(instructionId);
                if (instruction instanceof ReadGlobalInstruction) {
                    ReadGlobalInstruction readGlobal = (ReadGlobalInstruction) instruction;

                    markVariableAtEntry(info, functionBody, cfg, readGlobal.getGlobal(), blockId, instructionId, -1);
                }
            }
        }

        for (int blockId = blocks.size() - 1; blockId >= 0; --blockId) {
            SsaBlock block = blocks.get(blockId);
            List<SsaInstruction> instructions = block.getInstructions();

            for (int instructionId = instructions.size() - 1; instructionId >= 0; --instructionId) {
                SsaInstruction instruction = instructions.get(instructionId);
                if (instruction instanceof PhiInstruction) {
                    PhiInstruction phi = (PhiInstruction) instruction;

                    List<String> inputVariables = phi.getInputVariables();
                    List<Integer> sourceBlocks = phi.getSourceBlocks();

                    for (int i = 0; i < inputVariables.size(); ++i) {
                        String variableName = inputVariables.get(i);
                        int sourceBlock = sourceBlocks.get(i);

                        markVariableAtEntry(info, functionBody, cfg, variableName, blockId, instructionId, sourceBlock);
                    }
                } else {
                    for (String variableName : instruction.getInputVariables()) {
                        assert variableName != null : "Null variable name at " + instruction.toString();

                        markVariableAtEntry(info, functionBody, cfg, variableName, blockId, instructionId, -1);
                    }
                }
            }
        }

        return info;
    }

    // We could probably make this more efficient.
    // After all, if a variable is marked at the entry of i, then it is marked at the exit of i - 1.
    // We could optimize that to be less redundant.
    // So instead of two arrays of N elements (N = instructions size), we could have one array of N + 1.
    private static void markVariableAtEntry(LifetimeInformation info,
            FunctionBody functionBody,
            ControlFlowGraph cfg,
            String variableName,
            int blockId,
            int instructionId,
            int phiSource) {

        info.setLiveAtEntry(variableName, blockId, instructionId);

        boolean[] visitedBlocks = new boolean[functionBody.getBlocks().size()];
        int startInstruction;
        if (functionBody.getBlock(blockId).getInstructions().get(instructionId).getEntryInterferentOutputs()
                .contains(variableName)) {
            startInstruction = instructionId;
        } else {
            startInstruction = instructionId - 1;
        }
        markVariableAtExit(info, functionBody, cfg, variableName, blockId, startInstruction, visitedBlocks, phiSource);
    }

    private static void markVariableAtExit(LifetimeInformation info,
            FunctionBody functionBody,
            ControlFlowGraph cfg,
            String variableName,
            int blockId,
            int instructionId,
            int phiSource) {

        boolean[] visitedBlocks = new boolean[functionBody.getBlocks().size()];
        markVariableAtExit(info, functionBody, cfg, variableName, blockId, instructionId, visitedBlocks, phiSource);
    }

    private static void markVariableAtExit(LifetimeInformation info,
            FunctionBody functionBody,
            ControlFlowGraph cfg,
            String variableName,
            int blockId,
            int instructionId,
            boolean[] visitedBlocks,
            int phiSource) {

        List<SsaInstruction> instructions = functionBody
                .getBlocks()
                .get(blockId)
                .getInstructions();

        while (instructionId >= 0) {
            SsaInstruction instruction = instructions
                    .get(instructionId);

            info.setLiveAtExit(variableName, blockId, instructionId);

            if (instruction.getEntryInterferentOutputs().contains(variableName)) {
                // Consider the case of a matrix multiplication
                // $1 = $2 * $3
                // We can't assign $1 and $2 to the same variable
                // so we say that $2 is still alive at the entry.

                info.setLiveAtEntry(variableName, blockId, instructionId);
            }

            if (instruction.getOutputs().contains(variableName)) {
                return;
            }

            if (instruction instanceof WriteGlobalInstruction
                    && instruction.getReferencedGlobals().contains(variableName)) {
                return;
            }

            info.setLiveAtEntry(variableName, blockId, instructionId);

            instructionId--;
        }

        if (phiSource == -1) {
            List<Integer> antecedents = cfg.getAntecedentsOf(blockId);
            if (antecedents.isEmpty()) {
                if (variableName.startsWith("^")) {
                    // Global variable
                    return;
                } else {
                    throw new RuntimeException(
                            "At function " + functionBody.getName() + ": Local variable " + variableName
                                    + " is live at function entry.");
                }
            }

            for (int antecedent : antecedents) {
                if (visitedBlocks[antecedent]) {
                    continue;
                }

                SsaBlock block = functionBody.getBlocks().get(antecedent);
                visitedBlocks[antecedent] = true;
                markVariableAtExit(info, functionBody, cfg, variableName, antecedent,
                        block.getInstructions().size() - 1,
                        visitedBlocks,
                        -1);
            }
        } else {
            assert cfg.getAntecedentsOf(blockId).contains(phiSource) : "Variable " + variableName
                    + ": antecedents of block do not contain phi source\n" + functionBody + "\nWith phiSource="
                    + phiSource + "\nAntecedents of " + blockId + " are " + cfg.getAntecedentsOf(blockId);

            if (visitedBlocks[phiSource]) {
                return;
            }

            SsaBlock block = functionBody.getBlocks().get(phiSource);
            visitedBlocks[phiSource] = true;
            markVariableAtExit(info, functionBody, cfg, variableName, phiSource,
                    block.getInstructions().size() - 1,
                    visitedBlocks,
                    -1);
        }
    }
}
