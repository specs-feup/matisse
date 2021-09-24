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

package org.specs.matlabtocl.v2.ssa.passes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.UsageMap;
import org.specs.matisselib.passes.TypeNeutralSsaPass;
import org.specs.matisselib.passes.ssa.DeadCodeEliminationPass;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.EndInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.ssa.instructions.CompleteReductionInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Identifies cases where a complete_reduction MATRIX_SET operation is used, but the result is only used for numel/size
 * operations. Since MATRIX_SET preserves the size of the original buffer, the size operation can be applied to the
 * original buffer.
 * 
 * <p>
 * This pass requires a subsequent {@link DeadCodeEliminationPass} to actually optimize anything.
 * </p>
 * 
 * @author Luís Reis
 *
 */
public class RedundantCopyForSizeEliminationPass extends TypeNeutralSsaPass {

    public static final String PASS_NAME = "redundant_copy_for_size_elimination";

    private static final List<String> SIZE_FUNCTIONS = Arrays.asList("size", "numel", "length", "ndims");

    @Override
    public void apply(FunctionBody source, DataStore data) {

        Logger logger = PassUtils.getLogger(data, PASS_NAME);

        if (PassUtils.skipPass(source, PASS_NAME)) {
            logger.log("Skipping " + source.getName());

            return;
        }

        logger.log("Starting " + source.getName());

        // Get initial buffers
        Map<String, String> matrixSources = new HashMap<>();

        List<SsaBlock> blocks = source.getBlocks();
        for (SsaBlock block : blocks) {
            for (SsaInstruction instruction : block.getInstructions()) {
                if (!(instruction instanceof CompleteReductionInstruction)) {
                    continue;
                }

                CompleteReductionInstruction completeReduction = (CompleteReductionInstruction) instruction;

                if (completeReduction.getReductionType() != ReductionType.MATRIX_SET) {
                    continue;
                }

                if (completeReduction.getOutput().endsWith("$ret")) {
                    logger.log("Skipping " + completeReduction.getOutput() + " source because it is a returned value.");
                    continue;
                }

                logger.log("Adding potential replacement: " + completeReduction);
                addPotentialReplacement(matrixSources, completeReduction.getOutput(),
                        completeReduction.getInitialValue());
            }
        }

        for (SsaBlock block : blocks) {
            for (SsaInstruction instruction : block.getInstructions()) {
                if (!(instruction instanceof PhiInstruction)) {
                    continue;
                }

                PhiInstruction phi = (PhiInstruction) instruction;

                String acceptedOrigin = null;
                String output = phi.getOutput();

                if (output.endsWith("$ret")) {
                    logger.log("Skipping " + output + " because it is a returned value.");
                    continue;
                }

                for (String input : phi.getInputVariables()) {
                    String origin = matrixSources.get(input);

                    if (origin == null) {
                        acceptedOrigin = null;
                        break;
                    }
                    if (origin.equals(output)) {
                        continue;
                    }

                    if (acceptedOrigin == null) {
                        acceptedOrigin = origin;
                    } else if (!acceptedOrigin.equals(origin)) {
                        acceptedOrigin = null;
                        break;
                    }
                }

                if (acceptedOrigin == null) {
                    logger.log("Skipping " + output + " because it has no accepted origin");
                    continue;
                }

                logger.log("Adding potential replacement: " + output + " = " + acceptedOrigin);
                addPotentialReplacement(matrixSources, output, acceptedOrigin);
            }
        }

        UsageMap totalUsages = UsageMap.build(source);
        UsageMap sizeUsages = UsageMap.buildEmpty();

        logger.log("Replacements: " + matrixSources);

        for (SsaBlock block : blocks) {
            for (SsaInstruction instruction : block.getInstructions()) {
                if (instruction instanceof FunctionCallInstruction) {

                    FunctionCallInstruction functionCall = (FunctionCallInstruction) instruction;

                    if (!SIZE_FUNCTIONS.contains(functionCall.getFunctionName())) {
                        continue;
                    }
                } else if (instruction instanceof CompleteReductionInstruction) {
                    CompleteReductionInstruction completeReduction = (CompleteReductionInstruction) instruction;

                    if (completeReduction.getReductionType() != ReductionType.MATRIX_SET) {
                        continue;
                    }

                    // On MATRIX_SET, the initial value is only used for the size properties.
                    sizeUsages.increment(completeReduction.getInitialValue());
                    continue;
                } else if (!(instruction instanceof EndInstruction)) {
                    continue;
                }

                // We have a size function
                for (String input : instruction.getInputVariables()) {
                    sizeUsages.increment(input);
                }
                for (String output : instruction.getOutputs()) {
                    sizeUsages.increment(output);
                }
            }
        }

        Set<String> keysToRemove = new HashSet<>();
        for (String key : matrixSources.keySet()) {
            if (totalUsages.getUsageCount(key) != sizeUsages.getUsageCount(key)) {
                keysToRemove.add(key);
            }
        }

        for (String keyToRemove : keysToRemove) {
            matrixSources.remove(keyToRemove);
        }

        for (SsaBlock block : blocks) {
            for (SsaInstruction instruction : block.getInstructions()) {
                if (instruction instanceof CompleteReductionInstruction) {
                    CompleteReductionInstruction completeReduction = (CompleteReductionInstruction) instruction;

                    List<String> inputs = completeReduction.getInputVariables();
                    if (inputs.stream().filter(matrixSources::containsKey).count() != 1) {
                        continue;
                    }

                    if (!matrixSources.containsKey(completeReduction.getInitialValue())) {
                        continue;
                    }

                    if (completeReduction.getOutputs().stream().anyMatch(matrixSources::containsKey)) {
                        continue;
                    }

                    logger.log("Renaming " + matrixSources + " at " + instruction);
                    instruction.renameVariables(matrixSources);
                    System.out.println(instruction);
                    continue;
                }
                if (instruction instanceof FunctionCallInstruction) {

                    FunctionCallInstruction functionCall = (FunctionCallInstruction) instruction;

                    if (!SIZE_FUNCTIONS.contains(functionCall.getFunctionName())) {
                        continue;
                    }
                } else if (!(instruction instanceof EndInstruction)) {
                    continue;
                }

                List<String> inputs = instruction.getInputVariables();
                if (inputs.size() == 0) {
                    continue;
                }

                if (!matrixSources.containsKey(inputs.get(0))) {
                    continue;
                }

                for (int i = 1; i < inputs.size(); ++i) {
                    if (matrixSources.containsKey(inputs.get(i))) {

                        // We don't know how to handle this case, so don't.
                        logger.log(inputs.get(i) + " is a matrix source. How to handle this?");
                        continue;
                    }
                }

                logger.log("Renaming " + matrixSources + " at " + instruction);
                instruction.renameVariables(matrixSources);
            }
        }
    }

    private void addPotentialReplacement(
            Map<String, String> matrixSources,
            String output,
            String acceptedOrigin) {

        matrixSources.put(output, matrixSources.getOrDefault(acceptedOrigin, acceptedOrigin));
    }

}
