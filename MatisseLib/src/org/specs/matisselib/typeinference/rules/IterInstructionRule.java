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

import org.specs.CIR.CirKeys;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferenceRule;

public class IterInstructionRule implements TypeInferenceRule {

    @Override
    public boolean accepts(SsaInstruction instruction) {
        return instruction instanceof IterInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context,
            InstructionLocation location,
            SsaInstruction instruction) {

        String start = context.getForLoopStartName().get();
        String step = context.getForLoopIntervalName().get();

        VariableType startType = context.requireVariableType(start);
        VariableType stepType = context.requireVariableType(step);

        if (!(startType instanceof ScalarType) || !(stepType instanceof ScalarType)) {
            throw new UnsupportedOperationException("Ranged for loops with non-scalar are not currently supported.");
        }

        ScalarType startScalar = (ScalarType) startType;
        ScalarType stepScalar = (ScalarType) stepType;

        VariableType result;
        if (startScalar.scalar().isInteger() && stepScalar.scalar().isInteger()) {
            result = context.getNumerics().newInt();
        } else {
            result = context.getProviderData().getSettings().get(CirKeys.DEFAULT_REAL);
        }

        // TODO
        // In MATLAB, the variable is a double.
        // In MATISSE C, the variable seems to be an integer.
        // We'll keep it integer here. See if it makes sense later.

        String outputName = instruction.getOutputs().get(0);
        VariableType type = context
                .getDefaultVariableType(outputName)
                .orElse(result);
        context.addVariable(outputName, type);
    }

}
