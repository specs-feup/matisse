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
import java.util.Map;
import java.util.Set;

import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.EndInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matlabtocl.v2.CLServices;
import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.ssa.instructions.CompleteReductionInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.CopyToGpuInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.InvokeKernelInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.SetGpuRangeInstruction;
import org.specs.matlabtocl.v2.types.api.GpuGlobalBufferType;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.collections.MultiMap;

/**
 * 
 * @author Luís Reis
 *
 */
public class GpuSVMBufferEliminationPass implements PostTypeInferencePass {

    private static final boolean ENABLE_LOG = false;

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        GpuSVMEliminationMode eliminationMode = passData.get(CLServices.CODE_GENERATION_STRATEGY_PROVIDER)
                .getSVMEliminationMode();

        if (eliminationMode == GpuSVMEliminationMode.DO_NOT_ELIMINATE
                || PassUtils.skipPass(instance, "svm_buffer_elimination")) {
            return;
        }

        boolean tolerateOutOfLoopCopies = eliminationMode == GpuSVMEliminationMode.ELIMINATE_COPIES_OUT_OF_LOOPS;

        log("Starting " + instance.getFunctionIdentification().getName());
        log("Tolerate out of loop copies: " + tolerateOutOfLoopCopies);

        while (true) {
            boolean optimized = optimizeSequentialRedundant(instance);
            optimized |= optimizeChain(instance, tolerateOutOfLoopCopies);
            if (tolerateOutOfLoopCopies) {
                optimized |= optimizeReadOnlyUsesInLoops(instance);
            }

            if (!optimized) {
                break;
            }

            log("Repeat");
        }
    }

    private boolean optimizeReadOnlyUsesInLoops(TypedInstance instance) {
        MultiMap<Integer, String> internalVars = new MultiMap<>();
        MultiMap<Integer, String> invokedSvmArgs = new MultiMap<>();

        List<Integer> rootNest = new ArrayList<>();
        rootNest.add(0);
        processBlock(instance, internalVars, invokedSvmArgs, rootNest, 0, true);

        log("Invoked SVM args: " + invokedSvmArgs);

        return optimizeReadOnlyUsesInLoops(instance, internalVars, invokedSvmArgs, new HashMap<>(), 0, 0);
    }

    private boolean optimizeReadOnlyUsesInLoops(TypedInstance instance,
            MultiMap<Integer, String> internalVars,
            MultiMap<Integer, String> invokedSvmArgs,
            Map<String, String> optimizing,
            int currentLoop,
            int blockId) {

        boolean optimized = false;

        SsaBlock block = instance.getBlock(blockId);
        List<SsaInstruction> instructions = block.getInstructions();
        for (int instructionId = 0; instructionId < instructions.size(); instructionId++) {
            SsaInstruction instruction = instructions.get(instructionId);

            if (instruction instanceof InvokeKernelInstruction) {
                InvokeKernelInstruction invoke = (InvokeKernelInstruction) instruction;

                if (invoke.getArguments().stream().anyMatch(var -> optimizing.containsKey(var))) {
                    optimized = true;
                    invoke.renameVariables(optimizing);
                }

                continue;
            }

            if (instruction instanceof ForInstruction) {
                ForInstruction xfor = (ForInstruction) instruction;
                int loopBlock = xfor.getLoopBlock();

                Set<String> varsToOptimize = new HashSet<>(invokedSvmArgs.get(loopBlock));
                varsToOptimize.removeAll(internalVars.get(loopBlock));

                log("Vars to optimize at #" + loopBlock + ": " + varsToOptimize);

                Map<String, String> innerOptimizing = new HashMap<>(optimizing);

                for (String var : varsToOptimize) {
                    if (!innerOptimizing.containsKey(var)) {
                        String buffer = makeBuffer(instance, block, var, instructionId);

                        innerOptimizing.put(var, buffer);
                    }
                }

                optimized |= optimizeReadOnlyUsesInLoops(instance, internalVars, invokedSvmArgs,
                        innerOptimizing,
                        xfor.getLoopBlock(), xfor.getLoopBlock());
                optimized |= optimizeReadOnlyUsesInLoops(instance, internalVars, invokedSvmArgs,
                        innerOptimizing,
                        currentLoop, xfor.getEndBlock());

                return optimized;
            }

            for (int ownedBlock : instruction.getOwnedBlocks()) {
                optimized |= optimizeReadOnlyUsesInLoops(instance, internalVars, invokedSvmArgs, optimizing,
                        currentLoop, ownedBlock);
            }
        }

        return optimized;
    }

    private void processBlock(TypedInstance instance,
            MultiMap<Integer, String> internalVars,
            MultiMap<Integer, String> invokedSvmArgs,
            List<Integer> currentNest,
            int blockId,
            boolean safeContext) {

        SsaBlock block = instance.getBlock(blockId);
        for (SsaInstruction instruction : block.getInstructions()) {
            if (instruction instanceof InvokeKernelInstruction) {
                InvokeKernelInstruction invoke = (InvokeKernelInstruction) instruction;

                List<String> arguments = invoke.getArguments();
                for (int inputIndex = 0; inputIndex < arguments.size(); inputIndex++) {
                    String argument = arguments.get(inputIndex);
                    if (invoke.getOutputSources().contains(inputIndex)) {
                        log("Ignoring " + argument + " because it is not read-only");
                        continue;
                    }

                    boolean isMatrix = instance
                            .getVariableType(argument)
                            .filter(DynamicMatrixType.class::isInstance)
                            .isPresent();
                    if (!isMatrix) {
                        log("Ignoring " + argument + " because it is not an SVM variable");
                        continue;
                    }

                    if (safeContext) {
                        // Not the best use, but it's not awful either
                        registerVar(invokedSvmArgs, currentNest, argument);
                    }
                }

                for (String output : invoke.getOutputs()) {
                    registerVar(internalVars, currentNest, output);
                }

                continue;
            }

            for (String output : instruction.getOutputs()) {
                registerVar(internalVars, currentNest, output);
            }

            if (instruction instanceof ForInstruction) {
                ForInstruction xfor = (ForInstruction) instruction;

                List<Integer> innerNest = new ArrayList<>(currentNest);
                innerNest.add(xfor.getLoopBlock());
                processBlock(instance, internalVars, invokedSvmArgs, innerNest, xfor.getLoopBlock(), true);
                processBlock(instance, internalVars, invokedSvmArgs, currentNest, xfor.getEndBlock(), true);
                continue;
            }

            for (int innerBlockId : instruction.getOwnedBlocks()) {
                processBlock(instance, internalVars, invokedSvmArgs, currentNest, innerBlockId, false);
            }
        }
    }

    private void registerVar(MultiMap<Integer, String> group, List<Integer> currentNest, String var) {
        for (int loop : currentNest) {
            group.put(loop, var);
        }
    }

    /**
     * Optimizes cases where a chain of SVM variables is only ever used on the GPU.
     */
    private boolean optimizeChain(TypedInstance instance, boolean tolerateOutOfLoopCopies) {

        log("Checking chains");

        Map<Integer, Integer> parentOfLoop = new HashMap<>();

        for (int blockId = 0; blockId < instance.getBlocks().size(); ++blockId) {
            SsaBlock block = instance.getBlocks().get(blockId);

            int capturedBlockId = blockId;

            block.getEndingInstruction()
                    .filter(ForInstruction.class::isInstance)
                    .map(ForInstruction.class::cast)
                    .map(xfor -> xfor.getLoopBlock())
                    .ifPresent(loop -> parentOfLoop.put(loop, capturedBlockId));
        }

        boolean optimized = false;

        for (int blockId = 0; blockId < instance.getBlocks().size(); ++blockId) {
            // A chain of variables A -> B -> C are SVM variables that are used in kernels
            // If *any* variable in the chain is "invalidly used", then the whole chain can not be made pure-GPU.
            // When an SVM variable is used in a read-only manner, it is not added to a chain *if* it is the last
            // element of that chain. If it is another element, then the whole chain is invalidated.
            // Each variable can only appear in a chain. Otherwise: invalidated.
            // No $ret variable can ever be in a chain
            List<List<String>> variableChains = new ArrayList<>();
            List<Boolean> isChainUseful = new ArrayList<>();

            // A phi-chain is a variable chain starting in a phi node. The end of the chain must match the in-loop input
            // of the input. Phi-chains only make sense in tolerateOutOfLoopCopies mode.
            // If the phi does not match, then invalidate the whole chain.
            Map<String, String> phiChains = new HashMap<>();
            // TODO: Do we want to check the phis *after* a given loop?

            int currentBlockId = blockId;
            SsaBlock block = instance.getBlock(currentBlockId);

            for (int instructionId = 0; instructionId < block.getInstructions().size(); ++instructionId) {
                SsaInstruction instruction = block.getInstructions().get(instructionId);

                if (instruction instanceof PhiInstruction) {
                    PhiInstruction phi = (PhiInstruction) instruction;

                    if (phi.getSourceBlocks().size() == 2 && tolerateOutOfLoopCopies) {
                        if (parentOfLoop.containsKey(currentBlockId)) {
                            String output = phi.getOutput();
                            int parentBlock = parentOfLoop.get(currentBlockId);
                            int loopEndIndex = 1 - phi.getSourceBlocks().indexOf(parentBlock);

                            phiChains.put(output, phi.getInputVariables().get(loopEndIndex));
                            List<String> outputChain = new ArrayList<>();
                            outputChain.add(output);
                            variableChains.add(outputChain);
                            isChainUseful.add(false);
                            continue;
                        } else {
                            log("Ignoring phi as it is not a loop-start phi: " + phi);
                        }
                    }

                    // No continue here
                }

                if (instruction instanceof InvokeKernelInstruction) {
                    InvokeKernelInstruction invoke = (InvokeKernelInstruction) instruction;

                    List<String> outputs = invoke.getOutputs();
                    for (int i = 0; i < outputs.size(); i++) {
                        String output = outputs.get(i);
                        String input = invoke.getArguments().get(invoke.getOutputSources().get(i));

                        for (int chainIndex = 0; chainIndex < variableChains.size(); chainIndex++) {
                            List<String> chain = variableChains.get(chainIndex);
                            if (chain.get(chain.size() - 1).equals(input)) {
                                chain.add(output);
                                isChainUseful.set(chainIndex, true);
                                break;
                            } else if (chain.contains(input)) {
                                variableChains.remove(chainIndex);
                                isChainUseful.remove(chainIndex);
                                --chainIndex;
                                break;
                            }
                        }

                        // If not on any chain, it was most likely defined out of the block.
                        // We won't deal with that case.
                    }

                    List<String> arguments = invoke.getArguments();
                    for (int inputIndex = 0; inputIndex < arguments.size(); inputIndex++) {
                        if (invoke.getOutputSources().contains(inputIndex)) {
                            continue;
                        }

                        String input = arguments.get(inputIndex);
                        boolean foundChain = false;
                        for (int chainIndex = 0; chainIndex < variableChains.size(); chainIndex++) {
                            List<String> chain = variableChains.get(chainIndex);

                            if (chain.contains(input)) {
                                if (!chain.get(chain.size() - 1).equals(input)) {
                                    // Not last in chain
                                    log("Removing chain due to input " + input + " not in end: " + chain);

                                    variableChains.remove(chainIndex);
                                    isChainUseful.remove(chainIndex);
                                    --chainIndex;
                                }
                            }
                        }
                    }

                    log("Chains so far: " + variableChains);

                    continue;
                }

                if (instruction instanceof SetGpuRangeInstruction) {
                    SetGpuRangeInstruction setGpuRange = (SetGpuRangeInstruction) instruction;

                    if (setGpuRange.getOutput().isPresent()) {
                        String input = setGpuRange.getBuffer();
                        String output = setGpuRange.getOutput().get();

                        for (int chainIndex = 0; chainIndex < variableChains.size(); chainIndex++) {
                            List<String> chain = variableChains.get(chainIndex);
                            if (chain.get(chain.size() - 1).equals(input)) {
                                chain.add(output);
                                isChainUseful.set(chainIndex, true);
                                break;
                            } else if (chain.contains(input)) {
                                variableChains.remove(chainIndex);
                                isChainUseful.remove(chainIndex);
                                --chainIndex;
                                break;
                            }
                        }

                        log("Chains so far: " + variableChains);

                        continue;
                    }
                }

                if (instruction instanceof FunctionCallInstruction) {
                    FunctionCallInstruction functionCall = (FunctionCallInstruction) instruction;

                    String functionName = functionCall.getFunctionName();
                    if (functionName.equals("size") || functionName.equals("numel") || functionName.equals("ndims")) {
                        markBadUses(variableChains, isChainUseful, functionCall.getOutputs(), instruction); // Outputs
                                                                                                            // only
                        continue;
                    } else if (functionName.equals("matisse_new_array")
                            || functionName.equals("matisse_new_array_from_dims") ||
                            functionName.equals("matisse_new_array_from_matrix")) {

                        log("Found allocation: " + instruction);

                        // Not a bad use
                        List<String> outputChain = new ArrayList<>();
                        outputChain.add(functionCall.getOutputs().get(0));
                        variableChains.add(outputChain);
                        isChainUseful.add(false);

                        continue;
                    }

                    // No continue
                }

                if (instruction instanceof EndInstruction) {
                    continue;
                }

                markBadUses(variableChains, isChainUseful, instruction.getOutputs(), instruction);
                markBadUses(variableChains, isChainUseful, instruction.getInputVariables(), instruction);

                int endBlockId = instruction.tryGetEndBlock().orElse(-1);

                for (int ownedBlockId : instruction.getOwnedBlocks()) {
                    if (ownedBlockId != endBlockId) {
                        checkUsesInBlock(instance, ownedBlockId, variableChains, isChainUseful);
                    }
                }

                if (endBlockId >= 0) {
                    currentBlockId = endBlockId;
                    block = instance.getBlock(currentBlockId);
                    instructionId = -1;
                }
            }

            for (int i = 0; i < variableChains.size(); ++i) {
                List<String> chain = variableChains.get(i);

                if (chain.stream().anyMatch(var -> var.endsWith("$ret")) && !phiChains.containsKey(chain.get(0))) {
                    // Allow optimizable "$ret"s inside of loops.
                    log("Excluding chain due to presence of return variable " + chain);

                    variableChains.remove(i);
                    isChainUseful.remove(i);
                    --i;
                } else if (!isChainUseful.get(i)) {
                    log("Excluding chain as it was deemed not-SVM-useful: " + chain);

                    variableChains.remove(i);
                    isChainUseful.remove(i);
                    --i;
                } else if (phiChains.containsKey(chain.get(0))) {
                    String expectedLast = phiChains.get(chain.get(0));
                    String actualLast = chain.get(chain.size() - 1);

                    if (expectedLast != actualLast) {
                        log("Excluding chain due to mismatched phi: " + chain);

                        variableChains.remove(i);
                        isChainUseful.remove(i);
                        --i;
                    }
                }
            }

            // TODO: Do we need to check for overlapping chains?

            log("Should optimize: " + variableChains);
            if (!variableChains.isEmpty()) {
                optimized = true;
            }

            // Optimize
            currentBlockId = blockId;
            block = instance.getBlock(currentBlockId);

            for (int instructionId = 0; instructionId < block.getInstructions().size(); ++instructionId) {
                SsaInstruction instruction = block.getInstructions().get(instructionId);

                // We will not be directly optimizing the read-only SVM buffers
                // leaving it for a sequentialRedundant pass.

                if (instruction instanceof InvokeKernelInstruction) {
                    InvokeKernelInstruction invoke = (InvokeKernelInstruction) instruction;

                    List<String> outputs = invoke.getOutputs();
                    for (int outputIndex = 0; outputIndex < outputs.size(); outputIndex++) {
                        String output = outputs.get(outputIndex);
                        int inputIndex = invoke.getOutputSources().get(outputIndex);
                        String input = invoke.getArguments().get(inputIndex);

                        if (variableChains.stream().anyMatch(chain -> chain.contains(output))) {
                            // Optimize here
                            optimizeCall(instance, block, instructionId, invoke, inputIndex, input);
                            ++instructionId;
                        }
                    }
                    continue;
                }
                if (instruction instanceof SetGpuRangeInstruction) {
                    SetGpuRangeInstruction setGpu = (SetGpuRangeInstruction) instruction;

                    if (!setGpu.getOutput().isPresent()) {
                        continue;
                    }

                    String input = setGpu.getBuffer();
                    String output = setGpu.getOutput().get();

                    String buffer = makeBuffer(instance, block, input, instructionId);
                    ++instructionId;

                    block.replaceInstructionAt(instructionId,
                            new SetGpuRangeInstruction(buffer, setGpu.getBegin(), setGpu.getEnd(), setGpu.getValue(),
                                    setGpu.getSettings()));
                    ++instructionId;
                    makeCopyToCpu(instance, block, output, buffer, input, instructionId);
                    continue;
                }

                int endBlockId = instruction.tryGetEndBlock().orElse(-1);

                if (endBlockId >= 0) {
                    currentBlockId = endBlockId;
                    block = instance.getBlock(currentBlockId);
                    instructionId = -1;
                }
            }
        }

        return optimized;
    }

    private void checkUsesInBlock(TypedInstance instance,
            int blockId,
            List<List<String>> variableChains,
            List<Boolean> isChainUseful) {

        for (SsaInstruction instruction : instance.getBlock(blockId).getInstructions()) {
            // TODO

            markBadUses(variableChains, isChainUseful, instruction.getOutputs(), instruction);
            markBadUses(variableChains, isChainUseful, instruction.getInputVariables(), instruction);

            for (int ownedBlockId : instruction.getOwnedBlocks()) {
                checkUsesInBlock(instance, ownedBlockId, variableChains, isChainUseful);
            }
        }
    }

    private void markBadUses(List<List<String>> variableChains, List<Boolean> isChainUseful, List<String> vars,
            SsaInstruction instruction) {

        for (String var : vars) {
            for (int i = 0; i < variableChains.size(); ++i) {
                List<String> chain = variableChains.get(i);

                if (chain.contains(var)) {
                    log("Excluding " + chain + " due to " + instruction);

                    variableChains.remove(i);
                    isChainUseful.remove(i);
                    --i;
                }
            }
        }
    }

    /**
     * Optimize cases where an SVM variable is going to be copied to the GPU anyway
     */
    private boolean optimizeSequentialRedundant(TypedInstance instance) {
        boolean optimized = false;

        for (int blockId = 0; blockId < instance.getBlocks().size(); ++blockId) {
            log("Start block #" + blockId);

            List<InstructionLocation> svmRefs = new ArrayList<>();

            int currentBlockId = blockId;
            SsaBlock block = instance.getBlock(currentBlockId);

            Map<String, Integer> justSvmUsed = new HashMap<>();
            Map<String, String> justCopiedToCpu = new HashMap<>();

            // These two have the same information, but in different orders.
            Map<String, String> justCopiedToGpu = new HashMap<>();
            Map<String, String> justCopiedFromCpu = new HashMap<>();

            for (int instructionId = 0; instructionId < block.getInstructions().size(); ++instructionId) {
                SsaInstruction instruction = block.getInstructions().get(instructionId);

                if (instruction instanceof CompleteReductionInstruction) {
                    CompleteReductionInstruction cr = (CompleteReductionInstruction) instruction;

                    if (cr.getReductionType() == ReductionType.MATRIX_SET) {
                        log("Detecting copy to CPU " + cr);
                        String output = cr.getOutput();
                        justCopiedToCpu.put(output, cr.getBuffer());
                        continue;
                    }
                }

                if (instruction instanceof InvokeKernelInstruction) {
                    InvokeKernelInstruction invoke = (InvokeKernelInstruction) instruction;

                    log("At " + invoke);
                    log("Just copied to CPU: " + justCopiedToCpu);
                    log("Just copied to GPU: " + justCopiedToGpu);

                    for (int i = 0; i < invoke.getArguments().size(); ++i) {
                        String input = invoke.getArguments().get(i);

                        if (!invoke.getInstance().getArguments().get(i).isReadOnly) {
                            // Invalidate the buffer
                            justCopiedToGpu.remove(justCopiedFromCpu.remove(input));
                        } else if (justCopiedToGpu.containsKey(input)) {
                            // Optimize

                            optimizeCall(instance, block, instructionId, invoke, i, input);
                            ++instructionId;
                            continue;
                        }

                        if (input.endsWith("$ret")) {
                            continue;
                        }

                        if (justCopiedToCpu.containsKey(input)) {
                            // Optimize!

                            optimizeCall(instance, block, instructionId, invoke, i, input);
                            ++instructionId;
                        }

                        // For read-only buffers
                        int matchingOutput = invoke.getOutputSources().indexOf(i);
                        if (matchingOutput < 0) {
                            log("SVM variable " + input + " is read-only in kernel.");
                            justSvmUsed.put(input, svmRefs.size());
                        }
                    }
                    for (String output : invoke.getOutputs()) {
                        log("SVM variable " + output + " is modified in kernel.");
                        justSvmUsed.put(output, svmRefs.size());
                    }

                    // Only add it at the end, as its position may change due to inserted copies.
                    svmRefs.add(new InstructionLocation(currentBlockId, instructionId));
                    continue;
                }

                if (instruction instanceof SetGpuRangeInstruction) {
                    SetGpuRangeInstruction setGpuRange = (SetGpuRangeInstruction) instruction;

                    log("At " + setGpuRange);
                    log("Just copied to CPU: " + justCopiedToCpu);
                    log("Just copied to GPU: " + justCopiedToGpu);

                    if (setGpuRange.getOutput().isPresent()) {
                        String output = setGpuRange.getOutput().get();
                        // TODO: Deal with justCopiedToCpu

                        log("SVM variable " + output + " in set_gpu_range");
                        justSvmUsed.put(output, svmRefs.size());
                        svmRefs.add(new InstructionLocation(currentBlockId, instructionId));
                    }

                    continue;
                }

                if (instruction instanceof CopyToGpuInstruction) {
                    CopyToGpuInstruction copyToGpu = (CopyToGpuInstruction) instruction;
                    String var = copyToGpu.getInput();
                    String gpuBuffer = copyToGpu.getOutput();

                    log("Got " + instruction);

                    justCopiedToGpu.put(var, gpuBuffer);
                    justCopiedFromCpu.put(gpuBuffer, var);

                    if (var.endsWith("$ret")) {
                        log("Skipping " + var + " because it is a return variable");
                        continue;
                    }

                    log("Just SVM used: " + justSvmUsed);

                    if (justSvmUsed.containsKey(var)) {
                        int invokeKernelId = justSvmUsed.get(var);
                        InstructionLocation loc = svmRefs.get(invokeKernelId);
                        SsaInstruction invokeKernelOrSetGpuRange = instance.getInstructionAt(loc);

                        log("Optimize: For output " + var + ", got " + invokeKernelOrSetGpuRange);

                        int insertedInstructions;
                        if (invokeKernelOrSetGpuRange instanceof InvokeKernelInstruction) {
                            InvokeKernelInstruction invokeKernel = (InvokeKernelInstruction) invokeKernelOrSetGpuRange;
                            int inputIndex;

                            String input;

                            int outputIndex = invokeKernel.getOutputs().indexOf(var);
                            // Variable can be output, or read-only input
                            if (outputIndex >= 0) {
                                inputIndex = invokeKernel.getOutputSources().get(outputIndex);
                                input = invokeKernel.getArguments().get(inputIndex);
                            } else {
                                assert invokeKernel.getArguments().contains(var);

                                inputIndex = invokeKernel.getArguments().indexOf(var);
                                input = var;
                            }

                            // Optimize
                            justSvmUsed.remove(var);
                            optimized = true;
                            boolean insertedSuffix = optimizeCall(instance,
                                    instance.getBlock(loc.getBlockId()),
                                    loc.getInstructionId(),
                                    invokeKernel,
                                    inputIndex,
                                    input);
                            insertedInstructions = insertedSuffix ? 2 : 1;
                        } else {
                            SetGpuRangeInstruction setGpuRange = (SetGpuRangeInstruction) invokeKernelOrSetGpuRange;
                            log("Optimize: For output " + var + ", got " + setGpuRange);

                            String input = setGpuRange.getBuffer();

                            // Optimize
                            justSvmUsed.remove(var);
                            optimized = true;

                            SsaBlock targetBlock = instance.getBlock(loc.getBlockId());
                            int targetInstructionId = loc.getInstructionId();

                            insertedInstructions = 2;
                            // TODO: is this necessary? can we use gpuBuffer directly?
                            String newGpuBuffer = makeBuffer(instance, targetBlock, input, targetInstructionId);
                            setGpuRange.useGpuBuffer(newGpuBuffer);
                            makeCopyToCpu(instance, targetBlock, var, newGpuBuffer, input, targetInstructionId + 2);
                        }
                        for (int locIndex = 0; locIndex < svmRefs.size(); ++locIndex) {
                            InstructionLocation keyLoc = svmRefs.get(locIndex);

                            if (keyLoc.getBlockId() == loc.getBlockId()
                                    && keyLoc.getInstructionId() > loc.getInstructionId()) {

                                svmRefs.set(locIndex, new InstructionLocation(keyLoc.getBlockId(),
                                        keyLoc.getInstructionId() + insertedInstructions));
                            }
                        }
                        // We will also have to fix the location of the current setgpurange
                        // Note that we add 1, not insertedInstructions, because the suffix instruction appears *after*
                        // the invocation.
                        svmRefs.set(invokeKernelId,
                                new InstructionLocation(loc.getBlockId(), loc.getInstructionId() + 1));
                    }

                    continue;
                }

                // FIXME: We probably want some kind of invalidation of justCopiedToCpu
                // and justCopiedToGpu/justCopiedFromCpu.

                if (instruction instanceof ForInstruction) {
                    ForInstruction xfor = (ForInstruction) instruction;

                    currentBlockId = xfor.getEndBlock();
                    block = instance.getBlock(currentBlockId);

                    instructionId = -1;
                    continue;
                }

                for (String input : instruction.getInputVariables()) {
                    String cpuVar = justCopiedFromCpu.remove(input);
                    if (cpuVar != null) {
                        justCopiedToGpu.remove(cpuVar);
                    }
                }
            }
        }

        return optimized;
    }

    private boolean optimizeCall(TypedInstance instance, SsaBlock block, int instructionId,
            InvokeKernelInstruction invoke,
            int inputIndex,
            String input) {

        assert inputIndex >= 0;

        boolean insertedSuffix = false;

        String gpuBuffer = makeBuffer(instance, block, input, instructionId);
        ++instructionId;

        log("At call to " + invoke.getInstance().getInstanceName() + ": Replacing "
                + invoke.getArguments().get(inputIndex) + " with " + gpuBuffer);

        invoke.setArgument(inputIndex, gpuBuffer);

        String output = null;
        int matchingOutput = invoke.getOutputSources().indexOf(inputIndex);
        if (matchingOutput >= 0) {
            output = invoke.getOutputs().get(matchingOutput);

            assert !output.equals(input);

            invoke.removeOutput(matchingOutput);

            // If this is called the first time we visit the IK instruction, then
            // we will revisit the inserted instructions again (as we do not change instructionId)
            // meaning that they will be added to justCopiedToCpu anyway.
            // If not, that will have to be handled on the next iteration
            makeCopyToCpu(instance, block, output, gpuBuffer, input, instructionId + 1);
            insertedSuffix = true;
        } else {
            // Variable is not modified on the GPU.
            // It is still "justCopiedToCpu".
        }

        log("Result: " + invoke);

        return insertedSuffix;
    }

    private String makeBuffer(TypedInstance instance, SsaBlock block, String var, int instructionId) {
        String buffer = instance.makeTemporary(NameUtils.getSuggestedName(var) + "_gpu", new GpuGlobalBufferType());
        SsaInstruction instruction = new CopyToGpuInstruction(buffer, var);
        block.insertInstruction(instructionId, instruction);

        return buffer;
    }

    private void makeCopyToCpu(TypedInstance instance, SsaBlock block, String cpuVar, String gpuBuffer,
            String srcBuffer,
            int instructionId) {

        assert !cpuVar.equals(srcBuffer);

        VariableType underlyingType = instance.getVariableType(cpuVar).get();

        SsaInstruction instruction = new CompleteReductionInstruction(cpuVar,
                ReductionType.MATRIX_SET,
                gpuBuffer,
                underlyingType,
                null,
                srcBuffer);
        block.insertInstruction(instructionId, instruction);
    }

    private static void log(String message) {
        if (ENABLE_LOG) {
            System.out.print("[gpu_svm_buffer_elimination] ");
            System.out.println(message);
        }
    }
}
