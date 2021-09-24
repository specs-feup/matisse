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
package org.specs.CIR.Types.ATypes.Scalar;

import org.specs.CIR.Types.VariableType;

/**
 * Checks if a scalar fits inside other scalar.
 * 
 * @author Joao Bispo
 * 
 */
public interface FittingRule {

    /**
     * 
     * 
     * @param sourceType
     * @param targetType
     * @return true if the sourceType fits inside the targetType
     */
    boolean fitsInto(VariableType sourceType, VariableType targetType);

}
