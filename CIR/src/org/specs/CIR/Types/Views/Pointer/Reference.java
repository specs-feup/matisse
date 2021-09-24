/**
 * Copyright 2013 SPeCS.
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

package org.specs.CIR.Types.Views.Pointer;

import org.specs.CIR.Types.VariableType;

/**
 * Pointer-related operations on a VariableType.
 * 
 * @author Joao Bispo
 * 
 */
public interface Reference {

    boolean isByReference();

    /**
     * @return true if the type supports conversion to reference, false otherwise
     */
    boolean supportsReference();

    /**
     * Returns the VariableType with the pointer status according to the given boolean. If modifications are needed,
     * returns a copy of the type.
     * 
     * @param isByReference
     * @return
     */
    VariableType getType(boolean isByReference);
}
