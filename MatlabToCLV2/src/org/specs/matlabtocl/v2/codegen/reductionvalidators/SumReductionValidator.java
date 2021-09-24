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

package org.specs.matlabtocl.v2.codegen.reductionvalidators;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.services.ScalarValueInformationBuilderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.ssa.ParallelRegionInstance;

public final class SumReductionValidator implements ReductionValidator {

    private static final boolean ENABLE_LOG = true;

    @Override
    public Optional<ReductionType> verifyReduction(
            ParallelRegionInstance parallelInstance,
            int outerBlockId,
            List<Integer> blocks,
            List<String> iterVariables,
            List<String> reductionVariables,
            List<InstructionLocation> constructionInstructions,
            List<InstructionLocation> midUsageInstructions,
            ScalarValueInformationBuilderService scalarBuilderService) {

        FunctionBody functionBody = parallelInstance.getBody();

        if (!midUsageInstructions.isEmpty()) {
            return Optional.empty();
        }

        for (InstructionLocation location : constructionInstructions) {
            SsaInstruction instruction = functionBody.getInstructionAt(location);

            for (String input : instruction.getInputVariables()) {
                if (!parallelInstance.getType(input)
                        .map(ScalarType.class::isInstance)
                        .orElse(false)) {

                    // Not a scalar operation
                    return Optional.empty();
                }
            }

            List<String> inputs = instruction.getInputVariables();
            if (instruction.getOutputs().size() != 1) {
                return Optional.empty();
            }

            String output = instruction.getOutputs().get(0);

            if (instruction instanceof PhiInstruction) {
                if (hasCast(parallelInstance::getType, inputs.get(1), output)) {
                    return Optional.empty();
                }
                if (hasCast(parallelInstance::getType, inputs.get(2), output)) {
                    return Optional.empty();
                }
                continue;
            }
            if (instruction instanceof AssignmentInstruction) {
                if (hasCast(parallelInstance::getType, inputs.get(0), output)) {
                    return Optional.empty();
                }
                continue;
            }
            if (instruction instanceof FunctionCallInstruction) {
                FunctionCallInstruction functionCall = (FunctionCallInstruction) instruction;

                String functionName = functionCall.getFunctionName();

                String reductionVariable = inputs.stream()
                        .filter(reductionVariables::contains)
                        .findFirst()
                        .get();

                if (hasCast(parallelInstance::getType, reductionVariable, output)) {
                    return Optional.empty();
                }

                if (functionName.equals("plus")) {
                    continue;
                }
                if (functionName.equals("minus")) {
                    int reductionIndex = inputs.indexOf(reductionVariable);
                    if (reductionIndex != 0) {
                        return Optional.empty();
                    }

                    continue;
                }

                return Optional.empty();
            }

            log("Not a sum reduction due to: " + instruction);
            return Optional.empty();
        }

        return Optional.of(ReductionType.SUM);
    }

    private static boolean hasCast(Function<String, Optional<VariableType>> typeGetter,
            String input,
            String output) {

        VariableType inputType = typeGetter.apply(input).get();
        VariableType outputType = typeGetter.apply(output).get();

        return !inputType.equals(outputType);
    }

    private static void log(String message) {
        if (SumReductionValidator.ENABLE_LOG) {
            System.out.print("[sum_validator] ");
            System.out.println(message);
        }
    }

}
