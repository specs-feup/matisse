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

package org.specs.CIR.TypesOld;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Conversion.ConversionUtils;
import org.specs.CIRTypes.Types.String.StringTypeUtils;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Utility methods for type verification.
 * 
 * @author Joao Bispo
 * 
 */
public class TypeVerification {

    /**
     * Returns true if all types are declared matrixes.
     * 
     * @param inputTypes
     * @return
     */
    public static boolean areDeclaredMatrixes(List<VariableType> inputTypes) {

	// Check if all the inputs are declared matrices
	for (VariableType variableType : inputTypes) {
	    if (!MatrixUtils.isStaticMatrix(variableType)) {
		return false;
	    }
	}

	return true;
    }

    /**
     * Returns true if all types have the same shape.
     * 
     * <p>
     * If any of the types does not support shape-checking at compile time, throws an exception.
     * 
     * @param inputTypes
     * @return
     */
    /*
    public static boolean haveSameShape(List<VariableType> inputTypes) {

    if (inputTypes.isEmpty()) {
        return true;
    }

    List<Integer> shape = TypeUtils.getShape(inputTypes.get(0));

    // Check if all the matrices have the same shape
    for (int i = 1; i < inputTypes.size(); i++) {
        List<Integer> otherShape = TypeUtils.getShape(inputTypes.get(i));

        if (!shape.equals(otherShape)) {
    	return false;
        }
    }

    return true;
    }
    */

    /**
     * Returns true if all arguments types are function-assignment compatible with the corresponding function type.
     * 
     * <p>
     * - If any of the arguments is not compatible with the function argument, returns false; <br>
     * - If the number of function types is not the same as the number of argument types, returns false;<br>
     * 
     * @param argumentsTypes
     * @param inputCTypes
     * @return
     */
    public static boolean areTypesFunctionAssignable(List<VariableType> functionTypes, List<VariableType> argumentsTypes) {

	if (functionTypes.size() != argumentsTypes.size()) {
	    return false;
	}

	// Compare the function inpuy type with the argument input type
	for (int i = 0; i < functionTypes.size(); i++) {
	    VariableType functionType = functionTypes.get(i);
	    VariableType inputType = argumentsTypes.get(i);

	    // if (!ConversionUtilsG.isAssignable(functionType, inputType)) {
	    if (!ConversionUtils.isAssignable(inputType, functionType)) {
		return false;
	    }
	}

	return true;
    }

    /**
     * Checks if the given input is a numeric integer constant.
     * 
     * @param input
     *            - the {@link VariableType} representing the input
     * @return true when the input is a constant integer, false otherwise
     */
    public static boolean isIntegerConstant(VariableType input) {

	if (!ScalarUtils.hasConstant(input)) {
	    return false;
	}

	if (!ScalarUtils.isInteger(input)) {
	    return false;
	}

	return true;
    }

    /**
     * Checks if the given inputs are numeric constant.
     * 
     * @param inputTypes
     * @return
     */
    public static boolean haveConstants(List<VariableType> inputTypes) {
	for (VariableType type : inputTypes) {
	    if (!ScalarUtils.hasConstant(type)) {
		return false;
	    }
	}

	return true;
    }

    public static <T> boolean contains(T value, List<T> values) {

	// int value = VariableTypeContent.getNumeric(tConstant).getIntValue();

	if (values.contains(value)) {
	    return true;
	}

	return false;
    }

    /**
     * Checks if the value of the given integer constant is on the list of valid values, that is, if it is valid.
     * 
     * @param integerConstant
     *            - the {@link VariableType} representing the input
     * @param values
     *            - the valid values
     * @return true if the integer value is valid, false otherwise
     */
    public static boolean isIntegerConstantValid(VariableType integerConstant, List<Integer> values) {

	// int value = VariableTypeContent.getNumeric(integerConstant).getIntValue();
	int value = ScalarUtils.getConstant(integerConstant).intValue();

	if (values.contains(value)) {
	    return true;
	}

	return false;
    }

    /**
     * Checks if the value of the given integer constant is on the list of valid values, that is, if it is valid.
     * 
     * @param integerConstant
     *            - the {@link VariableType} representing the input
     * @param values
     *            - the valid values
     * @return true if the integer value is valid, false otherwise
     */
    public static boolean isIntegerConstantValid(VariableType integerConstant, Integer... values) {

	return isIntegerConstantValid(integerConstant, Arrays.asList(values));
    }

