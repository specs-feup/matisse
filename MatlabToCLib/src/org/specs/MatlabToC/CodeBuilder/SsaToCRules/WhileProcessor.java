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

import java.util.List;

import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.WhileInstruction;

public class WhileProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
	return instruction instanceof WhileInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
	WhileInstruction whileInstruction = (WhileInstruction) instruction;

	CInstructionList loopContent = new CInstructionList(builder.getInstance().getFunctionType());
	builder.generateCodeForBlock(whileInstruction.getLoopBlock(), loopContent);

	List<CNode> loopInstructions = loopContent.get();
	currentBlock.addWhile(CNodeFactory.newCNumber(1), loopInstructions);

	builder.generateCodeForBlock(whileInstruction.getEndBlock(), currentBlock);
    }
}
