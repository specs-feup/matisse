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

package org.specs.MatlabToC.MFunctions;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabToC.CodeBuilder.MatlabToCBuilder;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementProcessor.MatlabToCException;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * Specialized version of an M-file.
 * 
 * @author Joao Bispo
 * 
 */
public class MFunctionInstance {

    public static InstructionsInstance newInstanceV2(String mFunctionName, String parentFunctionName,
	    FunctionNode rootToken, MatlabToCFunctionData data, List<VariableType> inputTypes) {

	List<String> outputNames = new ArrayList<>();

	// Verify if all outputs of the function have a different name than the inputs
	verifyOutputNames(rootToken, data, outputNames);

	// Parse input types (e.g., remove literal status
	inputTypes = parseInputTypes(inputTypes);

	// Add input types to local types
	if (!addInputTypes(inputTypes, data, rootToken)) {
	    return null;
	}

	// Add output type
	List<VariableType> outputTypes = data.getProviderData().getOutputTypes();

	if (outputTypes != null) {
	    for (int i = 0; i < outputTypes.size(); i++) {
		if (outputTypes.get(i) == null) {
		    continue;
		}

		data.addVariableType(outputNames.get(i), outputTypes.get(i));
	    }
	}

	// System.out.println("INPUT TYPES:" + inputTypes);
	// System.out.println("OUT TYPE:" + data.getProviderData().getOutputType());
	// Set functionName, in case of script
	// data.setFunctionName(mFunctionName);

	// data.setAssignmentReturnTypes(data.getProviderData().getOutputTypes());

	// Build instructions
	CInstructionList instructionList = MatlabToCBuilder.build(rootToken, data);
	if (instructionList == null) {
	    return null;
	}

	// Add return statement to instruction list if needed (if function has a
	// non-void return type)
	// CToCUtils.addReturnStatement(instructionList);
	instructionList.addReturn();

	// Add calls to free if needed, before each return
	new MFunctionsUtils(data.getProviderData().newInstance()).addCallsToFree(instructionList);

	// Add function comments
	List<String> comments = data.getFunctionComments();
	List<String> varComments = variableComments(instructionList.getFunctionTypes());
	addComments(varComments, comments);

	// Get constant args
	List<String> constantArgs = buildConstArgs(inputTypes);

	// Implementation Data
	FunctionType functionTypes = instructionList.getFunctionTypes();

	// Get implementation function name
	String implFunctionName = buildFunctionName(mFunctionName, parentFunctionName);

	// Build cfilename. If parentFunctionName is different than null, this means this is a sub-function, and should
	// go to the same file as the parent function.
	String cfileName = implFunctionName;
	if (parentFunctionName != null) {
	    cfileName = parentFunctionName;
	}

	// Check if there is a base C filename
	if (data.getBaseCFilename() != null) {
	    cfileName = data.getBaseCFilename() + cfileName;
	}

	String cFunctionName = getFunctionName(implFunctionName, instructionList, constantArgs);

	InstructionsInstance instance = new InstructionsInstance(functionTypes, cFunctionName, cfileName,
		instructionList);

	instance.setComments(comments);

	return instance;
    }

    // Throws an exception if any problem is detected
    private static void verifyOutputNames(FunctionNode functionToken, MatlabToCFunctionData data,
	    List<String> outputNames) {

	// Check if output names do not overlap with input names
	// MatlabNode fDec = StatementUtils.getFirstToken(MStatementType.FunctionDeclaration,
	// functionToken.getChildren());
	List<String> inputNames = functionToken.getInputNames();
	// List<String> outputNames = MatlabTokenContent.getFunctionDeclarationOutputNames(fDec);
	outputNames.addAll(functionToken.getOutputNames());
	String functionName = functionToken.getFunctionName();

	Set<String> forbiddenNames = SpecsFactory.newHashSet(inputNames);
	for (String outName : outputNames) {
	    if (forbiddenNames.contains(outName)) {
		throw new MatlabToCException("Output '" + outName
			+ "' has the same name as one of the inputs in function '" + functionName
			+ "', they must be different", data);
	    }
	}

    }

    /**
     * @param inputTypes
     * @return
     */
    private static List<VariableType> parseInputTypes(List<VariableType> inputTypes) {
	List<VariableType> parsedTypes = SpecsFactory.newArrayList();

	for (VariableType type : inputTypes) {
	    if (ScalarUtils.isScalar(type)) {
		type = ScalarUtils.getScalar(type).setLiteral(false);
	    }

	    parsedTypes.add(type);
	}

	return parsedTypes;
    }

    /**
     * Builds the C name.
     * 
     * <p>
     * Building the name might be an expensive operation, since the algorithm travels along all implementations needed
     * for the function.<br>
     * 
     * 
     */
    private static String buildFunctionName(String mFunctionName, String parentFunctionName) {
	StringBuilder builder = new StringBuilder();

	// If sub-function, add main function name
	if (parentFunctionName != null) {
	    builder.append(parentFunctionName);
	    builder.append("_");
	}

	// Append current function name
	builder.append(mFunctionName);

	return builder.toString();
    }

