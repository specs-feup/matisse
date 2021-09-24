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

package org.specs.MatlabToC.Functions.MatlabOps;

import static org.specs.MatlabToC.Functions.MatlabOps.MatlabOperatorsDecTemplate.CD_N;
import static org.specs.MatlabToC.Functions.MatlabOps.MatlabOperatorsDecTemplate.CD_SIGN;
import static org.specs.MatlabToC.Functions.MatlabOps.MatlabOperatorsDecTemplate.CD_TOL;
import static org.specs.MatlabToC.Functions.MatlabOps.MatlabOperatorsDecTemplate.MP_COPY_CALL;
import static org.specs.MatlabToC.Functions.MatlabOps.MatlabOperatorsDecTemplate.MP_EYE_CALL;
import static org.specs.MatlabToC.Functions.MatlabOps.MatlabOperatorsDecTemplate.MP_INPUT_NUM_TYPE;
import static org.specs.MatlabToC.Functions.MatlabOps.MatlabOperatorsDecTemplate.MP_LENGTH;
import static org.specs.MatlabToC.Functions.MatlabOps.MatlabOperatorsDecTemplate.MP_MAT_MULT_CALL;
import static org.specs.MatlabToC.Functions.MatlabOps.MatlabOperatorsDecTemplate.MP_SELF_CALL;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.CNumber;
import org.specs.CIR.TypesOld.VariableTypeFactory;
import org.specs.CIRFunctions.MatrixDec.DeclaredProvider;
import org.specs.CIRTypes.Language.CLiteral;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixTypeBuilder;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.MatlabToC.Functions.BaseFunctions.Static.EyeDecInstance;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * Contains implementations for some Matlab operators using templates.
 * 
 * @author Pedro Pinto
 * 
 */
public class MatlabOperatorsDec extends AInstanceBuilder {

    private final static String C_FILENAME = MatlabCFilename.MatlabGeneral.getCFilename();

    // private final ProviderData data;
    // private final NumericFactory numerics;

    public MatlabOperatorsDec(ProviderData data) {
	super(data);
	// this.data = data;
	// this.numerics = new NumericFactory(data.getSetupTable().getCBitSizes());
    }

    /*
        private NumericFactory getNumerics() {
    	return numerics;
        }
     */
    /**
     * Creates a new instance of the Matlab builtin operator 'Matrix power' ( ^ ) for the cases where the base is a
     * square matrix and the exponent is a positive integer. This instance uses an iterative version of the repeated
     * squaring algorithm.
     * 
     * </br> </br>
     * 
     * <b>Example:</b>
     * 
     * <pre>
     * {@code
     * Y = M ^ 3;
     * },
     * where <i>M</i> is a square matrix
     * </pre>
     * 
     * @param data
     *            TODO
     * 
     * @return a new instance of {@link LiteralInstance} that represents the implementation of the matrix power operator
     */
    public LiteralInstance newMatrixPowerPositiveIntegerIter() {

	SpecsLogs
	.msgWarn("This method is not working correctly, it uses unsafe matrix multiplication of the type 'A=A*A'");

	// Get the first input, the matrix
	MatrixType matrix = getTypeAtIndex(MatrixType.class, 0);

	// Get the numeric type of this matrix
	// NumericType numericType = VariableTypeUtilsOld.getNumericType(matrix);
	// String classString = MatlabToCTypes.getNumericType(numericType);
	String classString = MatlabToCTypesUtils.getNumericType(matrix);

	// Get the length of this matrix
	// List<Integer> shape = MatrixUtilsV2.getShapeDims(matrix);
	// Integer length = MatrixUtils.getMatrixLength(matrix);
	List<Integer> shape = matrix.getTypeShape().getDims();
	Integer length = matrix.getTypeShape().getNumElements();

	// Build the instance of the eye function call
	FunctionInstance eye = newEyeInstance(classString, shape);

	// Build the instance to the copy function call
	FunctionInstance copy = newCopyInstance(matrix, length);

	// Build the instance of the matrix multiplication operator
	FunctionInstance matrixMultiplication = newMatMultInstance(matrix);

	// Build the function types
	FunctionType functionTypes = newMatrixPowerFunctionTypes(matrix);

	// Build the function name
	String baseFunctionName = "matrix_power_positive_integer";
	String typesSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());
	String cFunctionName = baseFunctionName + typesSuffix;

