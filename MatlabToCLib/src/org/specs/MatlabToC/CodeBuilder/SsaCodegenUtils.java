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

package org.specs.MatlabToC.CodeBuilder;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.services.GlobalTypeProvider;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.Input;
import org.specs.matisselib.ssa.VariableInput;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.ParallelCopyInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matisselib.unssa.VariableAllocation;

public class SsaCodegenUtils {
    public static boolean isModified(FunctionBody body,
            GlobalTypeProvider globalTypeProvider,
            VariableAllocation allocations,
            int finalNameGroup,
            BiFunction<List<String>, GlobalTypeProvider, Optional<VariableType>> typeCombiner) {

        final VariableType groupType = typeCombiner.apply(
                allocations.getVariableGroups().get(finalNameGroup), globalTypeProvider)
                .orElseThrow(() -> new RuntimeException(
                        "Could not find type of " + allocations.getVariableGroups().get(finalNameGroup)));

        if (groupType instanceof ScalarType) {
            return false;
        }

        for (SsaInstruction instruction : body.getFlattenedInstructionsIterable()) {
            if (instruction instanceof ArgumentInstruction) {
                continue;
            }

            if (instruction instanceof AssignmentInstruction) {
                final AssignmentInstruction assignmentInstruction = (AssignmentInstruction) instruction;
                final String ssaOutput = assignmentInstruction.getOutput();
                final int ssaOutputGroup = allocations.getGroupIdForVariable(ssaOutput);
                if (ssaOutputGroup == finalNameGroup) {
                    final Input input = assignmentInstruction.getInput();
                    if (input instanceof VariableInput
                            && allocations
                                    .getGroupIdForVariable(((VariableInput) input).getName()) != ssaOutputGroup) {

                        return true;
                    }
                }
                continue;
            }
            if (instruction instanceof ParallelCopyInstruction) {
                final ParallelCopyInstruction parallelCopy = (ParallelCopyInstruction) instruction;

                final List<String> outputs = parallelCopy.getOutputs();
                final List<String> inputs = parallelCopy.getInputVariables();
                for (int i = 0; i < outputs.size(); ++i) {
                    final String ssaOutput = outputs.get(i);
                    final int ssaOutputGroup = allocations.getGroupIdForVariable(ssaOutput);

                    if (ssaOutputGroup == finalNameGroup) {
                        final String ssaInput = inputs.get(i);
                        final int ssaInputGroup = allocations.getGroupIdForVariable(ssaInput);

                        if (ssaOutputGroup != ssaInputGroup) {
                            return true;
                        }
                    }
                }

                continue;
            }
            if (instruction instanceof PhiInstruction) {
                final PhiInstruction phi = (PhiInstruction) instruction;

                final String ssaOutput = phi.getOutputs().get(0);
                final int ssaOutputGroup = allocations.getGroupIdForVariable(ssaOutput);

                if (ssaOutputGroup == finalNameGroup) {
                    for (final String ssaInput : phi.getInputVariables()) {
                        final int ssaInputGroup = allocations.getGroupIdForVariable(ssaInput);

                        if (ssaOutputGroup != ssaInputGroup) {
                            return true;
                        }
                    }
                }

                continue;
            }

            for (final String ssaOutputName : instruction.getOutputs()) {
                final int groupId = allocations.getGroupIdForVariable(ssaOutputName);

                if (groupId == finalNameGroup) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Optional<String> getFinalNameForArgument(FunctionBody body,
            List<String> variableNames,
            VariableAllocation allocations,
            int argumentIndex) {

        final List<String> names = body
                .getFlattenedInstructionsOfTypeStream(ArgumentInstruction.class)
                // That match our current argument
                .filter(instruction -> instruction.getArgumentIndex() == argumentIndex)
                // Get the SSA name
                .map(ArgumentInstruction::getOutput)
                // Convert the SSA name to the final name
                .map(variableName -> variableNames.get(allocations.getGroupIdForVariable(variableName)))
                .distinct()
                .collect(Collectors.toList());

        if (names.size() == 1) {
            return Optional.of(names.get(0));
        }
        return Optional.empty();
    }

    public static void processVariableNameChoices(TypedInstance instance,
            GlobalTypeProvider globalTypes,
            List<String> variableNames,
            VariableAllocation allocations) {

        processVariableNameChoices(instance, globalTypes, variableNames, allocations,
                instance::getCombinedVariableTypeFromVariables);
    }

    public static void processVariableNameChoices(TypedInstance instance,
            GlobalTypeProvider globalTypes,
            List<String> variableNames,
            VariableAllocation allocations,
            BiFunction<List<String>, GlobalTypeProvider, Optional<VariableType>> typeCombiner) {

        FunctionType functionType = instance.getFunctionType();
        List<String> argumentNames = functionType.getArgumentsNames();

        for (int i = 0; i < argumentNames.size(); ++i) {
            String argumentName = argumentNames.get(i);
            VariableType argumentType = functionType.getArgumentsTypes().get(i);

            Optional<String> potentialFinalName = SsaCodegenUtils.getFinalNameForArgument(
                    instance.getFunctionBody(),
                    variableNames, allocations, i);

            // Use of ifPresent crashes Eclipse's Java compiler.
            if (potentialFinalName.isPresent()) {
                String assignedVariable = potentialFinalName.get();

                int groupId = variableNames.indexOf(assignedVariable);
                Optional<VariableType> assignedType = instance
                        .getCombinedVariableTypeFromVariables(allocations.getVariableGroups().get(groupId),
                                globalTypes);

                if (!SsaCodegenUtils.isModified(instance.getFunctionBody(), globalTypes, allocations, groupId,
                        typeCombiner) &&
                        assignedType.isPresent() && argumentType.equals(assignedType.get())) {

                    variableNames.set(groupId, argumentName);
                }
            }
        }

        // We'll override name choices for the outputs-as-inputs.
        for (final String variableName : functionType.getOutputAsInputNames()) {
            final int groupId = allocations.getGroupIdForVariable(variableName + "$ret");
            variableNames.set(groupId, variableName);
        }
    }
}
