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

import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.UsageMap;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsCollections;

/**
 * Finds instances of "A(...) = B;" and makes it so that B has the correct type, for certain types of operations. This,
 * in turn, should help the weak type optimizations.
 */
public class SetOutputPropagationPass implements PostTypeInferencePass {

    @Override
    public void apply(TypedInstance instance, DataStore passData) {

        UsageMap usageMap = UsageMap.build(instance.getFunctionBody());

        SystemFunctionProviderService functionProvider = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

        Set<String> authorizedVariables = new HashSet<>();
        Set<String> modifiedVariables = new HashSet<>();

        for (SsaInstruction instruction : instance.getFlattenedInstructionsList()) {
            if (instruction instanceof TypedFunctionCallInstruction && instruction.getOutputs().size() == 1) {
                String output = instruction.getOutputs().get(0);

                if (!isAcceptedName(((FunctionCallInstruction) instruction).getFunctionName())) {
                    continue;
                }

                if (usageMap.getUsageCount(output) != 1) {
                    continue;
                }

                authorizedVariables.add(output);
            }
        }

        for (SsaInstruction instruction : instance.getFlattenedInstructionsList()) {
            if (instruction instanceof MatrixSetInstruction || instruction instanceof SimpleSetInstruction) {
                String output = instruction.getOutputs().get(0);
                String value = SpecsCollections.last(instruction.getInputVariables());
                if (!authorizedVariables.contains(value)) {
                    continue;
                }

                VariableType valueType = instance.getVariableType(value).get();
                if (!(valueType instanceof ScalarType)) {
                    continue;
                }
                ScalarType scalarValueType = (ScalarType) valueType;

                MatrixType outputType = (MatrixType) instance.getVariableType(output).get();
                ScalarType outputElementType = outputType.matrix().getElementType();

                if (outputElementType.strictEquals(scalarValueType.scalar().removeConstant())) {
                    continue;
                }

                instance.addOrOverwriteVariable(value, outputElementType);
                modifiedVariables.add(value);
            }
        }

        for (SsaBlock block : instance.getBlocks()) {
            ListIterator<SsaInstruction> iterator = block.getInstructions().listIterator();
            while (iterator.hasNext()) {
                SsaInstruction instruction = iterator.next();

                if (instruction.getOutputs().size() != 1) {
                    continue;
                }

                String output = instruction.getOutputs().get(0);
                if (!modifiedVariables.contains(output)) {
                    continue;
                }

                VariableType outputType = instance.getVariableType(output).get();

                assert instruction instanceof TypedFunctionCallInstruction;

                TypedFunctionCallInstruction functionCall = (TypedFunctionCallInstruction) instruction;

                InstanceProvider instanceProvider = functionProvider.getSystemFunction(functionCall.getFunctionName())
                        .get();
                ProviderData providerData = instance.getProviderData()
                        .create(functionCall.getFunctionType().getArgumentsTypes());
                providerData.setOutputType(outputType);

                FunctionType newFunctionType = instanceProvider.getType(providerData);
                FunctionCallInstruction newInstruction = new TypedFunctionCallInstruction(
                        functionCall.getFunctionName(),
                        newFunctionType,
                        functionCall.getOutputs(),
                        functionCall.getInputVariables());
                iterator.set(newInstruction);
            }
        }
    }

    private static boolean isAcceptedName(String functionName) {
        switch (functionName) {
        case "plus":
        case "minus":
        case "times":
        case "mtimes":
            return true;
        default:
            return false;
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }

}
