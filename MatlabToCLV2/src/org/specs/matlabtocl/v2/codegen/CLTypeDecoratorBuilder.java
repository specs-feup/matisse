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

package org.specs.matlabtocl.v2.codegen;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.GetOrFirstInstruction;
import org.specs.matisselib.ssa.instructions.MatrixGetInstruction;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.unssa.VariableAllocation;

public class CLTypeDecoratorBuilder {
    private final FunctionBody body;
    private final List<String> variableNames;
    private final VariableAllocation variableAllocation;
    private final Function<String, Optional<VariableType>> typeGetter;

    public CLTypeDecoratorBuilder(FunctionBody body,
	    List<String> variableNames,
	    VariableAllocation variableAllocation,
	    Function<String, Optional<VariableType>> typeGetter) {

	this.body = body;
	this.variableNames = variableNames;
	this.variableAllocation = variableAllocation;
	this.typeGetter = typeGetter;
    }

    public FunctionBody getBody() {
	return this.body;
    }

    public List<String> getVariableNames() {
	return this.variableNames;
    }

    public VariableAllocation getVariableAllocation() {
	return this.variableAllocation;
    }

    public CLTypeDecorator buildTypeDecorator() {
	MatrixTypeChooser matrixTypeChooser = new MatrixTypeChooser();

	for (SsaInstruction instruction : this.body.getFlattenedInstructionsIterable()) {
	    if (instruction instanceof GetOrFirstInstruction) {
		GetOrFirstInstruction getOrFirst = (GetOrFirstInstruction) instruction;

		String ssaMatrix = getOrFirst.getInputMatrix();
		String finalMatrix = this.variableNames.get(this.variableAllocation.getGroupIdForVariable(ssaMatrix));
		matrixTypeChooser.requireNumel(finalMatrix);
	    }
	    if (instruction instanceof FunctionCallInstruction) {
		FunctionCallInstruction typedCall = (FunctionCallInstruction) instruction;
		if (typedCall.getFunctionName().equals("numel") && typedCall.getInputVariables().size() == 1) {
		    String ssaMatrix = typedCall.getInputVariables().get(0);
		    String finalMatrix = this.variableNames
			    .get(this.variableAllocation.getGroupIdForVariable(ssaMatrix));
		    matrixTypeChooser.requireNumel(finalMatrix);
		}
		if (typedCall.getFunctionName().equals("size") && typedCall.getInputVariables().size() == 2) {
		    String ssaMatrix = typedCall.getInputVariables().get(0);
		    String finalMatrix = this.variableNames
			    .get(this.variableAllocation.getGroupIdForVariable(ssaMatrix));

		    String ssaDim = typedCall.getInputVariables().get(1);
		    ScalarType scalarType = (ScalarType) typeGetter.apply(ssaDim).get();
		    Number dim = scalarType.scalar().getConstant();
		    if (dim == null) {
			matrixTypeChooser.requireFullShape(finalMatrix);
		    } else {
			matrixTypeChooser.requireAtLeastShape(finalMatrix, dim.intValue());
		    }
		}
	    }
	    if (instruction instanceof MatrixGetInstruction) {
		MatrixGetInstruction get = (MatrixGetInstruction) instruction;

		String ssaMatrix = get.getInputMatrix();
		String finalMatrix = this.variableNames.get(this.variableAllocation.getGroupIdForVariable(ssaMatrix));
		int numIndices = get.getIndices().size();
		if (numIndices >= 2) {
		    matrixTypeChooser.requireAtLeastShape(finalMatrix, numIndices - 1);
		}
	    }
	    if (instruction instanceof SimpleGetInstruction) {
		SimpleGetInstruction get = (SimpleGetInstruction) instruction;

		String ssaMatrix = get.getInputMatrix();
		String finalMatrix = this.variableNames.get(this.variableAllocation.getGroupIdForVariable(ssaMatrix));
		int numIndices = get.getIndices().size();
		if (numIndices >= 2) {
		    matrixTypeChooser.requireAtLeastShape(finalMatrix, numIndices - 1);
		}
	    }
	    if (instruction instanceof MatrixSetInstruction) {
		MatrixSetInstruction get = (MatrixSetInstruction) instruction;

		String ssaMatrix = get.getInputMatrix();
		String finalMatrix = this.variableNames.get(this.variableAllocation.getGroupIdForVariable(ssaMatrix));
		int numIndices = get.getIndices().size();
		if (numIndices >= 2) {
		    matrixTypeChooser.requireAtLeastShape(finalMatrix, numIndices - 1);
		}
	    }
	    if (instruction instanceof SimpleSetInstruction) {
		SimpleSetInstruction get = (SimpleSetInstruction) instruction;

		String ssaMatrix = get.getInputMatrix();
		String finalMatrix = this.variableNames.get(this.variableAllocation.getGroupIdForVariable(ssaMatrix));
		int numIndices = get.getIndices().size();
		if (numIndices >= 2) {
		    matrixTypeChooser.requireAtLeastShape(finalMatrix, numIndices - 1);
		}
	    }
	}

	return new CLTypeDecorator(matrixTypeChooser);
    }
}
