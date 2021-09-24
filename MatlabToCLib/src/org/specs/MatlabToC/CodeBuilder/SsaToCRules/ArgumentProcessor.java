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
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.VariableType;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypedInstance;

public class ArgumentProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof ArgumentInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        ArgumentInstruction arg = (ArgumentInstruction) instruction;
        String out = arg.getOutput();
        int in = arg.getArgumentIndex();

        TypedInstance instance = builder.getInstance();

        VariableType argumentType = instance.getFunctionType().getArgumentsTypes().get(in);
        String argumentName = instance.getFunctionType().getArgumentsNames().get(in);

        if (instance.getFunctionType().isInputReference(in)) {
            argumentType = argumentType.pointer().getType(true);
        }

        VariableNode leftHand = builder.generateVariableNodeForSsaName(out);
        VariableNode rightHand = CNodeFactory.newVariable(argumentName, argumentType);
        builder.generateAssignment(currentBlock, leftHand, rightHand);
    }

}
