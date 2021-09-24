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

package org.specs.CIR.Tree.CToCRules;

import java.util.List;
import java.util.Optional;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.TemporaryUtils;
import org.specs.CIR.Tree.CNodes.AssignmentNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.CNumberNode;
import org.specs.CIR.Tree.CNodes.FunctionInputsNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;

import pt.up.fe.specs.util.classmap.BiConsumerClassMap;
import pt.up.fe.specs.util.treenode.NodeInsertUtils;

/**
 * Rules for CToken to CToken transformations.
 * 
 * @author Joao Bispo
 * 
 *         TODO: Instead of a map, it should be a list of rules? Map may be faster? Map with list of rules, change rule
 *         to return type it applies to, build processor which accepts such rules
 */
public class CToCRules {

    // public static class CTransformation extends BiConsumerClassMap<CNode, CToCData> {
    // }
    //
    // private static final CTransformation C_TRANSFORMATIONS2;
    // static {
    // C_TRANSFORMATIONS2 = new CTransformation();
    // C_TRANSFORMATIONS2.put(VariableNode.class, CToCRules::updateOutputAsInput);
    // }

    private static final BiConsumerClassMap<CNode, CToCData> C_TRANSFORMATIONS;

    static {
        C_TRANSFORMATIONS = BiConsumerClassMap.newInstance(true);

        CToCRules.C_TRANSFORMATIONS.put(VariableNode.class, CToCRules::updateOutputAsInput);
        CToCRules.C_TRANSFORMATIONS.put(FunctionInputsNode.class, CToCRules::updateTemporaryNames);
        CToCRules.C_TRANSFORMATIONS.put(AssignmentNode.class, CToCRules::updateCNumbers);

    }

    /**
     * @return
     */
    /*
    private static CToCRule simplify() {
    return new CToCRule() {
    
        public void cToC(CToken token, CToCData data) {
    
    	// Get function name
    	FunctionInstance fInstance = CTokenContent.getFunctionInstance(token);
    	String functionName = fInstance.getCName();
    
    	List<CToken> inputs = CTokenAccess.getFunctionCallInputTokens(token);
    		
    	
    	// Check if addition
    	if (functionName.equals(COperator.Addition.name())) {
    
    	    // Check if any of the children is the number 0
    	    Integer index = CToCRulesUtils.getFirstCNumber(inputs, 0);
    	    if(index == null) {
    		return;
    	    }
    	    
    	    LoggingUtils.msgWarn("Simplifying Addition with 0: check results");
    	    // System.out.println("ADDITION:"+token.getChildren());
    	}
    
    	// Check if multiplication
    	if (functionName.equals(COperator.Multiplication.name())) {
    
    	    // Get index of the first children which is the number 1
    	    Integer index = CToCRulesUtils.getFirstCNumber(inputs, 1);
    	    if(index == null) {
    		return;
    	    }
    	    
    	    LoggingUtils.msgWarn("Simplifying Multiplication with 1: check results");
    	}
    
        }
    
    };
    }
    
    */

    /**
     * Adapts the CNumbers to the majority types of the expression.
     * 
     * @return
     */
    private static void updateCNumbers(AssignmentNode assignToken, CToCData data) {

        // Get return type of assignment
        // CToken leftHand = CTokenAccess.getAssignmentLeftHand(token);
        CNode leftHand = assignToken.getLeftHand();
        VariableType leftHandType = leftHand.getVariableType();

        if (!ScalarUtils.hasScalarType(leftHandType)) {
            return;
        }

        leftHandType = ScalarUtils.toScalar(leftHandType).normalize();

        // CToken rightHand = CTokenAccess.getAssignmentRightHand(assignToken);
        CNode rightHand = assignToken.getRightHand();

        // If right hand if a CNumber, replace it
        if (rightHand instanceof CNumberNode) {
            CNode newCToken = CToCRulesUtils.newCNumber((CNumberNode) rightHand, leftHandType);
            // CTokenAccess.setAssignmentRightHand(assignToken, newCToken);
            assignToken.setRightHand(newCToken);

            return;
        }

        // Replace numeric type in all constants of right hand
        CToCRulesUtils.replaceLiteralTypes(rightHand, ScalarUtils.removeConstant(leftHandType));

    }

