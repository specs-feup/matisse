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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.helpers.sizeinfo.ScalarValueInformation;
import org.specs.matisselib.services.ScalarValueInformationBuilderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.loopproperties.NoIndexOverlapProperty;
import org.specs.matlabtocl.v2.ssa.ParallelRegionInstance;

public class MatrixConstructionReductionValidator implements ReductionValidator {

    private static final boolean ENABLE_LOG = false;

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

        boolean testIndexOverlap = false;
        for (int blockId : blocks) {
            boolean noIndexOverlap = parallelInstance.getBody().getBlock(blockId).getEndingInstruction()
                    .map(ForInstruction.class::cast)
                    .map(xfor -> xfor.getLoopProperties().stream().anyMatch(NoIndexOverlapProperty.class::isInstance))
                    .orElse(false);
            testIndexOverlap |= !noIndexOverlap;
        }

        log("Reduction variables: " + reductionVariables);
        FunctionBody functionBody = parallelInstance.getBody();

        if (iterVariables.contains(null)) {
            // If any iterations were unused, then it's impossible for there to be no conflicts
            log("Unused iteration variable");
            return Optional.empty();
        }

        List<List<String>> accesses = new ArrayList<>();
        List<List<String>> writes = new ArrayList<>();

        for (InstructionLocation midUsage : midUsageInstructions) {
            SsaInstruction instruction = functionBody.getInstructionAt(midUsage);
            if (instruction instanceof SimpleGetInstruction) {
                SimpleGetInstruction simpleGetInstruction = (SimpleGetInstruction) instruction;

                if (!reductionVariables.contains(simpleGetInstruction.getInputMatrix())) {
                    continue;
                }

                log("Get: " + instruction);
                accesses.add(simpleGetInstruction.getIndices());
                continue;
            }

            log("Unrecognized mid usage instruction: " + instruction);
            return Optional.empty();
        }

        List<ForInstruction> fors = new ArrayList<>();
        ForInstruction outerFor = (ForInstruction) functionBody.getBlock(outerBlockId).getEndingInstruction().get();
        fors.add(outerFor);
        ForInstruction innerMostFor = outerFor;
        for (int i = 1; i < iterVariables.size(); ++i) {
            innerMostFor = (ForInstruction) functionBody.getBlock(innerMostFor.getLoopBlock()).getEndingInstruction()
                    .get();

            fors.add(innerMostFor);
        }

        Set<String> variablesInLoop = BlockUtils.getVariablesDeclaredInContainedBlocks(functionBody,
                outerFor.getLoopBlock());
        Set<String> variablesInFunction = BlockUtils.getVariablesDeclaredInContainedBlocks(functionBody, 0);
        Set<String> externalVars = new HashSet<>(variablesInFunction);
        externalVars.removeAll(variablesInLoop);
        externalVars.addAll(parallelInstance.getInputVariables());

        log("External vars: " + externalVars);

        if (testIndexOverlap) {
            try (ScalarValueInformation scalarInfo = scalarBuilderService.build(parallelInstance::getType)) {
                for (String externalVar : externalVars) {
                    scalarInfo.addAlias(externalVar + "#1", externalVar + "#2");
                }

                String one = "#one";
                scalarInfo.specifyConstant(one, 1);

                for (int i = 0; i < iterVariables.size(); i++) {
                    String iter = iterVariables.get(i);
                    ForInstruction xfor = fors.get(i);

                    scalarInfo.setAtLeast(iter + "#1", one);
                    scalarInfo.setAtLeast(iter + "#2", one);
                    scalarInfo.setUpTo(iter + "#1", xfor.getEnd() + "#1");
                    scalarInfo.setUpTo(iter + "#2", xfor.getEnd() + "#1");

                    scalarInfo.setAtLeast(xfor.getEnd(), xfor.getStart(), "1");
                    scalarInfo.specifyConstant(xfor.getStart() + "#1", 1);
                }

                for (SsaInstruction instruction : functionBody.getFlattenedInstructionsIterable()) {
                    if (instruction instanceof FunctionCallInstruction) {
                        FunctionCallInstruction functionCall = (FunctionCallInstruction) instruction;

                        scalarInfo.addScalarFunctionCallInformation(functionCall, "1");
                        scalarInfo.addScalarFunctionCallInformation(functionCall, "2");
                    }
                }

                for (InstructionLocation location : constructionInstructions) {
                    SsaInstruction instruction = functionBody.getInstructionAt(location);

                    if (instruction instanceof AssignmentInstruction) {
                        // TODO
                        log("TODO: " + instruction);
                        return Optional.empty();
                    } else if (instruction instanceof SimpleSetInstruction) {
                        SimpleSetInstruction set = (SimpleSetInstruction) instruction;

                        List<String> indices = set.getIndices();

                        String input = set.getInputMatrix();
                        String output = set.getOutput();

                        log("Set: " + instruction);
                        if (!reductionVariables.contains(input)) {
                            log("Set not related to reduction.");
                            continue;
                        }

                        if (!parallelInstance.getType(input).equals(parallelInstance.getType(output))) {
                            // Implicit type casts are not supported
                            log("Implicit type cast");
                            return Optional.empty();
                        }

                        log("Set: " + instruction);
                        accesses.add(indices);
                        writes.add(indices);

                        // TODO
                    } else if (instruction instanceof PhiInstruction) {
                        // TODO
                        log("TODO: " + instruction);
                        return Optional.empty();
                    } else {
                        // Unrecognized operation
                        log("Unrecognized operation " + instruction);
                        return Optional.empty();
                    }
                }

                for (List<String> indices1 : accesses) {
                    for (List<String> indices2 : writes) {
                        log("Validating: " + indices1 + ", " + indices2);
                        if (scalarInfo.mayCollide(indices1, indices2, iterVariables, "1", "2")) {
                            // Right now, we only support matrix sets that use all iter variables directly.
                            log("Index variables may collide: " + indices1 + " with " + indices2 + ", relative to "
                                    + iterVariables);
                            return Optional.empty();
                        }
                    }
                }
            }
        }

        log("Valid set reduction");
        return Optional.of(ReductionType.MATRIX_SET);
    }

    private static void log(String message) {
        if (ENABLE_LOG) {
            System.out.print("[matrix_construction_reduction] ");
            System.out.println(message);
        }
    }
}