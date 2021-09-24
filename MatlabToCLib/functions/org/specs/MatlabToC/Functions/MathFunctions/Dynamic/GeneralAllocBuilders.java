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

package org.specs.MatlabToC.Functions.MathFunctions.Dynamic;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIRFunctions.MatrixAlloc.TensorFunctions;
import org.specs.CIRFunctions.MatrixAlloc.TensorProvider;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.Utilities.MatisseChecker;

/**
 * Builders for ArrayCreator functions which use allocated matrices.
 * 
 * @author Joao Bispo
 * 
 */
public class GeneralAllocBuilders {

    /**
     * Checks if dynamic memory allocation is allowed, if there is only one input, and if the input is a Matrix.
     * 
     * 
     * @param builderData
     * @return the MatrixType of the input if it passes the tests, or null otherwise
     */
    private static MatrixType checkOneAllocMatrix(ProviderData builderData) {

	MatisseChecker checker = new MatisseChecker(builderData);

	checker.dynamicAllocationEnabled();
	checker.numOfInputs(1);
	checker.areMatrices();

	if (!checker.check()) {
	    return null;
	}

	return MatrixUtils.cast(builderData.getInputTypes().get(0));
	/*
	// Check if allocated arrays are permitted
	if (!builderData.getSetup().useDynamicAllocation()) {
	    return false;
	}
	
	// Get input types
	List<VariableType> inputTypes = builderData.getInputTypes();
	
	// Check if has one input
	if (inputTypes.size() != 1) {
	    return false;
	}
	
	// Check if type is Allocated Matrix
	// if (inputTypes.get(0).getType() != CType.MatrixAlloc) {
	if (!MatrixUtilsV2.isDynamicMatrix(inputTypes.get(0))) {
	    return false;
	}
	
	return true;
	*/
    }

    /**
     * Creates a builder for returning the number of dimensions of a matrix.
     * 
     * <p>
     * Inputs:<br>
     * - AllocatedMatrix;
     * 
     * @return
     */
    public static MatlabInstanceProvider newNdimsBuilder() {
	return new MatlabInstanceProvider() {

	    /* (non-Javadoc)
	     * @see org.specs.MatlabToCLib.MatlabFunction.AFunctionBuilder#checkRule(org.specs.CIR.FunctionInstance.ProviderData)
	     */
	    @Override
	    public boolean checkRule(ProviderData data) {
		MatrixType matrixType = checkOneAllocMatrix(data);
		if (matrixType == null) {
		    return false;
		}

		return true;
	    }

	    @Override
	    public FunctionInstance create(ProviderData builderData) {
		// First argument is the matrix type
		MatrixType matrixType = builderData.getInputType(MatrixType.class, 0);

		return new TensorFunctions(builderData).newNdims(matrixType);
	    }

	};
    }

    /**
     * @return
     */
    public static MatlabInstanceProvider newLengthBuilder() {
	return new MatlabInstanceProvider() {

	    /* (non-Javadoc)
	     * @see org.specs.MatlabToCLib.MatlabFunction.AFunctionBuilder#checkRule(org.specs.CIR.FunctionInstance.ProviderData)
	     */
	    @Override
	    public boolean checkRule(ProviderData data) {
		MatrixType matrixType = checkOneAllocMatrix(data);
		if (matrixType == null) {
		    return false;
		}

		return true;
	    }

	    @Override
	    public FunctionInstance create(ProviderData builderData) {
		/*
				MatrixType matrixType = checkOneAllocMatrix(builderData);
				if (matrixType == null) {
				    return null;
				}
		*/
		return TensorProvider.LENGTH.newCInstance(builderData);
	    }
	};
    }
}
