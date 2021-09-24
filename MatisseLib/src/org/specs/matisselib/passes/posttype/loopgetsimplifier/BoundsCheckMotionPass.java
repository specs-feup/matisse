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

package org.specs.matisselib.passes.posttype.loopgetsimplifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.helpers.ForLoopHierarchy;
import org.specs.matisselib.helpers.ForLoopHierarchy.BlockData;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.services.ScalarValueInformationBuilderService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.MatrixGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.ValidateTrueInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.collect.Lists;

import pt.up.fe.specs.util.SpecsCollections;

/**
 * This pass identifies instances of matrix_get in for loops and attempts to convert them to simple gets by moving the
 * validation code to before the loop.
 * <p>
 * This differs from {@link LoopAccessSimplifierPass} since this pass is applied in cases where the conversion is
 * <em>not</em> known to be safe (hence the extra validation).
 * 
 * @author Luís Reis
 *
 */
public class BoundsCheckMotionPass implements PostTypeInferencePass {

    public static final String PASS_NAME = "loopgetsimplifier_boundscheck";

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

        FunctionBody body = instance.getFunctionBody();
        ForLoopHierarchy loops = ForLoopHierarchy.identifyLoops(body);

        for (BlockData forLoop : Lists.reverse(loops.getForLoops())) {
            int blockId = forLoop.getBlockId();

            List<Integer> maximumNesting = new ArrayList<>();
            maximumNesting.add(blockId);
            maximumNesting.addAll(forLoop.getNesting());

            List<String> allSizes = new ArrayList<>();
            List<String> allIters = new ArrayList<>();

            BlockUtils.computeForLoopIterationsAndSizes(
                    instance.getFunctionBody(),
                    maximumNesting,
                    allSizes,
                    allIters);

            for (int i = 0; i < maximumNesting.size(); ++i) {
                int nestedBlockId = maximumNesting.get(i);

                ForInstruction xfor = (ForInstruction) body.getBlock(nestedBlockId).getEndingInstruction().get();

                if (!ConstantUtils.isConstantOne(instance, xfor.getStart()) ||
                        !ConstantUtils.isConstantOne(instance, xfor.getInterval())) {

                    logger.log("Skipping loop starting at #" + blockId
                            + ". Only loops with start/interval of 1 are supported");

                    maximumNesting = maximumNesting.subList(0, i);
                    break;
                }
            }

            if (maximumNesting.isEmpty()) {
                continue;
            }

            for (int nestingLevel = maximumNesting.size(); nestingLevel > 0; --nestingLevel) {
                List<Integer> chosenNesting = maximumNesting.subList(0, nestingLevel);
                List<String> chosenSizes = allSizes.subList(0, nestingLevel);
                List<String> chosenIters = allIters.subList(0, nestingLevel);

                logger.log("Testing: " + chosenNesting);

                int outerMostBlockId = SpecsCollections.last(chosenNesting);
                SsaBlock outerMostBlock = body.getBlock(outerMostBlockId);

                int innerMostBlockId = chosenNesting.get(0);
                SsaBlock containerBlock = body.getBlock(innerMostBlockId);
                ForInstruction xfor = (ForInstruction) containerBlock.getEndingInstruction().get();

                List<SsaInstruction> instructions = instance.getBlock(xfor.getLoopBlock()).getInstructions();
                for (int instructionId = 0; instructionId < instructions.size(); instructionId++) {
                    SsaInstruction instruction = instructions.get(instructionId);
                    if (instruction instanceof MatrixGetInstruction) {
                        MatrixGetInstruction get = (MatrixGetInstruction) instruction;

                        Set<String> varsThatMustBeAccessible = new HashSet<>();
                        List<SsaInstruction> instructionsToInject = new ArrayList<>();

                        List<String> indices = get.getIndices();
                        List<String> varsToCheck = new ArrayList<>();
                        varsToCheck.add(get.getInputMatrix());
                        varsToCheck.addAll(indices);

                        if (!indexUtils.verifyInstructionsToInject(instance,
                                instructionsToInject,
                                varsThatMustBeAccessible,
                                chosenNesting,
                                chosenIters,
                                varsToCheck,
                                chosenSizes)) {
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
                                chosenIters,
                                indices,
                                instructionsToInject,
                                varsThatMustBeAccessible)) {
                            logger.log("Optimization only works when indices grow with iterations");
                            continue;
                        }

                        logger.log("Applying optimization");

                        List<String> inputValues = new ArrayList<>();

                        indexUtils.injectInstructions(instance,
                                SpecsCollections.last(chosenNesting),
                                instructionsToInject,
                                inputValues,
                                indices,
                                chosenIters,
                                chosenSizes);

                        SsaInstruction lastInstruction = outerMostBlock.getEndingInstruction().get();
                        BlockEditorHelper helper = new BlockEditorHelper(instance, functions, outerMostBlockId);
                        helper.removeLastInstruction();
                        for (int i = 0; i < indices.size(); ++i) {
                            String sizeValue;
                            if (indices.size() == 1) {
                                sizeValue = helper.addSimpleCallToOutput("numel", get.getInputMatrix());
                            } else {
                                sizeValue = helper.addMakeEnd("size_result", get.getInputMatrix(), i, indices.size());
                            }

                            String isSizeSufficient = helper.addSimpleCallToOutputWithSemantics("ge",
                                    "is_size_sufficient", sizeValue, inputValues.get(i));
                            helper.addInstruction(new ValidateTrueInstruction(isSizeSufficient));
                        }

                        helper.addInstruction(lastInstruction);

                        instructions.set(instructionId,
                                new SimpleGetInstruction(get.getOutput(), get.getInputMatrix(), get.getIndices()));
                    }
                }
            }
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
