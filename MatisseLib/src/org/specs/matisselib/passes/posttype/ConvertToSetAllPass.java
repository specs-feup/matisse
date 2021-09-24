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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.passes.TypeTransparentSsaPass;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.EndInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.ssa.instructions.SetAllInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Converts matrix sets that cover the entire range (A(:) = ...) to a SetAll instruction.
 * 
 * @author Luís Reis
 * @see SetAllInstruction
 *
 */
public class ConvertToSetAllPass extends TypeTransparentSsaPass {

    public static final String PASS_NAME = "convert_to_set_all";

    @Override
    public void apply(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            DataStore passData) {

        Logger logger = PassUtils.getLogger(passData, PASS_NAME);
        DataProviderService compilerData = passData.get(ProjectPassServices.DATA_PROVIDER);

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
                        && call.getOutputs().size() == 1
                        && ConstantUtils.isConstantOne(typeGetter.apply(call.getInputVariables().get(0)).get()))
                .collect(Collectors.toMap(call -> call.getOutputs().get(0), Function.identity()));

        // Find gets and apply transformations
        boolean appliedTransformations = false;
        for (SsaBlock block : body.getBlocks()) {
            List<SsaInstruction> instructions = block.getInstructions();
            for (int i = 0; i < instructions.size(); i++) {
                int instructionId = i; // Capture variable for lambda

                SsaInstruction instruction = instructions.get(instructionId);
                if (instruction instanceof MatrixSetInstruction) {
                    MatrixSetInstruction set = (MatrixSetInstruction) instruction;

                    String inputMatrix = set.getInputMatrix();
                    List<String> indices = set.getIndices();
                    String value = set.getValue();

                    if (indices.size() != 1) {
                        logger.log("Set all instructions can only reference a single index.");
                        continue;
                    }
                    String index = indices.get(0);

                    String output = set.getOutput();

                    FunctionCallInstruction colon = colonInstructions.get(index);
                    if (colon == null) {
                        logger.log("Matrix index does not correspond to a colon(1, N) call: " + index);
                        continue;
                    }

                    String colonSize = colon.getInputVariables().get(1);
                    EndInstruction end = endInstructions.get(colonSize);

                    if (end == null) {
                        logger.log("Colon index not an end function: " + colonSize);
                        continue;
                    }
                    if (!end.getInputVariable().equals(inputMatrix)
                            || end.getIndex() != 0
                            || end.getNumIndices() != 1) {

                        logger.log("Invalid colon index");
                        continue;
                    }

                    appliedTransformations = true;
                    instructions.set(i, new SetAllInstruction(output, inputMatrix, value));
                }
            }
        }

        if (appliedTransformations) {
            compilerData.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION // Explicitly invalidated
        );
    }

}
