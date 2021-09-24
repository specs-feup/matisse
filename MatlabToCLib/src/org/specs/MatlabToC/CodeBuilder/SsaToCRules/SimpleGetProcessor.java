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

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class SimpleGetProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
	return instruction instanceof SimpleGetInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
	SimpleGetInstruction get = (SimpleGetInstruction) instruction;

	String output = instruction.getOutputs().get(0);

	String matrix = get.getInputMatrix();
	List<String> indices = get.getIndices();

	VariableType inputType = builder.getVariableTypeFromSsaName(matrix).get();
	if (inputType instanceof MatrixType) {

	    for (String index : indices) {
		assert builder.getVariableTypeFromSsaName(index).get() instanceof ScalarType;
	    }

	    emitMatrixGetImplementation(builder, currentBlock, output, matrix, indices);

	} else if (inputType instanceof ScalarType) {
	    emitScalarImplementation(builder, currentBlock, output, matrix);
	} else {
	    throw new NotImplementedException("Simple get expression for type " + inputType);
	}
    }

    public static void emitScalarImplementation(SsaToCBuilderService builder,
	    CInstructionList currentBlock,
	    String output,
	    String matrix) {

	builder.generateAssignmentForSsaNames(currentBlock, output, matrix);

    }

    private static void emitMatrixGetImplementation(SsaToCBuilderService builder,
	    CInstructionList currentBlock,
	    String output,
	    String matrix,
	    List<String> indices) {

	ProviderData providerData = builder.getCurrentProvider();

	MatrixType inputMatrixType = (MatrixType) builder.getVariableTypeFromSsaName(matrix).get();
	CNode inputMatrix = builder.generateVariableExpressionForSsaName(currentBlock, matrix, false);

	List<CNode> indexNodes = new ArrayList<>();
	for (String index : indices) {
	    CNode indexNode = builder
		    .generateVariableExpressionForSsaName(currentBlock, index, false);

	    List<CNode> indexMinusOneArgs = Arrays.asList(indexNode, CNodeFactory.newCNumber(1));
	    InstanceProvider minusProvider = COperator.Subtraction;
	    ProviderData minusData = builder.getCurrentProvider().createFromNodes(indexMinusOneArgs);
	    CNode indexMinusOne = minusProvider.getCheckedInstance(minusData).newFunctionCall(indexMinusOneArgs);

	    indexNodes.add(indexMinusOne);
	}

	List<CNode> getArgs = new ArrayList<>();
	getArgs.add(inputMatrix);
	getArgs.addAll(indexNodes);
	CNode valueExpression = inputMatrixType
		.matrix()
		.functions()
		.get()
		.getCheckedInstance(
			providerData.createFromNodes(getArgs))
		.newFunctionCall(getArgs);

	CNode outputNode = builder.generateVariableNodeForSsaName(output);

	currentBlock.addAssignment(outputNode, valueExpression);
    }

}
