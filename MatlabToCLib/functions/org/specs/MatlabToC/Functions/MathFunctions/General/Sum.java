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

package org.specs.MatlabToC.Functions.MathFunctions.General;

import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Utilities.InputChecker.Check;
import org.specs.MatlabToC.Functions.MathFunctions.MathResource;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.Utilities.MatisseChecker;

/**
 * @author Joao Bispo
 * 
 */
public class Sum {

    /**
     * For the cases: <br>
     * - Sum(matrix)
     * 
     * 
     * @return
     */
    public static InstanceProvider newDynamicBuilder() {
	MatisseChecker checker = new MatisseChecker().
		// Check if it has between one or two inputs
		numOfInputsRange(1, 2).
		// Check if first argument is a matrix
		isMatrix(0).
		// Check if matrix is dynamic
		usesDynamicAllocation(0).
		// If two arguments, check if second argument is a scalar
		isScalar(1);

	InstanceProvider provider = MFileProvider.getProviderWithDependencies(MathResource.SUM_DYNAMIC,
		MathResource.SUM_MATRIX);

	return new GenericInstanceProvider(checker, provider);
	/*
		return new AFunctionBuilder() {
	
		    @Override
		    public FunctionInstance create(ProviderData builderData) {
	
			List<VariableType> inputTypes = builderData.getInputTypes();
	
			// Init boolean var
			boolean success = true;
	
			// Check if it has between one or two inputs
			success = CheckUtils.numOfInputsRange(inputTypes, 1, 2, success);
			// Check if first argument is a matrix
			success = CheckUtils.isMatrix(inputTypes, 0, success);
			// Check i matrix is static
			success = CheckUtils.hasDynamicMatrix(inputTypes, success);
			// If two arguments, check if second argument is a scalar
			if (inputTypes.size() > 1) {
			    success = CheckUtils.isScalar(inputTypes, 1, success);
			}
	
			if (!success) {
			    return null;
			}
	
			return MFileProvider.getInstance(MathResource.SUM_DYNAMIC, builderData);
		    }
	
		};
		*/
    }

    /**
     * @return
     */
    public static InstanceProvider newStaticBuilder() {
	MatisseChecker checker = new MatisseChecker().
		// Check if it has between one or two inputs
		numOfInputsRange(1, 2).
		// Check if first argument is a matrix
		isMatrix(0).
		// Check if matrix is static
		not().usesDynamicAllocation(0).
		// If two arguments, check if second argument is a scalar
		isScalar(1);

	InstanceProvider provider = MFileProvider.getProvider(MathResource.SUM_MATRIX);

	return new GenericInstanceProvider(checker, provider);
	/*
		return new AFunctionBuilder() {
	
		    @Override
		    public FunctionInstance create(ProviderData builderData) {
	
			List<VariableType> inputTypes = builderData.getInputTypes();
	
			// Init boolean var
			boolean success = true;
	
			// Check if it has between one or two inputs
			success = CheckUtils.numOfInputsRange(inputTypes, 1, 2, success);
			// Check if first argument is a matrix
			success = CheckUtils.isMatrix(inputTypes, 0, success);
			// Check i matrix is static
			success = CheckUtils.hasStaticMatrix(inputTypes, success);
			// If two arguments, check if second argument is a scalar
			if (inputTypes.size() > 1) {
			    success = CheckUtils.isScalar(inputTypes, 1, success);
			}
	
			if (!success) {
			    return null;
			}
	
			return MFileProvider.getInstance(MathResource.SUM_MATRIX, builderData);
		    }
	
		};
	*/
    }

    public static InstanceProvider newOneDimBuilder() {
	// Check if matrix has only one dimension
	Check oneDimCheck = (data) -> {
	    return data.getInputType(MatrixType.class, 0).matrix().getShape().isKnown1D();
	};
	// Check check = (data) -> ((MatrixType)data.getInputTypes().get(0)).matrix().getShape().getNumDims() == 1;

	MatisseChecker checker = new MatisseChecker()
		// Check if it has one input
		.numOfInputs(1)
		// Check if first argument is a matrix
		.isMatrix(0)
		// Check if matrix has one dimension
		.addCheck(oneDimCheck);

	InstanceProvider provider = MFileProvider.getProvider(MathResource.SUM_ONE_DIM);

	return new GenericInstanceProvider(checker, provider);
    }
}
