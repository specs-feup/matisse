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

package org.specs.matlabtocl.v2.codegen.ssatocrules;

import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.SsaToCRule;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matlabtocl.v2.CLServices;
import org.specs.matlabtocl.v2.ssa.instructions.SetGpuRangeInstruction;
import org.specs.matlabtocl.v2.types.api.EventType;

public class SetGpuRangeProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof SetGpuRangeInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        SetGpuRangeInstruction setRange = (SetGpuRangeInstruction) instruction;

        boolean profile = builder.getPassData()
                .get(CLServices.PROFILING_OPTIONS)
                .isDataTransferProfilingEnabled();

        VariableNode inputNode = builder.generateVariableNodeForSsaName(setRange.getBuffer());
        VariableNode patternNode = builder.generateVariableNodeForSsaName(setRange.getValue());

        // TODO: Deal with starts other than 1
        CNode startNode = CNodeFactory.newCNumber(0);
        CNode endNode = builder.generateVariableNodeForSsaName(setRange.getEnd());
        CNode lengthNode = endNode;

        Variable evtVariable = builder.generateTemporary("evt", new EventType());

        builder.addLiteralVariable(evtVariable);
        builder.addLiteralVariableIfNotArgument(inputNode.getVariable());
        builder.addLiteralVariableIfNotArgument(patternNode.getVariable());

        String functionName;
        String inputCode;
        String startCode = "";
        if (setRange.getOutput().isPresent()) {
            functionName = "clEnqueueSVMMemFill";

            VariableNode outputNode = builder.generateVariableNodeForSsaName(setRange.getOutput().get());
            if (!outputNode.equals(inputNode)) {
                builder.generateAssignmentForSsaNames(currentBlock, setRange.getOutput().get(), setRange.getBuffer());

                inputNode = outputNode;
            }

            inputCode = FunctionInstanceUtils.getFunctionCall(
                    ((MatrixType) inputNode.getVariableType()).functions().data(),
                    builder.getCurrentProvider(),
                    inputNode).getCode();
            // FIXME: If start != 0
        } else {
            functionName = "clEnqueueFillBuffer";
            inputCode = inputNode.getCode();
            startCode = startNode.getCode() + ", ";
        }

        currentBlock
                .addLiteralInstruction(
                        "CHECK(" + functionName + ", MATISSE_cl.command_queue, " + inputCode
                                + ", &" + patternNode.getCode() + ", sizeof(" + patternNode.getCode() + "), "
                                + startCode
                                + lengthNode.getCodeForLeftSideOf(PrecedenceLevel.Multiplication) + " * sizeof("
                                + patternNode.getCode() + "), 0, NULL, &" + evtVariable.getName() + ");");

        if (profile) {
            assert evtVariable != null;

            currentBlock
                    .addLiteralInstruction(
                            "MATISSE_cl_register_host_to_device_data_transfer_event(" + evtVariable.getName() + ");");
        }

        currentBlock.addLiteralInstruction("CHECK(clEnqueueBarrier, MATISSE_cl.command_queue);");
    }

}
