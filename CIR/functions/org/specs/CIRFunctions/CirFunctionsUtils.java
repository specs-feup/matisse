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

package org.specs.CIRFunctions;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;

/**
 * Utility methods related to the creation of functions using the CIR library.
 * 
 * @author Joao Bispo
 * 
 */
public class CirFunctionsUtils {

    public static VariableType getMatrixTypeByIndex(Enum<?> function, List<VariableType> inputTypes, int index) {

	return getMatrixTypeByIndex(function.name(), inputTypes, index);
    }

    /**
     * Checks if input types has size one, and if the first input is a declared matrix.
     * 
     * TODO: Do not use this function
     * 
     * @param function
     * 
     * @param inputTypes
     * @return
     */
    public static VariableType getMatrixTypeByIndex(String function, List<VariableType> inputTypes, int index) {

	if (inputTypes.size() <= index) {
	    String indexPlural = "";
	    if (index > 0) {
		indexPlural = "s";
	    }

	    String inputsPlural = "";
	    if (inputTypes.size() > 0) {
		inputsPlural = "s";
	    }

	    throw new RuntimeException("Function '" + function + "' receives at least " + (index + 1) + " argument"
		    + indexPlural + ", gave " + inputTypes.size() + " input" + inputsPlural);
	}

	// VariableType matrixType = inputTypes.get(index);
	VariableType matrixType = inputTypes.get(index);
	if (!MatrixUtils.isMatrix(matrixType)) {
	    throw new RuntimeException("Argument " + (index + 1) + " of function '" + function
		    + "' needs to be a matrix.");
	}

	return matrixType;
    }

    /**
     * Builds a string for variable names.
     * 
     * <p>
     * E.g., if prefix is 'index_' and numArgs is 2, returns the string "index_1, index_2".
     * 
     * @param prefix
     * @param numArgs
     * @return
     */
    public static String getNameString(String prefix, int numArgs) {
	StringBuilder builder = new StringBuilder();

	if (numArgs == 0) {
	    return "";
	}

	List<String> names = FunctionInstanceUtils.createNameList(prefix, numArgs);
	builder.append(names.get(0));
	for (int i = 1; i < numArgs; i++) {
	    builder.append(", ");
	    builder.append(names.get(i));
	}

	return builder.toString();
    }

}
