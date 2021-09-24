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

package org.specs.matisselib.passes.cir;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Passes.InstructionsBodyPass;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.BlockNode;
import org.specs.CIR.Tree.CNodes.InstructionNode;
import org.specs.CIR.Tree.Instructions.InstructionType;

import pt.up.fe.specs.util.SpecsCollections;

/**
 * Finds if(...) { ... } else {} and eliminates the redundant else.
 * 
 * @author Luís Reis
 *
 */
public class EmptyElseEliminationPass extends InstructionsBodyPass {

    @Override
    protected void apply(CInstructionList instructions, ProviderData providerData) {
        for (CNode node : instructions.get()) {
            visit(node);
        }
    }

    private void visit(CNode node) {
        for (CNode child : node.getChildren()) {
            visit(child);
        }

        if (node instanceof BlockNode) {
            BlockNode block = (BlockNode) node;
            InstructionNode header = block.getHeader();
            if (header.getInstructionType() == InstructionType.If) {

                CNode lastNode = SpecsCollections.last(block.getChildren());
                if (lastNode instanceof InstructionNode
                        && ((InstructionNode) lastNode).getInstructionType() == InstructionType.Else) {

                    block.removeChild(block.getNumChildren() - 1);
                }

            }
        }
    }

}
