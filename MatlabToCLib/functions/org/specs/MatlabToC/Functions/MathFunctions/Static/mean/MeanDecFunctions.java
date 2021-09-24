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

package org.specs.MatlabToC.Functions.MathFunctions.Static.mean;

import static org.specs.MatlabToC.Functions.MathFunctions.Static.mean.MeanDecTemplate.MM_DIM_SIZE;
import static org.specs.MatlabToC.Functions.MathFunctions.Static.mean.MeanDecTemplate.MM_MATRIX_SUM_CALL;
import static org.specs.MatlabToC.Functions.MathFunctions.Static.mean.MeanDecTemplate.MM_RIGHT_DIVISION_CALL;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.Functions.MathFunctions.Static.sum.SumDecInstance;
import org.specs.MatlabToC.Functions.MatlabOps.ElementWiseInstance;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * This class contains methods that create and return new instances foe the implementation fo the Matlab built-in
 * function 'mean' with numeric, declared vectors and matrices.
 * 
 * 
 * @author Pedro Pinto
 * 
 */
public class MeanDecFunctions extends AInstanceBuilder {

    private final static String C_FILENAME = MatlabCFilename.MatlabGeneral.getCFilename();

    public MeanDecFunctions(ProviderData data) {
	super(data);
    }

    /**
     * Creates a new instance for the cases where the input is a numeric matrix and the dimension parameter is not
     * specified. </br>
     * </br>
     * <b>Example call:</b>
     * 
     * <pre>
     * {@code
     * o = mean(m),
     * where m is a vector
     * }
     * </pre>
     * 
     * @param originalTypes
     *            - the original types of the Matlab function call inputs
     * @return an instance of {@link LiteralInstance}
     */
    @Override
    public FunctionInstance create() {
	// TODO: matrixElement, is being used correctly?
	List<VariableType> originalTypes = getData().getInputTypes();

	MatrixType matrix = getData().getInputType(MatrixType.class, 0);
	// VariableType matrixElement = MatrixUtilsV2.getElementType(matrix);
	ScalarType matrixElement = matrix.matrix().getElementType();

	// Create all the needed instances and values for the template
	// List<Integer> shape = MatrixUtilsV2.getShapeDims(matrix);
	// List<Integer> shape = matrix.getMatrixShape().getDims();

	int dimIndex = matrix.getTypeShape().getFirstNonSingletonDimension();
	Double dimLength = new Double(matrix.getTypeShape().getDim(dimIndex));

	ProviderData sumData = ProviderData.newInstance(getData(), matrix);
	FunctionInstance sum = SumDecInstance.newInstance(sumData);

	// FunctionInstance division = getInstance(COperator.Division.getProvider(true, false), matrixElement,
	// doubleType);
	FunctionInstance division = getInstance(COperator.Division.getProvider(true, false), matrixElement,
		matrixElement);

	// ProviderData rDivData = ProviderData.newInstance(getData(), matrix, doubleType);
	ProviderData rDivData = ProviderData.newInstance(getData(), matrix, matrixElement);
	FunctionInstance rightDivision = ElementWiseInstance.newProvider(division).newCInstance(rDivData);

	// Create the template, name and function types
	FunctionType functionTypes = buildMatrixFunctionTypes(originalTypes, sum);
	String cFunctionName = buildMatrixFunctionName(originalTypes);
	String cBody = MeanDecResources.MEAN_DEC_MATRIX.getTemplate();

	// Replace the tags on the template
	cBody = MeanDecTemplate.parseTemplate(cBody, MM_DIM_SIZE.getTag(), dimLength.toString(),
		MM_MATRIX_SUM_CALL.getTag(), sum.getCName(), MM_RIGHT_DIVISION_CALL.getTag(), rightDivision.getCName());

	LiteralInstance instance = new LiteralInstance(functionTypes, cFunctionName, MeanDecFunctions.C_FILENAME,
		cBody);

	instance.setComments(" mean for numeric declared matrices ");
	instance.getCustomImplementationInstances().add(rightDivision, sum);

	return instance;
    }

    /**
     * Builds the function name for newMeanDecMatrix().
     * 
     * @param originalTypes
     *            - the original types of the Matlab function call inputs
     * @return a {@link String} with the name
     */
    private static String buildMatrixFunctionName(List<VariableType> originalTypes) {

	StringBuilder builder = new StringBuilder();

	builder.append("mean_matrix_dec");
	builder.append(FunctionInstanceUtils.getTypesSuffix(originalTypes));

	return builder.toString();
    }

    /**
     * Builds the function types for newMeanDecMatrix().
     * 
     * @param originalTypes
     *            - the original types of the Matlab function call inputs
     * @param sumInstance
     *            - the instance of sum used
     * @return an instance of {@link FunctionType}
     */
    private static FunctionType buildMatrixFunctionTypes(List<VariableType> originalTypes,
	    FunctionInstance sumInstance) {

	List<String> inputNames = Arrays.asList(MeanDecTemplate.MATRIX_INPUT_NAME);
	List<VariableType> inputTypes = SpecsFactory.newArrayList(originalTypes);

	String outputName = MeanDecTemplate.MATRIX_OUTPUT_NAME;
	// The output of this instance is the same as the output of the sum
	VariableType outputType = sumInstance.getFunctionType().getCReturnType();

	return FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputName, outputType);
    }
}