    /**
     * Looks for temporary names and replaces them with unique identifiers.
     * 
     * @return
     */
    private static void updateTemporaryNames(FunctionInputsNode inputsToken, CToCData data) {

        // If snippet mode, ignore rule
        if (data.isSnippetMode()) {
            return;
        }

        // List<CToken> functionInputs = CTokenAccess.getFunctionInputs(inputsToken);
        List<CNode> functionInputs = inputsToken.getInputs();

        for (int i = 0; i < functionInputs.size(); i++) {
            CNode functionInput = functionInputs.get(i);
            // CNode variableToken = CNodeUtils.getToken(functionInput, CNodeType.Variable);
            Optional<VariableNode> variableToken = functionInput.cast(VariableNode.class);

            // If token is not a Variable, skip

            // if (variableToken == null) {
            if (!variableToken.isPresent()) {
                continue;
            }

            // If name of variable is not a temporary name, skip
            Variable variable = variableToken.get().getVariable();
            if (!TemporaryUtils.isTemporaryName(variable.getName())) {
                continue;
            }

            // Build the variable with the unique i
            String outputName = data.nextTemporaryName();
            CNode newVariable = CNodeFactory.newVariable(outputName, variable.getType());

            // Replace token
            // CTokenAccess.setFunctionInput(inputsToken, i, newVariable);
            inputsToken.setInput(i, newVariable);
        }

    }

    /**
     * Post-processes CTokens, bottom-up.
     * 
     * @param token
     * @return
     */
    public static void processCToken(CNode token, CToCData data) {

        // Apply rules to children
        for (CNode child : token.getChildren()) {
            processCToken(child, data);
        }

        // Apply rule to this token
        CToCRules.C_TRANSFORMATIONS.accept(token, data);

    }

    /**
     * Search variables and updates the type to pointer, if they are output as input and the type is not a pointer.
     * 
     * @return
     */
    private static void updateOutputAsInput(VariableNode token, CToCData data) {
        // Don't apply rule if it is not a function
        if (!data.isFunction()) {
            return;
        }

        Variable variable = token.getVariable();

        // Check if name is an output as input
        if (!data.isOutputAsInput(variable.getName())) {
            return;
        }

        // Create new variable
        Variable newVar = variable.getPointerType();
        NodeInsertUtils.replace(token, CNodeFactory.newVariable(newVar));
        // variable.convertToPointerType();
    }

    /**
     * If an expression starts with parenthesis, removes the parenthesis if there is only only child inside.
     * 
     * @return
     */
    /*
    private static CToCRule removeTopLevelParenthesis() {
    return new CToCRule() {
    
        public void cToC(CToken token, CToCData data) {
    
    	List<CToken> exprChildren = CTokenAccess.getExpressionChildren(token);
    
    	// Check if it has only one child
    	// if (token.numChildren() != 1) {
    	if (exprChildren.size() != 1) {
    	    return;
    	}
    
    	// Check if child is parenthesis
    	// CToken child = token.getChild(0);
    	CToken parenthesisChild = exprChildren.get(0);
    	// if(child.type != CTokenType.Parenthesis) {
    	if (parenthesisChild.getType() != CTokenType.Parenthesis) {
    	    return;
    	}
    
    	List<CToken> parChildren = CTokenAccess.getParenthesisChildren(parenthesisChild);
    
    	// Check if child has only one child
    	// if (parenthesisChild.numChildren() != 1) {
    	if (parChildren.size() != 1) {
    	    return;
    	}
    
    	// Replace parenthesis by parenthesis' child
    	token.setChild(0, parChildren.get(0));
        }
    
    };
    
    }
    */
}
