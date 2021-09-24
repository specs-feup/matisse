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

import java.util.List;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Passes.InstructionsBodyPass;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.InstructionNode;
import org.specs.CIR.Tree.Instructions.InstructionType;

/**
 * Removes redundant "return;" nodes at the end of a function body.
 */
public class RedundantReturnRemovalPass extends InstructionsBodyPass {

    private static final boolean ENABLE_DIAGNOSTICS = false;

    @Override
    public void apply(CInstructionList instructions, ProviderData providerData) {
	log("Starting");

	List<CNode> instructionList = instructions.get();

	for (int i = instructionList.size() - 1; i >= 0; --i) {
	    InstructionNode instruction = (InstructionNode) instructionList.get(i);

	    if (instruction.getInstructionType() == InstructionType.Comment) {
		continue;
	    }

	    if (instruction.getInstructionType() == InstructionType.Return) {
		CNode returnSt = instruction.getChild(0);

		if (returnSt.getNumChildren() != 0) {
		    log("Return statement has an expression, can't erase: " + returnSt.getCode());
		    return;
		}

		log("Removing return statement: " + returnSt.getCode());
		instructionList.remove(i);
		return;
	    }

	    log("Can't remove return statements, found: " + instruction.getCode());
	    return;
	}
    }

    private static void log(String message) {
	if (ENABLE_DIAGNOSTICS) {
	    System.out.print("[return_removal] ");
	    System.out.println(message);
	}
    }
}
