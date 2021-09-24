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

package org.specs.matisselib.passes.posttype;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.passes.TypeTransparentSsaPass;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.EndInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.MatrixGetInstruction;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.ssa.instructions.RangeGetInstruction;
import org.specs.matisselib.ssa.instructions.RangeInstruction;
import org.specs.matisselib.ssa.instructions.RangeInstruction.Index;
import org.specs.matisselib.ssa.instructions.RangeSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Converts matrix gets that reference entire ranges to range_gets or range_sets. Only applies to gets/sets with at
 * least 2 indices, since gets/sets with 1 index have more complicated rules in MATLAB.
 * 
 * @author Luís Reis
 *
 */
public class ConvertToRangePass extends TypeTransparentSsaPass {

    public static final String PASS_NAME = "convert_to_range";

    @Override
    public void apply(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            DataStore passData) {

        Logger logger = PassUtils.getLogger(passData, PASS_NAME);

        if (PassUtils.skipPass(body, PASS_NAME)) {
            logger.log("Skipping");
            return;
        }
        logger.log("Starting");

        // Identify cases of $1 = end $X, N, M
        Map<String, EndInstruction> endInstructions = body
                .getFlattenedInstructionsOfTypeStream(EndInstruction.class)
                .collect(Collectors.toMap(end -> end.getOutput(), Function.identity()));

        // Identify cases of $1 = [untyped_]call colon $one, $N
        Map<String, FunctionCallInstruction> colonInstructions = body
                .getFlattenedInstructionsOfTypeStream(FunctionCallInstruction.class)
                .filter(call -> call.getFunctionName().equals("colon")
                        && call.getInputVariables().size() == 2
                        && call.getOutputs().size() == 1)
                .collect(Collectors.toMap(call -> call.getOutputs().get(0), Function.identity()));

        // Find gets and apply transformations
        for (SsaBlock block : body.getBlocks()) {
            List<SsaInstruction> instructions = block.getInstructions();
            for (int i = 0; i < instructions.size(); i++) {
                int instructionId = i; // Capture variable for lambda

                SsaInstruction instruction = instructions.get(instructionId);
                if (instruction instanceof MatrixGetInstruction) {
                    MatrixGetInstruction get = (MatrixGetInstruction) instruction;

                    String inputMatrix = get.getInputMatrix();
                    List<String> indices = get.getIndices();

                    String output = get.getOutput();
                    buildTransformedIndices(typeGetter, endInstructions, colonInstructions, inputMatrix, indices,
                            logger)
                                    .ifPresent(newIndices -> {
                                        RangeGetInstruction newGet = new RangeGetInstruction(output, inputMatrix,
                                                newIndices);

                                        logger.log("Converted get");
                                        block.replaceInstructionAt(instructionId, newGet);
                                    });
                } else if (instruction instanceof MatrixSetInstruction) {
                    MatrixSetInstruction set = (MatrixSetInstruction) instruction;

                    String inputMatrix = set.getInputMatrix();
                    List<String> indices = set.getIndices();
                    String value = set.getValue();

                    String output = set.getOutput();
                    buildTransformedIndices(typeGetter, endInstructions, colonInstructions, inputMatrix, indices,
                            logger)
                                    .ifPresent(newIndices -> {
                                        RangeSetInstruction newSet = new RangeSetInstruction(output, inputMatrix,
                                                newIndices, value);

                                        logger.log("Converted set");
                                        block.replaceInstructionAt(instructionId, newSet);
                                    });
                }
            }
        }
    }

    private static Optional<List<Index>> buildTransformedIndices(
            Function<String, Optional<VariableType>> typeGetter,
            Map<String, EndInstruction> endInstructions,
            Map<String, FunctionCallInstruction> colonInstructions,
            String inputMatrix,
            List<String> indices,
            Logger logger) {

        if (indices.size() < 2) {
            logger.log("Too few matrix indices in access");
            return Optional.empty();
        }

        List<Index> newIndices = new ArrayList<>();
        int convertedIndices = 0;
        for (int i = 0; i < indices.size(); ++i) {
            String index = indices.get(i);

            FunctionCallInstruction colon = colonInstructions.get(index);
            if (colon == null) {
                logger.log("Matrix index does not correspond to a colon(1, N) call: " + index);
                newIndices.add(RangeInstruction.variable(indices.get(i)));
                continue;
            }

            String colonStart = colon.getInputVariables().get(0);
            String colonSize = colon.getInputVariables().get(1);
            EndInstruction end = endInstructions.get(colonSize);

            boolean startsAtOne = ConstantUtils.isConstantOne(typeGetter.apply(colonStart).get());
            if (end == null
                    || !startsAtOne
                    || !end.getInputVariable().equals(inputMatrix)
                    || end.getIndex() != i
                    || end.getNumIndices() != indices.size()) {
                // There are a few improvements we could do here, if we ever need them.
                // For instance, we should be able to improve this by detecting that 2 matrices
                // have identical sizes.

                // Regardless, we can fall back to partial ranges.
                newIndices.add(RangeInstruction.partialRange(colon.getInputVariables().get(0),
                        colon.getInputVariables().get(1)));
                ++convertedIndices;
                continue;
            }

            ++convertedIndices;
            newIndices.add(RangeInstruction.fullRange());
        }

        if (convertedIndices < 1) {
            logger.log("Only " + convertedIndices + " indices to convert");
            return Optional.empty();
        }

        logger.log("Indices: " + newIndices);
        return Optional.of(newIndices);
    }

}
