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

package org.specs.MatlabToC.CodeBuilder.SsaToCRules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

public class SimpleSetProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
	return instruction instanceof SimpleSetInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
	SimpleSetInstruction set = (SimpleSetInstruction) instruction;
	String ssaValue = set.getValue();
	List<String> ssaIndices = set.getIndices();
	String ssaInputMatrix = set.getInputMatrix();
	String ssaOutputMatrix = set.getOutputs().get(0);

	if (builder.generateAssignmentForSsaNames(currentBlock, ssaOutputMatrix, ssaInputMatrix)
		&& MatrixUtils.isMatrix(builder.getVariableTypeFromSsaName(ssaInputMatrix).get())) {

	    builder.getReporter().emitMessage(PassMessage.OPTIMIZATION_OPPORTUNITY, "Copying matrix");

	}

	// From here on, we'll just use the output matrix.

	VariableType ssaOutputMatrixType = builder.getInstance().getVariableType(ssaOutputMatrix).get();
	assert ssaOutputMatrixType instanceof MatrixType : "Expected MatrixType, got " + ssaOutputMatrix + ": "
		+ ssaOutputMatrixType;
	MatrixType matrixType = (MatrixType) ssaOutputMatrixType;
	InstanceProvider setProvider = matrixType.matrix().functions().set();

	List<CNode> inputs = new ArrayList<>();
	inputs.add(builder.generateVariableExpressionForSsaName(currentBlock, ssaOutputMatrix, false));

	CNode one = CNodeFactory.newCNumber(1);
	for (String index : ssaIndices) {
	    CNode inputVariable = builder.generateVariableExpressionForSsaName(currentBlock, index, false);

	    CNode inputNode = FunctionInstanceUtils.getFunctionCall(COperator.Subtraction,
		    builder.getCurrentProvider(), Arrays.asList(inputVariable, one));

	    inputs.add(inputNode);
	}
	inputs.add(builder.generateVariableExpressionForSsaName(currentBlock, ssaValue));

	CNode functionCall = FunctionInstanceUtils.getFunctionCall(setProvider, builder.getCurrentProvider(),
		inputs);
	currentBlock.addInstruction(functionCall);

    }
}
