/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.CIR.Utilities.InputChecker;

import java.util.Collection;
import java.util.List;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.TypeVerification;
import org.specs.CIRTypes.Types.String.StringTypeUtils;

/**
 * @author Joao Bispo
 * @deprecated Replaced by InputsChecker
 */
@Deprecated
public class CheckUtils {

    public static boolean numOfInputsRange(Collection<?> inputs, int start, int end, boolean success) {
	if (!success) {
	    return false;
	}

	if (inputs.size() < start) {
	    return false;
	}

	if (inputs.size() > end) {
	    return false;
	}

	return true;
    }

    /**
     * @param inputTypes
     * @param i
     * @param success
     * @return
     */
    public static boolean numOfInputs(Collection<?> inputs, int numberOfInputs, boolean success) {
	if (!success) {
	    return false;
	}

	if (inputs.size() != numberOfInputs) {
	    return false;
	}

	return true;
    }

    /**
     * @param inputTypes
     * @param i
     * @param success
     * @return
     */
    public static boolean numOfInputsAtLeast(Collection<?> inputs, int numberOfInputs, boolean success) {
	if (!success) {
	    return false;
	}

	if (inputs.size() < numberOfInputs) {
	    return false;
	}

	return true;
    }

    /**
     * @param inputTypes
     * @param success
     * @return
     */
    public static boolean areMatrices(List<VariableType> inputTypes, boolean success) {
	if (!success) {
	    return false;
	}

	return TypeVerification.areMatrices(inputTypes);
    }

    /**
     * @param inputTypes
     * @param success
     * @return
     */
    public static boolean isMatrix(List<VariableType> inputTypes, int index, boolean success) {
	if (!success) {
	    return false;
	}

	return MatrixUtils.isMatrix(inputTypes.get(index));
    }

    /**
     * @param inputTypes
     * @param success
     * @return
     */
    public static boolean areScalar(List<VariableType> inputTypes, boolean success) {
	if (!success) {
	    return false;
	}

	return TypeVerification.areScalar(inputTypes);
    }

    /**
     * @param inputTypes
     * @param success
     * @return
     */
    public static boolean isScalar(List<VariableType> inputTypes, int index, boolean success) {
	if (!success) {
	    return false;
	}

	return ScalarUtils.isScalar(inputTypes.get(index));
    }

    /**
     * Returns true if any of the inputs is a matrix, and if any of the matrices has a dynamic implementation.
     * 
     * @param inputTypes
     * @param success
     * @return
     */
    public static boolean hasDynamicMatrix(List<VariableType> inputTypes, boolean success) {
	if (!success) {
	    return false;
	}

	for (VariableType type : inputTypes) {
	    if (MatrixUtils.usesDynamicAllocation(type)) {
		return true;
	    }
	}

	return false;
    }

    /**
     * Returns true if any of the inputs is a matrix, and if all of the matrices have a dynamic implementation.
     * 
     * @param inputTypes
     * @param success
     * @return
     */
    public static boolean hasStaticMatrix(List<VariableType> inputTypes, boolean success) {
	if (!success) {
	    return false;
	}

	for (VariableType type : inputTypes) {
	    if (!MatrixUtils.isMatrix(type)) {
		continue;
	    }

	    if (!MatrixUtils.isStaticMatrix(type)) {
		return false;
	    }
	}

	return true;
    }

    /**
     * @param inputTypes
     * @param i
     * @param string
     * @param success
     * @return
     */
    public static boolean isStringType(List<VariableType> inputTypes, int i, boolean success) {
	if (!success) {
	    return false;
	}

	if (!StringTypeUtils.isString(inputTypes.get(i))) {
	    return false;
	}

	return true;
    }

}
