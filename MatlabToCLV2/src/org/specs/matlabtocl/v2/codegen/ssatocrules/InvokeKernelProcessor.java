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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.SsaToCRule;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matlabtocl.v2.CLServices;
import org.specs.matlabtocl.v2.codegen.GeneratedKernel;
import org.specs.matlabtocl.v2.codegen.KernelArgument;
import org.specs.matlabtocl.v2.ssa.ParallelRegionSettings;
import org.specs.matlabtocl.v2.ssa.instructions.InvokeKernelInstruction;
import org.specs.matlabtocl.v2.types.api.CLBridgeType;
import org.specs.matlabtocl.v2.types.api.EventType;
import org.specs.matlabtocl.v2.types.api.GpuGlobalBufferType;
import org.specs.matlabtocl.v2.types.api.GpuLocalBufferType;
import org.specs.matlabtocl.v2.types.api.WorkSizeType;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public final class InvokeKernelProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof InvokeKernelInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        InvokeKernelInstruction invoke = (InvokeKernelInstruction) instruction;

        boolean profile = builder.getPassData()
                .get(CLServices.PROFILING_OPTIONS)
                .isKernelProfilingEnabled();

        GeneratedKernel kernel = invoke.getInstance();
        ParallelRegionSettings settings = kernel.getParallelSettings();

        boolean usesSharedMemory = false;

        for (int i = 0; i < invoke.getArguments().size(); ++i) {
            String variable = invoke.getArguments().get(i);
            VariableNode variableNode = builder.generateVariableNodeForSsaName(variable);

            builder.addLiteralVariableIfNotArgument(variableNode.getVariable());

            VariableType variableType = variableNode.getVariableType();
            KernelArgument kernelArgument = kernel.getArguments().get(i);

            if (variableType instanceof DynamicMatrixType) {
                // Shared memory
                DynamicMatrixType dynamicMatrixType = (DynamicMatrixType) variableType;

                VariableNode outputNode;
                if (kernelArgument.referencedReduction != null) {
                    int outputIndex = invoke.getOutputSources().indexOf(i);
                    assert outputIndex >= 0 : "No output found for " + kernelArgument.name + ", at " + invoke;

                    String outputVar = invoke.getOutputs().get(outputIndex);

                    String outputSource = invoke.getArguments().get(invoke.getOutputSources().get(outputIndex));
                    assert outputSource.equals(variable) : "At " + invoke + ": " + variable + " is not the same as "
                            + outputSource;

                    outputNode = builder
                            .generateVariableNodeForSsaName(outputVar);
                    builder.generateAssignment(currentBlock, outputNode, variableNode);
                } else {
                    outputNode = variableNode;
                }

                CNode data = FunctionInstanceUtils.getFunctionCall(dynamicMatrixType.functions().data(),
                        builder.getCurrentProvider(), outputNode);
                usesSharedMemory = true;
                currentBlock.addLiteralInstruction("CHECK(clSetKernelArgSVMPointer, MATISSE_cl."
                        + kernel.getInstanceName() + ", " + i + ", (void*) "
                        + data.getCodeForContent(PrecedenceLevel.Cast) + ");");
            } else {

                String argSizeCode;
                String argValueCode;
                if (variableType instanceof GpuGlobalBufferType) {
                    argSizeCode = "sizeof(cl_mem)";
                    argValueCode = variableNode.getCodeAsPointer();
                } else if (variableType instanceof GpuLocalBufferType) {
                    argSizeCode = variableNode.getCode();
                    argValueCode = "NULL";
                } else if (kernelArgument.clType instanceof CLNativeType) {
                    CLBridgeType bridgeType = CLBridgeType.getBridgeTypeFor((CLNativeType) kernelArgument.clType);

                    VariableNode temporary = builder.generateTemporaryNode(kernelArgument.name, bridgeType);
                    builder.addLiteralVariable(temporary.getVariable());

                    currentBlock.addAssignment(temporary, CNodeFactory.newLiteral(variableNode.getCode(), bridgeType));

                    argSizeCode = "sizeof(" + bridgeType.code().getSimpleType() + ")";
                    argValueCode = temporary.getCodeAsPointer();
                } else {
                    throw new NotImplementedException(
                            variableType.toString() + ", for variable " + kernelArgument.name + "(" + i + "), role= "
                                    + kernelArgument.role + ", type=" + kernelArgument.clType);
                }

                currentBlock.addLiteralInstruction(
                        "CHECK(clSetKernelArg, MATISSE_cl." + kernel.getInstanceName() + ", " + i + ", " + argSizeCode
                                + ", " + argValueCode + "); /* " + kernelArgument.name + ", isReadOnly="
                                + kernelArgument.isReadOnly + " */");
            }
        }

        int numDims = invoke.getGlobalSizes().size();
        StaticMatrixType sizeArrayType = StaticMatrixType.newInstance(WorkSizeType.BASE_TYPE, 1, numDims);
        CNode globalSizes = builder.generateTemporaryNode("global_sizes", sizeArrayType);
        CNode localSizes = builder.generateTemporaryNode("local_sizes", sizeArrayType);

        for (int i = 0; i < numDims; ++i) {
            String localSizeVariable = settings.localSizes.get(i);
            VariableNode localSizeVariableNode = builder.generateVariableNodeForSsaName(localSizeVariable);

            builder.addLiteralVariableIfNotArgument(localSizeVariableNode.getVariable());

            // Silent cast
            CNode localSize = CNodeFactory.newLiteral(localSizeVariableNode.getCode(), WorkSizeType.BASE_TYPE);

            CNode numThreadsInDim = builder.generateVariableExpressionForSsaName(currentBlock,
                    invoke.getGlobalSizes().get(i));

            InstanceProvider setFunction = sizeArrayType.matrix().functions().set();

            List<CNode> setNumThreadsArgs = Arrays.asList(globalSizes, CNodeFactory.newCNumber(i), numThreadsInDim);
            ProviderData setNumThreadsData = builder.getCurrentProvider().createFromNodes(setNumThreadsArgs);
            CNode setNumThreadsNode = setFunction
                    .getCheckedInstance(setNumThreadsData)
                    .newFunctionCall(setNumThreadsArgs);
            currentBlock.addInstruction(setNumThreadsNode);

            List<CNode> setLocalSizeArgs = Arrays.asList(localSizes, CNodeFactory.newCNumber(i), localSize);
            ProviderData setLocalSizeData = builder.getCurrentProvider().createFromNodes(setLocalSizeArgs);
            CNode setLocalSizeNode = setFunction
                    .getCheckedInstance(setLocalSizeData)
                    .newFunctionCall(setLocalSizeArgs);
            currentBlock.addInstruction(setLocalSizeNode);
        }

        Variable evtVariable = null;
        if (profile || usesSharedMemory) {
            evtVariable = builder.generateTemporary("evt", new EventType());
            builder.addLiteralVariable(evtVariable);
        }

        List<CNode> runKernelCommands = new ArrayList<>();
        runKernelCommands
                .add(CNodeFactory.newLiteral("CHECK(clEnqueueNDRangeKernel, MATISSE_cl.command_queue, MATISSE_cl."
                        + kernel.getInstanceName() + ", " + numDims + ", NULL, " + globalSizes.getCode() + ", "
                        + localSizes.getCode() + ", 0, NULL, "
                        + (evtVariable != null ? "&" + evtVariable.getName() : "NULL")
                        + ");"));

        if (profile) {
            assert evtVariable != null;

            runKernelCommands
                    .add(CNodeFactory.newLiteral("MATISSE_cl_register_kernel_event(" + evtVariable.getName() + ");"));
        }

        if (usesSharedMemory) {
            runKernelCommands
                    .add(CNodeFactory.newLiteral("CHECK(clWaitForEvents, 1, &" + evtVariable.getName() + ");"));
        } else {
            runKernelCommands.add(CNodeFactory.newLiteral("CHECK(clEnqueueBarrier, MATISSE_cl.command_queue);"));
        }

        StringBuilder notEmptySizeConditionString = null;
        for (int i = 0; i < numDims; ++i) {
            CNode numThreadsInDim = builder.generateVariableExpressionForSsaName(currentBlock,
                    invoke.getGlobalSizes().get(i));
            String isNotZero = numThreadsInDim.getCodeForLeftSideOf(PrecedenceLevel.Equality) + " != 0";

            if (notEmptySizeConditionString == null) {
                notEmptySizeConditionString = new StringBuilder(isNotZero);
            } else {
                notEmptySizeConditionString.append(" && ");
                notEmptySizeConditionString.append(isNotZero);
            }
        }

        currentBlock.addIf(
                CNodeFactory.newLiteral(notEmptySizeConditionString.toString(),
                        builder.getCurrentProvider().getNumerics().newInt()),
                runKernelCommands);
    }

}
