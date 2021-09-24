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

import static org.specs.MatlabToC.Functions.MathFunctions.Static.GeneralDecTemplate.*;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Code.CodeUtils;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.CIRTypes.Types.StdInd.StdIntFactory;
import org.specs.CIRTypes.Types.String.StringTypeUtils;
import org.specs.MatlabIR.MatlabLanguage.NumericClassName;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.MatlabToCTypesUtils;

import pt.up.fe.specs.util.SpecsIo;

/**
 * @author Joao Bispo
 * 
 */
public class BitShiftDecFunctions extends AInstanceBuilder {

    private final static String C_FILENAME = MatlabCFilename.MatlabGeneral.getCFilename();

    private final List<VariableType> originalTypes;
    private final boolean scalarVersion;

    private BitShiftDecFunctions(ProviderData data, List<VariableType> originalTypes, boolean scalarVersion) {
	super(data);
	this.originalTypes = originalTypes;
	this.scalarVersion = scalarVersion;
    }

    // public BitShiftDecFunctions(ProviderData data) {
    // super(data);
    // }

    @Override
    public FunctionInstance create() {
	if (scalarVersion) {
	    return newBitshiftDoubleDecScalar();
	}

	return newBitshiftDoubleDecMatrix();
    }

    public static FunctionInstance newBitshiftDoubleDecMatrix(ProviderData data, List<VariableType> originalTypes) {
	return new BitShiftDecFunctions(data, originalTypes, false).create();
    }

    public static FunctionInstance newBitshiftDoubleDecScalar(ProviderData data, List<VariableType> originalTypes) {
	return new BitShiftDecFunctions(data, originalTypes, true).create();
    }

    private FunctionInstance newBitshiftDoubleDecMatrix() {

	// Get the string that represents the C type of the third input
	VariableType type = StdIntFactory.newUInt64();

	if (originalTypes.size() == 3) {

	    String originalTypeString = StringTypeUtils.getString(originalTypes.get(2));
	    type = MatlabToCTypesUtils.getVariableType(NumericClassName.getNumericClassName(originalTypeString),
		    getNumerics());
	}

	String typeString = CodeUtils.getSimpleType(type);

	// The output matrix shape and its length
	List<Integer> shape = MatrixUtils.getShapeDims(originalTypes.get(0));
	Integer matrixLength = 1;
	for (Integer integer : shape) {
	    matrixLength *= integer;
	}

	// Build instances
	FunctionInstance isInteger = newIsInteger();
	FunctionInstance inRange = newInRange(type);

	// Get the template
	String cBody = SpecsIo.getResource(GeneralDecResource.BITSHIFT_DOUBLE_DEC_MATRIX.getResource());

	// Replace the needed tags
	cBody = GeneralDecTemplate.parseTemplate(cBody, TYPE.getTag(), typeString, IS_INTEGER_CALL.getTag(),
		isInteger.getCName(), IN_RANGE_CALL.getTag(), inRange.getCName(), MATRIX_LENGTH.getTag(),
		matrixLength.toString());

	// Build the function types
	VariableType outputType = StaticMatrixType.newInstance(getNumerics().newDouble(), shape);
	String outputName = OUTPUT_NAME;

	List<VariableType> inputTypes = originalTypes.subList(0, 2);
	List<String> inputNames = Arrays.asList(FIRST_INPUT_NAME, SECOND_INPUT_NAME);

	FunctionType functionTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputName,
		outputType);

