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

package org.specs.CIR.Tree.CToCRules;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.CNumberNode;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.TypesOld.CNumber;
import org.specs.CIRTypes.Language.CLiteral;

/**
 * @author Joao Bispo
 * 
 */
public class CToCRulesUtils {

    /**
     * @param rightHand
     * @param numericType
     */
    // public static void replaceLiteralTypes(CToken token, NumericType numericType) {
    public static void replaceLiteralTypes(CNode token, VariableType numericType) {
	// If no children, return
	if (token.getNumChildren() == 0) {
	    return;
	}

	// If function and maintain function types is enable, stop
	if (token instanceof FunctionCallNode) {
	    if (((FunctionCallNode) token).getFunctionInstance().maintainLiteralTypes()) {
		// if (CTokenContent.getFunctionInstance(token).maintainLiteralTypes()) {
		return;
	    }
	}

	// Check all children. If children is CNumber, replace it.
	for (int i = 0; i < token.getNumChildren(); i++) {
	    CNode child = token.getChildren().get(i);

	    // If CNumber, replace type
	    if (child instanceof CNumberNode) {
		CNode newCToken = newCNumber((CNumberNode) child, numericType);
		token.setChild(i, newCToken);
		continue;
	    }

	    // Call the function recursively
	    replaceLiteralTypes(child, numericType);
	}

    }

    public static CNode newCNumber(CNumberNode cNumber, VariableType numericType) {
	CNumber number = cNumber.getCNumber();
	CNumber newCNumber = CLiteral.newInstance(number, ScalarUtils.toScalar(numericType));
	return CNodeFactory.newCNumber(newCNumber);
    }

    /**
     * Returns the first CNumber whose String representation is the same as the given number.
     * 
     * @param inputs
     * @param number
     * @return
     */
    /*
    public static Integer getFirstCNumber(List<CNode> inputs, Number number) {
    for (int i = 0; i < inputs.size(); i++) {
        CNode child = inputs.get(i);
    
        // Get a CNumber, bypassing parenthesis if needed
        Optional<CNumberNode> cnumberNode = child.cast(CNumberNode.class);
        // child = CNodeUtils.getToken(child, CNodeType.CNumber);
        // if (child == null) {
        if (!cnumberNode.isPresent()) {
    	continue;
        }
    
        // Get Number
        // CNumber cnumber = CTokenContent.getCNumber(child);
        CNumber cnumber = cnumberNode.get().getCNumber();
    
        // If the numbers are not the "same", continue
        System.out.println("TESTING " + cnumber.getNumber().toString() + " and " + number.toString());
    
        if (!cnumber.getNumber().toString().equals(number.toString())) {
    	continue;
        }
    
        // Found match
        return i;
    }
    
    // Could not find corresponding CNumber
    return null;
    }
    */
}
