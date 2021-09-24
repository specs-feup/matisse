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

package org.specs.matisselib.passes.posttype;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Whenever we have:
 * 
 * <pre>
 * <code>X = zeros(A); Y = zeros(size(X));</code>
 * </pre>
 * 
 * This pass converts that into:
 * 
 * <pre>
 * <code> X = zeros(A); Y = zeros(A);</code>
 * </pre>
 * 
 * This makes the code a bit friendlier to the dead code elimination pass, in cases where X is only used to determine
 * the size of Y.
 * 
 * @author Luís Reis
 *
 */
public class AllocationSimplifierPass implements PostTypeInferencePass {
    private static final boolean ENABLE_DIAGNOSTICS = false;

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        apply(instance.getFunctionBody(),
                instance.getProviderData(),
                instance::getVariableType,
                instance::makeTemporary,
                passData);

    }

    /**
     * This function is public so that unit tests can call it directly.
     */
    public static void apply(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            DataStore passData) {

        Map<String, FunctionCallInstruction> allocations = new HashMap<>();
        Map<String, String> sizes = new HashMap<>();

        SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

        for (SsaInstruction instruction : body.getFlattenedInstructionsIterable()) {
            if (instruction instanceof FunctionCallInstruction) {
                FunctionCallInstruction call = (FunctionCallInstruction) instruction;

                if (call.getOutputs().size() != 1) {
                    continue;
                }

                List<String> inputs = call.getInputVariables();
                String output = call.getOutputs().get(0);
                String functionName = call.getFunctionName();

                if (functionName.equals("matisse_new_array")) {
                    log("Adding allocation: " + call);
                    allocations.put(output, call);
                    continue;
                }
                if (functionName.equals("matisse_new_array_from_matrix")) {
                    log("Found matisse_new_array_from_matrix with inputs " + inputs);

                    if (inputs.size() == 1) {
                        log("Adding allocation: " + call);
                        allocations.put(output, call);
                    } else {
                        log("Found 'matisse_new_array_from_matrix' call with more than one input.");
                    }

                    continue;
                }
                if (isCommonMatlabAllocationFunction(functionName)) {
                    log("Adding allocation: " + call);
                    allocations.put(output, call);
                    continue;
                }

                if (functionName.equals("size") && inputs.size() == 1) {
                    log("Adding size: " + call);
                    sizes.put(output, inputs.get(0));
                    continue;
                }
            }
        }

        for (int blockId = 0; blockId < body.getBlocks().size(); ++blockId) {
            SsaBlock block = body.getBlock(blockId);
            BlockEditorHelper editor = new BlockEditorHelper(body, providerData, systemFunctions, typeGetter,
                    makeTemporary, blockId);

            ListIterator<SsaInstruction> iterator = block.getInstructions().listIterator();
            while (iterator.hasNext()) {
                SsaInstruction instruction = iterator.next();

                if (instruction instanceof FunctionCallInstruction) {
                    FunctionCallInstruction call = (FunctionCallInstruction) instruction;

                    if (call.getOutputs().size() != 1) {
                        continue;
                    }

                    List<String> inputs = call.getInputVariables();
                    String output = call.getOutputs().get(0);
                    String functionName = call.getFunctionName();
                    if (isCommonMatlabAllocationFunction(functionName)) {
                        if (inputs.size() == 1) {
                            String size = sizes.get(inputs.get(0));
                            FunctionCallInstruction allocation = allocations.get(size);
                            if (size != null && allocation != null) {
                                log("Applying to instruction: " + call);
                                call = editor.setCallWithExistentOutputs(iterator,
                                        functionName,
                                        call.getOutputs(),
                                        true,
                                        allocation.getInputVariables());
                            }
                        }

                        allocations.put(output, call);
                        continue;
                    }
                    if (functionName.equals("matisse_new_array_from_matrix")) {
                        String input = inputs.get(0);
                        FunctionCallInstruction allocation = allocations.get(input);
                        if (allocation != null) {
                            FunctionCallInstruction newCall = transformNewArrayCall(editor, typeGetter,
                                    iterator, call,
                                    functionName, allocation);

                            allocations.put(output, newCall);
                        }
                    }
                    if (functionName.equals("matisse_new_array")) {
                        String input = inputs.get(0);
                        String size = sizes.get(input);
                        FunctionCallInstruction allocation = allocations.get(size);
                        if (size != null && allocation != null) {
                            FunctionCallInstruction newCall = transformNewArrayCall(editor, typeGetter,
                                    iterator, call,
                                    functionName, allocation);

                            allocations.put(output, newCall);
                        }
                    }
                }
            }
        }
    }

    private static boolean isCommonMatlabAllocationFunction(String functionName) {
        return functionName.equals("zeros")
                || functionName.equals("ones")
                || functionName.equals("matisse_new_array_from_dims");
    }

    private static FunctionCallInstruction transformNewArrayCall(BlockEditorHelper editor,
            Function<String, Optional<VariableType>> typeGetter,
            ListIterator<SsaInstruction> iterator,
            FunctionCallInstruction call,
            String functionName,
            FunctionCallInstruction allocation) {

        String allocationFunction = allocation.getFunctionName();
        List<String> allocationInputs = allocation.getInputVariables();

        if (allocationFunction.equals("matisse_new_array_from_matrix")) {
            log("Applying to instruction: " + call);
            return editor.setCallWithExistentOutputs(iterator,
                    "matisse_new_array_from_matrix",
                    call.getOutputs(),
                    true,
                    allocation.getInputVariables());
        }
        if (allocationFunction.equals("matisse_new_array")
                || (isCommonMatlabAllocationFunction(allocationFunction) && allocationInputs.size() == 1
                        && MatrixUtils.isMatrix(typeGetter.apply(allocationInputs.get(0))))) {

            log("Applying to instruction: " + call);
            return editor.setCallWithExistentOutputs(iterator,
                    "matisse_new_array",
                    call.getOutputs(),
                    true,
                    allocation.getInputVariables());
        }

        if (isCommonMatlabAllocationFunction(allocationFunction)) {
            log("Applying to instruction: " + call);
            return editor.setCallWithExistentOutputs(iterator,
                    "matisse_new_array_from_dims",
                    call.getOutputs(),
                    true,
                    allocation.getInputVariables());
        }

        log("Could not apply to instruction: " + call);
        log("Reason: allocation is " + allocation);
        return call;
    }

    private static void log(String message) {
        if (AllocationSimplifierPass.ENABLE_DIAGNOSTICS) {
            System.out.print("[allocation_simplifier] ");
            System.out.println(message);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
