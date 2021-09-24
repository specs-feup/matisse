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

package org.specs.matisselib.passes.posttype.loopgetsimplifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.helpers.LoopVariable;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.helpers.UsageMap;
import org.specs.matisselib.helpers.sizeinfo.ScalarValueInformation;
import org.specs.matisselib.services.ScalarValueInformationBuilderService;
import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypedInstance;

import com.google.common.collect.Lists;

public class IndexExtractionUtils {
    private final Consumer<String> logger;

    public IndexExtractionUtils(Consumer<String> logger) {
        this.logger = logger;
    }

    public boolean checkIndicesGrowWithIters(TypedInstance instance,
            ScalarValueInformationBuilderService scalarBuilder,
            List<String> iters,
            List<String> indices,
            List<SsaInstruction> instructionsToInject,
            Set<String> externalVars) {

        try (ScalarValueInformation scalarInfo = scalarBuilder.build(instance::getVariableType)) {

            for (String externalVar : externalVars) {
                scalarInfo.addAlias(externalVar + "#1", externalVar + "#2");
            }

            for (SsaInstruction instruction : instructionsToInject) {
                if (instruction instanceof FunctionCallInstruction) {
                    FunctionCallInstruction functionCall = (FunctionCallInstruction) instruction;

                    scalarInfo.addScalarFunctionCallInformation(functionCall, "1");
                    scalarInfo.addScalarFunctionCallInformation(functionCall, "2");
                }
            }

            return scalarInfo.growsWith(indices, iters, "1", "2");
        }
    }

    public boolean checkSizesDeclarationsValid(TypedInstance instance,
            int blockId,
            Set<String> externalVars) {

        ForInstruction xfor = (ForInstruction) instance.getBlock(blockId).getEndingInstruction().get();
        Set<String> variablesDeclaredInLoop = BlockUtils.getVariablesDeclaredInContainedBlocks(
                instance.getFunctionBody(),
                xfor.getLoopBlock());

        for (String externalVar : externalVars) {
            if (variablesDeclaredInLoop.contains(externalVar)) {
                log("Declaration of " + externalVar + " is unavailable at the preallocation location.");
                return false;
            }
        }
        return true;
    }

    public Optional<LoopVariable> findMatchingVariable(TypedInstance instance,
            List<LoopVariable> candidateVariables,
            LoopVariable currentVariable,
            UsageMap usageMap) {

        log("Finding parent variable of " + currentVariable);
        log("Candidates are: " + candidateVariables);

        for (LoopVariable candidateVariable : candidateVariables) {
            if (!candidateVariable.loopStart.equals(currentVariable.beforeLoop) ||
                    !Optional.of(candidateVariable.loopEnd).equals(currentVariable.getAfterLoop())) {

                continue;
            }

            log("Testing " + candidateVariable + ", " + currentVariable);

            if (!checkUsageCounts(candidateVariable, usageMap, false)) {
                return Optional.empty();
            }

            if (!checkTypeCompatibility(instance, candidateVariable)) {
                log("Incompatible types");
                log(instance.getVariableType(candidateVariable.beforeLoop).toString());
                log(instance.getVariableType(candidateVariable.loopStart).toString());
                log(instance.getVariableType(candidateVariable.loopEnd).toString());
                if (candidateVariable.getAfterLoop().isPresent()) {
                    log(instance.getVariableType(candidateVariable.getAfterLoop().get()).toString());
                }
                return Optional.empty();
            }

            if (instance.getVariableType(candidateVariable.loopStart)
                    .equals(instance.getVariableType(currentVariable.loopStart))) {

                log("Found match");
                return Optional.of(candidateVariable);
            }

            return Optional.empty();
        }

        log("Could not find reasonable candidate");
        return Optional.empty();
    }

    public boolean checkUsageCounts(LoopVariable loopVariable, UsageMap usageMap, boolean innerMost) {
        if (usageMap.getUsageCount(loopVariable.loopStart) != (innerMost ? 1 : 2)) {

            log("(LoopStart) Variable has wrong number of uses: " + loopVariable.loopStart);
            log(usageMap.toString());
            return false;
        }

        if (loopVariable.getAfterLoop().isPresent()) {
            if (usageMap.getUsageCount(loopVariable.beforeLoop) != 2 ||
                    usageMap.getUsageCount(loopVariable.loopEnd) != 2) {

                log("Variable has wrong number of uses: " + loopVariable);
                log(usageMap.toString());
                return false;
            }
        } else {
            if (usageMap.getUsageCount(loopVariable.beforeLoop) != 1 ||
                    usageMap.getUsageCount(loopVariable.loopEnd) != 1) {

                log("Variable has wrong number of uses: " + loopVariable);
                log(usageMap.toString());
                return false;
            }
        }

        return true;
    }

