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

package org.specs.MatlabToC.Functions.BaseFunctions.Static;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixTypeBuilder;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.jOptions.AMatlabInstanceBuilder;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class RowCombineInstance extends AMatlabInstanceBuilder {

    public RowCombineInstance(ProviderData data) {
	super(data);
    }

    private static final String BASE_NAME = "new_row_combine";

    public static InstanceProvider getProvider() {
	return new InstanceProvider() {

	    @Override
	    public FunctionInstance newCInstance(ProviderData data) {

		return new RowCombineInstance(data).create();
	    }

	    @Override
	    public FunctionType getType(ProviderData data) {
		return new RowCombineInstance(data).getFunctionType();
	    }
	};
    }

    @Override
    public FunctionInstance create() {
	FunctionType functionType = getFunctionType();

	CInstructionList body = new CInstructionList(functionType);

	List<String> inputNames = functionType.getCInputNames();
	List<VariableType> inputTypes = functionType.getCInputTypes();
	String outputName = "output";
	MatrixType outputType = (MatrixType) functionType.getOutputAsInputTypes().get(0);

	List<VariableNode> inputVariables = new ArrayList<>();
	for (int i = 0; i < inputNames.size(); ++i) {
	    inputVariables.add(CNodeFactory.newVariable(inputNames.get(i), inputTypes.get(i)));
	}
	CNode outputMatrix = CNodeFactory.newVariable(outputName, outputType);

	int knownSize = 0;
	List<CNode> unknownSizeNodes = new ArrayList<>();
	for (int i = 0; i < getData().getNumInputs(); ++i) {
	    VariableType type = inputTypes.get(i);
	    if (type instanceof ScalarType) {
		knownSize++;
	    } else if (type instanceof MatrixType) {
		MatrixType matrixType = (MatrixType) type;

		if (matrixType.getTypeShape().isFullyDefined()) {
		    knownSize += matrixType.getTypeShape().getNumElements();
		} else {

		    unknownSizeNodes.add(inputVariables.get(i));

		}
	    } else {
		throw new NotImplementedException(type.getClass());
	    }
	}

	CNode lengthNode;
	if (unknownSizeNodes.size() == 0) {
	    lengthNode = CNodeFactory.newCNumber(knownSize);
	} else {
	    lengthNode = CNodeFactory.newVariable("length", getNumerics().newInt());

	    body.addAssignment(lengthNode, CNodeFactory.newCNumber(knownSize));

	    for (CNode unknownSizeNode : unknownSizeNodes) {
		InstanceProvider numelProvider = ((MatrixType) unknownSizeNode.getVariableType()).matrix().functions()
			.numel();

		CNode partialLength = getFunctionCall(numelProvider, unknownSizeNode);

		body.addAssignment(lengthNode, getFunctionCall(COperator.Addition, lengthNode, partialLength));
	    }
	}

	if (outputType instanceof DynamicMatrixType) {
	    List<CNode> zerosArgs = Arrays.asList(CNodeFactory.newCNumber(1), lengthNode);
	    ProviderData zerosData = getData().createFromNodes(zerosArgs);
	    zerosData.setOutputType(outputType);

	    FunctionCallNode allocNode = MatlabBuiltin.ZEROS.getMatlabFunction()
		    .getCheckedInstance(zerosData)
		    .newFunctionCall(zerosArgs);
	    allocNode.getFunctionInputs().setInput(2, outputMatrix);
	    body.addInstruction(allocNode);
	}

	InstanceProvider setProvider = outputType.matrix().functions().set();

	int currentIndex = 0;
	for (VariableNode input : inputVariables) {
	    VariableType inputType = input.getVariableType();
	    if (inputType instanceof ScalarType) {
		body.addInstruction(getFunctionCall(setProvider,
			outputMatrix,
			CNodeFactory.newCNumber(currentIndex),
			input));
		++currentIndex;
	    } else if (inputType instanceof MatrixType) {
		MatrixType matrixType = (MatrixType) inputType;
		if (matrixType.getTypeShape().isFullyDefined() && matrixType.getTypeShape().getNumElements() < 50) {
		    InstanceProvider getProvider = matrixType.matrix().functions().get();

		    int length = matrixType.getTypeShape().getNumElements();
		    for (int i = 0; i < length; ++i) {
			CNode value = getFunctionCall(getProvider, input, CNodeFactory.newCNumber(i));

			body.addInstruction(getFunctionCall(setProvider,
				outputMatrix,
				CNodeFactory.newCNumber(currentIndex),
				value));

			++currentIndex;
		    }
		} else {
		    CNode length = getFunctionCall(
			    matrixType.matrix().functions().numel(),
			    input);
		    VariableNode inductionVar = CNodeFactory.newVariable("i", getNumerics().newInt());

		    List<CNode> bodyInstructions = new ArrayList<>();
		    CNode get = getFunctionCall(
			    matrixType.matrix().functions().get(), input, inductionVar);
		    CNode set = getFunctionCall(
			    setProvider, outputMatrix, inductionVar, get);
		    bodyInstructions.add(set);

		    body.addInstruction(
			    new ForNodes(getData()).newForLoopBlock(inductionVar, length, bodyInstructions));
		}
	    } else {
		throw new NotImplementedException(inputType.getClass());
	    }
	}

	body.addReturn(outputMatrix);

	String typesSuffix = FunctionInstanceUtils.getTypesSuffix(inputTypes);
	String cFunctionName = RowCombineInstance.BASE_NAME + typesSuffix;
	String cFilename = MatlabCFilename.ArrayCreatorsDec.getCFilename();
	InstructionsInstance instance = new InstructionsInstance(functionType, cFunctionName, cFilename, body);

	return instance;
    }

    private FunctionType getFunctionType() {
	ProviderData providerData = getData();

	List<String> inputNames = new ArrayList<>();
	List<VariableType> inputTypes = providerData.getInputTypes();

	ScalarType underlyingType = getNumerics().newDouble();
	int ncols = 0;

	for (int i = 0; i < inputTypes.size(); ++i) {
	    VariableType inputType = inputTypes.get(i);
	    ScalarType underlyingInputType;

	    if (ScalarUtils.isScalar(inputType)) {
		underlyingInputType = (ScalarType) inputType;
		if (ncols != -1) {
		    ncols += 1;
		}
	    } else {
		underlyingInputType = MatrixUtils.getElementType(inputType);
		TypeShape shape = MatrixUtils.getShape(inputType);
		assert shape.getRawNumDims() == 2;
		int inputCols = shape.getDim(1);

		if (inputCols < 0) {
		    ncols = -1;
		} else if (ncols != -1) {
		    ncols += inputCols;
		}
	    }

	    // TODO: Char types
	    if (!underlyingType.scalar().isInteger()) {
		// If argument type is integer or single-precision float, then that is the new candidate output type.
		if (underlyingInputType.scalar().isInteger() || underlyingInputType.scalar().getBits() == 32) {
		    underlyingType = underlyingInputType;
		}
	    }

	    inputNames.add("in_" + inputType.getSmallId() + "_" + i);
	}

	underlyingType = underlyingType.scalar().removeConstant();

	MatrixType returnType;
	if (ncols >= 0) {
	    returnType = StaticMatrixTypeBuilder
		    .fromElementTypeAndShape(underlyingType, TypeShape.newInstance(1, ncols))
		    .build();
	} else {
	    returnType = DynamicMatrixType.newInstance(underlyingType, TypeShape.newRow());
	}

	FunctionType functionType = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, "output",
		returnType);
	return functionType;
    }

}
