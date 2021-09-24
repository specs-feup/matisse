/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.CIRFunctions.MatrixAlloc;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.InstanceBuilder.GenericInstanceBuilder;
import org.specs.CIR.FunctionInstance.InstanceBuilder.InstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;
import org.specs.CIRFunctions.CirFilename;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;

import pt.up.fe.specs.util.SpecsCollections;

/**
 * @author Joao Bispo
 * 
 */
public class Size extends AInstanceBuilder {

    private final ProviderData data;

    public Size(ProviderData data) {
	super(data);

	this.data = data;
    }

    /**
     * Creates a new instance of the function 'size', which returns a row vector with the size of given matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A dynamic matrix, which will be the source;<br>
     * 
     * @param variableType
     * @return
     */
    @Override
    public FunctionInstance create() {

	InstanceBuilder helper = new GenericInstanceBuilder(this.data);

	TensorFunctions tensorFunctions = new TensorFunctions(this.data);

	// Get element type
	MatrixType inputType = (MatrixType) this.data.getInputTypes().get(0);
	VariableType elementType = MatrixUtils.getElementType(inputType);
	MatrixType matrixType = DynamicMatrixType.newInstance(elementType);

	// Name of the function
	String functionName = "size_" + elementType.getSmallId();

	// Input name
	String tensorName = "t";

	List<String> inputNames = Arrays.asList(tensorName);

	// Input types
	VariableType tensorType = DynamicMatrixType.newInstance(elementType);
	List<VariableType> inputTypes = Arrays.asList(tensorType);

	// Output name
	String outputName = "size_array";
	VariableType intType = getNumerics().newInt();

	int numCols = -1;
	int rawNumDims = inputType.getTypeShape().getRawNumDims();
	if (rawNumDims == 2) {
	    numCols = 2;
	} else if (rawNumDims > 0) {
	    List<Integer> dims = inputType.getTypeShape().getDims();

	    int lastDim = SpecsCollections.last(dims);
	    if (lastDim >= 0) {
		assert lastDim != 1;

		numCols = rawNumDims;
	    }
	}

	DynamicMatrixType outputType = DynamicMatrixType.newInstance(intType, TypeShape.newRow(numCols));

	// FunctionTypes
	FunctionType fTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputName,
		outputType);

	CInstructionList body = new CInstructionList(fTypes);

	// Create newArray function
	CNode inputVar = CNodeFactory.newVariable(tensorName, matrixType);
	FunctionInstance newArray = new TensorCreationFunctions(getData()).newArray(intType, 2);
	CNode dim1 = CNodeFactory.newCNumber(1);
	CNode dim2 = CNodeFactory.newFunctionCall(tensorFunctions.newNdims(matrixType), inputVar);
	CNode asOutputVar = CNodeFactory.newVariable(outputName, ReferenceUtils.getType(outputType, true));

	body.addFunctionCall(newArray, dim1, dim2, asOutputVar);

	// Build For loop
	VariableNode inductionVar = CNodeFactory.newVariable("i", intType);

	// Loop instruction
	CNode leftHand = getFunctionCall(outputType.matrix().functions().get(), asOutputVar, inductionVar);
	CNode rightHand = helper.getFunctionCall(TensorProvider.DIM_SIZE, inputVar, inductionVar);

	CNode loopInst = CNodeFactory.newAssignment(leftHand, rightHand);

	CNode forToken = new ForNodes(this.data).newForLoopBlock(inductionVar, dim2, loopInst);
	body.addInstruction(forToken);

	// Add return
	body.addReturn(asOutputVar);

	return new InstructionsInstance(functionName, CirFilename.ALLOCATED.getFilename(), body);
    }
}
