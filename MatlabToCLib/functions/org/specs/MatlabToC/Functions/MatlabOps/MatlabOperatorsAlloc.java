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

package org.specs.MatlabToC.Functions.MatlabOps;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.CNative.CNativeUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRFunctions.MatrixAlloc.TensorProvider;
import org.specs.CIRTypes.Language.CLiteral;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixStruct;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.Functions.MathFunction;
import org.specs.MatlabToC.Functions.MathFunctions.Static.minmax.MinMax;
import org.specs.MatlabToC.Functions.MathFunctions.Static.minmax.MinMaxFunctions;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.utilities.Replacer;

/**
 * Contains implementations for some Matlab operators using templates.
 * 
 * @author Pedro Pinto
 * 
 */
public class MatlabOperatorsAlloc extends AInstanceBuilder {

    public MatlabOperatorsAlloc(ProviderData data) {
	super(data);
    }

    private final static String C_FILENAME = MatlabCFilename.MatlabGeneral.getCFilename();

    // For allocated scalar colon
    private final static String CA_FIRST_INPUT_NAME = "start";
    private final static String CA_SECOND_INPUT_NAME = "step";
    private final static String CA_THIRD_INPUT_NAME = "end";
    private final static String CA_OUTPUT_NAME = "output";

    // TODO: This function would benefit from default float, and from a MATLAB implementation
    @Override
    public FunctionInstance create() {

	ProviderData pdata = getData();

	List<VariableType> originalTypes = pdata.getInputTypes();
	List<ScalarType> scalarTypes = new ArrayList<>();
	for (VariableType originalType : originalTypes) {
	    if (!ScalarUtils.isScalar(originalType)) {
		throw new RuntimeException("Inputs can only be of scalar type, found '" + originalType + "'");
	    }

	    scalarTypes.add(ScalarUtils.toScalar(originalType));
	}

	// Get the inputs and the output
	VariableType start = originalTypes.get(0);
	VariableType step = originalTypes.get(1);
	VariableType end = originalTypes.get(2);

	// Get the dominant scalar type
	ScalarType floatType = getInferredType(scalarTypes, Optional.empty());

	// If inputs are all integer, use simple float
	if (floatType.scalar().isInteger()) {
	    floatType = getNumerics().newFloat();
	}

	// Just need to check start and step, even if end is double and the others integers the output will be integer
	List<VariableType> typesEvaluate = originalTypes.subList(0, originalTypes.size() - 1);
	// VariableType outputElementType = VariableTypeUtilsG.getMaximalFit(typesEvaluate);
	ScalarType outputElementType = ScalarUtils.getMaxRank(ScalarUtils.cast(typesEvaluate));
	outputElementType = outputElementType.scalar().setConstant(null);
	TypeShape outputShape = TypeShape.newRow();
	if (ScalarUtils.hasConstant(start) && ScalarUtils.hasConstant(step) && ScalarUtils.hasConstant(end)) {
	    Number startConstant = ScalarUtils.getConstant(start);
	    Number stepConstant = ScalarUtils.getConstant(step);
	    Number endConstant = ScalarUtils.getConstant(end);

	    if (stepConstant.doubleValue() == 1 &&
		    startConstant.doubleValue() == startConstant.intValue()
		    && endConstant.doubleValue() == endConstant.intValue()) {

		int size = endConstant.intValue() - startConstant.intValue() + 1;
		if (size < 0) {
		    size = 0;
		}

		outputShape = TypeShape.newRow(size);
	    }
	}
	VariableType output = DynamicMatrixType.newInstance(outputElementType, outputShape);

	// Build the function types
	FunctionType functionTypes = newScalarColonFunctionTypes(originalTypes, output);

	// Build the function name
	String baseFunctionName = "scalar_colon_alloc";
	String typesSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());
	String cFunctionName = baseFunctionName + typesSuffix;

	// Get the template
	Replacer cBody = new Replacer(SpecsIo.getResource(MatlabOperatorsAllocResource.SCALAR_COLON_ALLOC
		.getResource()));

	// Get the instances of the functions that are called
	FunctionInstance max = MinMaxFunctions.newMinMaxScalarsInstance(Arrays.asList(start, end), MinMax.MAX);
	InstanceProvider absProvider = MathFunction.ABS.getMatlabFunction();
	FunctionInstance absStart = absProvider.getCheckedInstance(pdata.create(start));
	FunctionInstance absEnd = absProvider.getCheckedInstance(pdata.create(end));

	List<VariableType> newArrayTypes = Arrays.asList(getNumerics().newInt(), getNumerics().newInt(), output);
	ProviderData newArrayData = ProviderData.newInstance(pdata, newArrayTypes);
	FunctionInstance newArray = TensorProvider.NEW_ARRAY.newCInstance(newArrayData);

	// cBody = MatlabOperatorsAllocTemplate.parseTemplate(cBody, CA_MAX_CALL.getTag(),
	// max.getCName(), CA_NEW_ARRAY_CALL.getTag(), newArray.getCName());

	// Call to functions
	cBody.replace("<CA_MAX_CALL>", max.getCName());
	cBody.replace("<CA_ABS_START_CALL>", absStart.getCName());
	cBody.replace("<CA_ABS_END_CALL>", absEnd.getCName());
	cBody.replace("<CA_NEW_ARRAY_CALL>", newArray.getCName());

	cBody.replace("<TENSOR_DATA>", DynamicMatrixStruct.TENSOR_DATA);
	cBody.replace("<FLOAT_TYPE>", floatType.code().getSimpleType());

	// Replace the number 2
	CLiteral twoLiteral = CLiteral.newInstance(2, CNativeUtils.toCNative(floatType));
	cBody.replace("<TWO_LITERAL>", twoLiteral.toCString());

	// Determine the correct EPS
	Optional<String> eps = floatType.scalar().getEps();
	String epsString = eps.orElse("2.2204e-016");

	// First convert to CLiteral
	CLiteral epsLiteral = CLiteral.newInstance(new BigDecimal(epsString), CNativeUtils.toCNative(floatType));
	// Replace using CLiteral C code
	cBody.replace("<EPS>", epsLiteral.toCString());

	// Build the function instance, set the includes and set the dependencies
	LiteralInstance instance = new LiteralInstance(functionTypes, cFunctionName, MatlabOperatorsAlloc.C_FILENAME,
		cBody.toString());

	// instance.setCustomImplementationIncludes(Arrays.asList(SystemInclude.Math));
	instance.setCustomImplementationIncludes(SystemInclude.Math);

	instance.getCustomImplementationInstances().add(max, newArray);

	instance.setComments(" colon implementation for numeric scalars using allocated matrices ");

	return instance;
    }

    private static FunctionType newScalarColonFunctionTypes(List<VariableType> originalTypes, VariableType outputType) {

	// The input and output names
	List<String> inputNames = Arrays.asList(MatlabOperatorsAlloc.CA_FIRST_INPUT_NAME,
		MatlabOperatorsAlloc.CA_SECOND_INPUT_NAME, MatlabOperatorsAlloc.CA_THIRD_INPUT_NAME);
	String outputName = MatlabOperatorsAlloc.CA_OUTPUT_NAME;

	return FunctionType.newInstanceWithOutputsAsInputs(inputNames, originalTypes, outputName, outputType);
    }
}
