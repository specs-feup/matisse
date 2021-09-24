/**
 * Copyright 2017 SPeCS.
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
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.matisselib.ssa.instructions.AssumeInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

public class AssumeProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
        return instruction instanceof AssumeInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
        builder.generateVariableExpressionForSsaName(currentBlock, ((AssumeInstruction) instruction).getVariable());
    }

}
