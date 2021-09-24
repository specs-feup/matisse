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

package org.specs.CIR.Types.ATypes.Matrix;

import org.specs.CIR.Utilities.InputChecker.Check;

public class MatrixChecks {

    /**
     * True if the shape of the MatrixType at the given index is fully defined.
     * <p>
     * Assumes type is a MatrixType.
     * 
     * @param index
     * @return
     */
    public static Check isShapeFullyDefined(int index) {
	return data -> data.getInputType(MatrixType.class, index).getTypeShape().isFullyDefined();
    }

}
