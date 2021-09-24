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

import java.util.Locale;
import java.util.Optional;

import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Logical.LogicalType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.BuiltinVariableInstruction;
import org.specs.matisselib.ssa.instructions.BuiltinVariableInstruction.BuiltinVariable;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferenceRule;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class BuiltinVariableInstructionRule implements TypeInferenceRule {

    @Override
    public boolean accepts(SsaInstruction instruction) {
        return instruction instanceof BuiltinVariableInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context,
            InstructionLocation location,
            SsaInstruction instruction) {

        BuiltinVariableInstruction builtin = (BuiltinVariableInstruction) instruction;

        BuiltinVariable variable = builtin.getVariable();

        NumericFactory numerics = context.getNumerics();
        String output = builtin.getOutput();

        Optional<VariableType> defaultVariableType = context.getDefaultVariableType(output);
        if (defaultVariableType.isPresent()) {
            context.addVariable(output, defaultVariableType.get());

            // Do we want a warning here?

            return;
        }

        switch (variable) {
        case NARGIN:
            context.addVariable(output, numerics.newInt(context.getProviderData().getNumInputs()));

            break;
        case PI:
            context.addVariable(output, numerics.newDouble(Math.PI));

            break;
        case TRUE:
            context.addVariable(output, LogicalType.newInstance(true));
            break;
        case FALSE:
            context.addVariable(output, LogicalType.newInstance(false));
            break;
        default:
            throw new NotImplementedException("Builtin " + variable.toString().toLowerCase(Locale.UK));
        }
    }

}
