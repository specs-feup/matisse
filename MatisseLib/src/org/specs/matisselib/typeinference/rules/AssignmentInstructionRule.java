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

package org.specs.matisselib.typeinference.rules;

import java.util.Optional;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.ssa.Input;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferencePass;
import org.specs.matisselib.typeinference.TypeInferenceRule;

public class AssignmentInstructionRule implements TypeInferenceRule {

    @Override
    public boolean accepts(SsaInstruction instruction) {
	return instruction instanceof AssignmentInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context,
	    InstructionLocation location,
	    SsaInstruction instruction) {

	AssignmentInstruction assignmentInstruction = (AssignmentInstruction) instruction;

	String outputName = assignmentInstruction.getOutput();

	Optional<VariableType> type = context.getDefaultVariableType(outputName);

	if (type.isPresent()) {
	    context.addVariable(outputName, type.get());
	} else {
	    Input input = assignmentInstruction.getInput();
	    Optional<VariableType> candidateType = TypeInferencePass.getInputVariableType(context, input);
	    candidateType.ifPresent(variableType -> {
		context.addVariable(outputName, variableType);
	    });
	}
    }

}
