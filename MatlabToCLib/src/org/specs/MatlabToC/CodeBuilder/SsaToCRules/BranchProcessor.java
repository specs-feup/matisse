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
import java.util.List;

import org.specs.CIR.Language.ReservedWord;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.BlockNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

public class BranchProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof BranchInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        BranchInstruction branch = (BranchInstruction) instruction;

        CNode conditionVariable = builder.generateVariableExpressionForSsaName(currentBlock,
                branch.getConditionVariable());

        CInstructionList thenList = new CInstructionList(builder.getInstance().getFunctionType());
        builder.generateCodeForBlock(branch.getTrueBlock(), thenList);

        CInstructionList elseList = new CInstructionList(builder.getInstance().getFunctionType());
        builder.generateCodeForBlock(branch.getFalseBlock(), elseList);

        List<CNode> ifInstructions = new ArrayList<>();
        ifInstructions.add(CNodeFactory.newInstruction(InstructionType.If,
                CNodeFactory.newReservedWord(ReservedWord.If), conditionVariable));

        ifInstructions.addAll(thenList.get());

        ifInstructions.add(CNodeFactory.newInstruction(InstructionType.Else,
                CNodeFactory.newReservedWord(ReservedWord.Else)));
        ifInstructions.addAll(elseList.get());

        BlockNode ifNode = CNodeFactory.newBlock(ifInstructions);

        currentBlock.addInstruction(ifNode);

        builder.generateCodeForBlock(branch.getEndBlock(), currentBlock);
    }
}
