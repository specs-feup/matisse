/**
 * Copyright 2014 SPeCS.
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

package org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementRules;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.TokenRules;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementProcessor.MatlabToCException;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementProcessor.MatlabToCRule;
import org.specs.MatlabToC.Functions.MathFunctions.Static.sum.SumDecVectorInlinedInstance;

/**
 * 
 * @author Joao Bispo
 */
public class SumVectorScalarization implements MatlabToCRule {

    @Override
    public CNode apply(MatlabNode statement, MatlabToCFunctionData data) throws MatlabToCException {

        // List<String> scope = data.getScope();
        // List<String> scope = (List<String>) data.getSetup().getValue(MFunctionBuilder.getScopeOption(), List.class);

        // TypesMap typesMap = data.getSetup().getValue(MatisseOption.TYPE_DEFINITION, TypesMap.class);

        // System.out.println("EXEAMPLE:" + typesMap.getSymbols(scope));
        // System.out.println("TOKEN:\n" + token);

        // Get sum in statement

        // Check if assignment
        if (!(statement instanceof AssignmentSt)) {
            return null;
        }

        AssignmentSt assignment = (AssignmentSt) statement;

        // Check if there is a call to sum
        // MatlabNode rightHand = StatementAccess.getAssignmentRightHand(statement);
        MatlabNode rightHand = assignment.getRightHand();

        if (!(rightHand instanceof AccessCallNode)) {
            return null;
        }

        AccessCallNode sumCall = (AccessCallNode) rightHand;

        if (!(sumCall.getName().equals("sum"))) {
            return null;
        }

        // Should have only one argument
        if (sumCall.getArguments().size() != 1) {
            return null;
        }

        // Argument should be an identifier
        if (!(sumCall.getArguments().get(0) instanceof IdentifierNode)) {
            return null;
        }

        IdentifierNode vectorId = (IdentifierNode) sumCall.getArguments().get(0);

        MatlabNode leftHand = assignment.getLeftHand();
        if (!(leftHand instanceof IdentifierNode)) {
            return null;
        }

        IdentifierNode leftHandId = (IdentifierNode) leftHand;

        /*
        if (rightHand.getType() != MType.AccessCall) {
            return null;
        }
        
        // Check if is a sum function
        if (!MatlabTokenAccess.getAccessCallName(rightHand).equals("sum")) {
            return null;
        }
        */

        System.out.println("INLINING CALL TO SUM");

        // Translate argument
        CNode cSumArg = TokenRules.convertTokenExpr(vectorId, data);

        // System.out.println("ARG TYPE:" + cSumArg.getVariableType());
        // System.out.println("ARG CODE:" + cSumArg.getCode());

        // Manually add return type of result of sum
        ScalarType resultType = ScalarUtils.toScalar(cSumArg.getVariableType());
        if (resultType == null) {
            throw new RuntimeException("Could not convert type '" + cSumArg + "' to scalar");
        }
        data.addVariableType(leftHandId.getName(), resultType);

        CNodeFactory cFactory = new CNodeFactory(data.getSettings());
        FunctionCallNode inlinedSum = cFactory.newFunctionCall(
                providerData -> SumDecVectorInlinedInstance.newInstance(leftHandId.getName(), vectorId.getName(),
                        providerData),
                cSumArg);
        // System.out.println("CURRENT SCOPE: " + data.getScope());
        // Manually add variable types from inlined code
        for (Variable var : inlinedSum.getFunctionInstance().getCallVars()) {
            // if (data.hasType(var.getName())) {
            // continue;
            // }
            // System.out.println("VARIABLE:" + var);
            data.addVariableType(var.getName(), var.getType());
        }

        // System.out.println("CODE:" + inlinedSum.getCode());
        return inlinedSum;

    }

}
