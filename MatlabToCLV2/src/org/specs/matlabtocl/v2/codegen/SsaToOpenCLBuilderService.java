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

package org.specs.matlabtocl.v2.codegen;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.MatlabToC.CodeBuilder.VariableManager;
import org.specs.matlabtocl.v2.MatisseCLKeys;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;
import org.specs.matlabtocl.v2.types.kernel.SizedMatrixType;

public abstract class SsaToOpenCLBuilderService {
    public abstract ProviderData getCurrentProvider();

    public abstract List<VariableNode> getGlobalIdNodes();

    public abstract List<CNode> getLocalIdNodes();

    public abstract List<CNode> getLocalSizeNodes();

    public abstract List<CNode> getGroupIdNodes();

    public abstract CNode getSubGroupLocalIdNode();

    public abstract CNode getSubGroupIdNode();

    public abstract CNode getSubGroupSizeNode();

    public abstract CNode getNumSubGroupsNode();

    public abstract VariableManager getVariableManager();

    public abstract String getFinalName(String ssaName);

    public Variable generateTemporary(String variableName, VariableType type) {
        return getVariableManager().generateTemporary(variableName, type);
    }

    public String getSubGroupBarrierCode() {
        if (getCurrentProvider().getSettings().get(MatisseCLKeys.SUBGROUP_AS_WARP_FALLBACK)) {
            return "barrier(CLK_LOCAL_MEM_FENCE);";
        } else {
            return "sub_group_barrier(CLK_LOCAL_MEM_FENCE);";
        }
    }

    public static void buildSizedMatrixExtraParameters(List<Variable> functionTypeBuilder,
            List<KernelArgument> kernelCallerArguments,
            CInstructionList argumentConstructionCode,
            String importedVariable,
            String finalName,
            String dataVariableName,
            SizedMatrixType sizedMatrix) {

        argumentConstructionCode.addLiteralVariable(new Variable(finalName, sizedMatrix));

        argumentConstructionCode.addLiteralInstruction(finalName + ".data = " + dataVariableName + ";");

        if (sizedMatrix.containsNumel()) {
            String numelVariableName = finalName + "_numel";
            KernelArgument numelArgument = KernelArgument.importNumel(numelVariableName,
                    importedVariable,
                    CLNativeType.UINT);
            kernelCallerArguments.add(numelArgument);
            functionTypeBuilder.add(new Variable(numelVariableName, CLNativeType.UINT));

            argumentConstructionCode.addLiteralInstruction(finalName + ".length = " + numelVariableName + ";");
        }
        int containedDims = sizedMatrix.containedDims();

        for (int dim = 0; dim < containedDims; ++dim) {
            String dimVariableName = finalName + "_dim" + (dim + 1);

            KernelArgument dimArgument = KernelArgument.importDim(dimVariableName,
                    importedVariable,
                    CLNativeType.UINT,
                    dim);
            kernelCallerArguments.add(dimArgument);
            functionTypeBuilder.add(new Variable(dimVariableName, CLNativeType.UINT));

            argumentConstructionCode
                    .addLiteralInstruction(finalName + ".dim" + (dim + 1) + " = " + dimVariableName + ";");
        }
    }

    public static void buildSizedMatrixExtraParameters(FunctionTypeBuilder functionTypeBuilder,
            List<KernelArgument> kernelArguments,
            CInstructionList argumentConstructionCode,
            String importedVariable,
            String finalName,
            String dataVariableName,
            SizedMatrixType sizedMatrix) {

        List<Variable> inputs = new ArrayList<>();

        buildSizedMatrixExtraParameters(inputs, kernelArguments, argumentConstructionCode, importedVariable, finalName,
                dataVariableName, sizedMatrix);

        for (Variable variable : inputs) {
            functionTypeBuilder.addInput(variable);
        }

    }
}
