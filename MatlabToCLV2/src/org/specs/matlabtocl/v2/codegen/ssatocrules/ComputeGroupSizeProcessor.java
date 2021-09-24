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

import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.SsaToCRule;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.ComputeGroupSizeInstruction;

public class ComputeGroupSizeProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof ComputeGroupSizeInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        ComputeGroupSizeInstruction computeGroups = (ComputeGroupSizeInstruction) instruction;

        CNode outputNode = builder.generateVariableNodeForSsaName(computeGroups.getOutput());
        VariableNode numItemsVarNode = builder.generateVariableNodeForSsaName(computeGroups.getNumItems());
        VariableNode blockSizeVarNode = builder.generateVariableNodeForSsaName(computeGroups.getBlockSize());

        builder.addLiteralVariableIfNotArgument(numItemsVarNode.getVariable());
        builder.addLiteralVariableIfNotArgument(blockSizeVarNode.getVariable());

        CNode numItemsNode = numItemsVarNode;
        if (!ScalarUtils.isInteger(numItemsVarNode.getVariableType())) {
            numItemsNode = CNodeFactory.newLiteral("(size_t)" + numItemsNode.getCodeForContent(PrecedenceLevel.Cast),
                    PrecedenceLevel.Cast);
        }
        CNode blockSizeNode = blockSizeVarNode;
        if (!ScalarUtils.isInteger(blockSizeVarNode.getVariableType())) {
            blockSizeNode = CNodeFactory.newLiteral("(size_t)" + blockSizeNode.getCodeForContent(PrecedenceLevel.Cast),
                    PrecedenceLevel.Cast);
        }

        VariableType variableType = outputNode.getVariableType();

        String code = "(" + numItemsNode.getCodeForLeftSideOf(PrecedenceLevel.Addition) + " + "
                + blockSizeNode.getCodeForRightSideOf(PrecedenceLevel.Addition) + " - 1) / "
                + blockSizeNode.getCodeForRightSideOf(PrecedenceLevel.Division);

        CNode rightHand = CNodeFactory.newLiteral(code, variableType);

        currentBlock.addAssignment(outputNode, rightHand);
    }
}
