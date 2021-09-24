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
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.SsaToCRule;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.ComputeGlobalSizeInstruction;

public class ComputeGlobalSizeProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof ComputeGlobalSizeInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        ComputeGlobalSizeInstruction computeSize = (ComputeGlobalSizeInstruction) instruction;

        CNode output = builder.generateVariableNodeForSsaName(computeSize.getOutput());
        VariableNode numGroups = builder.generateVariableNodeForSsaName(computeSize.getNumWorkgroups());
        VariableNode localSize = builder.generateVariableNodeForSsaName(computeSize.getWorkgroupSize());

        builder.addLiteralVariableIfNotArgument(numGroups.getVariable());
        builder.addLiteralVariableIfNotArgument(localSize.getVariable());

        String localSizeCode;
        if (ScalarUtils.isInteger(localSize.getVariableType())) {
            localSizeCode = localSize.getCodeForRightSideOf(PrecedenceLevel.Multiplication);
        } else {
            localSizeCode = "(size_t)" + localSize.getCodeForContent(PrecedenceLevel.Cast);
        }

        String code = numGroups.getCodeForLeftSideOf(PrecedenceLevel.Multiplication) + " * "
                + localSizeCode;
        CNode rightHand = CNodeFactory.newLiteral(code, output.getVariableType());

        currentBlock.addAssignment(output, rightHand);
    }

}
