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
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.SsaToCRule;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.AllocateLocalBufferInstruction;

public final class AllocateLocalBufferProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof AllocateLocalBufferInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        AllocateLocalBufferInstruction allocateLocalBuffer = (AllocateLocalBufferInstruction) instruction;

        String output = allocateLocalBuffer.getOutput();
        String size = allocateLocalBuffer.getLocalSize();
        VariableType baseType = allocateLocalBuffer.getBaseType();

        CNode outputNode = builder.generateVariableNodeForSsaName(output);
        VariableNode sizeNode = builder.generateVariableNodeForSsaName(size);

        builder.addLiteralVariableIfNotArgument(sizeNode.getVariable());

        String sizeCode = sizeNode.getCodeForRightSideOf(PrecedenceLevel.Multiplication);

        CNode rightSide = CNodeFactory.newLiteral("sizeof(" + baseType.code().getSimpleType() + ") * " + sizeCode,
                outputNode.getVariableType());

        currentBlock.addAssignment(outputNode, rightSide);
    }

}
