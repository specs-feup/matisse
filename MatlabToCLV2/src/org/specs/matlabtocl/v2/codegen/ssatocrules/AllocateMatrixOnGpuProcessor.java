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
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.SsaToCRule;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matlabtocl.v2.functions.memory.ReleaseBuffer;
import org.specs.matlabtocl.v2.ssa.instructions.AllocateMatrixOnGpuInstruction;
import org.specs.matlabtocl.v2.types.api.CLBridgeType;

public final class AllocateMatrixOnGpuProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof AllocateMatrixOnGpuInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        AllocateMatrixOnGpuInstruction copyToGpu = (AllocateMatrixOnGpuInstruction) instruction;

        String output = copyToGpu.getOutput();
        String input = copyToGpu.getInput();

        VariableNode outputNode = builder.generateVariableNodeForSsaName(output);
        VariableNode inputNode = builder.generateVariableNodeForSsaName(input);

        builder.addLiteralVariableIfNotArgument(inputNode.getVariable());
        builder.addLiteralVariableIfNotArgument(outputNode.getVariable());

        MatrixType matrixType = (MatrixType) inputNode.getVariableType();

        VariableNode errorVariable = builder.makeNamedTemporary("cl_err", CLBridgeType.CL_INT);

        StringBuilder code = new StringBuilder();
        code.append("if (");
        code.append(outputNode.getCode());
        code.append(" != 0) {\n");
        code.append("   clReleaseMemObject(");
        code.append(outputNode.getCode());
        code.append(");\n");
        code.append("}\n");
        code.append(outputNode.getCode());
        code.append(" = ");
        code.append("clCreateBuffer(MATISSE_cl.context, 0, sizeof(");
        code.append(matrixType.matrix().getElementType().code().getSimpleType());
        code.append(") * (size_t)(");

        InstanceProvider numelProvider = matrixType.functions().numel();
        ProviderData numelData = builder.getCurrentProvider().createFromNodes(inputNode);
        FunctionInstance numelInstance = numelProvider.newCInstance(numelData);
        CNode numelCall = CNodeFactory.newFunctionCall(numelInstance, inputNode);

        code.append(numelCall.getCode());
        code.append("), NULL, ");
        code.append(errorVariable.getCodeAsPointer());
        code.append(");");

        builder.addDependency(numelInstance);
        ProviderData providerData = builder.getCurrentProvider();
        ProviderData releaseData = providerData.create(
                providerData.getNumerics().newInt() // Type is not used, so literally anything will do.
        );
        builder.addDependencies(
                new ReleaseBuffer().newCInstance(releaseData).getImplementationInstances());
        builder.addDependencies(outputNode.getVariableType().code().getInstances());
        builder.addDependencies(inputNode.getVariableType().code().getInstances());

        currentBlock.addLiteralInstruction(code.toString());
        currentBlock.addLiteralInstruction("CHECK_CODE(clCreateBuffer, " + errorVariable.getCode() + ");");
    }

}
