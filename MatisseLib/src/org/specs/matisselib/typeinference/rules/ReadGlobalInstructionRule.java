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

package org.specs.matisselib.typeinference.rules;

import java.util.Optional;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.services.GlobalTypeProvider;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.ReadGlobalInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferenceRule;

public class ReadGlobalInstructionRule implements TypeInferenceRule {

    @Override
    public boolean accepts(SsaInstruction instruction) {
        return instruction instanceof ReadGlobalInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context, InstructionLocation location, SsaInstruction instruction) {
        ReadGlobalInstruction readGlobal = (ReadGlobalInstruction) instruction;

        GlobalTypeProvider globalTypeProvider = context.getPassData()
                .get(ProjectPassServices.GLOBAL_TYPE_PROVIDER);

        String output = readGlobal.getOutput();
        String global = readGlobal.getGlobal().substring(1);

        Optional<VariableType> globalType = globalTypeProvider.get(global);
        if (!globalType.isPresent()) {
            throw context.getInstructionReportService().emitError(context.getInstance(), instruction,
                    PassMessage.TYPE_INFERENCE_FAILURE, "Missing type of global " + global);
        }

        context.addVariable(output, globalType.get());
    }

}
