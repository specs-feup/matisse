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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.helpers.UsageMap;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * <p>
 * Although {@link LoopStartNormalizationPass} enables some passes to run, it also seems to make the generated code be
 * of worse quality, both in terms of readability and performance (e.g., dirich).
 * <p>
 * This pass reverts the changes of that pass. It is meant to be a late-stage optimization.
 * 
 * @author Luís Reis
 *
 */
public class LoopIterationSimplificationPass implements PostTypeInferencePass {

    public static final String PASS_NAME = "loop_iteration_simplification";

    @Override
    public void apply(TypedInstance instance, DataStore passData) {

        Logger logger = PassUtils.getLogger(passData, PASS_NAME);

        if (PassUtils.skipPass(instance, PASS_NAME)) {
            logger.log("Skipping " + instance.getFunctionIdentification());
            return;
        }

        UsageMap usages = UsageMap.build(instance.getFunctionBody());

        logger.log("Starting " + instance.getFunctionIdentification());

        Map<String, String> subtractionsOne = new HashMap<>();
        Map<String, List<String>> subtractions = new HashMap<>();

        for (SsaInstruction instruction : instance.getFlattenedInstructionsIterable()) {
            if (instruction instanceof TypedFunctionCallInstruction) {
                TypedFunctionCallInstruction functionCall = (TypedFunctionCallInstruction) instruction;

                if (!functionCall.getFunctionName().equals("minus")) {
                    continue;
                }

                List<String> inputs = functionCall.getInputVariables();
                if (inputs.size() != 2) {
                    continue;
                }
                if (functionCall.getOutputs().size() != 1) {
                    continue;
                }

                String output = functionCall.getOutputs().get(0);

                String input1 = inputs.get(1);
                if (ConstantUtils.isConstantOne(instance, input1)) {
                    subtractionsOne.put(output, inputs.get(0));
                }

                subtractions.put(output, inputs);
            }
        }

        Map<Integer, Integer> declarators = new HashMap<>();
        Map<Integer, String> offsets = new HashMap<>();
        Map<Integer, String> ends = new HashMap<>();

        List<SsaBlock> blocks = instance.getBlocks();
        for (int blockId = 0; blockId < blocks.size(); blockId++) {
            int capturedBlockId = blockId;

            SsaBlock block = blocks.get(blockId);
            block.getEndingInstruction()
                    .filter(ForInstruction.class::isInstance)
                    .map(ForInstruction.class::cast)
                    .ifPresent(xfor -> {
                        String start = xfor.getStart();
                        if (!ConstantUtils.isConstantOne(instance, start)) {
                            return;
                        }

                        String end = xfor.getEnd();
                        List<String> endSource = subtractions.get(end);
                        if (endSource == null) {
                            return;
                        }

                        assert endSource.size() == 2;

                        String source0 = endSource.get(0);
                        String source1 = endSource.get(1);

                        String source1Base = subtractionsOne.get(source1);
                        if (source1Base == null) {
                            return;
                        }

                        declarators.put(xfor.getLoopBlock(), capturedBlockId);
                        offsets.put(capturedBlockId, source1);
                        ends.put(capturedBlockId, source0);
                    });
        }

        logger.log("Considering loops: " + declarators);

        candidate_loops: for (int loopBlockId : declarators.keySet()) {
            int parentBlockId = declarators.get(loopBlockId);

            SsaBlock loopBlock = instance.getBlock(loopBlockId);
            String iter = null;
            String newIter = null;
            String displacement = offsets.get(parentBlockId);
            String proposedOffset = subtractionsOne.get(displacement);
            String proposedEnd = ends.get(parentBlockId);
            int numUsages = 0;

            logger.log("Testing " + loopBlockId + ":" + parentBlockId);

            for (SsaInstruction instruction : loopBlock.getInstructions()) {
                if (instruction instanceof IterInstruction) {
                    if (iter != null) {
                        logger.log("Skipping: Multiple iters?");
                        continue candidate_loops;
                    }

                    iter = ((IterInstruction) instruction).getOutput();

                    numUsages = usages.getUsageCount(iter);
                    if (numUsages > 1) {
                        logger.log("Iter is used multiple times");
                        continue candidate_loops;
                    }
                }

                if (instruction instanceof TypedFunctionCallInstruction) {
                    TypedFunctionCallInstruction functionCall = (TypedFunctionCallInstruction) instruction;

                    if (!functionCall.getFunctionName().equals("plus")) {
                        continue;
                    }

                    List<String> inputs = functionCall.getInputVariables();
                    if (inputs.size() != 2) {
                        continue;
                    }
                    if (!inputs.get(0).equals(iter)) {
                        continue;
                    }

                    if (functionCall.getOutputs().size() != 1) {
                        continue candidate_loops;
                    }
                    if (!inputs.get(1).equals(displacement)) {
                        logger.log("Expected " + displacement + ", got " + inputs.get(1));
                        continue candidate_loops;
                    }

                    newIter = functionCall.getOutputs().get(0);
                }
            }

            if (numUsages == 1 && newIter == null) {
                logger.log("Wrong number of iter usages");
                continue candidate_loops;
            }

            logger.log("Simplifying iterations of #" + parentBlockId);

            SsaBlock parentBlock = instance.getBlock(parentBlockId);
            ForInstruction originalFor = (ForInstruction) parentBlock.getEndingInstruction().get();

            ForInstruction newFor = new ForInstruction(proposedOffset,
                    originalFor.getInterval(),
                    proposedEnd, loopBlockId, originalFor.getEndBlock(),
                    originalFor.getLoopProperties());
            parentBlock.replaceInstructionAt(parentBlock.getInstructions().size() - 1, newFor);

            if (newIter == null && iter != null && iter.startsWith("$base_")) {
                // Rename the iteration variable from base_<X> to <X>.
                // If it has some other name (not starting with base_) then don't rename it.

                String iterName = NameUtils.getSuggestedName(iter.substring("$base_".length()));
                String newProposedName = instance.makeTemporary(iterName, instance.getVariableType(iter));
                Map<String, String> newNames = new HashMap<>();
                newNames.put(iter, newProposedName);
                instance.renameVariables(newNames);
            }
            if (newIter != null) {
                assert iter != null;

                ListIterator<SsaInstruction> iterator = loopBlock.getInstructions().listIterator();
                while (iterator.hasNext()) {
                    SsaInstruction instruction = iterator.next();

                    if (instruction instanceof IterInstruction) {
                        iterator.remove();
                    }
                    if (instruction.getOutputs().equals(Arrays.asList(newIter))) {
                        iterator.remove();
                        iterator.add(new IterInstruction(newIter));
                    }
                }
            }
        }
    }

}
