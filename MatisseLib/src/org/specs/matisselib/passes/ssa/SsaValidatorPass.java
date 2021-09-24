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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.passes.TypeNeutralSsaPass;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.DataStore.SimpleDataStore;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.Interfaces.DataView;

/**
 * Validates some that some properties hold.
 * 
 * <ul>
 * <li>That each variable is declared only once.
 * <li>That phi nodes have at least one input.
 * <li>That phi nodes have no duplicated input blocks.
 * <li>That phi nodes after a branch instruction only reference the ending blocks of that instruction.
 * <li>That phi nodes that appear in a loop entrance block necessarily reference the outer block.
 * <li>That phi nodes appear before all other types of nodes (aside from line instructions) in any block.
 * <li>That any ending instructions appear last in a block.
 * <li>That any referenced blocks exist.
 * <li>That all used variables are declared at some point.
 * <li>That all variables used in the first block were previously declared.
 * <li>That all variables declared in a block are not later declared in that block (excluding phis)
 * </ul>
 * 
 * @author Luís Reis
 *
 */
public class SsaValidatorPass extends TypeNeutralSsaPass {

    public static DataKey<String> VALIDATOR_NAME = KeyFactory.string("validator-name");

    private final String validatorName;

    public SsaValidatorPass(String validatorName) {
        this.validatorName = validatorName;
    }

    public SsaValidatorPass(DataView parameters) {
        if (!parameters.hasValue(SsaValidatorPass.VALIDATOR_NAME)) {
            throw new RuntimeException("Parameter validator-name is missing.");
        }

        this.validatorName = parameters.getValue(SsaValidatorPass.VALIDATOR_NAME);
    }

