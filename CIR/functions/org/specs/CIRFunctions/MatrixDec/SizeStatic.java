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

package org.specs.CIRFunctions.MatrixDec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRFunctions.CirFilename;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixTypeBuilder;

import pt.up.fe.specs.util.SpecsStrings;

/**
 * @author Joao Bispo
 * 
 */
public class SizeStatic extends AInstanceBuilder {

    public SizeStatic(ProviderData data) {
	super(data);
    }

    /**
     * Creates a new instance of the function 'size' for scalar values, for one or two inputs, which always returns one.
     * 
     * <p>
     * This version replaces in the code the function call.
     * 
     * <p>
     * FunctionCall receives:<br>
     * nothing
     * 
     * @param variableType
     * @return
     */
    public FunctionInstance newSizeScalar() {

	String functionName = "size_scalar";

	// Use double types as input to accept any numeric type
	// Arrays.asList(doubleType, doubleType)
	List<VariableType> emptyTypes = Collections.emptyList();
	FunctionType fTypes = FunctionType.newInstanceNotImplementable(emptyTypes, getNumerics().newInt());

	InlineCode code = arguments -> "1";

	InlinedInstance instance = new InlinedInstance(fTypes, functionName, code);

	return instance;
    }

    public FunctionInstance newSizeScalarMatrix() {

	// Use double types as input to accept any numeric type
	// Arrays.asList(doubleType, doubleType)

	ScalarType intType = getNumerics().newInt();
	MatrixType outputType = StaticMatrixTypeBuilder
		.fromElementTypeAndShape(intType, TypeShape.newInstance(1, 2))
		.inRange(1, 1)
		.build();

	FunctionType fTypes = FunctionType.newInstanceWithOutputsAsInputs(Collections.emptyList(),
		Collections.emptyList(),
		"size",
		outputType);

	CInstructionList cBody = new CInstructionList(fTypes);

	InstanceProvider setFunction = outputType.matrix().functions().set();

	CNode sizeNode = CNodeFactory.newVariable("size", outputType);
	CNode zeroNode = CNodeFactory.newCNumber(0);
	CNode oneNode = CNodeFactory.newCNumber(1);

	cBody.addInstruction(getFunctionCall(setFunction, sizeNode, zeroNode, oneNode));
	cBody.addInstruction(getFunctionCall(setFunction, sizeNode, oneNode, oneNode));

	cBody.addReturn(sizeNode);

	String functionName = "size_scalar_matrix";
	InstructionsInstance instance = new InstructionsInstance(functionName, "lib/size", cBody);

	return instance;
    }

    /**
     * Creates a new instance of the function 'size' for two inputs, which returns the value of the size for the given
     * index.
     * 
     * <p>
     * If the given (zero-based) index scalar has a constant, this version replaces in the code the function call for
     * the value indicated by index.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A declared matrix, which will be the source (also works with numerics, which are assumed to have size 1x1);<br>
     * - A scalar;
     * 
     * @param variableType
     * @return
     */
    private FunctionInstance newSize(List<VariableType> inputTypes) {

	// Get index
	VariableType indexType = inputTypes.get(1);

	if (ScalarUtils.hasConstant(indexType)) {

	    String indexString = ScalarUtils.getConstantString(indexType);
	    int index = SpecsStrings.parseInt(indexString);
	    return newSizeConstant((MatrixType) inputTypes.get(0), index);
	}

	return newSizeWithIndex(inputTypes);

    }

    /**
     * Creates a new instance of the function 'size', which returns a row vector with the size of given matrix.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A declared matrix, which will be the source (also works with numerics, which are assumed to have size 1x1);<br>
     * 
     * @param matrixType
     * @return
     */
    private FunctionInstance newSize(MatrixType matrixType) {

	// Name will be "size_" + shape
	String functionName = "size_" + matrixType.getTypeShape().getString();

	// Get declared matrix
	List<Integer> shape = matrixType.getTypeShape().getDims();

	// Build output info
	String outputName = "size_array";

	// Output will be a row-vector with size 1x<SHAPE_SIZE>
	final List<Integer> rowShape = Arrays.asList(1, shape.size());
	final TypeShape matrixShapeWithValues = TypeShape.newShapeWithValues(rowShape, shape);

	ScalarType elementType = getNumerics().newInt();
	// StaticMatrixType outputType = StaticMatrixType.newInstance(elementType, rowShape);
	MatrixType outputType;
	if (getData().getOutputTypes().size() == 1 && getData().getOutputTypes().get(0) instanceof MatrixType) {
	    outputType = (MatrixType) getData().getOutputTypes().get(0);

	    functionName += "_" + outputType.getSmallId();
	} else {
	    outputType = StaticMatrixTypeBuilder
		    .fromElementTypeAndShape(elementType, matrixShapeWithValues)
		    .build();
	}

	List<String> inputNames = Collections.emptyList();
	List<VariableType> inputTypes = Collections.emptyList();

	FunctionType functionTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputName,
		outputType);

	CInstructionList body = new CInstructionList(functionTypes);

	CNode outputVar = CNodeFactory.newVariable(outputName, outputType);

	List<CNode> createNodes = new ArrayList<>(shape.size());
	for (int i = 0; i < shape.size(); i++) {
	    createNodes.add(CNodeFactory.newCNumber(shape.get(i)));
	}
	ProviderData createData = getData().createFromNodes(createNodes);
	createData.setOutputType(outputType);
	FunctionInstance instance = outputType.matrix().functions().create().getCheckedInstance(createData);
	if (!instance.getFunctionType().isNoOp()) {
	    FunctionCallNode functionCall = instance.newFunctionCall(createNodes);
	    functionCall.getFunctionInputs().setChild(createNodes.size(), outputVar);
	    body.addInstruction(functionCall);
	}