	// Build the function name
	String baseFunctionName = "bitshift_double_dec";
	String typesSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());
	String cFunctionName = baseFunctionName + typesSuffix;

	LiteralInstance instance = new LiteralInstance(functionTypes, cFunctionName, C_FILENAME, cBody);

	// Set the includes needed for this instance
	instance.setCustomImplementationIncludes(Arrays.asList(SystemInclude.Stdint.getIncludeName(),
		SystemInclude.Stdlib.getIncludeName(), SystemInclude.Stdio.getIncludeName()));

	instance.getCustomImplementationInstances().add(isInteger, inRange);

	return instance;
    }

    private FunctionInstance newBitshiftDoubleDecScalar() {

	// Get the string that represents the C type of the third input
	VariableType type = StdIntFactory.newUInt64();
	if (originalTypes.size() == 3) {

	    String originalTypeString = StringTypeUtils.getString(originalTypes.get(2));
	    type = MatlabToCTypesUtils.getVariableType(NumericClassName.getNumericClassName(originalTypeString),
		    getNumerics());
	}

	String typeString = CodeUtils.getSimpleType(type);

	// Build instances
	FunctionInstance isInteger = newIsInteger();
	FunctionInstance inRange = newInRange(type);

	// Get the template
	String cBody = SpecsIo.getResource(GeneralDecResource.BITSHIFT_DOUBLE_DEC_SCALAR.getResource());

	// Replace the needed tags
	cBody = GeneralDecTemplate.parseTemplate(cBody, TYPE.getTag(), typeString, IS_INTEGER_CALL.getTag(),
		isInteger.getCName(), IN_RANGE_CALL.getTag(), inRange.getCName());

	// Build the function types
	VariableType outputType = getNumerics().newDouble();
	String outputName = OUTPUT_NAME;

	List<VariableType> inputTypes = originalTypes.subList(0, 2);
	List<String> inputNames = Arrays.asList(FIRST_INPUT_NAME, SECOND_INPUT_NAME);

	FunctionType functionTypes = FunctionType.newInstance(inputNames, inputTypes, outputName, outputType);

	// Build the function name
	String baseFunctionName = "bitshift_double_dec";
	String typesSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());
	String cFunctionName = baseFunctionName + typesSuffix;

	LiteralInstance instance = new LiteralInstance(functionTypes, cFunctionName, C_FILENAME, cBody);

	// Set the includes needed for this instance
	instance.setCustomImplementationIncludes(Arrays.asList(SystemInclude.Stdint.getIncludeName(),
		SystemInclude.Stdlib.getIncludeName(), SystemInclude.Stdio.getIncludeName()));

	instance.getCustomImplementationInstances().add(isInteger, inRange);

	return instance;
    }

    private LiteralInstance newInRange(VariableType numericType) {

	// Get the template
	String cBody = SpecsIo.getResource(GeneralDecResource.BITSHIFT_DOUBLE_DEC_IN_RANGE.getResource());

	// Replace the needed tags
	MacroStrings macro = getMacroStrings(numericType);

	cBody = GeneralDecTemplate.parseTemplate(cBody, MIN_MACRO.getTag(), macro.minMacro, MAX_MACRO.getTag(),
		macro.maxMacro);

	// Build the function name
	String baseFunctionName = "in_range_";

	String typeSuffix = numericType.getSmallId();
	String cFunctionName = baseFunctionName + typeSuffix;

	// Build the function types
	VariableType outputType = StdIntFactory.newUInt8();
	String outputName = OUTPUT_NAME;

	List<VariableType> inputType = Arrays.asList(getNumerics().newDouble());
	List<String> inputName = Arrays.asList(FIRST_INPUT_NAME);

	FunctionType functionTypes = FunctionType.newInstance(inputName, inputType, outputName, outputType);

	// Build the function instance, set the includes and return
	LiteralInstance instance = new LiteralInstance(functionTypes, cFunctionName, C_FILENAME, cBody);

	instance.setCustomImplementationIncludes(SystemInclude.Stdint.getIncludeName());

	instance.setComments("Checks if the input is within range of the type passed as third argument ");

	return instance;
    }

    /**
     * Creates the correct macro strings to be used in the function in_range().
     * 
     * @param minMacro
     * @param maxMacro
     * @return
     */
    // private static MacroStrings getMacroStrings(NumericType numericType) {
    private static MacroStrings getMacroStrings(VariableType numericType) {

	String minMacro, maxMacro;

	/*
	if (numericType == NumericType.Cint) {
	    numericType = NumericType.Int32;
	}
	*/

	minMacro = numericType.toString().toUpperCase().concat("_MIN");
	maxMacro = numericType.toString().toUpperCase().concat("_MAX");

	// if (numericType.isUnsigned()) {
	if (ScalarUtils.isUnsigned(numericType)) {
	    minMacro = "0";
	}

	return new MacroStrings(minMacro, maxMacro);
    }

    private LiteralInstance newIsInteger() {

	// Get the template
	String cBody = SpecsIo.getResource(GeneralDecResource.BITSHIFT_DOUBLE_DEC_IS_INTEGER.getResource());

	// Build the function name
	String cFunctionName = "is_integer";

	// Build the function types
	VariableType outputType = StdIntFactory.newUInt8();
	String outputName = OUTPUT_NAME;

	List<VariableType> inputType = Arrays.asList(getNumerics().newDouble());
	List<String> inputName = Arrays.asList(FIRST_INPUT_NAME);

	FunctionType functionTypes = FunctionType.newInstance(inputName, inputType, outputName, outputType);

	// Build the function instance, set the includes and return
	LiteralInstance instance = new LiteralInstance(functionTypes, cFunctionName, C_FILENAME, cBody);

	instance.setCustomImplementationIncludes(SystemInclude.Math.getIncludeName());

	instance.setComments("Checks if this double value is integer ( has no fractional part )");

	return instance;
    }

    public static class MacroStrings {
	public final String minMacro;
	public final String maxMacro;

	public MacroStrings(String minMacro, String maxMacro) {
	    this.minMacro = minMacro;
	    this.maxMacro = maxMacro;
	}
    }
}
