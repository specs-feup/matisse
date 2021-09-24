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

package org.specs.MatlabToC.CodeBuilder.SsaToCRules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.TemporaryUtils;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Void.VoidType;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.providers.MatlabFunction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.typeinference.TypedInstance;

public class TypedFunctionCallProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof TypedFunctionCallInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        TypedFunctionCallInstruction typedCall = (TypedFunctionCallInstruction) instruction;

        List<String> inputs = typedCall.getInputVariables();
        List<VariableType> inputTypes = inputs.stream()
                .map(name -> builder.getOriginalSsaType(name).get()) // Force use of original SSA type
                .collect(Collectors.toList());
        List<VariableType> outputTypes = typedCall.getFunctionType().getOutputTypes();

        ProviderData providerData = builder.getCurrentProvider().create(inputTypes);
        providerData.setOutputType(outputTypes);
        // FIXME

        FunctionInstance functionInstance;

        String functionName = typedCall.getFunctionName();
        Optional<MatlabFunction> matlabFunction = builder.getSystemFunction(functionName);
        if (matlabFunction.isPresent()) {
            Optional<InstanceProvider> maybeProvider = matlabFunction.get()
                    .accepts(providerData);
            assert maybeProvider.isPresent() : "Empty value for provider of " + functionName + ", with data: "
                    + providerData + ", at " + builder.getInstance();
            InstanceProvider provider = maybeProvider.get();

            if (provider.getType(providerData).isNoOp()) {
                // We'll still try to delete the argument nodes if appropriate.
                for (int i = inputs.size() - 1; i >= 0; --i) {
                    String ssaName = inputs.get(i);
                    builder.generateVariableExpressionForSsaName(currentBlock, ssaName, false);
                    builder.removeUsage(ssaName);
                }

                StringBuilder callCode = new StringBuilder();
                for (String output : typedCall.getOutputs()) {
                    if (callCode.length() != 0) {
                        callCode.append(", ");
                    }
                    callCode.append(builder.generateVariableNodeForSsaName(output).getCode());
                }
                if (callCode.length() != 0) {
                    callCode.append(" = ");
                }
                callCode.append(functionName);
                callCode.append(inputs.stream()
                        .map(builder::generateVariableNodeForSsaName)
                        .map(node -> node.getCode())
                        .collect(Collectors.joining(", ", "(", ")")));

                currentBlock.addComment("Removed no-op function call: " + callCode);
                return;
            }

            functionInstance = provider
                    .getCheckedInstance(providerData);

        } else {
            TypedInstance newInstance = builder.getSpecializedUserFunctionInScope(typedCall.getFunctionName(),
                    providerData);
            functionInstance = builder.buildAuxiliaryImplementation(newInstance);
        }

        List<CNode> arguments = new ArrayList<>(inputs.size());
        for (int i = 0; i < inputs.size(); ++i) {
            // Fill with dummy values
            arguments.add(null);
        }

        Map<String, Variable> byRefInputs = new HashMap<>();

        // Fill with the actual values
        // Inline instances have special rules, and never have by-ref parameters anyway
        FunctionType functionType = functionInstance.getFunctionType();
        for (int i = inputs.size() - 1; i >= 0; --i) {
            String input = inputs.get(i);

            boolean canInlineArgument = true;

            if (functionType.getArgumentsTypes().size() == inputs.size()) {
                if (functionType.isInputReference(i)) {
                    VariableNode inputNode = builder.generateVariableNodeForSsaName(input);

                    canInlineArgument = false;
                    byRefInputs.put(functionType.getArgumentsNames().get(i),
                            inputNode.getVariable());
                }
            }

            CNode node = canInlineArgument ? builder.generateVariableExpressionForSsaName(currentBlock, input,
                    inputs.size() == 1) : builder.generateVariableNodeForSsaName(input);
            arguments.set(i, node);
        }

        FunctionCallNode functionCall = functionInstance.newFunctionCall(arguments);

        CNode instructionNode = functionCall;
        if (functionType.hasOutputsAsInputs()) {
            List<VariableType> outputAsInputTypes = functionType.getOutputAsInputTypes();

            for (int i = 0; i < outputAsInputTypes.size(); i++) {
                List<CNode> inputTokens = functionCall.getInputTokens();

                int tokenIndex = inputTokens.size() - outputAsInputTypes.size() + i;
                CNode previousNode = inputTokens.get(tokenIndex);

                assert previousNode instanceof VariableNode
                        && TemporaryUtils.isTemporaryName(
                                ((VariableNode) previousNode).getVariableName()) : "Expected temporary variable, got "
                                        + previousNode + ", in " + instruction + ": " + i + ",\n function call: "
                                        + functionCall;

                VariableNode outputNode = builder.generateVariableNodeForSsaName(instruction.getOutputs().get(i));

                if (!byRefInputs.isEmpty()) {
                    String outputAsInputName = functionType
                            .getOutputAsInputNames()
                            .get(i);

                    if (byRefInputs.containsKey(outputAsInputName)) {
                        Variable inputVariable = byRefInputs.get(outputAsInputName);
                        Variable outputVariable = outputNode.getVariable();

                        if (!inputVariable.equals(outputVariable)) {
                            builder.getReporter()
                                    .emitMessage(PassMessage.OPTIMIZATION_OPPORTUNITY,
                                            "Could not allocate input and output to same variable in %!by_ref call. Adding matrix copy.");

                            builder.generateAssignment(currentBlock, outputNode,
                                    CNodeFactory.newVariable(inputVariable));
                        }
                    }

                }

                functionCall.getFunctionInputs().setInput(tokenIndex, outputNode);
            }
        } else if (functionType.getCReturnType() instanceof VoidType) {
            assert instruction.getOutputs().size() == 0;
        } else {
            assert instruction.getOutputs().size() <= 1;

            CNode leftHand = builder.generateVariableNodeForSsaName(instruction.getOutputs().get(0));
            instructionNode = CNodeFactory.newAssignment(leftHand, instructionNode);
        }

        currentBlock.addInstruction(instructionNode);
    }
}
