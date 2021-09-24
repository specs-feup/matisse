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

import java.util.List;
import java.util.Optional;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.Scalar;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.matisselib.helpers.InputProcessor;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.ArgumentInstruction;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferenceRule;

public class ArgumentInstructionRule implements TypeInferenceRule {

    @Override
    public boolean accepts(SsaInstruction instruction) {
	return instruction instanceof ArgumentInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context,
	    InstructionLocation location,
	    SsaInstruction instruction) {

	ArgumentInstruction argumentInstruction = (ArgumentInstruction) instruction;
	int argumentIndex = argumentInstruction.getArgumentIndex();
	List<VariableType> inputTypes = context.getProviderData().getInputTypes();

	String outputName = argumentInstruction.getOutput();

	Optional<VariableType> defaultType = context.getDefaultVariableType(outputName);

	if (argumentIndex >= inputTypes.size()) {
	    // Argument is undefined.
	    // Do not add the variable even if it is defined in the aspects.
	    context.pushInstructionModification(location, AssignmentInstruction.fromUndefinedValue(outputName));
	    return;
	}

	defaultType.ifPresent(type -> System.out.println("Overriding type of " + outputName + " to " + type));
	VariableType type = defaultType.orElseGet(() -> {
	    VariableType candidate = inputTypes.get(argumentIndex);

	    if (candidate instanceof DynamicMatrixType) {
		DynamicMatrixType dynamicMatrixType = (DynamicMatrixType) candidate;
		ScalarType elementType = dynamicMatrixType.matrix().getElementType();
		return DynamicMatrixType.newInstance(elementType,
			InputProcessor.processDynamicMatrixInputShape(dynamicMatrixType));
	    }
	    if (candidate instanceof ScalarType) {
		Scalar scalar = ((ScalarType) candidate).scalar();
		if (!context.isScalarConstantSpecialized(argumentIndex) && scalar.hasConstant()) {
		    // Forget constant information
		    return scalar.removeConstant();
		}
	    }

	    return candidate;
	});

	context.addVariable(outputName, type);
    }
}
