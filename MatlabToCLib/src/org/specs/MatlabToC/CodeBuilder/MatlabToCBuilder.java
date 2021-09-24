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

import java.util.ArrayList;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.MatlabUnitNode;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementProcessor.MatlabToCException;
import org.specs.MatlabToC.MFunctions.MFunctionsUtils;

/**
 * Class which transforms a Matlab tree representation into an first-pass C tree representation.
 * 
 * <p>
 * Other post-processing operations are needed (such as adding a return statement), after building the C tree.
 * 
 * @author Joao Bispo
 * 
 */
public class MatlabToCBuilder {

    private final MatlabToCFunctionData data;
    private final CInstructionList instructionList;

    /**
     * 
     */
    private MatlabToCBuilder(MatlabToCFunctionData data) {
	this.data = data;

	instructionList = new CInstructionList();

    }

    /**
     * @param token
     */
    private void addStatement(StatementNode token) {

	CNode ctoken = CodeBuilderUtils.matlabToC(token, data);

	if (ctoken == null) {
	    return;
	}

	instructionList.addInstruction(ctoken, null);
    }

    /**
     * Accepts a Script/Function MATLAB token, and generates a list of C instructions.
     * 
     * @param rootToken
     * @param data
     * @return
     */
    public static CInstructionList build(MatlabUnitNode rootToken, MatlabToCFunctionData data) {

	// New builder
	MatlabToCBuilder builder = new MatlabToCBuilder(data);

	// Add statements and build first-pass C tree
	try {
	    // Creating a new list, because the MATLAB tree might be transformed during the MATLAB-to-C transformation
	    // and raise a co-modification exception
	    for (StatementNode token : new ArrayList<>(rootToken.getStatements())) {
		builder.addStatement(token);
	    }
	} catch (MatlabToCException e) {
	    throw e;
	} catch (Exception e) {
	    throw new MatlabToCException(e.getMessage() + "\nMATLAB function:\n"
		    + rootToken.getCode(), e, data);
	}

	// Get C function instructions
	CInstructionList instructionList = builder.buildInstructions();

	// Return C instruction list
	return instructionList;
    }

    /**
     * @return
     */
    private CInstructionList buildInstructions() {
	// Build input/output types
	FunctionType cTypeData = MFunctionsUtils.newFunctionTypes(data);
	instructionList.setFunctionTypes(cTypeData);

	// Add variable initializations (e.g., from MATLAB constants)
	instructionList.getInitializations().add(data.getInitializations());

	return instructionList;
    }

}