    /**
     * Checks if the string input is valid, that is, if it is in the list of valid values.
     * 
     * @param string
     *            - the input string
     * @param values
     *            - the valid values
     * @return true if the string is valid, false otherwise
     */
    public static boolean isStringValid(VariableType string, String... values) {

	// List<String> newValues = Arrays.asList(values);
	Set<String> newValues = SpecsFactory.newHashSet(Arrays.asList(values));

	return isStringValid(string, newValues);
    }

    /**
     * Checks if the string input is valid, that is, if it is in the list of valid values.
     * 
     * @param string
     *            - the input string
     * @param values
     *            - the valid values
     * @return true if the string is valid, false otherwise
     */
    public static boolean isStringValid(VariableType string, Collection<String> values) {

	String value = StringTypeUtils.getString(string);

	if (values.contains(value)) {
	    return true;
	}

	return false;
    }

    /**
     * Checks if the size of the input arguments is valid, that is, if it is in the list of valid values.
     * 
     * @param inputTypes
     *            - the input arguments
     * @param values
     *            - the possible values for the size
     * @return true if the size is valid, false otherwise
     */
    public static boolean isSizeValid(List<VariableType> inputTypes, Integer... values) {

	List<Integer> newValues = Arrays.asList(values);

	// return isSizeValid(inputTypes, newValues);
	return isSizeValid(inputTypes, new HashSet<>(newValues));
    }

    /**
     * Checks if the size of the input arguments is valid, that is, if it is in the list of valid values.
     * 
     * @param inputTypes
     *            - the input arguments
     * @param values
     *            - the possible values for the size
     * @return true if the size is valid, false otherwise
     */
    public static boolean isSizeValid(List<VariableType> inputTypes, Set<Integer> values) {

	int size = inputTypes.size();

	if (values.contains(size)) {
	    return true;
	}

	return false;
    }

    /**
     * Returns true if all the given types are scalars.
     * 
     * @param types
     * @return
     */
    public static boolean areScalar(List<VariableType> types) {
	for (VariableType type : types) {
	    if (!ScalarUtils.isScalar(type)) {
		return false;
	    }
	}

	return true;
    }

    /**
     * Returns true if all the given types are of the specified type.
     * 
     * @param types
     * @return
     */
    public static <T extends VariableType> boolean are(Class<T> checkType, List<VariableType> types) {
	for (VariableType type : types) {
	    if (!checkType.isInstance(type)) {
		return false;
	    }
	}

	return true;
    }

    /**
     * Checks if the input variable type is within the range specified by the parameter <code>start</code> and
     * <code>end</code>.
     * 
     * @param constant
     *            - the variable type we will test
     * @param lowerBound
     *            - the lower bound ( can be null to represent no lower bound )
     * @param upperBound
     *            - the upper bound ( can be null to represent no upper bound )
     * @return - true if the input is within range, false otherwise
     */
    public static boolean isIntegerConstantInRange(VariableType constant, Integer lowerBound, Integer upperBound) {

	// No bounds, return true
	if (lowerBound == null && upperBound == null) {
	    return true;
	}

	// int value = VariableTypeContent.getNumeric(constant).getIntValue();
	int value = ScalarUtils.getConstant(constant).intValue();

	// No lower bound
	if (lowerBound == null) {
	    return value < upperBound;
	}

	// No upper bound
	if (upperBound == null) {
	    return value > lowerBound;
	}

	// The general case
	return value > lowerBound && value < upperBound;
    }

    /**
     * @param argumentTypes
     * @return
     */
    public static boolean areMatrices(List<VariableType> inputTypes) {

	// Check if all the inputs are matrices
	for (VariableType variableType : inputTypes) {
	    if (!MatrixUtils.isMatrix(variableType)) {
		return false;
	    }
	}

	return true;
    }

    /**
     * @param argumentTypes
     * @return
     */
    public static boolean areAllocatedMatrices(List<VariableType> inputTypes) {

	// Check if all the inputs are matrices
	for (VariableType variableType : inputTypes) {
	    if (!MatrixUtils.usesDynamicAllocation(variableType)) {
		return false;
	    }
	}

	return true;
    }

}
