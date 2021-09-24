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

package org.specs.CIR.Types.ATypes.Scalar.Utils;

import org.specs.CIR.Types.ATypes.Scalar.ScalarType;

/**
 * For VariableTypes which can be converted to a ScalarType (e.g., Matrices have a Scalar element).
 * 
 * @author Joao Bispo
 *
 */
public interface ToScalar {

    /**
     * Try to extract a scalar type from current type. If it is not possible, throws an Exception.
     * 
     * <p>
     * Should return a base type (e.g., remove pointer information) and maintain information about constants.
     * 
     * @return
     */
    ScalarType toScalarType();

}
