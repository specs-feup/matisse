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

package org.specs.MatlabToC.CodeBuilder.MatlabToCRules.Assignment;

import java.util.List;

import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Utilities.CirBuilder;
import org.specs.CIRFunctions.MatrixFunction;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndex;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.ArrayIndex.ArrayIndexUtils;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public class SetWithColon extends CirBuilder {

    private final MatlabToCFunctionData data;

    /**
     * @param data
     */
    public SetWithColon(MatlabToCFunctionData data) {
	super(data.getProviderData());
	this.data = data;

    }

    /**
     * Returns a CToken of type Block, with instructions for implementing MATLAB code of the type:<br>
     * A(n:m, :, :) = B;
     * 
     * @param accessCall
     * @param identifier
     * @return
     */
    public CNode newInstance(AccessCallNode accessCall, IdentifierNode identifier) {
	// Get name
	String accessCallName = accessCall.getName();

	// Get identifier
	String rightHandId = identifier.getName();

	// Build array C variable
	VariableType arrayType = data.getVariableType(accessCallName);
	CNode arrayVar = CNodeFactory.newVariable(accessCallName, arrayType);

	// Build identifier C variable
	VariableType idType = data.getVariableType(rightHandId);
	CNode identifierVar = CNodeFactory.newVariable(rightHandId, idType);

	// Get indexes
	List<MatlabNode> mIndexes = accessCall.getArguments();
	List<ArrayIndex> arrayIndexes = null;
	try {
	    arrayIndexes = new ArrayIndexUtils(data).getArrayIndexes(arrayVar, mIndexes);
	} catch (Exception e) {
	    SpecsLogs.warn("!Possible optimization:" + e.getMessage(), e);
	    return null;
	}

	// Instructions for block
	List<CNode> insts = SpecsFactory.newArrayList();

	// Comment with original instruction
	String originalInst = accessCall.getCode() + " = " + identifier.getCode();

	insts.add(CNodeFactory.newComment("ORIGINAL MATLAB: " + originalInst));

	// Counter token
	CNode counterVar = newCounterVar();
	CNode counterReset = CNodeFactory.newAssignment(counterVar, CNodeFactory.newCNumber(0));
	insts.add(counterReset);

	// / Inner loop assignment

	// Identifier Get (simple counter)
	CNode idGet = getFunctionCall(MatrixFunction.GET, identifierVar, counterVar);

	// Matrix variable Set
	List<CNode> setArguments = SpecsFactory.newArrayList();
	setArguments.add(arrayVar);

	for (ArrayIndex arrayIndex : arrayIndexes) {
	    setArguments.add(arrayIndex.getIndex());
	}

	setArguments.add(idGet);

	CNode matrixSet = getFunctionCall(MatrixFunction.SET, setArguments);

	// Increment counter
	CNode addition = getFunctionCall(COperator.Addition, counterVar, CNodeFactory.newCNumber(1));
	CNode incrementCounter = CNodeFactory.newAssignment(counterVar, addition);

	CNode innerLoop = CNodeFactory.newBlock(matrixSet, incrementCounter);
	CNode fors = new ArrayIndexUtils(data).buildFors(arrayIndexes, innerLoop);
	insts.add(fors);

	// Add empty space
	insts.add(CNodeFactory.newLiteral(" "));

	return CNodeFactory.newBlock(insts);
    }

    /**
     * @return
     */
    private CNode newCounterVar() {
	String counterName = "matisse_counter";
	CNode counterVar = CNodeFactory.newVariable(counterName, getNumerics().newInt());
	return counterVar;
    }

}
