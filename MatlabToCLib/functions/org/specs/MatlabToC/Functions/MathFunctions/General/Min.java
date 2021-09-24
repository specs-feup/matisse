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
import org.specs.MatlabToC.Functions.MathFunctions.MathResource;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.Utilities.MatisseChecker;

/**
 * @author Joao Bispo
 * 
 */
public class Min {

    /**
     * For the cases: <br>
     * - Min(scalar) <br>
     * - Min(vector) <br>
     * - Min(matrix) <br>
     * 
     * 
     * @return
     */
    public static InstanceProvider newDynamicBuilder() {

	MatisseChecker checker = new MatisseChecker().
		// Check if it has two input (second input is number of arguments)
		numOfInputs(1).
		// If argument is a dynamic matrix
		isMatrix(0)
		.usesDynamicAllocation(0);

	InstanceProvider provider = MFileProvider.getProvider(MathResource.MIN_DYNAMIC);

	return new GenericInstanceProvider(checker, provider);
    }

    public static InstanceProvider newMin3Builder() {
	MatisseChecker checker = new MatisseChecker()
		.numOfInputs(3);

	return new GenericInstanceProvider(checker, MFileProvider.getProvider(MathResource.MIN3));
    }

}
