/**
 * Copyright 2016 SPeCS.
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

package org.specs.CIR.Types;

import org.specs.CIR.Exceptions.CirUnsupportedException;
import org.specs.CIR.FunctionInstance.InstanceProvider;

public interface CommonFunctions {
    /**
     * Creates a new instance of the function 'free', which frees the heap memory of a matrix, in case it uses
     * dynamically allocated memory.
     * 
     * <p>
     * Inputs:<br>
     * - A matrix, which will be freed;<br>
     * 
     * @return
     */
    default InstanceProvider free() {
        throw new CirUnsupportedException(getClass());
    }

    /**
     * Copy an instance of the variable.
     * 
     * @return
     */
    default InstanceProvider assign() {
        throw new CirUnsupportedException(getClass());
    }
}
