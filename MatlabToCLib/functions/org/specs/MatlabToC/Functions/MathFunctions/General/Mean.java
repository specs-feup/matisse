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

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.MatlabToC.Functions.MathFunctions.MathResource;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.Utilities.MatisseChecker;

/**
 * @author Joao Bispo
 *
 */
public class Mean {

    /**
     * For the cases: <br>
     * - Mean(vector)
     * 
     * 
     * @return
     */
    public static InstanceProvider newStaticBuilder() {
	MatisseChecker checker = new MatisseChecker().
		// Check if it has two inputs
		numOfInputs(2).
		// Check if first argument is a matrix
		isMatrix(0).
		// Check if matrix at index 0 does not use dynamic allocation
		not().usesDynamicAllocation(0).
		// If two arguments, check if second argument is a scalar
		isScalar(1);

	InstanceProvider provider = data -> MFileProvider.getInstance(MathResource.MEAN_STATIC, data);

	return new GenericInstanceProvider(checker, provider);
	/*
		return new AFunctionBuilder() {

		    @Override
		    public FunctionInstance create(ProviderData builderData) {

			List<VariableType> inputTypes = builderData.getInputTypes();

			// Init boolean var
			boolean success = true;

			// Check if it has two inputs
			// success = CheckUtils.numOfInputsRange(inputTypes, 1, 2, success);
			success = CheckUtils.numOfInputs(inputTypes, 2, success);
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

			ResourceProvider prov = MathResource.MEAN_STATIC;

			return MFileProvider.getInstance(prov, builderData);
		    }

		};
	 */
    }

    /**
     * For the cases: <br>
     * - Mean(vector) - Find(vector, scalar)
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
		// Check i matrix is dynamic
		usesDynamicAllocation(0).
		// If two arguments, check if second argument is a scalar
		isScalar(1);

	InstanceProvider provider = new InstanceProvider() {

	    private InstanceProvider getUnderlyingProvider(ProviderData data) {

		// If input matrix has one dimension and no second argument, use mean for vectors/columns
		MatrixType matrix = data.getInputType(MatrixType.class, 0);
		if (matrix.matrix().getShape().getNumDims() == 1 && data.getInputTypes().size() == 1) {
		    return MFileProvider.getProvider(MathResource.MEAN_DYNAMIC_ONEDIM);
		}

		return MFileProvider.getProvider(MathResource.MEAN_DYNAMIC);
	    }

	    @Override
	    public FunctionInstance newCInstance(ProviderData data) {
		return getUnderlyingProvider(data).newCInstance(data);
	    }

	    @Override
	    public FunctionType getType(ProviderData data) {
		return getUnderlyingProvider(data).getType(data);
	    }
	};

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

		return MFileProvider.getInstance(MathResource.MEAN_DYNAMIC, builderData);
	    }

	};
	*/
    }
}
