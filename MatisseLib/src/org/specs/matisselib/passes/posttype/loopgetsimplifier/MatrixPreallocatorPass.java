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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.helpers.ConventionalLoopVariableAnalysis;
import org.specs.matisselib.helpers.ForLoopHierarchy;
import org.specs.matisselib.helpers.ForLoopHierarchy.BlockData;
import org.specs.matisselib.helpers.LoopVariable;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.helpers.UsageMap;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.services.ScalarValueInformationBuilderService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.UndefinedInput;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.collect.Lists;

import pt.up.fe.specs.util.SpecsCollections;

/**
 * Identifies cases of matrices being declared (and grown) inside loops and preallocates them before the loop with the
 * appropriate size.
 * 
 * Example:
 * 
 * <pre>
 * <code>
 * function y = test(n)
 *   for i = 1:n,
 *     y(i) = i;
 *   end
 * end
 * </code>
 * </pre>
 * 
 * @author Luís Reis
 *
 */
public class MatrixPreallocatorPass implements PostTypeInferencePass {
    public static final String PASS_NAME = "matrix_preallocator";

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        Logger logger = PassUtils.getLogger(passData, PASS_NAME);
        if (PassUtils.skipPass(instance, PASS_NAME)) {
            logger.log("Skipping " + instance.getFunctionIdentification().getName());
            return;
        }
        logger.log("Starting " + instance.getFunctionIdentification().getName());

        IndexExtractionUtils indexUtils = new IndexExtractionUtils(logger::log);

