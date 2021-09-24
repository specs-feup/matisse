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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.passes.TypeTransparentSsaPass;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class RedundantAllocationEliminationPass extends TypeTransparentSsaPass {

    private static final boolean ENABLE_DIAGNOSTICS = false;

    private static final List<String> DIMENSIONAL_ALLOCATION_FUNCTIONS = Arrays.asList("ones", "zeros",
            "matisse_new_array_from_dims", "eye");
    private static final List<String> MATRIX_COPY_ALLOCATION_FUNCTIONS = Arrays.asList("matisse_new_array_from_matrix");

    @Override
    public void apply(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            DataStore passData) {

        DataProviderService dataProvider = passData.get(ProjectPassServices.DATA_PROVIDER);

        SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);
        InstanceProvider numelProvider = systemFunctions.getSystemFunction("numel").get();
        InstanceProvider timesProvider = systemFunctions.getSystemFunction("times").get();

        Map<String, SsaInstruction> dimensionalAllocationCalls = body
                .getFlattenedInstructionsOfTypeStream(FunctionCallInstruction.class)
                .filter(call -> DIMENSIONAL_ALLOCATION_FUNCTIONS.contains(call.getFunctionName()))
                .filter(call -> call.getOutputs().size() == 1)
                .filter(call -> call.getInputVariables().size() > 1)
                .filter(call -> areAllArgumentsScalar(typeGetter, call))
                .collect(Collectors.toMap(call -> call.getOutputs().get(0), call -> call));
        Map<String, SsaInstruction> matrixCopyAllocationCalls = body
                .getFlattenedInstructionsOfTypeStream(FunctionCallInstruction.class)
                .filter(call -> MATRIX_COPY_ALLOCATION_FUNCTIONS.contains(call.getFunctionName()))
                .filter(call -> call.getOutputs().size() == 1)
                .filter(call -> call.getInputVariables().size() == 1)
                .collect(Collectors.toMap(call -> call.getOutputs().get(0), call -> call));

        Set<String> safeAllocations = new HashSet<>();
        safeAllocations.addAll(dimensionalAllocationCalls.keySet());
        safeAllocations.addAll(matrixCopyAllocationCalls.keySet());

        for (SsaInstruction instruction : body.getFlattenedInstructionsIterable()) {
            if (instruction instanceof FunctionCallInstruction) {
                FunctionCallInstruction call = (FunctionCallInstruction) instruction;

                if (call.getFunctionName().equals("numel")) {
                    continue;
                }
            }

            safeAllocations.removeAll(instruction.getInputVariables());
        }

        log("Safe allocations: " + safeAllocations);

        for (SsaBlock block : body.getBlocks()) {
            ListIterator<SsaInstruction> iterator = block.getInstructions().listIterator();

            while (iterator.hasNext()) {
                SsaInstruction instruction = iterator.next();

                if (instruction instanceof FunctionCallInstruction) {
                    FunctionCallInstruction call = (FunctionCallInstruction) instruction;

                    if (call.getInputVariables().size() == 1 && !call.getOutputs().isEmpty()) {
                        String input = call.getInputVariables().get(0);
                        if (safeAllocations.contains(input)) {

                            log("Optimizing call: " + call);
                            dataProvider.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);

                            assert call.getFunctionName().equals("numel");

                            if (dimensionalAllocationCalls.containsKey(input)) {
                                iterator.remove();

                                List<String> values = dimensionalAllocationCalls.get(input).getInputVariables();

                                String currentTemporary = values.get(0);
                                for (int i = 1; i < values.size(); ++i) {
                                    String value = values.get(i);

                                    VariableType previousType = typeGetter.apply(currentTemporary).get();
                                    VariableType newType = typeGetter.apply(value).get();
                                    ProviderData timesData = providerData.create(previousType, newType);
                                    FunctionType functionType = timesProvider.getType(timesData);

                                    String name;
                                    if (i == values.size() - 1) {
                                        name = call.getOutputs().get(0);
                                    } else {
                                        name = makeTemporary.apply("partial_result",
                                                functionType.getOutputTypes().get(0));
                                    }

                                    SsaInstruction newInstruction = new TypedFunctionCallInstruction("times",
                                            functionType,
                                            Arrays.asList(name), Arrays.asList(currentTemporary, value));
                                    iterator.add(newInstruction);

                                    currentTemporary = name;
                                }
                            } else if (matrixCopyAllocationCalls.containsKey(input)) {
                                String output = call.getOutputs().get(0);
                                List<String> values = matrixCopyAllocationCalls.get(input).getInputVariables();
                                List<VariableType> valuesTypes = values.stream()
                                        .map(variable -> typeGetter.apply(variable).get())
                                        .collect(Collectors.toList());

                                ProviderData numelData = providerData.create(valuesTypes);
                                numelData.setOutputType(typeGetter.apply(output).get());
                                FunctionType functionType = numelProvider.getType(numelData);

                                iterator.set(new TypedFunctionCallInstruction("numel", functionType,
                                        Arrays.asList(output), values));
                            }
                        }
                    }
                }
            }
        }

    }

    private static boolean areAllArgumentsScalar(
            Function<String, Optional<VariableType>> typeGetter,
            FunctionCallInstruction call) {

        return call.getInputVariables()
                .stream()
                .map(name -> typeGetter.apply(name).orElse(null))
                .allMatch(type -> type instanceof ScalarType);
    }

    private static void log(String message) {
        if (ENABLE_DIAGNOSTICS) {
            System.out.print("[redundant_allocation] ");
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
