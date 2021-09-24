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

import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.matisselib.ssa.instructions.BuiltinVariableInstruction;
import org.specs.matisselib.ssa.instructions.BuiltinVariableInstruction.BuiltinVariable;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class BuiltinProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof BuiltinVariableInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        BuiltinVariableInstruction builtin = (BuiltinVariableInstruction) instruction;

        String output = builtin.getOutput();
        BuiltinVariable builtinVariable = builtin.getVariable();

        CNode outputNode = builder.generateVariableNodeForSsaName(output);

        CNode rightHand;
        switch (builtinVariable) {
        case PI:
            rightHand = CNodeFactory.newLiteral(Double.toString(Math.PI), outputNode.getVariableType());

            break;
        case NARGIN:
            rightHand = CNodeFactory.newCNumber(builder.getCurrentProvider().getNumInputs());

            break;
        case TRUE:
            rightHand = CNodeFactory.newCNumber(1, outputNode.getVariableType());

            break;
        case FALSE:
            rightHand = CNodeFactory.newCNumber(0, outputNode.getVariableType());

            break;
        default:
            throw new NotImplementedException(builtinVariable.name());
        }

        currentBlock.addAssignment(outputNode, rightHand);
    }

}