	// Get the template
	String cBody = SpecsIo.getResource(MatlabOperatorsDecResource.MATRIX_POW_POSITIVE_INT_ITER);

	// Replace the needed tags
	cBody = MatlabOperatorsDecTemplate.parseTemplate(cBody, MP_SELF_CALL.getTag(), cFunctionName,
		MP_MAT_MULT_CALL.getTag(), matrixMultiplication.getCName(), MP_EYE_CALL.getTag(), eye.getCName(),
		MP_LENGTH.getTag(), length.toString(), MP_INPUT_NUM_TYPE.getTag(), matrix.matrix().getElementType()
		.code().getType(), MP_COPY_CALL.getTag(),
		copy.getCName());

	// Build the function instance, set the includes and set the dependencies
	LiteralInstance instance = new LiteralInstance(functionTypes, cFunctionName, MatlabOperatorsDec.C_FILENAME,
		cBody);

	instance.setCustomImplementationIncludes(SystemInclude.Stdlib.getIncludeName(),
		SystemInclude.Stdio.getIncludeName());

	instance.getCustomImplementationInstances().add(matrixMultiplication, eye, copy);

	return instance;
    }

    /**
     * Private method to create an instance of the copy function for the Matrix power instance.
     * 
     * @param functionSettings
     * @param matrix
     * @param length
     * @return
     */
    private FunctionInstance newCopyInstance(MatrixType matrix, Integer length) {
	VariableType constantType = getNumerics().newInt(length);
	List<VariableType> copyInputs = Arrays.asList(matrix, matrix, constantType);

	// FunctionInstance copy = DeclaredProvider.COPY.newCInstance(ProviderData.newInstance(data, copyInputs));
	FunctionInstance copy = DeclaredProvider.COPY.newCInstance(getData().create(copyInputs));

	return copy;
    }

    /**
     * Private method to create an instance of the eye function for the Matrix power instance.
     * 
     * @param functionSettings
     * @param classString
     * @param shape
     * @param data
     * @return
     */
    private FunctionInstance newEyeInstance(String classString, List<Integer> shape) {

	List<VariableType> eyeInputs = SpecsFactory.newArrayList();

	eyeInputs.add(getNumerics().newInt(shape.get(0)));
	eyeInputs.add(getNumerics().newInt(shape.get(0)));

	eyeInputs.add(VariableTypeFactory.newString(classString));

	// FunctionInstance eye = EyeDecInstance.newInstance(getData().createWithContext(eyeInputs));
	FunctionInstance eye = EyeDecInstance.newInstance(getData().create(eyeInputs));
	return eye;
    }

    /**
     * Private method to create the function types for the Matrix power instance.
     * 
     * @param originalTypes
     * @param matrix
     * @return
     */
    private FunctionType newMatrixPowerFunctionTypes(MatrixType matrix) {

	List<VariableType> inputTypes = getData().getInputTypes();
	List<String> inputNames = Arrays.asList(MatlabOperatorsDecTemplate.MP_FIRST_INPUT_NAME,
		MatlabOperatorsDecTemplate.MP_SECOND_INPUT_NAME);

	VariableType outputType = matrix;
	String outputName = MatlabOperatorsDecTemplate.MP_OUTPUT_NAME;

	FunctionType functionTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputName,
		outputType);

	return functionTypes;
    }

    /**
     * 
     * @param functionSettings
     * @param matrix
     * @return
     */
    private FunctionInstance newMatMultInstance(MatrixType matrix) {

	List<VariableType> matMultTypes = Arrays.asList(matrix, matrix);

	FunctionInstance matrixMultiplication = MatlabOp.MatrixMultiplication.getMatlabFunction().newCInstance(
		getData().create(matMultTypes));

	return matrixMultiplication;
    }

    /**
     * Creates a new instance of the Matlab builtin operator 'Colon' ( : ) for the cases where all inputs are numeric
     * scalar constants, the start value is an integer and the step value is either <b>1</b> or <b>omitted</b>. This
     * instance is specialized for several internal variables. </br> </br>
     * 
     * <b>Examples:</b>
     * 
     * <pre>
     * {@code
     * 2:1:14
     * 3:20
     * }
     * </pre>
     * 
     * @param originalTypes
     *            - the original variable types of the operator call
     * @return a new instance of {@link LiteralFunctionInstance} that represents the implementation of the colon
     *         operator
     */
    public LiteralInstance newScalarColonConsecIntsInstance(List<VariableType> originalTypes) {

	// Get the inputs, the output and the value of the internal variables
	VariableType start = originalTypes.get(0);
	// Integer startValue = VariableTypeContent.getNumeric(start).getIntValue();
	int startValue = ScalarUtils.getConstant(start).intValue();

	VariableType end = originalTypes.get(2);
	// Double endValue = VariableTypeContent.getNumeric(end).getDoubleValue();
	double endValue = ScalarUtils.getConstant(end).doubleValue();

	double floored = Math.floor(endValue);
	int n = (int) floored - startValue;

	double eps = 2.2204e-016;
	double tol = 2.0 * eps * Math.max(Math.abs(startValue), Math.abs(endValue));

	int sign = 1;

	if (sign * (startValue + n * 1 - endValue) > tol) {
	    n = n - 1;
	}

	int outputLength = n + 1;
	if (outputLength < 0) {
	    outputLength = 0;
	}

	ScalarType intType = getNumerics().newInt();

	double lowerBound;
	double upperBound;
	if (floored > startValue) {
	    lowerBound = startValue;
	    upperBound = floored;
	} else {
	    lowerBound = endValue;
	    upperBound = startValue;
	}

	VariableType output = StaticMatrixTypeBuilder
		.fromElementTypeAndShape(intType, TypeShape.newInstance(1, outputLength))
		.inRange(lowerBound, upperBound)
		.build();

	// Build the function types
	FunctionType functionTypes = newScalarColonFunctionTypes(originalTypes, output);

	// Build the function name
	String baseFunctionName = "scalar_colon_dec_consec_ints";
	String nameWithN = baseFunctionName + "_" + Integer.toString(n).replace("-", "n");
	String cFunctionName = newScalarColonFunctionName(nameWithN, originalTypes);

	// Get the template
	// String cBody = IoUtils
	// .getResourceString(MatlabOperatorsDecResource.SCALAR_COLON_DEC_CONSEC_INTS
	// .getResource());
	String cBody = SpecsIo.getResource(MatlabOperatorsDecResource.SCALAR_COLON_DEC_CONSEC_INTS);

	// Replace the needed tags
	cBody = MatlabOperatorsDecTemplate.parseTemplate(cBody,
		CD_N.getTag(), Integer.toString(n),
		CD_SIGN.getTag(), Integer.toString(sign),
		CD_TOL.getTag(), Double.toString(tol));

	// Build the function instance, set the includes, the dependencies and the comments
	LiteralInstance instance = new LiteralInstance(functionTypes, cFunctionName, MatlabOperatorsDec.C_FILENAME,
		cBody);

	instance.setComments(" colon implementation for constant numeric scalars using declared matrices\n special case with consecutive integers ");

	return instance;
    }

    /**
     * Creates a new instance of the Matlab builtin operator 'Colon' ( : ) for the cases where all inputs are numeric
     * scalar constants, the start value is an integer and the step value is an integer different from 1. This instance
     * is specialized for several internal variables. </br> </br>
     * 
     * <b>Examples:</b>
     * 
     * <pre>
     * {@code
     * 3:4:14
     * 1:20:100
     * }
     * </pre>
     * 
     * @param originalTypes
     *            - the original variable types of the operator call
     * @return a new instance of {@link LiteralFunctionInstance} that represents the implementation of the colon
     *         operator
     */
    public LiteralInstance newScalarColonSpacedIntsInstance(List<VariableType> originalTypes) {

	// Get the inputs, the output and the value of the internal variables
	VariableType start = originalTypes.get(0);
	// Integer startValue = VariableTypeContent.getNumeric(start).getIntValue();
	Integer startValue = ScalarUtils.getConstant(start).intValue();

	VariableType step = originalTypes.get(1);
	// Integer stepValue = VariableTypeContent.getNumeric(step).getIntValue();
	Integer stepValue = ScalarUtils.getConstant(step).intValue();

	VariableType end = originalTypes.get(2);
	// Double endValue = VariableTypeContent.getNumeric(end).getDoubleValue();
	Double endValue = ScalarUtils.getConstant(end).doubleValue();

	Double q = Math.floor(startValue / stepValue);
	Double r = startValue - q * stepValue;
	Double n = Math.floor((endValue - r) / stepValue) - q;
	Integer nFloored = n.intValue();

	Double eps = 2.2204e-016;
	Double tol = 2.0 * eps * Math.max(Math.abs(startValue), Math.abs(endValue));

	Integer sign = 0;
	if (stepValue > 0) {
	    sign = 1;
	} else {
	    if (stepValue < 0) {
		sign = -1;
	    }
	}

	if (sign * (startValue + n * 1 - endValue) > tol) {
	    nFloored = nFloored - 1;
	}

	Integer outputLength = nFloored + 1;

	VariableType intType = getNumerics().newInt();
	VariableType output = VariableTypeFactory.newDeclaredMatrix(intType, 1, outputLength);

	// Build the function types
	FunctionType functionTypes = newScalarColonFunctionTypes(originalTypes, output);

	// Build the function name
	String baseFunctionName = "scalar_colon_dec_spaced_ints";
	String nameWithN = baseFunctionName + "_" + nFloored.toString().replace("-", "n");
	String cFunctionName = newScalarColonFunctionName(nameWithN, originalTypes);

	// Get the template
	// String cBody = IoUtils
	// .getResourceString(MatlabOperatorsDecResource.SCALAR_COLON_DEC_SPACED_INTS
	// .getResource());
	String cBody = SpecsIo.getResource(MatlabOperatorsDecResource.SCALAR_COLON_DEC_SPACED_INTS);

	// Replace the needed tags
	cBody = MatlabOperatorsDecTemplate.parseTemplate(cBody,
		CD_N.getTag(), nFloored.toString(),
		CD_SIGN.getTag(), sign.toString(),
		CD_TOL.getTag(), tol.toString());

	// Build the function instance, set the includes, the dependencies and the comments
	LiteralInstance instance = new LiteralInstance(functionTypes, cFunctionName, MatlabOperatorsDec.C_FILENAME,
		cBody);

	instance.setComments(" colon implementation for constant numeric scalars using declared matrices\n special case with spaced integers ");

	return instance;
    }

    /**
     * Creates a new instance of the Matlab builtin operator 'Colon' ( : ) for the cases where all inputs are numeric
     * scalar constants. This instance is specialized for several internal variables. </br> </br>
     * 
     * <b>Examples:</b>
     * 
     * <pre>
     * {@code
     * 3:3:9
     * 1:2.5:100
     * }
     * </pre>
     * 
     * @param originalTypes
     *            - the original variable types of the operator call
     * @return a new instance of {@link LiteralFunctionInstance} that represents the implementation of the colon
     *         operator
     */
    public LiteralInstance newScalarColonGeneralInstance(List<VariableType> originalTypes) {

	// Get the inputs, the output and the value of the internal variables
	VariableType start = originalTypes.get(0);
	// Number startNumber = VariableTypeContent.getNumeric(start).getConstant();
	Number startNumber = ScalarUtils.getConstant(start);
	double startValue = startNumber.doubleValue();

	VariableType step = originalTypes.get(1);
	// Number stepNumber = VariableTypeContent.getNumeric(step).getConstant();
	Number stepNumber = ScalarUtils.getConstant(step);
	double stepValue = stepNumber.doubleValue();

	VariableType end = originalTypes.get(2);
	// Number endNumber = VariableTypeContent.getNumeric(end).getConstant();
	Number endNumber = ScalarUtils.getConstant(end);
	double endValue = endNumber.doubleValue();

	Double n = (double) Math.round((endValue - startValue) / stepValue);

	Double eps = 2.2204e-016;
	Double tol = 2.0 * eps * Math.max(Math.abs(startValue), Math.abs(endValue));

	Integer sign = 0;
	if (stepValue > 0) {
	    sign = 1;
	} else {
	    if (stepValue < 0) {
		sign = -1;
	    }
	}

	if (sign * (startValue + n * stepValue - endValue) > tol) {
	    n = n - 1;
	}

	Double nFloored = Math.floor(n);

	Integer outputLength = nFloored.intValue() + 1;

	// Create a 1x0 matrix if it is not possible to execute
	if (outputLength < 0) {
	    outputLength = 0;
	}

	// NumericType outputNumericType = getOutputNumericType(startNumber, stepNumber, endNumber);
	// VariableType outputType = VariableTypeFactoryOld.newNumeric(outputNumericType);
	VariableType outputType = getOutputNumericType(startNumber, stepNumber, endNumber);

	VariableType output = VariableTypeFactory.newDeclaredMatrix(outputType, 1, outputLength);

	// Build the function types
	FunctionType functionTypes = newScalarColonFunctionTypes(originalTypes, output);

	// Build the function name
	String baseFunctionName = "scalar_colon_dec_general";
	String nameWithN = baseFunctionName + "_" + n.toString().replace(".", "_").replace("-", "n");
	String cFunctionName = newScalarColonFunctionName(nameWithN, originalTypes);

	// Get the template
	// String cBody = IoUtils
	// .getResourceString(MatlabOperatorsDecResource.SCALAR_COLON_DEC_GENERAL
	// .getResource());
	String cBody = SpecsIo.getResource(MatlabOperatorsDecResource.SCALAR_COLON_DEC_GENERAL);

	// Replace the needed tags
	cBody = MatlabOperatorsDecTemplate.parseTemplate(cBody,
		CD_N.getTag(), n.toString(),
		CD_SIGN.getTag(), sign.toString(),
		CD_TOL.getTag(), tol.toString());

	// Build the function instance, set the includes, the dependencies and the comments
	LiteralInstance instance = new LiteralInstance(functionTypes, cFunctionName, MatlabOperatorsDec.C_FILENAME,
		cBody);

	instance.setCustomImplementationIncludes(SystemInclude.Math.getIncludeName());

	instance.setComments(" colon implementation for constant numeric scalars using declared matrices ");

	return instance;
    }

    /**
     * @param startNumber
     * @param stepNumber
     * @param endNumber
     * @return
     */
    // static NumericType getOutputNumericType(Number startNumber, Number stepNumber, Number endNumber) {
    VariableType getOutputNumericType(Number startNumber, Number stepNumber, Number endNumber) {

	CNumber startC = CLiteral.newNumber(startNumber.toString());
	CNumber stepC = CLiteral.newNumber(stepNumber.toString());
	CNumber endC = CLiteral.newNumber(endNumber.toString());

	// Check if all Numbers are integer
	if (startC.isInteger() && stepC.isInteger() && endC.isInteger()) {
	    // return NumericType.Cint;
	    return getNumerics().newInt();
	}

	// return NumericType.Double;
	return getNumerics().newDouble();
    }

    /**
     * @param startValue
     * @param stepValue
     * @param endValue
     * @return
     */
    /*
    private static Number getN(CNumber start, CNumber step, CNumber end) {

    // Check if all Numbers are integer
    if (!start.isReal() && !step.isReal() && !end.isReal()) {
        return new Long((end.getLong() - start.getLong()) / step.getLong());
    }

    return new Double((end.getDouble() - start.getDouble()) / step.getDouble());
    }
     */

    /**
     * Builds the function types used in the scalar colon instances.
     * 
     * @param originalTypes
     *            - the original inputs
     * @param output
     *            - the output
     * @return an instance of {@link FunctionType}
     */
    private static FunctionType newScalarColonFunctionTypes(List<VariableType> originalTypes, VariableType output) {

	List<String> inputNames = Arrays.asList(MatlabOperatorsDecTemplate.CD_FIRST_INPUT_NAME,
		MatlabOperatorsDecTemplate.CD_SECOND_INPUT_NAME, MatlabOperatorsDecTemplate.CD_THIRD_INPUT_NAME);
	String outputName = MatlabOperatorsDecTemplate.CD_OUTPUT_NAME;

	return FunctionType.newInstanceWithOutputsAsInputs(inputNames, originalTypes, outputName, output);
    }

    /**
     * Builds the function name for the scalar colon instances.
     * 
     * @param baseFunctionName
     *            - the base name
     * @param originalTypes
     *            - the original inputs
     * @return a {@link String} with the full name
     */
    private static String newScalarColonFunctionName(String baseFunctionName, List<VariableType> originalTypes) {

	String typesSuffix = FunctionInstanceUtils.getTypesSuffix(originalTypes);
	String cFunctionName = baseFunctionName + typesSuffix;
	return cFunctionName;
    }

    /**
     * Not implemented yet!
     */
    @Override
    public FunctionInstance create() {
	throw new RuntimeException("Not implemented yet!");
    }
}