    @Override
    public void apply(FunctionBody source, DataStore data) {
        final Set<String> declaredVariables = new HashSet<>();
        final Set<String> usedVariables = new HashSet<>();

        for (SsaBlock block : source.getBlocks()) {
            List<SsaInstruction> instructions = block.getInstructions();
            for (int instructionIndex = 0; instructionIndex < instructions.size(); instructionIndex++) {

                SsaInstruction instruction = instructions.get(instructionIndex);

                // Test multiple declarations
                for (String output : instruction.getOutputs()) {
                    if (declaredVariables.contains(output)) {
                        throw declareViolation(source, "Violation of SSA semantics: Variable " + output
                                + " is declared multiple times.");
                    }
                    declaredVariables.add(output);
                }

                usedVariables.addAll(instruction.getInputVariables());

                // Some basic phi tests
                if (instruction instanceof PhiInstruction) {
                    PhiInstruction phi = (PhiInstruction) instruction;

                    if (phi.getInputVariables().size() == 0) {
                        throw declareViolation(source, "Found phi without any inputs");
                    }

                    List<Integer> sourceBlocks = phi.getSourceBlocks();
                    if (sourceBlocks.size() != new HashSet<>(sourceBlocks).size()) {
                        throw declareViolation(source, "Phi node has duplicated input blocks: " + phi);
                    }
                }

                // Test position of ending instruction
                if (instruction.isEndingInstruction() && instructionIndex != instructions.size() - 1) {
                    throw declareViolation(source, "Found ending instruction in middle of block: " + instruction);
                }

                // Make sure every referenced block exists.
                for (int referencedBlock : instruction.getOwnedBlocks()) {
                    int numBlocks = source.getBlocks().size();
                    if (referencedBlock < 0 || referencedBlock >= numBlocks) {
                        throw declareViolation(source,
                                "Found reference to missing block #" + referencedBlock + " (of " + numBlocks
                                        + ") in " + instruction);
                    }
                }
            }
        }

        for (String usedVariable : usedVariables) {
            if (!declaredVariables.contains(usedVariable)) {
                throw declareViolation(source, "Variable " + usedVariable + " is used but never declared.");
            }
        }

        for (SsaBlock block : source.getBlocks()) {
            boolean allowPhi = true;
            for (SsaInstruction instruction : block.getInstructions()) {
                if (instruction.getInstructionType() == InstructionType.LINE) {
                    continue;
                }

                if (instruction instanceof PhiInstruction) {
                    if (!allowPhi) {
                        throw declareViolation(source, "Phi instruction not at the beginning of block: " + instruction);
                    }
                } else {
                    allowPhi = false;
                }
            }
        }

        // We will now validate that all variables used in the first block were previously declared.
        // We do not do so for later blocks because it's more complicated.
        // For instance, a for loop can have a phi referencing a variable from later on that same loop.
        // In contrast, the very *first* block is very straightforward.
        Set<String> availableVariables = new HashSet<>();
        for (SsaInstruction instruction : source.getBlock(0).getInstructions()) {
            for (String usedVariable : instruction.getInputVariables()) {
                if (!availableVariables.contains(usedVariable)) {
                    throw declareViolation(source,
                            "Used variable " + usedVariable + " has not been declared before use.");
                }
            }

            availableVariables.addAll(instruction.getOutputs());
        }

        // Ensure that in phi nodes after branches, only the ending blocks of the branch can be referenced.
        // Ensure that in phi nodes inside loops, the outer block is referenced.
        List<SsaBlock> blocks = source.getBlocks();
        for (int blockId = 0; blockId < blocks.size(); blockId++) {
            SsaBlock block = blocks.get(blockId);
            for (SsaInstruction instruction : block.getInstructions()) {
                if (instruction instanceof BranchInstruction) {
                    BranchInstruction branch = (BranchInstruction) instruction;

                    int trueEnd = BlockUtils.getBlockEnd(source, branch.getTrueBlock());
                    int falseEnd = BlockUtils.getBlockEnd(source, branch.getFalseBlock());

                    for (SsaInstruction endBlockInstruction : source.getBlock(branch.getEndBlock()).getInstructions()) {
                        if (endBlockInstruction instanceof PhiInstruction) {
                            PhiInstruction phi = (PhiInstruction) endBlockInstruction;

                            for (int sourceBlock : phi.getSourceBlocks()) {
                                if (sourceBlock != trueEnd && sourceBlock != falseEnd) {
                                    throw declareViolation(source, "Invalid phi node after branch: " + phi
                                            + "\nValid blocks are: " + trueEnd + ", " + falseEnd);
                                }
                            }
                        }
                    }
                    continue;
                }
                if (instruction instanceof ForInstruction) {
                    ForInstruction xfor = (ForInstruction) instruction;

                    for (SsaInstruction inLoopInstruction : source.getBlock(xfor.getLoopBlock()).getInstructions()) {
                        if (inLoopInstruction instanceof PhiInstruction) {
                            PhiInstruction phi = (PhiInstruction) inLoopInstruction;
                            if (phi.getSourceBlocks().indexOf(blockId) < 0) {
                                throw declareViolation(source, "Invalid phi node in loop: " + phi
                                        + "\nPhi does not reference outer block (" + blockId + ")");
                            }
                        }
                    }
                }
            }
        }

        for (int blockId = 0; blockId < blocks.size(); blockId++) {
            SsaBlock block = blocks.get(blockId);
            Set<String> variablesUsedInBlock = new HashSet<>();
            for (SsaInstruction instruction : block.getInstructions()) {
                for (String output : instruction.getOutputs()) {
                    if (variablesUsedInBlock.contains(output)) {
                        throw declareViolation(source,
                                "Variable " + output + " is used in block #" + blockId + " before being declared.");
                    }
                }

                if (!(instruction instanceof PhiInstruction)) {
                    variablesUsedInBlock.addAll(instruction.getInputVariables());
                }
            }
        }
    }

    @Override
    public DataView getParameters() {
        DataStore parameters = new SimpleDataStore("ssa-validator-data");
        parameters.add(SsaValidatorPass.VALIDATOR_NAME, this.validatorName);

        return DataView.newInstance(parameters);
    }

    private RuntimeException declareViolation(FunctionBody source, String message) {
        System.err.println("At " + source.getName() + ":");
        System.err.println(source);
        throw new IllegalStateException("[" + this.validatorName + "] " + message);
    }

    public static List<DataKey<?>> getRequiredParameters() {
        return Arrays.asList(SsaValidatorPass.VALIDATOR_NAME);
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return true;
    }
}
