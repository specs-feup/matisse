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

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.EndInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferenceRule;

public class EndInstructionRule implements TypeInferenceRule {

    @Override
    public boolean accepts(SsaInstruction instruction) {
	return instruction instanceof EndInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context,
	    InstructionLocation location,
	    SsaInstruction instruction) {

	EndInstruction end = (EndInstruction) instruction;

	Optional<TypeShape> inputShape = context
		.getVariableType(end.getInputVariable())
		.filter(MatrixType.class::isInstance)
		.map(MatrixType.class::cast)
		.map(MatrixType::getTypeShape);

	int index = end.getIndex();
	int numIndices = end.getNumIndices();

	Optional<Integer> size = getSize(inputShape, index, numIndices);

	String variableName = end.getOutput();
	VariableType type = context
		.getDefaultVariableType(variableName)
		.orElseGet(() -> context.getNumerics().newInt(size));
	context.addVariable(variableName, type);
    }

    private static Optional<Integer> getSize(Optional<TypeShape> inputShape, int index, int numIndices) {
	if (!inputShape.isPresent()) {
	    return Optional.empty();
	}

	TypeShape shape = inputShape.get();

	if (numIndices == 1) {
	    if (shape.isFullyDefined()) {
		return Optional.of(shape.getNumElements());
	    }

	    return Optional.empty();
	}
	if (shape.getRawNumDims() >= 2) {
	    if (shape.getRawNumDims() <= index) {
		return Optional.of(1);
	    }

	    if (index < numIndices - 1 || shape.getRawNumDims() == numIndices) {
		int dim = shape.getDim(index);
		if (dim < 0) {
		    return Optional.empty();
		}
		return Optional.of(dim);
	    }
	}

	return Optional.empty();
    }
}
