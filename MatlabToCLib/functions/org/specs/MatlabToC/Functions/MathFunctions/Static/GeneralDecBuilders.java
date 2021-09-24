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

package org.specs.MatlabToC.Functions.MathFunctions.Static;

import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRFunctions.MatrixDec.DeclaredFunctions;
import org.specs.MatlabToC.Functions.Misc.MatlabCheckers;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProviderHelper;

/**
 * Builders for general MATLAB functions which use declared matrices.
 * 
 * @author Joao Bispo
 * 
 */
public class GeneralDecBuilders {

    /**
     * Creates a builder for returning the length of a declared matrix.
     * 
     * <p>
     * Inputs:<br>
     * - DeclaredMatrix;
     * 
     * @return
     */
    public static MatlabInstanceProvider newLengthBuilder() {
	InstanceProvider provider = data -> {
	    List<VariableType> inputTypes = data.getInputTypes();
	    VariableType matrixType = inputTypes.get(0);

	    return new DeclaredFunctions(data).newLengthDec(matrixType);
	};

	return new MatlabInstanceProviderHelper(MatlabCheckers.oneStaticMatrix(), provider);

    }
}
