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

package org.specs.matlabtocl.v2.codegen.ssatocrules;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
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
import org.specs.matlabtocl.v2.functions.memory.ReleaseBuffer;
import org.specs.matlabtocl.v2.services.ProfilingOptions;
import org.specs.matlabtocl.v2.ssa.instructions.OverrideGpuBufferContentsInstruction;
import org.specs.matlabtocl.v2.types.api.EventType;

public final class OverrideGpuBufferContentsProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof OverrideGpuBufferContentsInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        OverrideGpuBufferContentsInstruction copyToGpu = (OverrideGpuBufferContentsInstruction) instruction;

        String buffer = copyToGpu.getBuffer();
        String matrix = copyToGpu.getMatrix();

        VariableNode bufferNode = builder.generateVariableNodeForSsaName(buffer);
        VariableNode matrixNode = builder.generateVariableNodeForSsaName(matrix);

        builder.addLiteralVariableIfNotArgument(matrixNode.getVariable());
        builder.addLiteralVariableIfNotArgument(bufferNode.getVariable());

        MatrixType matrixType = (MatrixType) matrixNode.getVariableType();

        InstanceProvider numelProvider = matrixType.functions().numel();
        ProviderData numelData = builder.getCurrentProvider().createFromNodes(matrixNode);
        FunctionInstance numelInstance = numelProvider.newCInstance(numelData);
        CNode numelCall = CNodeFactory.newFunctionCall(numelInstance, matrixNode);
        String size = "sizeof(" + matrixType.matrix().getElementType().code().getSimpleType() + ") * "
                + numelCall.getCodeForRightSideOf(PrecedenceLevel.Multiplication);

        InstanceProvider dataProvider = matrixType.functions().data();
        ProviderData dataData = builder.getCurrentProvider().createFromNodes(matrixNode);
        FunctionInstance dataInstance = dataProvider.newCInstance(dataData);
        String dataCode = dataInstance.getCallCode(matrixNode);

        ProfilingOptions profilingOptions = builder.getPassData()
                .get(CLServices.PROFILING_OPTIONS);

        Variable evtVariable = null;
        StringBuilder copyDataCode = new StringBuilder();
        copyDataCode.append("CHECK(clEnqueueWriteBuffer, MATISSE_cl.command_queue, ");
        copyDataCode.append(bufferNode.getCode());
        copyDataCode.append(", CL_TRUE, 0, ");
        copyDataCode.append(size);
        copyDataCode.append(", ");

        copyDataCode.append(dataCode);

        copyDataCode.append(", 0, NULL, ");
        if (profilingOptions.isDataTransferProfilingEnabled()) {
            evtVariable = builder.generateTemporary("evt", new EventType());
            builder.addLiteralVariable(evtVariable);

            copyDataCode.append("&");
            copyDataCode.append(evtVariable.getName());
        } else {
            copyDataCode.append("NULL");
        }
        copyDataCode.append(");");
        currentBlock.addLiteralInstruction(copyDataCode.toString());

        if (profilingOptions.isDataTransferProfilingEnabled()) {
            assert evtVariable != null;

            currentBlock
                    .addLiteralInstruction(
                            "MATISSE_cl_register_host_to_device_data_transfer_event(" + evtVariable.getName() + ");");
        }

        builder.addDependency(numelInstance);
        builder.addDependency(dataInstance);
        builder.addDependencies(
                new ReleaseBuffer().newCInstance(builder.getCurrentProvider()).getImplementationInstances());
        builder.addDependencies(bufferNode.getVariableType().code().getInstances());
        builder.addDependencies(matrixNode.getVariableType().code().getInstances());
    }

}
