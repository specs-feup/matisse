/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.MatlabToC.Functions.BaseFunctions.Dynamic;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;
import org.specs.CIRFunctions.MatrixAlloc.TensorFunctionsUtils;
import org.specs.CIRFunctions.MatrixAlloc.TensorProvider;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.MatlabToCTypesUtils;

import com.google.common.collect.Lists;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * 
 * @author Joao Bispo
 * 
 */
public class ArrayAllocFunctions extends AInstanceBuilder {

    final String prefixName;
    final CNode value;

    /**
     * @param data
     */
    public ArrayAllocFunctions(ProviderData data, String prefixName, CNode value) {
	super(data);

	this.prefixName = prefixName;
	this.value = value;
    }

    /**
     * Helper method for 'newConstantArrayAlloc',
     *
     * 
     * @param tensorType
     * @param numDims
     * @param prefixName
     * @param value
     * @return
     */
    public static InstanceProvider newConstantHelper(final String prefixName, final CNode value) {

	return new InstanceProvider() {

	    @Override
	    public FunctionInstance newCInstance(ProviderData data) {
		return new ArrayAllocFunctions(data, prefixName, value).create();
	    }
	};
    }

    @Override
    public FunctionInstance create() {

	// Get dims
	int numDims = getDims(getData().getInputTypes());

	// Check if input types have constant information
	List<Integer> dims = Lists.newArrayList();
	// int numIntegers = 0;
	for (VariableType type : getData().getInputTypes()) {
	    // If string, skip
	    if (StringTypeUtils.isString(type)) {
		continue;
	    }

	    if (!ScalarUtils.toScalar(type).scalar().hasConstant()) {
		dims.add(null);
		continue;
	    }

	    dims.add(ScalarUtils.getConstant(type).intValue());
	    // numIntegers++;
	}

	TypeShape shape = TypeShape.newInstance(dims);
	// If no
	// if(numIntegers == 0) {
	// shape = MatrixShape.
	// }

	// Build tensor type
	// VariableType elementType = MatlabToCUtils.getElementTypeFromString(getData().getInputTypes(), numerics());
	VariableType elementType = MatlabToCTypesUtils.getElementType(getData());

	// VariableType tensorType = DynamicMatrixType.newInstance(elementType, MatrixShape.newDimsShape(numDims));
	VariableType tensorType = DynamicMatrixType.newInstance(elementType, shape);

	String functionName = prefixName + "_" + elementType.getSmallId() + numDims;
	String cFilename = MatlabCFilename.ArrayCreatorsAlloc.getCFilename();

	// Input names
	List<String> inputNames = SpecsFactory.newArrayList();
	for (int i = 0; i < numDims; i++) {
	    String inputName = TensorFunctionsUtils.getInputName(i);
	    inputNames.add(inputName);
	}

	// Input types
	VariableType intType = getNumerics().newInt();
	List<VariableType> inputTypes = SpecsFactory.newArrayList();
	for (int i = 0; i < numDims; i++) {
	    inputTypes.add(intType);
	}

	String tensorName = "t";

	FunctionType types = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, tensorName,
		tensorType);

	CInstructionList instructions = new CInstructionList(types);

	List<CNode> functionInputs = SpecsFactory.newArrayList();
	for (int i = 0; i < inputNames.size(); i++) {
	    functionInputs.add(CNodeFactory.newVariable(inputNames.get(i), inputTypes.get(i)));
	}

	functionInputs.add(value);

	VariableType pointerToAllocType = ReferenceUtils.getType(tensorType, true);
	functionInputs.add(CNodeFactory.newVariable(tensorName, pointerToAllocType));

	CNode functionCall = getFunctionCall(TensorProvider.NEW_CONSTANT_ARRAY, functionInputs);

	instructions.addReturn(functionCall);

	return new InstructionsInstance(types, functionName, cFilename, instructions);
    }

    private static int getDims(List<VariableType> inputTypes) {
	// Number of inputs
	int numDims = inputTypes.size();

	// Check that last argument is of type String
	VariableType lastArg = inputTypes.get(inputTypes.size() - 1);
	if (StringTypeUtils.isString(lastArg)) {
	    numDims -= 1;
	}

	// If only one dimension, add another one, for a square matrix
	if (numDims == 1) {
	    numDims += 1;
	}

	return numDims;
    }

}
