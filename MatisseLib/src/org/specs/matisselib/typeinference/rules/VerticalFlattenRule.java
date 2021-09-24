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
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.VerticalFlattenInstruction;
import org.specs.matisselib.typeinference.TypeInferenceContext;
import org.specs.matisselib.typeinference.TypeInferenceRule;

public class VerticalFlattenRule implements TypeInferenceRule {

    @Override
    public boolean accepts(SsaInstruction instruction) {
	return instruction instanceof VerticalFlattenInstruction;
    }

    @Override
    public void inferTypes(TypeInferenceContext context,
	    InstructionLocation location,
	    SsaInstruction instruction) {

	VerticalFlattenInstruction flatten = (VerticalFlattenInstruction) instruction;

	String output = flatten.getOutput();
	String input = flatten.getInput();

	VariableType outputType = getOutputType(context, output, input);

	context.addVariable(output, outputType);
    }

    private static VariableType getOutputType(TypeInferenceContext context, String output, String input) {
	Optional<VariableType> outputType = context.getDefaultVariableType(output);
	if (outputType.isPresent()) {
	    // TODO: Validate
	    return outputType.get();
	}

	VariableType inputType = context.requireVariableType(input);
	if (inputType instanceof ScalarType) {
	    return inputType;
	} else if (inputType instanceof MatrixType) {
	    ScalarType scalarType = MatrixUtils.getElementType(inputType);
	    TypeShape originalShape = MatrixUtils.getShape(inputType);

	    TypeShape flattenedShape;
	    if (originalShape.isFullyDefined()) {
		flattenedShape = TypeShape.newInstance(originalShape.getNumElements(), 1);
	    } else {
		flattenedShape = TypeShape.newColumn();
	    }

	    if (inputType instanceof StaticMatrixType || inputType instanceof StringType) {
		return ((StaticMatrixType) inputType).asVerticalFlat();
	    }

	    return DynamicMatrixType.newInstance(scalarType, flattenedShape);

	} else {
	    throw context
		    .getProviderData()
		    .getReportService()
		    .emitError(PassMessage.TYPE_INFERENCE_FAILURE,
			    "Can not flatten variable of type " + inputType);
	}
    }
}
