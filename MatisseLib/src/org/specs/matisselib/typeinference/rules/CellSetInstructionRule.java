/**
 * Copyright 2016 SPeCS.
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

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.CellSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferenceRule;
import org.specs.matisselib.types.DynamicCellType;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class CellSetInstructionRule implements TypeInferenceRule {

    @Override
    public boolean accepts(SsaInstruction instruction) {
	return instruction instanceof CellSetInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context, InstructionLocation location, SsaInstruction instruction) {
	CellSetInstruction set = (CellSetInstruction) instruction;

	String input = set.getInputMatrix();
	VariableType type = context.requireVariableType(input);

	VariableType resultType;
	if (type instanceof DynamicCellType) {
	    resultType = handleDynamicCellType((DynamicCellType) type, context, set);
	} else {
	    throw new NotImplementedException(type.getClass());
	}

	String output = set.getOutput();
	context.addVariable(output, resultType, context.getDefaultVariableType(output));
    }

    private static DynamicCellType handleDynamicCellType(DynamicCellType type, TypeInferenceContext context,
	    CellSetInstruction set) {

	// TODO: We can improve the shape information
	return new DynamicCellType(type.getUnderlyingType(), TypeShape.newUndefinedShape());
    }

}
