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

package org.specs.CIR.Types.ATypes.Scalar;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Types.ATypes.Scalar.Functions.IsInf;
import org.specs.CIR.Types.ATypes.Scalar.Functions.IsNan;

/**
 * Instance providers for Scalar primitives.
 * 
 * @author Joao Bispo
 * 
 */
public interface ScalarFunctions {

    /**
     * Instance that implements the given op.
     * 
     * <p>
     * Inputs:<br>
     * - A number of scalar-compatible inputs with the given op;
     * 
     * @return
     */
    default InstanceProvider cOperator(COperator op) {
	throw new UnsupportedOperationException("Not yet implemented for class '" + getClass() + "'");
    }

    /**
     * Instance that tests if a scalar is Not-A-Number. Returns 1 if scalar is a NaN, 0 otherwise
     * 
     * <p>
     * Inputs:<br>
     * - A single Scalar, that will be tested;
     * 
     * <p>
     * As default, uses provider from class IsNan.
     * 
     * @return
     */
    default InstanceProvider isNan() {
	return IsNan.getProvider();
    }

    /**
     * Instance that tests if a scalar is infinite. Returns 0 if the scalar is not infinite, -1 if is negative infinite
     * and 1 if positive infinite.
     * 
     * <p>
     * Inputs:<br>
     * - A single Scalar, that will be tested;
     * 
     * <p>
     * As default, uses provider from class IsInf.
     * 
     * @return
     */
    default InstanceProvider isInf() {
	return IsInf.getProvider();
    }

}
