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
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.SsaToCRule;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matlabtocl.v2.CLServices;
import org.specs.matlabtocl.v2.codegen.MatrixCopyToGpuStrategy;
import org.specs.matlabtocl.v2.functions.memory.ReleaseBuffer;
import org.specs.matlabtocl.v2.services.ProfilingOptions;
import org.specs.matlabtocl.v2.ssa.instructions.CopyToGpuInstruction;
import org.specs.matlabtocl.v2.types.api.CLBridgeType;
import org.specs.matlabtocl.v2.types.api.EventType;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public final class CopyToGpuProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof CopyToGpuInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        CopyToGpuInstruction copyToGpu = (CopyToGpuInstruction) instruction;

        String output = copyToGpu.getOutput();
        String input = copyToGpu.getInput();

        VariableNode outputNode = builder.generateVariableNodeForSsaName(output);
        VariableNode inputNode = builder.generateVariableNodeForSsaName(input);

        builder.addLiteralVariableIfNotArgument(inputNode.getVariable());
        builder.addLiteralVariableIfNotArgument(outputNode.getVariable());

        MatrixType matrixType = (MatrixType) inputNode.getVariableType();

        VariableNode errorVariable = builder.makeNamedTemporary("cl_err", CLBridgeType.CL_INT);

        InstanceProvider numelProvider = matrixType.functions().numel();
        ProviderData numelData = builder.getCurrentProvider().createFromNodes(inputNode);
        FunctionInstance numelInstance = numelProvider.newCInstance(numelData);
        CNode numelCall = CNodeFactory.newFunctionCall(numelInstance, inputNode);
        String size = "sizeof(" + matrixType.matrix().getElementType().code().getSimpleType() + ") * (size_t)("
                + numelCall.getCode() + ")";

        InstanceProvider dataProvider = matrixType.functions().data();
        ProviderData dataData = builder.getCurrentProvider().createFromNodes(inputNode);
        FunctionInstance dataInstance = dataProvider.newCInstance(dataData);
        String dataCode = dataInstance.getCallCode(inputNode);

        MatrixCopyToGpuStrategy matrixCopyStrategy = builder.getPassData()
                .get(CLServices.CODE_GENERATION_STRATEGY_PROVIDER)
                .getMatrixCopyStrategy();

        ProfilingOptions profilingOptions = builder.getPassData()
                .get(CLServices.PROFILING_OPTIONS);

        boolean createAndCopy = false;
        boolean separateEnqueueCommand = false;
        if (profilingOptions.isDataTransferProfilingEnabled()) {
            separateEnqueueCommand = true;
        } else {
            if (matrixCopyStrategy == MatrixCopyToGpuStrategy.CREATE_BUFFER_COPY_HOST_PTR) {
                createAndCopy = true;
            } else if (matrixCopyStrategy == MatrixCopyToGpuStrategy.CREATE_BUFFER_THEN_ENQUEUE_WRITE) {
                separateEnqueueCommand = true;
            } else {
                throw new NotImplementedException("Strategy: " + matrixCopyStrategy);
            }
        }

        StringBuilder createBufferCode = new StringBuilder();
        createBufferCode.append("if (");
        createBufferCode.append(outputNode.getCode());
        createBufferCode.append(" != 0) {\n");
        createBufferCode.append("   clReleaseMemObject(");
        createBufferCode.append(outputNode.getCode());
        createBufferCode.append(");\n");
        createBufferCode.append("}\n");
        createBufferCode.append(outputNode.getCode());
        createBufferCode.append(" = ");
        createBufferCode.append("clCreateBuffer(MATISSE_cl.context, ");
        if (createAndCopy) {
            createBufferCode.append("CL_MEM_COPY_HOST_PTR");
        } else {
            createBufferCode.append("CL_MEM_READ_WRITE");
        }
        createBufferCode.append(", ");
        createBufferCode.append(size);
        createBufferCode.append(", ");
        if (createAndCopy) {
            createBufferCode.append(dataCode);
        } else {
            createBufferCode.append("NULL");
        }
        createBufferCode.append(",");
        createBufferCode.append(errorVariable.getCodeAsPointer());
        createBufferCode.append(");");

        currentBlock.addLiteralInstruction(createBufferCode.toString());
        currentBlock.addLiteralInstruction("CHECK_CODE(clCreateBuffer, " + errorVariable.getCode() + ");");

        Variable evtVariable = null;
        if (separateEnqueueCommand) {
            StringBuilder copyDataCode = new StringBuilder();
            copyDataCode.append("CHECK(clEnqueueWriteBuffer, MATISSE_cl.command_queue, ");
            copyDataCode.append(outputNode.getCode());
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
        }

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
        builder.addDependencies(outputNode.getVariableType().code().getInstances());
        builder.addDependencies(inputNode.getVariableType().code().getInstances());
    }

}
