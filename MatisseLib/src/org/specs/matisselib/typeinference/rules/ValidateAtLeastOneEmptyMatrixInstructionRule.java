package org.specs.matisselib.typeinference.rules;

import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.ValidateAtLeastOneEmptyMatrixInstruction;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferenceRule;

public class ValidateAtLeastOneEmptyMatrixInstructionRule implements TypeInferenceRule {

    @Override
    public boolean accepts(SsaInstruction instruction) {
        return instruction instanceof ValidateAtLeastOneEmptyMatrixInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context, InstructionLocation location, SsaInstruction instruction) {
        // No action needed.
    }

}
