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

package org.specs.MatlabToCTester.CGeneration;

import java.io.File;
import java.util.List;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabIR.MatlabLanguage.NumericClassName;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToCTester.FileOperations.ScriptResource;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public class CodegenUtils {

    /**
     * @param matlabFiles
     * @param functionToPutMain
     * @return
     */
    public static File getMainFile(List<File> matlabFiles, String functionToPutMain) {
	String filename = functionToPutMain + ".m";
	for (File matlabFile : matlabFiles) {
	    if (matlabFile.getName().equals(filename)) {
		return matlabFile;
	    }
	}

	SpecsLogs.msgInfo("Could not find M file '" + functionToPutMain + "'");
	return null;
    }

    /**
     * @param functionName
     * @param inputNames
     * @param inputTypes
     * @param alloc
     * @param mTempFolder
     * @return
     */
    public static String getCodegenScript(String functionName, List<String> inputNames, TypesMap inputTypes,
	    boolean dynamicAllocationAllowed) {

	String template = SpecsIo.getResource(ScriptResource.CODEGEN_RUN);

	template = template.replace("<FUNCTION_NAME>", functionName);

	String types = getTypes(inputNames, inputTypes);
	template = template.replace("<INPUT_TYPES>", types);

	// template = template.replace("<SCRIPT_FOLDER>", mTempFolder.getAbsolutePath());

	// Indicate which types will be used for the inputs
	StringBuilder builder = new StringBuilder();
	if (!inputNames.isEmpty()) {
	    builder.append(getNameTypeString(inputNames.get(0), inputTypes));
	}

	for (int i = 1; i < inputNames.size(); i++) {
	    builder.append(", ").append(getNameTypeString(inputNames.get(i), inputTypes));
	}

	SpecsLogs.msgInfo("Types: " + builder.toString());

	// System.out.println("NAMES:" + inputNames);
	// System.out.println("TYPES:" + types);

	if (dynamicAllocationAllowed) {
	    template = template.replace("<DYNAMIC_MEMORY>", "'AllVariableSizeArrays'");
	} else {
	    template = template.replace("<DYNAMIC_MEMORY>", "'Off'");
	}
	/*
	if (alloc == MemoryAllocationOption.Dynamic) {
	    template = template.replace("<DYNAMIC_MEMORY>", "'AllVariableSizeArrays'");
	} else if (alloc == MemoryAllocationOption.Static) {
	    template = template.replace("<DYNAMIC_MEMORY>", "'Off'");
	} else {
	    LoggingUtils.msgWarn("Case not defined:" + alloc);
	}
	*/

	/*
	// SetupHelper setup = new SetupHelper(SimpleSetup.newInstance("coder main test"));
	Setup setup = SimpleSetup.newInstance("coder main test");

	setup.getOptionTable().setOption(
		new GenericOption("inputNames", new StringList(inputNames), StringList.class));

	setup.getOptionTable().setOption(
		new GenericOption("functionName", functionName, String.class));

	setup.getOptionTable().setOption(new GenericOption("typesMap", inputTypes, TypesMap.class));

	MatisseUtils
		.saveAspect(
			new File(
				"C:\\Users\\Joao Bispo\\Dropbox\\Research\\Work\\2013-11-20 Generate main for Coder code\\"+functionName+".info"),
			setup);
	*/
	return template;
    }

    /**
     * @param inputNames
     * @param inputTypes
     * @param builder
     */
    private static String getNameTypeString(String inputName, TypesMap inputTypes) {
	return inputName + " -> " + getCoderType(inputTypes.getSymbol(inputName));
    }

    /**
     * @param inputNames
     * @param inputTypes
     * @return
     */
    private static String getTypes(List<String> inputNames, TypesMap inputTypes) {
	StringBuilder builder = new StringBuilder();

	boolean firstTime = true;
	for (String inputName : inputNames) {
	    if (firstTime) {
		firstTime = false;
	    } else {
		builder.append(", ");
	    }

	    VariableType type = inputTypes.getSymbol(inputName);
	    if (type == null) {
		throw new RuntimeException("Could not find type for input '" + inputName + "'");
	    }

	    String coderType = getCoderType(type);
	    builder.append("coder.typeof(").append(coderType).append(")");
	}

	return builder.toString();
    }

    /**
     * @param type
     * @return
     */
    private static String getCoderType(VariableType type) {

	// if (type.getType() == CType.Numeric) {
	if (ScalarUtils.isScalar(type)) {
	    // NumericClassName nClass =
	    // MatlabToCTypes.getNumericClassStrict(VariableTypeUtilsOld.getNumericType(type));
	    // NumericClassName nClass = MatlabToCTypes.getNumericClassStrict(type);
	    NumericClassName nClass = MatlabToCTypesUtils.getNumericClass(type);
	    return nClass.getMatlabString() + "(0)";
	}

	if (MatrixUtils.isMatrix(type)) {
	    if (MatrixUtils.isStaticMatrix(type)) {
		StringBuilder builder = new StringBuilder();

		// Get inner type string
		String innerType = getCoderType(MatrixUtils.getElementType(type));
		builder.append(innerType);

		// Get shape
		List<Integer> shape = MatrixUtils.getShapeDims(type);

		builder.append(getShapeString(shape));

		return builder.toString();
	    }

	    if (MatrixUtils.usesDynamicAllocation(type)) {
		StringBuilder builder = new StringBuilder();

		// Get inner type string
		String innerType = getCoderType(MatrixUtils.getElementType(type));
		builder.append(innerType);

		// Check if it has a shape
		// List<Integer> shape = MatrixUtilsV2.getShapeDims(type);

		// if (shape.isEmpty) {
		if (type.getTypeShape().isUndefined()) {
		    builder.append(", [Inf]");
		} else {
		    // builder.append(getShapeString(shape));
		    builder.append(getShapeString(type.getTypeShape().getDims()));
		}

		// builder.append(", [Inf, 1]");
		// builder.append(", [Inf, Inf]");

		return builder.toString();
	    }

	}

	SpecsLogs.warn("Case not defined:" + type);
	return null;
    }

    /**
     * @param builder
     * @param shape
     */
    private static String getShapeString(List<Integer> shape) {
	StringBuilder builder = new StringBuilder();

	builder.append(", [");
	boolean firstTime = true;
	for (Integer dim : shape) {
	    if (firstTime) {
		firstTime = false;
	    } else {
		builder.append(" ");
	    }

	    // If dim is -1, interpret as infinite
	    if (dim == -1) {
		builder.append("Inf");
	    } else {
		builder.append(dim);
	    }
	}
	builder.append("]");

	return builder.toString();
    }
}
