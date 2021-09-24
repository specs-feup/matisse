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

package org.specs.matlabtocl.v2.ssa.passes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.helpers.ForLoopHierarchy;
import org.specs.matisselib.helpers.ForLoopHierarchy.BlockData;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matisselib.unssa.ControlFlowGraph;
import org.specs.matlabtocl.v2.codegen.GeneratedKernel;
import org.specs.matlabtocl.v2.codegen.KernelArgument;
import org.specs.matlabtocl.v2.ssa.instructions.AllocateMatrixOnGpuInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.BufferBuilderFromMatrixInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.CopyToGpuInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.InvokeKernelInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Extracts "copy_to_gpu" instruction from loop bodies.
 * 
 * @author Luís Reis
 *
 */
public class LoopReadOnlyBufferCopyExtractionPass implements PostTypeInferencePass {

    private static final String PASS_NAME = "loop_read_only_buffer_copy_extraction";

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        Logger logger = PassUtils.getLogger(passData, PASS_NAME);

        if (PassUtils.skipPass(instance, PASS_NAME)) {
            logger.log("Skipping " + instance.getFunctionIdentification().getName());
            return;
        }

        logger.log("Starting " + instance.getFunctionIdentification().getName());

        ControlFlowGraph cfg = PassUtils.getData(passData, CompilerDataProviders.CONTROL_FLOW_GRAPH);

        ForLoopHierarchy loopHierarchy = ForLoopHierarchy.identifyLoops(instance.getFunctionBody());
        logger.log("Loops: " + loopHierarchy);

        // We are only going to move read-only buffers when dealing with CopyToGpuInstruction.
        // There are cases where moving modified buffers is safe, but detecting this is somewhat complicated.
        // For now, this will do.
        // For AllocateMatrixOnGpu, any data that's left can be treated as garbage, so we won't worry about it.
        Set<String> copyBlacklistedBuffers = new HashSet<>();
        for (SsaInstruction instruction : instance.getFlattenedInstructionsIterable()) {
            if (instruction instanceof InvokeKernelInstruction) {
                InvokeKernelInstruction invokeKernel = (InvokeKernelInstruction) instruction;

                GeneratedKernel kernel = invokeKernel.getInstance();
                for (int i = 0; i < kernel.getArguments().size(); ++i) {
                    KernelArgument kernelArgument = kernel.getArguments().get(i);
                    if (!kernelArgument.isReadOnly) {
                        String input = invokeKernel.getArguments().get(i);
                        logger.log("Non read-only buffer: " + instruction + ": " + input + "(" + kernelArgument.name
                                + ")");
                        copyBlacklistedBuffers.add(input);
                    }
                }
            } else {
                copyBlacklistedBuffers.addAll(instruction.getInputVariables());
            }
        }

        Map<String, Integer> declarationOf = new HashMap<>();
        for (int blockId = 0; blockId < instance.getBlocks().size(); ++blockId) {
            SsaBlock block = instance.getBlock(blockId);

            for (SsaInstruction instruction : block.getInstructions()) {
                for (String output : instruction.getOutputs()) {
                    declarationOf.put(output, blockId);
                }
            }
        }

        for (int parentBlockId = 0; parentBlockId < instance.getBlocks().size(); ++parentBlockId) {
            Optional<BlockData> optBlockData = loopHierarchy.getBlockData(parentBlockId);
            if (!optBlockData.isPresent()) {
                logger.log("Ignoring block #" + parentBlockId
                        + " because it was not visited by ForLoopHierarchy.identifyLoops.");
                continue;
            }

            // Since it is in ForLoopHierarchy, it is necessarily a for loop.
            ForInstruction xfor = (ForInstruction) instance.getBlock(parentBlockId).getEndingInstruction().get();
            int blockId = xfor.getLoopBlock();

            BlockData blockData = optBlockData.get();

            List<Integer> nesting = new ArrayList<>(blockData.getNesting());
            nesting.add(parentBlockId);

            SsaBlock block = instance.getBlock(blockId);
            instructions_visitor: for (ListIterator<SsaInstruction> iterator = block.getInstructions()
                    .listIterator(); iterator.hasNext();) {
                SsaInstruction instruction = iterator.next();
                if (instruction instanceof CopyToGpuInstruction
                        || instruction instanceof AllocateMatrixOnGpuInstruction) {

                    BufferBuilderFromMatrixInstruction builder = (BufferBuilderFromMatrixInstruction) instruction;

                    String inputMatrix = builder.getInput();
                    String outputBuffer = builder.getOutput();

                    if (instruction instanceof CopyToGpuInstruction && copyBlacklistedBuffers.contains(outputBuffer)) {
                        logger.log("Skipping " + inputMatrix + " because it is not read-only.");
                        continue;
                    }

                    int declarationBlockId = declarationOf.get(inputMatrix);
                    for (int ownerBlockId : nesting) {

                        if (ownerBlockId == declarationBlockId
                                || BlockUtils.covers(cfg, declarationBlockId, ownerBlockId)) {
                            logger.log("Moving allocation to block #" + ownerBlockId + ".");

                            iterator.remove();

                            SsaBlock motionBlock = instance.getBlock(ownerBlockId);
                            assert motionBlock.getEndingInstruction().get() instanceof ForInstruction;

                            // TODO: Add if block to check that loop has any iterations?
                            motionBlock.insertInstruction(motionBlock.getInstructions().size() - 1, instruction);

                            continue instructions_visitor;
                        } else {
                            logger.log("#" + declarationBlockId + " does not cover #" + ownerBlockId);
                        }
                    }

                    logger.log("Could not find any outer loop block to move " + outputBuffer + " to.");
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
