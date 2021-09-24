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

package org.specs.matisselib.passes.posttype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.functionproperties.DisableOptimizationProperty;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.helpers.ConventionalLoopVariableAnalysis;
import org.specs.matisselib.helpers.LoopVariable;
import org.specs.matisselib.helpers.UsageMap;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.VariableInput;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.collections.AccumulatorMap;

/**
 * Finds calls to functions such as zeros or ones, where the result is entirely overwritten before being read. In these
 * cases, we can replace the call to something such as "matisse_new_array_from_dims", which has lower overhead.
 * 
 * @author Luís Reis
 *
 */
public class AllocationValueEliminationPass implements PostTypeInferencePass {

    public static final String PASS_NAME = "allocation_value_elimination";

    private static final List<String> ALLOCATION_FUNCTIONS = Arrays.asList("zeros", "ones");
    private static final List<String> DATA_INVARIANT_FUNCTIONS = Arrays.asList("numel", "size", "ndims", "length");

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        Logger logger = PassUtils.getLogger(passData, PASS_NAME);

        if (PassUtils.skipPass(instance, PASS_NAME)) {
            logger.log("Skipping");
            return;
        }

        boolean skipAliasing = instance.getPropertyStream(DisableOptimizationProperty.class)
                .anyMatch(opt -> opt.getOptimizationId().equals("allocation_value_elimination_aliasing"));

        SizeGroupInformation sizeInfo = passData.get(ProjectPassServices.DATA_PROVIDER)
                .buildData(CompilerDataProviders.SIZE_GROUP_INFORMATION);

        Map<String, InstructionLocation> allocations = new HashMap<>();
        Map<Integer, String> iters = new HashMap<>();
        Map<String, String> iterAliases = new HashMap<>();
        Map<String, SimpleSetInstruction> simpleSets = new HashMap<>();
        Map<String, PhiInstruction> phiInstructions = new HashMap<>();
        AccumulatorMap<String> phis = new AccumulatorMap<>();
        UsageMap usages = UsageMap.build(instance.getFunctionBody());

        List<SsaBlock> blocks = instance.getBlocks();
        for (int blockId = 0; blockId < blocks.size(); blockId++) {
            SsaBlock block = blocks.get(blockId);
            List<SsaInstruction> instructions = block.getInstructions();
            for (int instructionId = 0; instructionId < instructions.size(); instructionId++) {
                SsaInstruction instruction = instructions.get(instructionId);
                if (instruction instanceof TypedFunctionCallInstruction) {

                    TypedFunctionCallInstruction functionCall = (TypedFunctionCallInstruction) instruction;
                    if (!ALLOCATION_FUNCTIONS.contains(functionCall.getFunctionName())) {
                        continue;
                    }
                    if (functionCall.getOutputs().size() != 1) {
                        continue;
                    }

                    String output = functionCall.getOutputs().get(0);

                    allocations.put(output, new InstructionLocation(blockId, instructionId));
                } else if (instruction instanceof AssignmentInstruction && !skipAliasing) {
                    AssignmentInstruction assignment = (AssignmentInstruction) instruction;

                    if (assignment.getInput() instanceof VariableInput) {
                        String input = assignment.getInputVariables().get(0);

                        iterAliases.put(assignment.getOutput(), input);
                    }
                } else if (instruction instanceof IterInstruction) {
                    String iter = ((IterInstruction) instruction).getOutput();
                    iters.put(blockId, iter);
                } else if (instruction instanceof SimpleSetInstruction) {
                    SimpleSetInstruction set = (SimpleSetInstruction) instruction;

                    simpleSets.put(set.getOutput(), set);
                }
            }
        }

        logger.log("Early possibilities: " + allocations.keySet());

        AccumulatorMap<String> dataInvariantUsages = new AccumulatorMap<>();

        for (SsaInstruction instruction : instance.getFlattenedInstructionsIterable()) {
            if (instruction instanceof PhiInstruction) {
                for (String input : instruction.getInputVariables()) {
                    phis.add(input);
                }
                PhiInstruction phiInstruction = (PhiInstruction) instruction;
                phiInstructions.put(phiInstruction.getOutput(), phiInstruction);
                continue;
            }
            if (instruction instanceof FunctionCallInstruction) {
                FunctionCallInstruction functionCall = (FunctionCallInstruction) instruction;

                if (DATA_INVARIANT_FUNCTIONS.contains(functionCall.getFunctionName())) {
                    for (String input : functionCall.getInputVariables()) {
                        dataInvariantUsages.add(input);
                    }
                    continue;
                }
            }
            for (String input : instruction.getInputVariables()) {
                if (allocations.containsKey(input)) {
                    logger.log("Not optimizing " + input + " due to " + instruction);
                }
                allocations.remove(input);
            }
        }

