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

package org.specs.MatlabToC.Functions.MathFunctions.Static.mean;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.TypeVerification;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;

/**
 * Builder for the Maltab built-in function 'mean' for the cases where the input is a numeric matrix. </br> </br>
 * <b>Example call:</b>
 * 
 * <pre>
 * {@code
 * o = mean(m),
 * where m is a matrix
 * }
 * </pre>
 * 
 * @author Pedro Pinto
 * 
 */
public class MeanDecMatrixProvider implements MatlabInstanceProvider {

    @Override
    public FunctionInstance create(ProviderData builderData) {

	if (!checkRule(builderData)) {
	    return null;
	}

	if (builderData.getInputTypes().size() == 1) {
	    return new MeanDecFunctions(builderData).create();
	}

	return new MeanDecFunctions(builderData).create();
    }

    @Override
    public boolean checkRule(ProviderData builderData) {

	List<VariableType> originalTypes = builderData.getInputTypes();

	// Need to use declared arrays
	/*
	if (!builderData.getSetup().useStaticAllocation()) {
	    return false;
	}
	*/

	// Need to have 1 or 2 inputs
	if (!TypeVerification.isSizeValid(originalTypes, 1, 2)) {
	    return false;
	}

	// This input needs to be a numeric matrix
	VariableType matrix = originalTypes.get(0);

	if (!MatrixUtils.isStaticMatrix(matrix)) {
	    return false;
	}

	// if (MatrixUtils.getElementType(matrix).getType() != CType.Numeric) {
	if (!ScalarUtils.isScalar(MatrixUtils.getElementType(matrix))) {
	    return false;
	}

	// If there is a second one, it needs to be a constant integer
	if (originalTypes.size() == 2) {
	    VariableType dimType = originalTypes.get(1);

	    if (!TypeVerification.isIntegerConstant(dimType)) {
		return false;
	    }
	}

	return true;
    }

}
