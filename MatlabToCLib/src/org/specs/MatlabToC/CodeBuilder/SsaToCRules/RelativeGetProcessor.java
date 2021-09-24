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
import org.specs.matisselib.ssa.instructions.RelativeGetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class RelativeGetProcessor implements SsaToCRule {

    @Override
    public boolean accepts(SsaToCBuilderService builder, SsaInstruction instruction) {
	return instruction instanceof RelativeGetInstruction;
    }

    @Override
    public void apply(SsaToCBuilderService builder, CInstructionList currentBlock, SsaInstruction instruction) {
	RelativeGetInstruction get = (RelativeGetInstruction) instruction;

	String output = instruction.getOutputs().get(0);

	String matrix = get.getInputMatrix();
	String sizes = get.getSizeMatrix();
	List<String> indices = get.getIndices();

	VariableType inputType = builder.getVariableTypeFromSsaName(matrix).get();
	if (inputType instanceof MatrixType) {

	    indices
		    .forEach(index -> {
			assert builder.getVariableTypeFromSsaName(index).get() instanceof ScalarType;
		    });

	    emitMatrixGetImplementation(builder, currentBlock, output, matrix, sizes, indices);

	} else if (inputType instanceof ScalarType) {
	    SimpleGetProcessor.emitScalarImplementation(builder, currentBlock, output, matrix);
	} else {
	    throw new NotImplementedException("Simple get expression for type " + inputType);
	}
    }

    private static void emitMatrixGetImplementation(SsaToCBuilderService builder,
	    CInstructionList currentBlock,
	    String output,
	    String matrix,
	    String sizes,
	    List<String> indices) {

	ProviderData providerData = builder.getCurrentProvider();

	MatrixType inputMatrixType = (MatrixType) builder.getVariableTypeFromSsaName(matrix).get();
	CNode inputMatrix = builder.generateVariableExpressionForSsaName(currentBlock, matrix, false);

	MatrixType sizeMatrixType = (MatrixType) builder.getVariableTypeFromSsaName(sizes).get();
	CNode sizesNode = builder.generateVariableNodeForSsaName(sizes);

	CNode zeroNode = CNodeFactory.newCNumber(0);
	CNode oneNode = CNodeFactory.newCNumber(1);

	CNode normalZeroBasedIndex = null;
	for (int i = 0; i < indices.size(); ++i) {
	    CNode indexValue = builder.generateVariableExpressionForSsaName(currentBlock, indices.get(i), false);

	    CNode minusOne = COperator.Subtraction
		    .newCInstance(providerData.createFromNodes(indexValue, oneNode))
		    .newFunctionCall(indexValue, oneNode);
	    CNode partialIndex = minusOne;
	    for (int j = 0; j < i; ++j) {
		CNode dim = CNodeFactory.newCNumber(j);
		CNode dimValue = sizeMatrixType.matrix().functions()
			.get()
			.newCInstance(providerData.createFromNodes(sizesNode, dim))
			.newFunctionCall(sizesNode, dim);

		partialIndex = COperator.Multiplication
			.newCInstance(providerData.createFromNodes(partialIndex, dimValue))
			.newFunctionCall(partialIndex, dimValue);
	    }

	    if (normalZeroBasedIndex == null) {
		normalZeroBasedIndex = partialIndex;
	    } else {
		normalZeroBasedIndex = COperator.Addition
			.newCInstance(providerData.createFromNodes(normalZeroBasedIndex, partialIndex))
			.newFunctionCall(normalZeroBasedIndex, partialIndex);
	    }
	}

	assert normalZeroBasedIndex != null;

	InstanceProvider getProvider = inputMatrixType
		.matrix()
		.functions()
		.get();

	CNode numel = inputMatrixType
		.matrix()
		.functions()
		.numel()
		.getCheckedInstance(providerData.createFromNodes(inputMatrix))
		.newFunctionCall(inputMatrix);
	CNode numelIsOne = COperator.Equal
		.getCheckedInstance(providerData.createFromNodes(numel, oneNode))
		.newFunctionCall(numel, oneNode);

	CNode zeroBasedIndexExpression = COperator.Ternary
		.getCheckedInstance(providerData.createFromNodes(numelIsOne, zeroNode, normalZeroBasedIndex))
		.newFunctionCall(numelIsOne, zeroNode, normalZeroBasedIndex);
	CNode valueExpression = getProvider
		.getCheckedInstance(
			providerData.createFromNodes(inputMatrix, zeroBasedIndexExpression))
		.newFunctionCall(inputMatrix, zeroBasedIndexExpression);

	CNode outputNode = builder.generateVariableNodeForSsaName(output);

	currentBlock.addAssignment(outputNode, valueExpression);
    }
}
