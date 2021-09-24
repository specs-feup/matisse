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

package org.specs.matisselib.passes.posttype;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.passes.TypeTransparentSsaPass;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AccessSizeInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.MatrixGetInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.VerticalFlattenInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class TrivialAccessSizeEliminationPass extends TypeTransparentSsaPass {

    private static final boolean ENABLE_LOGGING = false;
    private static final Set<String> ALLOCATION_FUNCTIONS = new HashSet<>();

    static {
        ALLOCATION_FUNCTIONS.add("zeros");
        ALLOCATION_FUNCTIONS.add("ones");
        ALLOCATION_FUNCTIONS.add("matisse_new_array");
    }

    @Override
    public void apply(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            DataStore passData) {

        log("Starting");

        DataProviderService dataProvider = passData.get(ProjectPassServices.DATA_PROVIDER);

        SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);
        InstanceProvider sizeProvider = systemFunctions.getSystemFunction("size").get();

        Map<String, InstructionLocation> locations = new HashMap<>();
        Map<String, AccessSizeInstruction> accessSizes = new HashMap<>();

        List<SsaBlock> blocks = body.getBlocks();
        for (int blockId = 0; blockId < blocks.size(); blockId++) {
            SsaBlock block = blocks.get(blockId);

            List<SsaInstruction> instructions = block.getInstructions();
            for (int instructionId = 0; instructionId < instructions.size(); instructionId++) {
                SsaInstruction instruction = instructions.get(instructionId);

                if (instruction instanceof AccessSizeInstruction) {
                    AccessSizeInstruction accessSize = (AccessSizeInstruction) instruction;
                    String output = accessSize.getOutput();

                    if (output.endsWith("$ret")) {
                        log("Not optimizing " + output + " because it is a return variable.");
                        continue;
                    }

                    locations.put(output, new InstructionLocation(blockId, instructionId));
                    accessSizes.put(output, accessSize);
                }
            }
        }

        assert locations.keySet().equals(accessSizes.keySet());

        // S = access_size A, I is considered trivial if and only if:
        // (1) S is only used for matrix allocations (such as zeros(S), ones(S) or matisse_new_array(S)
        // (2) Those allocated matrices (X) are used only in a 1D-safe manner
        // A matrix is considered 1D-safe if flattening it would not change it
        // For instance B = A(1) is 1D-safe. B = A(1, 2) is not.

        for (String sizeMatrix : locations.keySet()) {
            Set<String> allocatedMatrices = new HashSet<>();

            if (!computeAllocatedMatrices(body, sizeMatrix, allocatedMatrices)) {
                continue;
            }

            // Now see if all those allocated matrices are 1D-safe.
            boolean is1DSafe = true;
            for (String matrix : allocatedMatrices) {
                if (!is1DSafe(body, matrix)) {
                    is1DSafe = false;
                    break;
                }
            }

            if (is1DSafe) {
                // Since it's safe, we can replace S = access_size A, I with S = size(I).
                log("Optimizing " + sizeMatrix);

                dataProvider.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);

                String indexMatrix = accessSizes.get(sizeMatrix).getIndexMatrix();
                VariableType sizeType = typeGetter.apply(sizeMatrix).get();

                VariableType indexType = typeGetter.apply(indexMatrix).get();
                ProviderData sizeData = providerData.create(indexType);
                sizeData.setOutputType(sizeType);
                FunctionType functionType = sizeProvider.getType(sizeData);
                SsaInstruction sizeInstruction = new TypedFunctionCallInstruction("size", functionType, sizeMatrix,
                        indexMatrix);
                body.setInstructionAt(locations.get(sizeMatrix), sizeInstruction);
            }
        }
    }

    private static boolean is1DSafe(FunctionBody body, String matrix) {

        Set<String> matrixGroup = new HashSet<>();
        matrixGroup.add(matrix);

        boolean changed = true;
        while (changed) {
            changed = false;

            for (SsaBlock block : body.getBlocks()) {
                for (SsaInstruction instruction : block.getInstructions()) {

                    if (!matrixGroup.stream().anyMatch(instruction.getInputVariables()::contains)) {
                        continue;
                    }

                    if (instruction instanceof SimpleSetInstruction) {
                        SimpleSetInstruction set = (SimpleSetInstruction) instruction;

                        assert matrixGroup.contains(set.getInputMatrix());

                        if (set.getIndices().size() != 1) {
                            log("Not optimizing " + matrix + " because it is involved in set with "
                                    + set.getIndices().size() + " indices.");
                            return false;
                        }

                        changed |= matrixGroup.add(set.getOutput());
                        continue;
                    }

                    if (instruction instanceof PhiInstruction) {
                        changed |= matrixGroup.add(instruction.getOutputs().get(0));
                        continue;
                    }
                }
            }
        }

        if (matrixGroup.stream().anyMatch(x -> x.endsWith("$ret"))) {
            // Returned matrices aren't known to be 1D safe.
            // (to be sure, we'd need inter-procedural analysis)
            log("Not optimizing matrix group " + matrixGroup + " because it contains return variable.");
            return false;
        }

        log("Matrix group: " + matrixGroup);

        for (SsaBlock block : body.getBlocks()) {
            for (SsaInstruction instruction : block.getInstructions()) {

                if (!matrixGroup.stream().anyMatch(instruction.getInputVariables()::contains)) {
                    continue;
                }

                if (instruction instanceof SimpleSetInstruction ||
                        instruction instanceof PhiInstruction) {
                    continue;
                }

                if (instruction instanceof SimpleGetInstruction ||
                        instruction instanceof MatrixGetInstruction) {
                    if (instruction.getInputVariables().size() != 2) {
                        log("Not optimizing due to " + instruction);
                        return false;
                    }
                    continue;
                }
                if (instruction instanceof VerticalFlattenInstruction) {
                    continue;
                }

                if (instruction instanceof FunctionCallInstruction) {
                    FunctionCallInstruction call = (FunctionCallInstruction) instruction;

                    if (call.getFunctionName().equals("numel")) {
                        continue;
                    }

                    log("Not optimizing due to function call: " + call);
                    return false;
                }

                log("Not optimizing due to instruction " + instruction);
                return false;
            }
        }

        return true;
    }

    private static boolean computeAllocatedMatrices(FunctionBody body,
            String sizeMatrix,
            Set<String> allocatedMatrices) {

        for (SsaBlock block : body.getBlocks()) {
            for (SsaInstruction instruction : block.getInstructions()) {
                if (instruction.getInputVariables().contains(sizeMatrix)) {

                    if (!(instruction instanceof FunctionCallInstruction)) {
                        return false;
                    }

                    FunctionCallInstruction call = (FunctionCallInstruction) instruction;

                    if (call.getInputVariables().size() != 1) {
                        return false;
                    }

                    if (call.getOutputs().size() != 1) {
                        return false;
                    }

                    String name = call.getFunctionName();

                    if (!ALLOCATION_FUNCTIONS.contains(name)) {
                        return false;
                    }

                    allocatedMatrices.add(call.getOutputs().get(0));
                }
            }
        }

        return true;
    }

    private static void log(String message) {
        if (ENABLE_LOGGING) {
            System.out.print("[trivial_access_size] ");
            System.out.println(message);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                // Explicitly invalidated
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
