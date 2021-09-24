/**
 * Copyright 2014 SPeCS.
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

package org.specs.MatlabToC.Functions.MathFunctions;

import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.MatlabToC.Functions.MathFunctions.General.BitshiftScalar;
import org.specs.MatlabToC.Utilities.MatisseChecker;

/**
 * 
 * 
 * @author Joao Bispo
 *
 */
public class MathScalarBuilders {

    /**
     * When the first input is a scalar and the second is a constant from a literal.
     * 
     * 
     * @return
     */
    public static InstanceProvider bitshiftScalarLiteral() {
	MatisseChecker checker = new MatisseChecker().numOfInputs(2).areScalar().isConstant(1);

	return new GenericInstanceProvider(checker, data -> new BitshiftScalar(data).create());
    }
}
