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

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Utilities.CLibraryProvider.CLibraryProvider;
import org.specs.CIRFunctions.CLibrary.StdlibFunctions;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.ValidateSameSizeInstruction;

public class ValidateSameSizeProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof ValidateSameSizeInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        ValidateSameSizeInstruction validation = (ValidateSameSizeInstruction) instruction;
        ProviderData providerData = builder.getCurrentProvider();
        NumericFactory numerics = providerData.getNumerics();

        List<String> ssaInputs = validation.getInputVariables();

        if (ssaInputs.size() == 0) {
            return;
        }

        CNode previousInput = null;

        for (String input : ssaInputs) {
            CNode inputNode = builder.generateVariableNodeForSsaName(input);
            if (previousInput == null) {
                previousInput = inputNode;
                continue;
            }

            InstanceProvider ndims = MatlabBuiltin.NDIMS.getMatlabFunction();
            CNode previousNumDims = ndims
                    .getCheckedInstance(providerData.createFromNodes(previousInput))
                    .newFunctionCall(previousInput);
            CNode numDims = ndims
                    .getCheckedInstance(providerData.createFromNodes(inputNode))
                    .newFunctionCall(inputNode);

            CNode comparison = COperator.NotEqual
                    .getCheckedInstance(providerData.createFromNodes(previousNumDims, numDims))
                    .newFunctionCall(previousNumDims, numDims);

            CNode printAbortNode = CNodeFactory.newLiteral("printf(\"At: " + instruction + "\");");

            CLibraryProvider abortProvider = new StdlibFunctions(numerics).abort();
            CNode abortNode = abortProvider.getCheckedInstance(providerData.create()).newFunctionCall();
            currentBlock.addIf(comparison,
                    abortNode);

            Variable dimVariable = builder.generateTemporary("dim", numerics.newInt());
            VariableNode dimNode = CNodeFactory.newVariable(dimVariable);

            InstanceProvider sizeProvider = MatlabBuiltin.SIZE.getMatlabFunction();

            CNode previousDim = sizeProvider
                    .getCheckedInstance(providerData.createFromNodes(previousInput, dimNode))
                    .newFunctionCall(previousInput, dimNode);
            CNode dim = sizeProvider
                    .getCheckedInstance(providerData.createFromNodes(inputNode, dimNode))
                    .newFunctionCall(inputNode, dimNode);
            CNode compareDim = COperator.NotEqual
                    .getCheckedInstance(providerData.createFromNodes(previousDim, dim))
                    .newFunctionCall(previousDim, dim);
            CNode validateDim = IfNodes.newIfThen(compareDim, printAbortNode, abortNode);

            currentBlock.addInstruction(new ForNodes(providerData).newForLoopBlock(dimNode, CNodeFactory.newCNumber(1),
                    numDims,
                    COperator.LessThanOrEqual,
                    Arrays.asList(validateDim)));

            previousInput = inputNode;
        }
    }
}
