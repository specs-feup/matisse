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

package org.specs.MatlabToC.Functions.MathFunctions.Static.transpose;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.MatrixUtils.MatrixImplementation;
import org.specs.CIRFunctions.MatrixAlloc.TensorProvider;
import org.specs.CIRFunctions.MatrixDec.TransposeFunction;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;

public class NumericTransposeBuilder implements MatlabInstanceProvider {

    @Override
    public FunctionInstance create(ProviderData builderData) {

	if (!checkRule(builderData)) {

	    return null;
	}

	List<VariableType> originalTypes = builderData.getInputTypes();

	// First input is matrix
	VariableType matrixType = originalTypes.get(0);
	// Check matrix implemenetation
	MatrixImplementation mImpl = MatrixUtils.getImplementation(matrixType);

	if (mImpl == MatrixImplementation.ALLOCATED) {
	    return TensorProvider.TRANSPOSE.newCInstance(builderData);
	}

	// return NumericTransposeInstance.newInstance(originalTypes);
	return TransposeFunction.getProvider().newCInstance(builderData);
    }

    @Override
    public boolean checkRule(ProviderData builderData) {

	List<VariableType> originalTypes = builderData.getInputTypes();

	// Only one input is allowed
	if (originalTypes.size() != 1) {
	    return false;
	}

	// The input needs to be a numeric matrix, allocated or declared
	VariableType input = originalTypes.get(0);
	if (!MatrixUtils.isMatrix(input)) {
	    return false;
	}

	if (!ScalarUtils.hasScalarType(input)) {
	    return false;
	}

	return true;
    }

}
