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

package org.specs.MatlabToC.Functions.BaseFunctions.General;

import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.MatlabToC.Functions.BaseFunctions.BaseResource;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.Utilities.MatisseChecker;

/**
 * @author Joao Bispo
 * 
 */
public class Find {

    /**
     * find(vector)
     * 
     * @return
     */
    public static InstanceProvider newDynamicBuilder1Arg() {
	MatisseChecker checker = new MatisseChecker()
		.numOfInputs(1)
		.isMatrix(0);

	InstanceProvider provider = MFileProvider.getProvider(BaseResource.FIND_DYNAMIC_1);

	return new GenericInstanceProvider(checker, provider);
    }

    /**
     * find(vector, scalar)
     * 
     * @return
     */
    public static InstanceProvider newDynamicBuilder2Args() {
	MatisseChecker checker = new MatisseChecker()
		.numOfInputs(2)
		.isMatrix(0)
		.isScalar(1);

	InstanceProvider provider = MFileProvider.getProvider(BaseResource.FIND_DYNAMIC_2);

	return new GenericInstanceProvider(checker, provider);
    }

    /**
     * @return
     */
    public static InstanceProvider newScalarDynamicBuilder() {
	MatisseChecker checker = new MatisseChecker()
		.numOfInputs(1)
		.isScalar(0);

	return new GenericInstanceProvider(checker, data -> new FindScalar(data).create());
    }
}