        SystemFunctionProviderService functions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);
        ScalarValueInformationBuilderService scalarBuilder = passData
                .get(ProjectPassServices.SCALAR_VALUE_INFO_BUILDER_PROVIDER);

        Optional<SizeGroupInformation> sizeGroupInformation = passData.getTry(ProjectPassServices.DATA_PROVIDER)
                .flatMap(provider -> provider.tryGet(CompilerDataProviders.SIZE_GROUP_INFORMATION));

        FunctionBody body = instance.getFunctionBody();
        ForLoopHierarchy loops = ForLoopHierarchy.identifyLoops(body);

        Set<String> undefinedVars = new HashSet<>();

        for (SsaInstruction instruction : instance.getFlattenedInstructionsIterable()) {
            if (instruction instanceof AssignmentInstruction) {
                AssignmentInstruction assignment = (AssignmentInstruction) instruction;

                if (assignment.getInput() instanceof UndefinedInput) {
                    undefinedVars.add(assignment.getOutput());
                }
            }
        }

        logger.log("Undefined variables: " + undefinedVars);

        Map<Integer, List<LoopVariable>> conventionalLoopVariables = new HashMap<>();
        for (BlockData forLoop : loops.getForLoops()) {
            // We set enforceSameTypes to false because the initial type may be undefined.
            conventionalLoopVariables.put(forLoop.getBlockId(),
                    ConventionalLoopVariableAnalysis.analyzeStandardLoop(
                            body,
                            instance::getVariableType,
                            forLoop.getBlockId(),
                            false));
        }

        // Since we have enforceSameTypes to false, we have to check if the types are
        // compatible ourselves.
        for (int blockId : conventionalLoopVariables.keySet()) {
            List<LoopVariable> loopVariables = conventionalLoopVariables.get(blockId);

            for (LoopVariable loopVariable : new ArrayList<>(loopVariables)) {
                boolean areTypesCompatible = indexUtils.checkTypeCompatibility(instance, loopVariable);
                if (!areTypesCompatible) {
                    logger.log("Excluding " + loopVariable.loopStart + " due to type mismatches");
                    loopVariables.remove(loopVariable);
                }
            }
        }

        logger.log("Conventional Loop Variables: " + conventionalLoopVariables);

        UsageMap usageMap = UsageMap.build(body);

        for (BlockData forLoop : Lists.reverse(loops.getForLoops())) {
            logger.log("Checking " + forLoop);

            int blockId = forLoop.getBlockId();

            ForInstruction xfor = (ForInstruction) body.getBlock(blockId).getEndingInstruction().get();
            if (!ConstantUtils.isConstantOne(instance, xfor.getStart()) ||
                    !ConstantUtils.isConstantOne(instance, xfor.getInterval())) {

                logger.log("Skipping loop starting at #" + blockId
                        + ". Only loops with start/interval of 1 are supported");
                continue;
            }

            for (LoopVariable innerLoopVariable : conventionalLoopVariables.get(blockId)) {
                logger.log("Testing " + innerLoopVariable.loopStart);

                if (!instance.getVariableType(innerLoopVariable.beforeLoop).isPresent()
                        && !undefinedVars.contains(innerLoopVariable.beforeLoop)) {

                    logger.log("Non-standard undefined loop variable: " + innerLoopVariable.beforeLoop);
                    continue;
                }

                if (!indexUtils.checkUsageCounts(innerLoopVariable, usageMap, true)) {
                    continue;
                }

                List<Integer> chosenNesting = new ArrayList<>();
                List<LoopVariable> matchingVariables = new ArrayList<>();
                {
                    chosenNesting.add(blockId);

                    LoopVariable outerMostVariable = innerLoopVariable;
                    matchingVariables.add(outerMostVariable);
                    for (int containerBlockId : forLoop.getNesting()) {
                        Optional<LoopVariable> matchingVariable = indexUtils.findMatchingVariable(
                                instance,
                                conventionalLoopVariables.get(containerBlockId),
                                outerMostVariable,
                                usageMap);

                        if (!matchingVariable.isPresent()) {
                            break;
                        }

                        outerMostVariable = matchingVariable.get();
                        chosenNesting.add(containerBlockId);
                        matchingVariables.add(outerMostVariable);
                    }
                }

                while (!chosenNesting.isEmpty()) {
                    logger.log("Chosen nesting: " + chosenNesting);

                    int outerMostBlockId = SpecsCollections.last(chosenNesting);
                    LoopVariable outerMostVariable = matchingVariables.get(chosenNesting.size() - 1);

                    List<String> loopSizes = new ArrayList<>();
                    List<String> iters = new ArrayList<>();

                    BlockUtils.computeForLoopIterationsAndSizes(
                            instance.getFunctionBody(),
                            chosenNesting,
                            loopSizes,
                            iters);

                    boolean optimized = false;
                    SsaBlock loopBlock = instance.getBlock(xfor.getLoopBlock());
                    for (int instructionId = 0; instructionId < loopBlock.getInstructions().size(); ++instructionId) {
                        SsaInstruction instruction = loopBlock.getInstructions().get(instructionId);

                        if (instruction instanceof MatrixSetInstruction) {
                            MatrixSetInstruction set = (MatrixSetInstruction) instruction;

                            if (set.getInputMatrix().equals(innerLoopVariable.loopStart) &&
                                    set.getOutput().equals(innerLoopVariable.loopEnd)) {

                                Set<String> varsThatMustBeAccessible = new HashSet<>();
                                List<SsaInstruction> instructionsToInject = new ArrayList<>();

                                if (!indexUtils.verifyInstructionsToInject(instance,
                                        instructionsToInject,
                                        varsThatMustBeAccessible,
                                        chosenNesting,
                                        iters,
                                        set.getIndices(),
                                        loopSizes)) {
                                    logger.log("Can't find extra instructions to inject");
                                    continue;
                                }

                                if (!indexUtils.checkSizesDeclarationsValid(instance,
                                        SpecsCollections.last(chosenNesting),
                                        varsThatMustBeAccessible)) {
                                    continue;
                                }

                                if (!indexUtils.checkIndicesGrowWithIters(instance,
                                        scalarBuilder,
                                        iters,
                                        set.getIndices(),
                                        instructionsToInject,
                                        varsThatMustBeAccessible)) {
                                    logger.log("Optimization only works when indices grow with iterations");
                                    continue;
                                }

                                logger.log("Applying optimization");

                                List<String> inputValues = new ArrayList<>();

                                indexUtils.injectInstructions(instance,
                                        outerMostBlockId,
                                        instructionsToInject,
                                        inputValues,
                                        set.getIndices(),
                                        iters,
                                        loopSizes);

                                if (instance.getVariableType(outerMostVariable.beforeLoop).isPresent()) {
                                    injectResize(instance,
                                            sizeGroupInformation,
                                            outerMostBlockId,
                                            innerLoopVariable.loopStart,
                                            outerMostVariable.beforeLoop,
                                            inputValues,
                                            functions);
                                } else {
                                    preallocate(instance,
                                            sizeGroupInformation,
                                            outerMostBlockId,
                                            innerLoopVariable.loopStart,
                                            outerMostVariable.beforeLoop,
                                            inputValues,
                                            functions);
                                }

                                SsaInstruction newInstruction = new SimpleSetInstruction(set.getOutput(),
                                        set.getInputMatrix(), set.getIndices(), set.getValue());
                                loopBlock.replaceInstructionAt(instructionId, newInstruction);

                                optimized = true;
                                break;
                            }
                        }
                        Optional<Integer> tryEnd = instruction.tryGetEndBlock();
                        if (tryEnd.isPresent()) {
                            loopBlock = instance.getBlock(tryEnd.get());
                            instructionId = 0;
                        }
                    }

                    if (optimized) {
                        break;
                    }

                    chosenNesting = chosenNesting.subList(0, chosenNesting.size() - 1);
                }
            }
        }
    }

    private void preallocate(TypedInstance instance,
            Optional<SizeGroupInformation> sizeGroupInformation,
            int blockId,
            String loopStart,
            String beforeLoop,
            List<String> inputValues,
            SystemFunctionProviderService functions) {

        // Remove $beforeLoop = !undefined
        ListIterator<SsaInstruction> iterator = instance.getBlock(0).getInstructions().listIterator();
        while (iterator.hasNext()) {
            if (iterator.next().getOutputs().contains(beforeLoop)) {
                iterator.remove();
            }
        }

        SsaBlock block = instance.getBlock(blockId);

        if (inputValues.size() == 1) {
            String one = instance.makeTemporary("one", instance.getProviderData().getNumerics().newInt(1));
            SsaInstruction makeOne = AssignmentInstruction.fromInteger(one, 1);
            block.insertInstruction(block.getInstructions().size() - 1, makeOne);

            inputValues.add(0, one);
        }

        List<VariableType> inputTypes = inputValues
                .stream()
                .map(name -> instance.getVariableType(name).get())
                .collect(Collectors.toList());

        ProviderData providerData = instance.getProviderData().create(inputTypes);
        providerData.setOutputType(instance.getVariableType(loopStart).get());

        FunctionType functionType = functions.getSystemFunction("matisse_new_array_from_dims").get()
                .getType(providerData);
        SsaInstruction instruction = new TypedFunctionCallInstruction("matisse_new_array_from_dims", functionType,
                Arrays.asList(beforeLoop), inputValues);
        updateSizeInfo(sizeGroupInformation, instruction);

        block.insertInstruction(block.getInstructions().size() - 1, instruction);

        instance.addVariable(beforeLoop, functionType.getOutputTypes().get(0));
    }

    private void injectResize(TypedInstance instance,
            Optional<SizeGroupInformation> sizeGroupInformation,
            int blockId,
            String loopStart,
            String beforeLoop,
            List<String> inputValues,
            SystemFunctionProviderService functions) {

        // FIXME: Update sizeGroupInformation?

        SsaBlock block = instance.getBlock(blockId);

        Optional<VariableType> originalBeforeLoopType = instance.getVariableType(beforeLoop);

        String newVar = instance.makeTemporary(NameUtils.getSuggestedName(beforeLoop),
                instance.getVariableType(loopStart).get());

        Map<String, String> newNames = new HashMap<>();
        newNames.put(beforeLoop, newVar);
        instance.renameVariables(newNames);

        Map<String, String> reverseNewNames = new HashMap<>();
        reverseNewNames.put(newVar, beforeLoop);

        for (SsaInstruction instruction : instance.getFlattenedInstructionsList()) {
            if (instruction.getOutputs().contains(newVar)) {
                instruction.renameVariables(reverseNewNames);
                break;
            }
        }

        List<String> functionInputs = new ArrayList<>();
        functionInputs.add(beforeLoop);
        functionInputs.addAll(inputValues);

        List<VariableType> inputTypes = inputValues
                .stream()
                .map(name -> instance
                        // javac doesn't seem to infer .orElseThrow properly
                        .getVariableType(name).<RuntimeException> orElseThrow(() -> new RuntimeException(
                                "Could not find type of " + name)))
                .collect(Collectors.toList());
        inputTypes.add(0, instance.getVariableType(newVar).get());

        ProviderData providerData = instance.getProviderData().create(inputTypes);
        providerData.setOutputType(instance.getVariableType(loopStart).get());

        FunctionType functionType = functions.getSystemFunction("MATISSE_reserve_capacity").get()
                .getType(providerData);
        SsaInstruction newInstruction = new TypedFunctionCallInstruction("MATISSE_reserve_capacity", functionType,
                Arrays.asList(newVar), functionInputs);

        block.insertInstruction(block.getInstructions().size() - 1, newInstruction);

        // We have to re-add this because rename deleted the original variable type.
        instance.addVariable(beforeLoop, originalBeforeLoopType.get());
    }

    private void updateSizeInfo(Optional<SizeGroupInformation> sizeGroupInformation,
            SsaInstruction newInstruction) {

        sizeGroupInformation.ifPresent(info -> {
            info.addInstructionInformation(newInstruction);
        });
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
