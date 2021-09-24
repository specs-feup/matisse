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

package org.specs.matisselib.typeinference.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.OutputData;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.PreTypeInferenceServices;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.services.InstructionReportingService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.services.TypedInstanceProviderService;
import org.specs.matisselib.services.WideScopeService;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.UntypedFunctionCallInstruction;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferencePass;
import org.specs.matisselib.typeinference.TypeInferenceRule;

public class UntypedFunctionCallTypeInferenceRule implements TypeInferenceRule {

    @Override
    public boolean accepts(SsaInstruction instruction) {
        return instruction instanceof UntypedFunctionCallInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context,
            InstructionLocation location,
            SsaInstruction instruction) {

        UntypedFunctionCallInstruction untypedCall = (UntypedFunctionCallInstruction) instruction;
        String functionName = untypedCall.getFunctionName();
        WideScopeService wideScope = context.getPassData().get(PreTypeInferenceServices.WIDE_SCOPE);
        InstructionReportingService report = context.getInstructionReportService();

        List<OutputData> outputs = new ArrayList<>();
        for (String output : instruction.getOutputs()) {

            VariableType type = context.getDefaultVariableType(output)
                    .orElse(null);

            // TODO: Optimization opportunity: Check when unused
            boolean used = true;

            outputs.add(new OutputData(type, used));
        }

        Optional<FunctionIdentification> potentialFunction = wideScope.getUserFunction(functionName);
        FunctionType functionType = potentialFunction.map(
                userFunction -> {
                    TypedInstanceProviderService providerService = context.getPassData()
                            .get(ProjectPassServices.TYPED_INSTANCE_PROVIDER);

                    ProviderData providerData = TypeInferencePass.getCallProviderData(context, untypedCall, outputs);

                    return providerService.getTypedInstance(userFunction, providerData).getFunctionType();
                }).orElseGet(
                        () -> {
                            SystemFunctionProviderService systemService = context.getPassData()
                                    .get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

                            InstanceProvider potentialProvider = systemService
                                    .getSystemFunction(functionName)
                                    .orElseThrow(
                                            () -> report.emitError(context.getInstance(), instruction,
                                                    PassMessage.MISSING_IDENTIFIER,
                                                    "Could not find variable or function: " + functionName));

                            ProviderData providerData = TypeInferencePass.getCallProviderData(context, untypedCall,
                                    outputs);

                            potentialProvider = potentialProvider.accepts(providerData)
                                    .orElseThrow(
                                            () -> report.emitError(context.getInstance(), instruction,
                                                    PassMessage.SPECIALIZATION_FAILURE,
                                                    "Could not get specialized function instance for " + functionName
                                                            + ", argument types: " + providerData.getInputTypes()));

                            try {
                                return potentialProvider.getType(providerData);
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw report.emitError(context.getInstance(), instruction, PassMessage.INTERNAL_ERROR,
                                        "Got exception", e);
                            }
                        });

        if (functionType.getArgumentsNames().size() != 0) {
            for (int i = 0; i < functionType.getArguments().size(); ++i) {
                String argumentName = functionType.getArgumentsNames().get(i);

                if (functionType.isInputReference(i)) {
                    String inputSsaName = untypedCall.getInputVariables().get(i);

                    if (inputSsaName.startsWith("$")) {
                        throw report.emitError(context.getInstance(),
                                instruction,
                                PassMessage.CORRECTNESS_ERROR,
                                "%!by_ref inputs must be variables.");
                    }

                    int outputIndex = functionType.getOutputAsInputNames().indexOf(argumentName);
                    assert outputIndex >= 0;

                    String inputBaseName = inputSsaName.substring(0, inputSsaName.indexOf('$'));
                    String outputSsaName = instruction.getOutputs().get(outputIndex);

                    if (!outputSsaName.startsWith(inputBaseName + "$")) {
                        throw report.emitError(context.getInstance(),
                                instruction,
                                PassMessage.CORRECTNESS_ERROR,
                                "%!by_ref outputs must be the same variable as the corresponding input.");
                    }
                }
            }
        }

        TypedFunctionCallInstruction newInstruction = new TypedFunctionCallInstruction(
                untypedCall.getFunctionName(),
                functionType,
                untypedCall.getOutputs(),
                untypedCall.getInputVariables());

        List<String> outputNames = instruction.getOutputs();

        List<VariableType> outputTypes = functionType.getOutputTypes();
        assert outputs.size() == outputTypes.size() : "Error at call to " + functionName
                + ". Expected " + outputs.size() + " outputs, got " + outputTypes.size();

        for (int i = 0; i < outputNames.size(); i++) {
            String output = outputNames.get(i);
            if (i < outputTypes.size()) {
                VariableType outputType = outputTypes.get(i);
                context.addVariable(output, outputType, Optional.ofNullable(outputs.get(i).getVariableType()));
            } else {
                // Undefined: Unused output
            }
        }

        context.pushInstructionModification(location, newInstruction);
    }
}
