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

package org.specs.MatlabToC.CodeBuilder;

import java.util.List;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Conversion.ConversionUtils;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabToC.CodeBuilder.MatToMatRules.MatToMatUtils;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementProcessor.MatlabToCException;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public class CodeBuilderUtils {

    /**
     * Parses the list of MatlabTokens, which must all be identifiers, and adds the names of the outputs to the
     * MatlabToCFunctionData.
     * 
     * @param functionHead
     * @param children
     * @param varTable
     */
    public static void addOutputs(List<MatlabNode> outputs, MatlabToCFunctionData data) throws MatlabToCException {

	// Check if no outputs
	if (outputs.isEmpty()) {
	    return;
	}

	// List<VariableType> outputTypes = data.getProviderData().getOutputTypes();

	// for (MatlabToken identifier : outputs) {
	for (int i = 0; i < outputs.size(); i++) {

	    MatlabNode identifier = outputs.get(i);

	    // Outputs should be identifiers
	    if (!(identifier instanceof IdentifierNode)) {
		throw new MatlabToCException("Given output is not an identifier:" + identifier);
	    }

	    // Output name
	    // String outputName = identifier.getContentString();
	    String outputName = ((IdentifierNode) identifier).getName();

	    // Add name
	    data.addOutputName(outputName);

	    // If output is defined for variable, add to types
	    /*
	    if (outputTypes.size() > i) {
	    VariableType outputType = outputTypes.get(i);
	    if (outputType != null) {
	         data.addVariableType(outputName, outputType);
	        LoggingUtils.msgLib("CodeBuilderUtils.addOutputs: adding type for output in M function '"
	    	    + data.getFunctionName() + "', " + outputName
	    	    + " -> " + outputType);
	    }
	    }
	    */

	    // Do not create output variables, they should be created by
	    // assignments or function inputs
	}

    }

    /**
     * Converts a MatlabToken statement in a CToken object and/or updates given data.
     * 
     * @param token
     * @param systemTable
     * @return a CToken representing the given statement, or null if no CToken was created
     */
    public static CNode matlabToC(StatementNode statement, MatlabToCFunctionData data) {
	// CToken token = null;

	// Apply MATLAB-to-MATLAB transformations to statement
	List<StatementNode> newStatements = MatToMatUtils.applyRules(statement, data);
	// List<MatlabNode> newStatements = FactoryUtils.newArrayList(Arrays.asList(statement));

	// Convert each statement
	List<CNode> ctokens = SpecsFactory.newArrayList();
	for (StatementNode newStatement : newStatements) {
	    CNode newToken = null;
	    try {
		newToken = matlabToCHelper(newStatement, data);
	    } catch (Exception e) {
		// String message = "Could not transform the following statement to C:\n"
		// + MatlabProcessorUtils.toMFile(newStatement);

		// If already a MatlabToCException, just throw it
		String message = null;
		if (MatlabToCException.class.isInstance(e)) {
		    throw e;
		    // message = e.getMessage() + "--------\n->\n\n" + MatlabProcessorUtils.toMFile(newStatement);
		}

		message = "Could not transform the following statement on line '" + data.getLineNumber()
			+ "' of function '" + data.getFunctionName() + "' to C:\n\n"
			+ newStatement.getCode();

		throw new MatlabToCException(message, e, data);
	    }

	    ctokens.add(newToken);
	}

	// If there is only one token, return it
	if (ctokens.size() == 1) {
	    return ctokens.get(0);
	}

	// Build Block CToken
	return CNodeFactory.newBlock(ctokens);

	/*
	try {
	    token = matlabToCHelper(statement, data);
	
	} catch (Exception e) {
	    throw new MatlabToCException(e.toString(), e, data);
	}
	*/

	// return token;
    }

    private static CNode matlabToCHelper(StatementNode statement, MatlabToCFunctionData data) {

	CNode token = data.getImplementationData().getStatementProcessor().process(statement, data);

	// If null, return
	if (token == null) {
	    SpecsLogs.msgInfo("Returning null CToken! Check if should return empty CToken.\nType:"
		    + statement.getNodeName() + "\nCode:" + statement.getCode());
	    return null;
	}

	return token;
    }

    /**
     * Checks if the output type of an assignment is getting promoted (e.g., was initially defined as an int, but later
     * in the code it is being used as a double).
     * 
     * <p>
     * If it is found a type promotion from a type that was not externally defined, warns the user.
     * 
     * <p>
     * This code shares similarities with VariableTypeUtils.isAssignanable.
     * 
     * @param variableName
     * @param newType
     * @param data
     */
    public static boolean checkTypePromotion(String variableName, VariableType newType, MatlabToCFunctionData data) {

	// Check if variable type was externally defined
	if (data.isVariableDefinedExternally(variableName)) {
	    // System.out.println("Is defined externally:"+variableName);
	    return false;
	}

	// Check if variable name does not have a type
	if (!data.hasType(variableName)) {
	    return false;
	}

	VariableType currentType = null;
	try {
	    currentType = data.getVariableType(variableName);
	} catch (MatlabToCException e) {
	    SpecsLogs.warn("It should never get here, we are checking first "
		    + "if the type of the variable exists.");
	}

	// Check if type is the same as the type on the right hand of the
	// assignment
	if (newType.equals(currentType)) {
	    return false;
	}

	// boolean isAssignable = ConversionUtilsG.isAssignable(currentType, newType);
	boolean isAssignable = ConversionUtils.isAssignable(newType, currentType);
	// If is assignable, there was no type promotion
	if (isAssignable) {
	    return false;
	}

	// Warn the user
	String msg = "Variable '" + variableName + "' in function '" + data.getScope()
		+ "' type was initially inferred to" + " be of type '" + currentType + "', but now it needs to be a '"
		+ newType + "'.\n" + "The initial type will be maintained. If you want this message to disappear, "
		+ "define the type of variable in an aspect file.";

	SpecsLogs.warn(msg);

	return true;
    }

}