        for (String phi : phis.getAccMap().keySet()) {
            int count = phis.getCount(phi);
            if (count != 2) {
                if (allocations.containsKey(phi)) {
                    logger.log("Not optimizing " + phi + " due to incorrect number of phis: " + count);
                }
                allocations.remove(phi);
            }
        }

        logger.log("Candidates after usage exclusion: " + allocations.keySet());

        for (int blockId = 0; blockId < blocks.size(); blockId++) {
            SsaBlock block = blocks.get(blockId);
            Optional<ForInstruction> endingFor = block.getEndingInstruction()
                    .filter(ForInstruction.class::isInstance)
                    .map(ForInstruction.class::cast);

            if (!endingFor.isPresent()) {
                continue;
            }

            ForInstruction xfor = endingFor.get();
            if (!ConstantUtils.isConstantOne(instance.getVariableType(xfor.getStart())) ||
                    !ConstantUtils.isConstantOne(instance.getVariableType(xfor.getInterval()))) {
                continue;
            }

            String relevantIter = iters.get(xfor.getLoopBlock());
            String end = xfor.getEnd();
            List<LoopVariable> variables = ConventionalLoopVariableAnalysis.analyzeStandardLoop(instance, blockId,
                    true);

            logger.log("Loop variables: " + variables);
            for (LoopVariable variable : variables) {
                String beforeLoopVariable = variable.beforeLoop;
                if (!variable.getAfterLoop().isPresent()) {
                    logger.log("No after loop for  " + beforeLoopVariable);
                    continue;
                }

                if (!allocations.containsKey(beforeLoopVariable)) {
                    logger.log(beforeLoopVariable + " does not match any candidate allocation.");
                    continue;
                }
                InstructionLocation allocation = allocations.get(beforeLoopVariable);

                searchVariable(instance, variable, allocation, sizeInfo, usages,
                        Arrays.asList(relevantIter),
                        Arrays.asList(end),
                        iters,
                        iterAliases,
                        simpleSets,
                        phiInstructions,
                        dataInvariantUsages,
                        logger);
            }
        }
    }

    private void searchVariable(TypedInstance instance,
            LoopVariable variable,
            InstructionLocation allocation,
            SizeGroupInformation sizeInfo,
            UsageMap usages,
            List<String> loopIters,
            List<String> loopEnds,
            Map<Integer, String> iters,
            Map<String, String> iterAliases,
            Map<String, SimpleSetInstruction> simpleSets,
            Map<String, PhiInstruction> phiInstructions,
            AccumulatorMap<String> dataInvariantUsages,
            Logger logger) {

        int loopEndUsages = usages.getUsageCount(variable.loopEnd)
                - dataInvariantUsages.getCount(variable.loopEnd);
        if (loopEndUsages != 2) {
            logger.log("Wrong number of usages of " + variable.loopEnd + " (expected 2, got " + loopEndUsages + ")");
            return;
        }

        SimpleSetInstruction setBuilder = simpleSets.get(variable.loopEnd);
        PhiInstruction phiBuilder = phiInstructions.get(variable.loopEnd);
        if (setBuilder != null) {
            int loopStartUsages = usages.getUsageCount(variable.loopStart)
                    - dataInvariantUsages.getCount(variable.loopStart);
            if (loopStartUsages != 1) {
                logger.log("Wrong number of usages of " + variable.loopStart + " (expected 1, got " + loopStartUsages
                        + ")");
                return;
            }

            if (!setBuilder.getInputMatrix().equals(variable.loopStart)) {
                logger.log("simple_set not in expected format: " + setBuilder);
                return;
            }

            Set<String> usedIters = new HashSet<>();
            for (int i = 0; i < setBuilder.getIndices().size(); ++i) {
                String setIndex = setBuilder.getIndices().get(i);
                String aliasedIndex = iterAliases.getOrDefault(setIndex, setIndex);

                if (!loopIters.contains(aliasedIndex)) {
                    logger.log("Set index is not a loop iteration");
                    return;
                }

                if (!usedIters.add(aliasedIndex)) {
                    logger.log("Repeated iteration");
                    return;
                }

                String correspondingEnd = loopEnds.get(loopIters.indexOf(aliasedIndex));
                String relevantSize = i == setBuilder.getIndices().size() - 1
                        ? sizeInfo.getSizeSinceResult(variable.loopStart, i)
                        : sizeInfo.getSizeResult(variable.loopStart, i);
                if (!sizeInfo.areSameValue(correspondingEnd, relevantSize)) {
                    logger.log("Mismatch in number of iterations between " + aliasedIndex + " and " + relevantSize);
                    return;
                }
            }

            // Optimize

            logger.log("Optimize " + instance.getInstructionAt(allocation));
            optimize(instance, allocation, logger);
        } else if (phiBuilder != null) {
            int loopStartUsages = usages.getUsageCount(variable.loopStart)
                    - dataInvariantUsages.getCount(variable.loopStart);
            if (loopStartUsages != 2) {
                logger.log("Wrong number of usages of " + variable.loopStart + " (expected 2, got " + loopStartUsages
                        + ")");
                return;
            }

            if (phiBuilder.getSourceBlocks().size() != 2) {
                logger.log("Phi has wrong number of source blocks");
                return;
            }

            int beforeNestedBlockIndex = phiBuilder.getInputVariables().indexOf(variable.loopStart);
            if (beforeNestedBlockIndex < 0) {
                logger.log("Phi does not reference " + variable.beforeLoop);
                return;
            }
            int beforeNestedBlockId = phiBuilder.getSourceBlocks().get(beforeNestedBlockIndex);

            Optional<ForInstruction> endingFor = instance.getBlock(beforeNestedBlockId)
                    .getEndingInstruction()
                    .filter(ForInstruction.class::isInstance)
                    .map(ForInstruction.class::cast);
            if (!endingFor.isPresent()) {
                logger.log("Variable not constructed from nested loop");
                return;
            }
            ForInstruction xfor = endingFor.get();
            if (!ConstantUtils.isConstantOne(instance.getVariableType(xfor.getStart())) ||
                    !ConstantUtils.isConstantOne(instance.getVariableType(xfor.getInterval()))) {

                logger.log("For does not start at 1");
                return;
            }

            List<String> ends = new ArrayList<>(loopEnds);
            ends.add(xfor.getEnd());

            List<String> newIters = new ArrayList<>(loopIters);
            newIters.add(iters.get(xfor.getLoopBlock()));

            List<LoopVariable> nestedVariables = ConventionalLoopVariableAnalysis.analyzeStandardLoop(instance,
                    beforeNestedBlockId,
                    true);
            Optional<LoopVariable> nestedVariable = nestedVariables.stream()
                    .filter(lv -> lv.getAfterLoop().equals(Optional.of(variable.loopEnd)))
                    .findAny();
            if (!nestedVariable.isPresent()) {
                logger.log("No matching variable");
                return;
            }
            if (!nestedVariable.get().beforeLoop.equals(variable.loopStart)) {
                logger.log("Loop variable not in expected format");
                return;
            }

            searchVariable(instance, nestedVariable.get(), allocation, sizeInfo, usages, newIters, ends, iters,
                    iterAliases,
                    simpleSets,
                    phiInstructions, dataInvariantUsages,
                    logger);
        } else {
            logger.log("No matching simple_set instruction for " + variable.loopEnd);
            return;
        }
    }

    private void optimize(TypedInstance instance, InstructionLocation instructionLocation, Logger logger) {
        TypedFunctionCallInstruction original = (TypedFunctionCallInstruction) instance
                .getInstructionAt(instructionLocation);

        String functionName;
        List<String> inputs = original.getInputVariables();
        if (inputs.size() == 1) {
            String input = inputs.get(0);
            Optional<VariableType> inputType = instance.getVariableType(input);
            if (MatrixUtils.isMatrix(inputType)) {
                functionName = "matisse_new_array";
            } else if (ScalarUtils.isScalar(inputType)) {
                functionName = "matisse_new_array_from_dims";
                inputs = Arrays.asList(input, input);
            } else {
                // Can't handle this case.
                logger.log("Can't handle case, input=" + inputType);
                return;
            }
        } else {
            for (String input : inputs) {
                if (!ScalarUtils.isScalar(instance.getVariableType(input))) {
                    // Can't handle this case.
                    logger.log("Can't handle " + input);
                    return;
                }
            }

            functionName = "matisse_new_array_from_dims";
        }

        logger.log("Replacing");
        TypedFunctionCallInstruction newInstruction = new TypedFunctionCallInstruction(functionName,
                original.getFunctionType(), original.getOutputs(), inputs);
        instance.setInstructionAt(instructionLocation, newInstruction);
    }
}
