/**
 * Copyright 2015 SPeCS.
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

public class Dot {
    public static InstanceProvider new1DImplementation() {
	MatisseChecker checker = new MatisseChecker()
		.numOfInputs(2)
		.is1DMatrix(0)
		.is1DMatrix(1);

	InstanceProvider provider = MFileProvider.getProvider(MathResource.DOT1D);

	return new GenericInstanceProvider(checker, provider);
    }
}
