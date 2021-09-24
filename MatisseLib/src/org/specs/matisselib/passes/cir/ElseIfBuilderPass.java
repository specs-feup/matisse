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

package org.specs.matisselib.passes.cir;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.ReservedWord;
import org.specs.CIR.Passes.InstructionsBodyPass;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.BlockNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.InstructionNode;
import org.specs.CIR.Tree.Instructions.InstructionType;

/**
 * Generates "else if (...) { ... }" nodes from else { if (...) { ... } }" blocks.
 */
public class ElseIfBuilderPass extends InstructionsBodyPass {

    private static final boolean ENABLE_DIAGNOSTICS = false;

    @Override
    public void apply(CInstructionList instructions, ProviderData providerData) {
	log("Starting");
	for (CNode node : instructions.get()) {
	    visitInstruction(node);
	}
    }

    private void visitInstruction(CNode node) {
	assert node instanceof InstructionNode;
	InstructionNode instruction = (InstructionNode) node;
	if (instruction.getInstructionType() == InstructionType.Block) {
	    assert instruction.getNumChildren() == 1;
	    apply((BlockNode) instruction.getChild(0));
	}
    }

    private void apply(BlockNode block) {
	for (CNode child : block.getChildren()) {
	    visitInstruction(child);
	}

	CNode firstChild = block.getChild(0);
	if (!(firstChild instanceof InstructionNode)) {
	    return;
	}

	InstructionNode firstInstruction = (InstructionNode) firstChild;
	if (firstInstruction.getInstructionType() != InstructionType.If) {
	    return;
	}

	// Search for else node
	int elseIndex = -1;
	for (int i = 1; i < block.getNumChildren(); ++i) {
	    CNode child = block.getChild(i);
	    assert child instanceof InstructionNode;

	    InstructionNode instruction = (InstructionNode) child;
	    if (instruction.getInstructionType() == InstructionType.Else) {
		elseIndex = i;
		break;
	    }
	}

	if (elseIndex == -1) {
	    // No else found.
	    log("If has no else statement");
	    return;
	}

	if (block.getNumChildren() == elseIndex + 1) {
	    // Empty else. We'll just remove it
	    log("Removing empty else");
	    block.removeChild(elseIndex);
	    return;
	}

	if (block.getNumChildren() != elseIndex + 2) {
	    // Else has too much content.
	    // We can only convert it to "else if" if there is only one child (the if block) in the else.
	    log("Too many children after else statement");
	    return;
	}

	InstructionNode elseContent = (InstructionNode) block.getChild(elseIndex + 1);
	if (elseContent.getInstructionType() != InstructionType.Block) {
	    // Body of else is not a block, so it can't be an if
	    log("Body of else is not a block");
	    return;
	}

	BlockNode elseBlock = (BlockNode) elseContent.getChild(0);

	InstructionNode elseHeader = (InstructionNode) elseBlock.getChild(0);
	if (elseHeader.getInstructionType() != InstructionType.If) {
	    log("Else content block is not an if statement");
	    return;
	}

	block.removeChild(elseIndex + 1);
	block.removeChild(elseIndex);

	CNode condition = elseHeader.getChild(1);

	CNode elseIf = CNodeFactory.newInstruction(InstructionType.ElseIf,
		CNodeFactory.newReservedWord(ReservedWord.Else),
		CNodeFactory.newReservedWord(ReservedWord.If),
		condition);

	log("Generating elseif");
	block.addChild(elseIf);
	block.addChildren(elseBlock.getChildren().subList(1, elseBlock.getNumChildren()));
    }

    private static void log(String message) {
	if (ElseIfBuilderPass.ENABLE_DIAGNOSTICS) {
	    System.out.print("[else_if_builder] ");
	    System.out.println(message);
	}
    }
}
