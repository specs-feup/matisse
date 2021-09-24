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

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.MatlabToC.Functions.MathFunctions.MathResource;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.Utilities.MatisseChecker;

/**
 * Rounds number to the integer nearest to zero.
 * 
 * @author Joao Bispo
 * 
 */
public class Fix {

    /**
     * @return
     */
    public static InstanceProvider newScalarBuilder() {
	MatisseChecker checker = new MatisseChecker().
		// Check if it has one input
		numOfInputs(1).
		// Check if scalar
		isScalar(0);

	InstanceProvider provider = MFileProvider.getProvider(MathResource.FIX_GENERAL);

	return new GenericInstanceProvider(checker, provider);
	/*
		return new AFunctionBuilder() {

		    @Override
		    public FunctionInstance create(ProviderData builderData) {

			List<VariableType> inputTypes = builderData.getInputTypes();

			boolean success = true;
			// Check if it has one input
			success = CheckUtils.numOfInputs(inputTypes, 1, success);
			// Check if scalar
			success = CheckUtils.isScalar(inputTypes, 0, success);

			if (!success) {
			    return null;
			}

			// return GeneralFunctions.newCast(inputTypes.get(0), NumericClassName.INT32);
			return MFileProvider.getInstance(MathResource.FIX_GENERAL, builderData);
		    }

		};
		*/
    }
}
