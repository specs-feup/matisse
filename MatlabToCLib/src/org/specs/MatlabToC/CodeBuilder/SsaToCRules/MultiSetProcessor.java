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

package org.specs.MatlabToC.CodeBuilder.SsaToCRules;

import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.matisselib.ssa.instructions.MultiSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

public class MultiSetProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof MultiSetInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        MultiSetInstruction set = (MultiSetInstruction) instruction;

        String output = set.getOutput();
        String input = set.getInputMatrix();

        VariableNode outputNode = builder.generateVariableNodeForSsaName(output);
        VariableNode inputNode = builder.generateVariableNodeForSsaName(input);

        if (!outputNode.getVariable().equals(inputNode.getVariable())) {
            builder.generateAssignment(currentBlock, outputNode, inputNode);
        }

        InstanceProvider setProvider = ((MatrixType) outputNode.getVariableType()).functions().set();

        for (int i = 0; i < set.getValues().size(); ++i) {
            String value = set.getValues().get(i);

            CNode indexNode = CNodeFactory.newCNumber(i);
            CNode valueNode = builder.generateVariableExpressionForSsaName(currentBlock, value);

            VariableType valueType = valueNode.getVariableType();
            if (valueType instanceof MatrixType) {
                // Assume that the variable has a single character.
                // FIXME: Add proper validation?

                MatrixType matrixType = (MatrixType) valueType;
                InstanceProvider get = matrixType.functions().get();

                valueNode = FunctionInstanceUtils.getFunctionCall(get, builder.getCurrentProvider(),
                        valueNode, CNodeFactory.newCNumber(0));
            }

            ProviderData setData = builder.getCurrentProvider().create(
                    outputNode.getVariableType(),
                    indexNode.getVariableType(),
                    valueNode.getVariableType());
            currentBlock.addFunctionCall(setProvider.newCInstance(setData), outputNode, indexNode, valueNode);
        }
    }

}