	// Add an assignment for each shape element
	for (int i = 0; i < shape.size(); i++) {
	    // Current index
	    CNode index = CNodeFactory.newCNumber(i);
	    // Create array access
	    // CToken access = CTokenFactory.newFunctionCall(getInline, outputVar, index);
	    CNode access = getFunctionCall(outputType.matrix().functions().get(), outputVar, index);

	    // Shape
	    CNode shapeValue = CNodeFactory.newCNumber(shape.get(i));
	    // Create assignment
	    body.addAssignment(access, shapeValue);
	}

	// Add return
	body.addReturn(outputVar);

	InstructionsInstance inst = new InstructionsInstance(functionName, CirFilename.DECLARED.getFilename(), body);

	return inst;
    }

    FunctionInstance newSizeWithIndex(List<VariableType> inputs) {

	// VariableType variableType = inputs.get(0);
	MatrixType variableType = getTypeAtIndex(MatrixType.class, 0);

	// Get declared matrix
	List<Integer> shape = MatrixUtils.getShapeDims(variableType);

	String functionName = "size_index_" + variableType.getTypeShape().getString();

	VariableType intType = getNumerics().newInt();

	// Build output info
	String outputName = "size_dim";

	// Output will be an integer
	VariableType outputType = intType;

	// Output will be a row-vector with size 1x<SHAPE_SIZE>
	// final List<Integer> rowShape = Arrays.asList(1, shape.size());
	// VariableType elementType = numerics().newInt();
	// VariableType outputType = VariableTypeFactory.newDeclaredMatrix(elementType, rowShape);

	String indexName = "index";
	List<String> inputNames = Arrays.asList(indexName);
	List<VariableType> inputTypes = Arrays.asList(intType);

	FunctionType functionTypes = FunctionType.newInstance(inputNames, inputTypes, outputName, outputType);

	CInstructionList body = new CInstructionList(functionTypes);

	CNode indexVar = CNodeFactory.newVariable(indexName, intType);
	CNode numElemVar = CNodeFactory.newCNumber(shape.size());

	// Add if(index >= elem) return 1;
	CNode condition = getFunctionCall(COperator.GreaterThanOrEqual, indexVar, numElemVar);
	CNode return1 = CNodeFactory.newReturn(CNodeFactory.newCNumber(1));

	body.addInstruction(IfNodes.newIfThen(condition, return1));

	// Add size(X);
	FunctionInstance sizeInstance = newSize(variableType);
	VariableType sizeArrayType = sizeInstance.getFunctionType().getCReturnType();
	CNode sizeVar = CNodeFactory.newVariable("size_array", sizeArrayType);
	// CToken sizeCall = CTokenFactory.newFunctionCall(sizeInstance, sizeVar);

	body.addFunctionCall(sizeInstance, sizeVar);

	// Add return get(size(X), index)
	// Add return get(sizeVar, index)

	// FunctionInstance getInstance = new DeclaredFunctions(getData()).newGetInline(sizeArrayType, 1);
	// CToken getCall = CTokenFactory.newFunctionCall(getInstance, sizeVar, indexVar);
	CNode getCall = getFunctionCall(variableType.matrix().functions().get(), sizeVar, indexVar);

	body.addReturn(getCall);
	// body.addReturn(sizeCall);
	InstructionsInstance inst = new InstructionsInstance(functionName, CirFilename.DECLARED.getFilename(), body);

	return inst;
    }

    FunctionInstance newSizeConstant(MatrixType variableType, int index) {

	// Get declared matrix
	List<Integer> shape = MatrixUtils.getShapeDims(variableType);

	// Get value
	int value = 1;
	if (index < shape.size()) {
	    value = shape.get(index);
	}

	String functionName = "size_static_" + variableType.getTypeShape().getString() + "_" + value;

	// Create output type with value
	// VariableType outputType = VariableTypeFactoryOld.newNumeric(NumericDataFactory
	// .newInstance(value));
	VariableType outputType = getNumerics().newInt(value);
	VariableType intType = getNumerics().newInt();
	// BUG: Check if first input should be used in inputs list
	FunctionType fTypes = FunctionType.newInstanceNotImplementable(Arrays.asList(variableType, intType),
		outputType);

	InlineCode code = new SizeStaticInlined(value);

	InlinedInstance instance = new InlinedInstance(fTypes, functionName, code);

	return instance;
    }

    static class SizeStaticInlined implements InlineCode {

	private final int value;

	/**
	 * @param value
	 */
	public SizeStaticInlined(int value) {
	    this.value = value;
	}

	/* (non-Javadoc)
	 * @see org.specs.CIR.Functions.Instances.InlineCode#getInlineCode(java.util.List)
	 */
	@Override
	public String getInlineCode(List<CNode> arguments) {
	    return Integer.toString(this.value);
	}

    }

    /**
     * @param data2
     * @return
     */
    @Override
    public FunctionInstance create() {
	List<VariableType> inputTypes = getData().getInputTypes();

	// Check number of inputs
	if (inputTypes.size() == 1) {
	    return newSize(getData().getInputType(MatrixType.class, 0));
	} else if (inputTypes.size() == 2) {
	    return newSize(inputTypes);
	} else {
	    throw new RuntimeException("Not implemented for " + inputTypes.size() + " inputs");
	}
    }

}
