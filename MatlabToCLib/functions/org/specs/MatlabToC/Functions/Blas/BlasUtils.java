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

package org.specs.MatlabToC.Functions.Blas;

import org.specs.CIR.Language.SystemInclude;

public class BlasUtils {

    private static final String BLAS_LIB = "lib/blas";

    public static String getBlasFilename(String name) {
	return BLAS_LIB;
    }

    public static String getInclude() {
	return SystemInclude.Blas.getIncludeName();
    }

}
