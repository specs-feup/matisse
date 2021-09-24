/**
 * Copyright 2014 SPeCS.
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

package org.specs.MatlabToC.Functions.Blas;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Utilities.CodeReplacer;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.MatlabToC.Functions.MatlabOpsV2.MatrixMul;
import org.specs.MatlabToC.jOptions.AMatlabInstanceBuilder;

public class BlasMult extends AMatlabInstanceBuilder {

    private final static String BLAS_FILENAME = "blas";

    private final static String INPUT_A = "A";
    private final static String INPUT_B = "B";
    private final static String OUTPUT_C = "C";

    public BlasMult(ProviderData data) {
	super(data);
    }

    /**
     * A BLAS call to matrix multiplication.
     * 
     * <p>
     * Currently, both inputs must be dynamic matrices of the same type and double.
     * 
     * @return
     */
    @Override
    public FunctionInstance create() {

	// Return type will be the same as any of the outputs
	// Optional<VariableType> emptyOp = Optional.empty();

	FunctionType type = getType();

	String functionName = getFunctionName("matrix_mul_blas", type.getCInputTypes());

	String filename = BlasUtils.getBlasFilename(BLAS_FILENAME);

	CodeReplacer body = getBody(type);

	LiteralInstance fInst = new LiteralInstance(type, functionName, filename, body);

	fInst.setCustomImplementationIncludes(SystemInclude.Blas);
	// fInst.getCustomImplementationInstances().add(body.getCNodes());

	return fInst;
    }

    private FunctionType getType() {
	ScalarType inferredElement = getInferredType(MatrixUtils.getElementTypes(getData().getInputTypes()),
		Optional.empty());

	TypeShape shape = MatrixUtils.getMultiplicationShape(getTypeAtIndex(MatrixType.class, 0),
		getTypeAtIndex(MatrixType.class, 1));

	MatrixType inferredType = DynamicMatrixType.newInstance(inferredElement, shape);

	VariableType returnType = inferredType;

	List<String> inputNames = Arrays.asList("A", "B");
	List<VariableType> inputTypes = Arrays.asList(inferredType, inferredType);
	// FunctionType type = FunctionType.newInstanceWithOutputsAsInputs(inputNames, getData().getInputTypes(), "C",
	// returnType);
	FunctionType type = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, "C", returnType);
	return type;
    }

    private CodeReplacer getBody(FunctionType type) {
	CodeReplacer body = new CodeReplacer(BlasResource.MULT);

	// Getters

	// MatrixType matrixAType = type.getInput(MatrixType.class, 0);
	// MatrixType matrixBType = type.getInput(MatrixType.class, 1);

	Variable matrixA = new Variable(INPUT_A, type.getInput(MatrixType.class, 0));
	Variable matrixB = new Variable(INPUT_B, type.getInput(MatrixType.class, 1));

	body.replace("<GET_A_0>", getNodes().matrix().getDim(matrixA, 0));
	body.replace("<GET_A_1>", getNodes().matrix().getDim(matrixA, 1));
	body.replace("<GET_B_0>", getNodes().matrix().getDim(matrixB, 0));
	body.replace("<GET_B_1>", getNodes().matrix().getDim(matrixB, 1));

	// New array

	// MatrixType matrixCType = type.getOutput(MatrixType.class, 0);
	Variable matrixC = new Variable(OUTPUT_C, type.getOutput(MatrixType.class, 0));
	NumericTypeV2 intType = getNumerics().newInt();

	Variable nRowA = new Variable("nRowA", intType);
	Variable nColB = new Variable("nColB", intType);

	body.replace("<NEW_ARRAY_C>", getNodes().matrix().create(matrixC, nRowA, nColB));

	// BLAS call type
	ScalarType elementType = MatrixUtils.getElementType(type.getCReturnType());

	String blasCall = "cblas_dgemm";
	if (elementType.scalar().getBits() <= 32) {
	    blasCall = "cblas_sgemm";
	}

	body.replace("<BLAS_CALL>", blasCall);

	// Get dims
	body.replace("<GET_DIMS_A>", getNodes().matrix().numDims(matrixA));
	body.replace("<GET_DIMS_B>", getNodes().matrix().numDims(matrixB));

	// Mult naive as last resort
	// FunctionInstance multInstance = MFileProvider.getInstance(OpsResource.MATRIX_MULV3, getData());
	FunctionInstance multInstance = MatrixMul.newNaiveProvider().newCInstance(getData());
	CNode multCall = CNodeFactory.newFunctionCall(
		multInstance,
		Arrays.asList(CNodeFactory.newVariable(matrixA),
			CNodeFactory.newVariable(matrixB),
			CNodeFactory.newVariable(matrixC)));

	body.replace("<MATRIX_MULT>", multCall);

	// Data

	body.replace("<DATA_A>", getNodes().matrix().data(matrixA));
	body.replace("<DATA_B>", getNodes().matrix().data(matrixB));
	body.replace("<DATA_C>", getNodes().matrix().data(matrixC));

	return body;
    }

    public static InstanceProvider getProvider() {
	return new InstanceProvider() {
	    @Override
	    public FunctionInstance newCInstance(ProviderData data) {
		return new BlasMult(data).create();
	    }

	    @Override
	    public FunctionType getType(ProviderData data) {
		return new BlasMult(data).getType();
	    }
	};
    }
}
