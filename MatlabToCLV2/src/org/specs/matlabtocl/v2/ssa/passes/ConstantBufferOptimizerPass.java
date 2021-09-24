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

package org.specs.matlabtocl.v2.ssa.passes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockUtils;
import org.specs.matisselib.passes.TypeTransparentSsaPass;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.unssa.ControlFlowGraph;
import org.specs.matlabtocl.v2.codegen.KernelArgument;
import org.specs.matlabtocl.v2.ssa.instructions.CopyToGpuInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.InvokeKernelInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.OverrideGpuBufferContentsInstruction;
import org.suikasoft.jOptions.DataStore.SimpleDataStore;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.Interfaces.DataView;

import pt.up.fe.specs.util.collections.MultiMap;
import pt.up.fe.specs.util.lazy.Lazy;

public class ConstantBufferOptimizerPass extends TypeTransparentSsaPass {

    public static DataKey<Boolean> ALLOW_OVERWRITE_KEY = KeyFactory.bool("allow-overwrite");

    private static final boolean ENABLE_LOG = false;

    private final boolean allowOverwrite;

    public ConstantBufferOptimizerPass(boolean allowOverwrite) {
        this.allowOverwrite = allowOverwrite;
    }

    public ConstantBufferOptimizerPass(DataView parameters) {
        if (!parameters.hasValue(ALLOW_OVERWRITE_KEY)) {
            throw new RuntimeException("Parameter allow-overwrite is missing.");
        }

        this.allowOverwrite = parameters.getValue(ALLOW_OVERWRITE_KEY);
    }

    @Override
    public void apply(FunctionBody source,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            DataStore passData) {

        log("Starting");

        ControlFlowGraph cfg = passData
                .get(ProjectPassServices.DATA_PROVIDER)
                .buildData(CompilerDataProviders.CONTROL_FLOW_GRAPH);

        MultiMap<String, String> gpuBuffers = new MultiMap<>();
        Map<String, InstructionLocation> originalLocations = new HashMap<>();
        Set<String> mutableBuffers = new HashSet<>();

        List<SsaBlock> blocks = source.getBlocks();
        for (int blockId = 0; blockId < blocks.size(); blockId++) {
            SsaBlock block = blocks.get(blockId);
            List<SsaInstruction> instructions = block.getInstructions();
            for (int instructionId = 0; instructionId < instructions.size(); instructionId++) {
                SsaInstruction instruction = instructions.get(instructionId);
                if (instruction instanceof CopyToGpuInstruction) {
                    CopyToGpuInstruction copy = (CopyToGpuInstruction) instruction;
                    gpuBuffers.put(copy.getInput(), copy.getOutput());
                    originalLocations.put(copy.getOutput(), new InstructionLocation(blockId, instructionId));
                }

                if (instruction instanceof InvokeKernelInstruction) {
                    InvokeKernelInstruction invoke = (InvokeKernelInstruction) instruction;
                    for (int argumentIndex = 0; argumentIndex < invoke.getArguments().size(); ++argumentIndex) {
                        String argument = invoke.getArguments().get(argumentIndex);
                        KernelArgument kernelArgument = invoke.getInstance().getArguments().get(argumentIndex);

                        if (!kernelArgument.isReadOnly) {
                            mutableBuffers.add(argument);
                        }
                    }
                }
            }
        }

        log("Mutable buffers: " + mutableBuffers);

        for (int blockId = 0; blockId < source.getBlocks().size(); ++blockId) {
            SsaBlock block = source.getBlock(blockId);
            List<SsaInstruction> instructions = block.getInstructions();
            for (int instructionId = 0; instructionId < instructions.size(); instructionId++) {
                SsaInstruction instruction = instructions.get(instructionId);

                if (instruction instanceof CopyToGpuInstruction) {
                    // See if we can eliminate this one.
                    String input = ((CopyToGpuInstruction) instruction).getInput();
                    String output = ((CopyToGpuInstruction) instruction).getOutput();

                    String chosenBuffer = null;
                    boolean overwrite = false;

                    for (String buffer : gpuBuffers.get(input)) {
                        if (buffer.equals(output)) {
                            continue;
                        }

                        int capturedBlockId = blockId;
                        int capturedInstructionId = instructionId;
                        Lazy<Boolean> bufferDead = Lazy
                                .newInstance(() -> BlockUtils.isBufferDeadAfter(cfg,
                                        buffer,
                                        source,
                                        capturedBlockId,
                                        capturedInstructionId,
                                        ConstantBufferOptimizerPass::log));

                        if (mutableBuffers.contains(output)) {
                            if (bufferDead.get()) {
                                // No need to overwrite anything.
                                // The *output* may be mutable, but the source buffer has the correct initial data.
                                log("Output buffer is mutable but that's ok: " + output);
                                // No continue
                            } else {
                                // Output may have been modified.
                                log("Output buffer is mutable and buffer is reused: " + output + "(to " + buffer + ")");
                                continue;
                            }
                        }

                        if (mutableBuffers.contains(buffer)) {
                            if (bufferDead.get()) {
                                if (allowOverwrite) {
                                    log("Buffer is mutable but that's ok: " + buffer);
                                    overwrite = true;
                                    // No continue
                                } else {
                                    log("Buffer is mutable. That would be OK if allow-overwrite was set to true.");
                                    continue;
                                }
                            } else {
                                log("Buffer is mutable and reused: " + buffer + " (from " + output + ")");
                                continue;
                            }
                        }

                        // We use originalLocations.get(output) instead of new InstructionLocation(blockId,
                        // instructionId)
                        // because the location may have changed due to removed instructions.
                        if (BlockUtils.covers(cfg, originalLocations.get(buffer), originalLocations.get(output))) {
                            chosenBuffer = buffer;
                            break;
                        }
                    }

                    if (chosenBuffer != null) {
                        log("Optimizing " + output + " -> " + chosenBuffer + " (overwrite=" + overwrite + ")");

                        Map<String, String> newNames = new HashMap<>();
                        newNames.put(output, chosenBuffer);
                        source.renameVariables(newNames);

                        if (overwrite) {
                            instructions.set(instructionId,
                                    new OverrideGpuBufferContentsInstruction(chosenBuffer,
                                            ((CopyToGpuInstruction) instruction).getInput()));
                            mutableBuffers.add(chosenBuffer);
                        } else {
                            instructions.remove(instructionId);
                            --instructionId;
                        }
                        gpuBuffers.get(input).remove(output);
                    }
                }
            }
        }
    }

    private static void log(Object message) {
        if (ENABLE_LOG) {
            System.out.println("[constant_buffer_optimizer] " + message);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.SIZE_GROUP_INFORMATION,
                CompilerDataProviders.CONTROL_FLOW_GRAPH);
    }

    @Override
    public DataView getParameters() {
        DataStore store = new SimpleDataStore("cbo-params");

        store.add(ALLOW_OVERWRITE_KEY, this.allowOverwrite);

        return DataView.newInstance(store);
    }

    public static List<DataKey<?>> getRequiredParameters() {
        return Arrays.asList(ALLOW_OVERWRITE_KEY);
    }
}
