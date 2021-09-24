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

package org.specs.CIRFunctions.Utilities;

import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Utilities.CirBuilder;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public class TransformationUtils extends CirBuilder {

    /**
     * @param data
     */
    public TransformationUtils(ProviderData data) {
	super(data);
    }

    /**
     * Transforms a matrix assignment into a matrix copy.
     * 
     * @param rightHandExpression
     * @param rightHandType
     * @param cLeftHand
     * @param data
     * @return
     */
    public CNode parseMatrixCopy(CNode rightHandExpression, CNode cLeftHand) {

	// Get input variable from right hand
	// CToken inputVar = CTokenUtils.getToken(rightHandExpression, CTokenType.Variable);

	// Get variable in left hand
	// CToken outputVar = CTokenUtils.getToken(cLeftHand, CTokenType.Variable);

	// Get general function arguments
	// List<CToken> inputVars = Arrays.asList(inputVar, outputVar);
	List<CNode> inputVars = SpecsFactory.newArrayList();
	// inputVars.add(inputVar);
	inputVars.add(rightHandExpression);
	// inputVars.add(outputVar);
	inputVars.add(cLeftHand);

	// Create function call
	List<VariableType> inputTypes = CNodeUtils.getVariableTypes(inputVars);
	MatrixType matrixType = (MatrixType) inputTypes.get(0);

	InstanceProvider copyProvider = matrixType.matrix().functions().copy();
	CNode copyCall = getFunctionCall(copyProvider, inputVars);

	return CNodeFactory.newInstruction(InstructionType.FunctionCall, copyCall);
    }

    /**
     * Builds a function call which casts the given token of type numeric, to another numeric type.
     * 
     * @param offset
     * @return
     */
    /*
    public static CToken castNumeric(CToken numericToken, VariableType outputType) {

    VariableType tokenType = DiscoveryUtils.getVarType(numericToken);

    if (!TypeVerification.areOfType(CType.Numeric, tokenType, outputType)) {
        throw new RuntimeException("Given inputs, token (" + tokenType
    	    + ") and numericOutput (" + outputType + ") are not of numeric type");
    }

    // if (tokenType.getType() != CType.Numeric) {
    // throw new RuntimeException("Given token is not of numeric type (" + tokenType + ")");
    // }

    NumericType inputNumericType = VariableTypeUtils.getNumericType(tokenType);
    NumericType outputNumericType = VariableTypeUtils.getNumericType(outputType);
    //	NumericType numericType = VariableTypeUtils.getNumericType(tokenType);

    // Check if it is already of the same type of the output type
    if (inputNumericType == outputNumericType) {
        return numericToken;
    }

    // Create cast function
    //VariableType intType = VariableTypeFactory.newInt();
    //FunctionInstance castInst = UtilityProvider.CAST_NUMERIC.getInstance(intType);
    FunctionInstance castInst = UtilityProvider.CAST_NUMERIC.getInstance(tokenType, outputType);

    // Cast function is inlined, will not create second variable.
    //CToken dummyVar = CTokenFactory.newVariable("_dummy_var", outputType);
    
    return FunctionUtils.getFunctionCall(castInst, numericToken);
    //return FunctionUtils.getFunctionCall(castInst, numericToken, dummyVar);
    }
    */
}