    /**
     * @param inputTypes
     * @return
     */
    private static List<String> buildConstArgs(List<VariableType> inputTypes) {
	List<String> constArgs = SpecsFactory.newArrayList();

	// Get a fixed locale, so that the separator is always the same
	NumberFormat f = NumberFormat.getInstance(Locale.UK);
	f.setMaximumFractionDigits(2);

	for (int i = 0; i < inputTypes.size(); i++) {
	    VariableType inputType = inputTypes.get(i);

	    // Check if it has a numeric element
	    VariableType normalizedType = ScalarUtils.toScalar(inputType);
	    if (normalizedType == null) {
		continue;
	    }

	    // Check if constant
	    // NumericData numericData = VariableTypeContent.getNumeric(normalizedType);
	    // if (!numericData.hasConstant()) {
	    if (!ScalarUtils.hasConstant(normalizedType)) {
		continue;
	    }

	    // String constantString = MFunctionsUtils.getConstantString(f, numericData);
	    String constantString = MFunctionsUtils.getConstantString(f, normalizedType);
	    // String constantString = numericData.getConstant().toString();

	    // String argString = "arg" + (i + 1) + "_" + constantString;
	    String argString = "c" + constantString;
	    constArgs.add(argString);
	}

	return constArgs;
    }

    /**
     * @param cTypeData
     * @return
     */
    private static List<String> variableComments(FunctionType cTypeData) {
	List<String> varComments = new ArrayList<>();

	// Check if any of the inputs is a Matrix
	for (int i = 0; i < cTypeData.getCNumInputs(); i++) {
	    String name = cTypeData.getCInputNames().get(i);
	    VariableType type = cTypeData.getCInputTypes().get(i);

	    if (MatrixUtils.isStaticMatrix(type)) {
		String matrixComment = matrixComment(name, (MatrixType) type);
		varComments.add(matrixComment);
		continue;
	    }
	}

	return varComments;
    }

    public static String matrixComment(String name, MatrixType matrixType) {
	return new StringBuilder().append("Array '").append(name).append("' has shape ")
		.append(matrixType.getTypeShape().getStringV2()).toString();
    }

    /**
     * @param newComments
     * @param comments
     */
    private static void addComments(List<String> newComments, List<String> comments) {
	if (newComments.isEmpty()) {
	    return;
	}

	// There are new comments to add. Check if it is needed to add a line
	if (!comments.isEmpty()) {
	    comments.add("");
	}

	comments.addAll(newComments);
    }

    /**
     * Extracts the function input names from 'mFunctionToken' and adds local type definitions for those inputs.
     * 
     * @param inputTypes
     * @param data
     * @param rootToken
     */
    private static boolean addInputTypes(List<VariableType> inputTypes, MatlabToCFunctionData data,
	    FunctionNode functionNode) {

	List<String> inputNames = functionNode.getInputNames();

	// Input types cannot be bigger then the number of inputs of the function
	if (inputTypes.size() > inputNames.size()) {
	    SpecsLogs.warn("Function has '" + inputNames.size() + "' inputs, but specialization has '"
		    + inputTypes.size() + "' input types.");
	    return false;
	}

	for (int i = 0; i < inputTypes.size(); i++) {
	    VariableType type = inputTypes.get(i);

	    String variableName = inputNames.get(i);

	    if (data.hasType(variableName)) {
		// Override type, even if it already exists.
		// Input types have priority over already set types.
		data.setVariableType(variableName, type);
	    } else {
		data.addVariableType(variableName, type);
	    }
	}

	return true;
    }

    private static String getFunctionName(String baseName, CInstructionList instructions, List<String> constantArgs) {

	String cname = baseName
		+ FunctionInstanceUtils.getTypesSuffix(instructions.getFunctionTypes().getCInputTypes())
		+ getSpecializationSuffix(instructions, constantArgs);
	return FunctionInstanceUtils.sanitizeFunctionName(cname);
    }

    /**
     * Builds the constant specialization suffix, for appending to the function C name.
     * 
     * <p>
     * Building the name might be an expensive operation, since the algorithm travels along all implementations needed
     * for the function. Hence, calculating the name at construction time.<br>
     * 
     * 
     */
    private static String getSpecializationSuffix(CInstructionList instructions, List<String> constantArgs) {

	StringBuilder builder = new StringBuilder();

	Set<FunctionInstance> instances = SpecsFactory.newHashSet();

	// Add declaration instances
	FunctionType functionTypes = instructions.getFunctionTypes();
	SpecsFactory.addAll(instances, FunctionInstanceUtils.getFunctionTypesInstances(functionTypes));

	// Add instruction instances
	SpecsFactory.addAll(instances, FunctionInstanceUtils.getInstrucionsInstances(instructions));

	// Check if any of the function calls of this method uses a specialized
	// function. Check all methods recursively
	boolean hasSpecialized = FunctionInstanceUtils.hasSpecializedFunctionCallsRecursive(instances);
	if (hasSpecialized) {
	    String constantsString = getConstantsString(constantArgs);
	    if (!constantsString.isEmpty()) {
		builder.append("_");
	    }
	    builder.append(constantsString);
	}

	return builder.toString();
    }

    /**
     * @return
     */
    private static String getConstantsString(List<String> constantArgs) {
	StringBuilder builder = new StringBuilder();

	// Check if function was specialized with any constants
	for (String constArg : constantArgs) {
	    builder.append(constArg);
	}

	String unsanitizedString = builder.toString();
	return unsanitizedString;
    }

}