    public boolean verifyInstructionsToInject(TypedInstance instance,
            List<SsaInstruction> instructionsToInject,
            Set<String> varsThatMustBeAccessible,
            List<Integer> loopBlocks,
            List<String> iters,
            List<String> indices,
            List<String> loopSizes) {

        Map<String, SsaInstruction> declarations = new HashMap<>();

        for (int blockId : loopBlocks) {
            ForInstruction xfor = (ForInstruction) instance.getBlock(blockId).getEndingInstruction().get();

            for (SsaInstruction instruction : instance.getBlock(xfor.getLoopBlock()).getInstructions()) {
                if (instruction instanceof IterInstruction
                        || instruction instanceof PhiInstruction
                        || instruction.getInstructionType() != InstructionType.NO_SIDE_EFFECT) {
                    continue;
                }

                for (String output : instruction.getOutputs()) {
                    declarations.put(output, instruction);
                }
            }
        }

        log(declarations.toString());

        Queue<String> varsToVisit = new LinkedList<>();
        Set<String> visitedVars = new HashSet<>();
        varsToVisit.addAll(indices);

        Set<SsaInstruction> unorderedInstructionsToInject = new HashSet<>();

        while (!varsToVisit.isEmpty()) {
            String var = varsToVisit.poll();
            if (!visitedVars.add(var)) {
                // Already visited

                continue;
            }

            int iterIndex = iters.indexOf(var);
            if (iterIndex >= 0) {
                varsThatMustBeAccessible.add(loopSizes.get(iterIndex));
                continue;
            }

            if (!declarations.containsKey(var)) {
                varsThatMustBeAccessible.add(var);
                continue;
            }

            SsaInstruction declaration = declarations.get(var);
            unorderedInstructionsToInject.add(declaration);

            varsToVisit.addAll(declaration.getInputVariables());
        }

        for (int blockId : Lists.reverse(loopBlocks)) {
            ForInstruction xfor = (ForInstruction) instance.getBlock(blockId).getEndingInstruction().get();

            for (SsaInstruction instruction : instance.getBlock(xfor.getLoopBlock()).getInstructions()) {
                if (unorderedInstructionsToInject.contains(instruction)) {
                    instructionsToInject.add(instruction);
                }
            }
        }

        assert unorderedInstructionsToInject.equals(new HashSet<>(instructionsToInject));

        return true;
    }

    public void injectInstructions(TypedInstance instance,
            int blockId,
            List<SsaInstruction> instructionsToInject,
            List<String> inputValues,
            List<String> indices,
            List<String> iters,
            List<String> loopSizes) {

        SsaBlock block = instance.getBlock(blockId);
        int injectionPoint = block.getInstructions().size() - 1;

        Map<String, String> newNames = new HashMap<>();
        for (int i = 0; i < iters.size(); ++i) {
            newNames.put(iters.get(i), loopSizes.get(i));
        }

        instructionsToInject.stream()
                .flatMap(instruction -> instruction.getOutputs().stream())
                .distinct()
                .forEach(output -> {
                    String newName = instance.makeTemporary("max_" + NameUtils.getSuggestedName(output),
                            instance.getVariableType(output));

                    newNames.put(output, newName);
                });

        for (SsaInstruction instructionToInject : instructionsToInject) {
            SsaInstruction newInstruction = instructionToInject.copy();
            newInstruction.renameVariables(newNames);

            block.insertInstruction(injectionPoint++, newInstruction);
        }

        for (String index : indices) {
            String newName = newNames.getOrDefault(index, index);
            inputValues.add(newName);
        }
    }

    public boolean checkTypeCompatibility(TypedInstance instance, LoopVariable loopVariable) {
        Optional<VariableType> referenceType = instance.getVariableType(loopVariable.loopStart);

        boolean beforeLoopTypeMatches = instance
                .getVariableType(loopVariable.beforeLoop)
                .map(type -> type.equals(referenceType.get()))
                .orElse(true);
        boolean afterLoopTypeMatches = !loopVariable.getAfterLoop().isPresent() ||
                instance.getVariableType(loopVariable.getAfterLoop().get()).equals(referenceType);

        boolean areTypesCompatible = beforeLoopTypeMatches &&
                instance.getVariableType(loopVariable.loopEnd).equals(referenceType) &&
                afterLoopTypeMatches;
        return areTypesCompatible;
    }

    public void log(String message) {
        logger.accept(message);
    }
}
