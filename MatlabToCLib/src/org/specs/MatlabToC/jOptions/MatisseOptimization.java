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

package org.specs.MatlabToC.jOptions;

import org.specs.MatlabToC.Functions.MatisseInternalFunctions.MatisseIsOptimizationEnabled;

/**
 * Note: {@link MatisseIsOptimizationEnabled} relies on {@link MatisseOptimization#valueOf}, so be cautious when
 * renaming fields.
 * 
 * @author Joao Bispo
 */
public enum MatisseOptimization {

    /**
     * Creates a 'for' when finds assignments of the type: 'A = B + C .* D'
     */
    InlineElementWiseMatrixOps,
    /**
     * Enables the use of MATISSE primitives, such as 'matisse_new_array'
     */
    UseMatissePrimitives,
    /**
     * Always uses pointer views where otherwise copy views would be used.
     */
    UsePointerViewsAlways,
    /**
     * Creates C code that calls BLAS functions.
     */
    UseBlas;

    public String getName() {
	return name();
    }
}
