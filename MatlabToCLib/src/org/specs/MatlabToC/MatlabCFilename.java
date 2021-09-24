/**
 * Copyright 2012 SPeCS Research Group.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License. under the License.
 */

package org.specs.MatlabToC;

/**
 * Contains the CFilenames for the implementable built-in functions.
 * 
 * @author Joao Bispo
 * 
 */
public enum MatlabCFilename {

    DeclaredMatrix("lib/matrix_declared"),
    AllocatedMatrix("lib/matrix_alloc"),
    MatrixMath("lib/matrix_math"),
    ScalarMath("lib/scalar_math"),
    ArrayCreatorsDec("lib/array_creators_dec"),
    ArrayCreatorsAlloc("lib/array_creators_alloc"),
    MatlabGeneral("lib/matlab_general");
    

    private final String cFilename;

    /**
     * 
     */
    private MatlabCFilename(String filename) {
	this.cFilename = filename;
    }

    /**
     * @return the cFile
     */
    public String getCFilename() {
	return cFilename;
    }

}
