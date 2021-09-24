package org.specs.MatlabToC.Functions.MatlabOps;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;

public class MatrixMultiplicationDecBuilder implements MatlabInstanceProvider {

    @Override
    public boolean checkRule(ProviderData builderData) {

	// Get the inputs
	List<VariableType> inputTypes = builderData.getInputTypes();

	// Need exactly 2 inputs
	if (inputTypes.size() != 2) {
	    return false;
	}

	// Both inputs must be declared matrices
	for (VariableType inputType : inputTypes) {
	    if (!MatrixUtils.isStaticMatrix(inputType)) {
		return false;
	    }
	    /*
	    if (inputType.getType() != CType.Matrix) {
	    return false;
	    }
	    if (!VariableTypeContent.getMatrixDec(inputType).isDeclaredMatrix()) {
	    return false;
	    }
	    */
	}

	// Both matrices must be of a numeric type
	for (VariableType inputType : inputTypes) {

	    // Check if it has a numeric element
	    if (!ScalarUtils.hasScalarType(inputType)) {
		return false;
	    }
	}

	// Get the shapes of both matrices
	List<Integer> firstShape = MatrixUtils.getShapeDims(inputTypes.get(0));
	List<Integer> secondShape = MatrixUtils.getShapeDims(inputTypes.get(1));

	// Check if both matrices are bi-dimensional
	if (firstShape.size() != 2 || secondShape.size() != 2) {
	    throw new RuntimeException("The matrices used in matrix multiplication must be 2-dimensional.");
	}

	// Check if the inner dimensions of the matrices agree
	if (!firstShape.get(1).equals(secondShape.get(0))) {
	    throw new RuntimeException("The inner dimensions of the matrices used in matrix multiplication must agree.");
	}

	return true;
    }

    @Override
    public FunctionInstance create(ProviderData builderData) {

	if (!checkRule(builderData)) {
	    return null;
	}

	return MatrixMultiplicationDecInstance.newInstance(builderData);
    }
}
