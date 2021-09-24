/**
 * Copyright 2015 SPeCS.
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

package org.specs.MatlabToC.CodeBuilder.MatToMatRules;

import java.util.List;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.Processor.TreeTransformException;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.Outlinable.OutlinableMap;
import org.specs.MatlabToC.jOptions.MatlabToCKeys;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.treenode.NodeInsertUtils;

public class FunctionCallOutliner implements MatlabToMatlabRule {

    private static final OutlinableMap DEFAULT_RULES;

    static {
        DEFAULT_RULES = new OutlinableMap();
        FunctionCallOutliner.DEFAULT_RULES.addRule("sum", FunctionCallOutliner::sumRule);
    }

    public static OutlinableMap getDefaultRules() {
        return FunctionCallOutliner.DEFAULT_RULES;
    }

    private static boolean sumRule(AccessCallNode accessCall, MatlabToCFunctionData data) {
        List<MatlabNode> args = accessCall.getArguments();

        // Check if sum has only one input
        if (args.size() != 1) {
            return false;
        }

        // Check if arg is a variable
        MatlabNode arg = args.get(0);
        if (!(arg instanceof IdentifierNode)) {
            return false;
        }

        String idName = ((IdentifierNode) arg).getName();
        if (data.isFunctionCall(idName)) {
            return false;
        }

        // Check if it is a vector matrix
        VariableType inputType = data.getVariableType(idName);

        if (!(inputType instanceof MatrixType)) {
            return false;
        }

        if (((MatrixType) inputType).getTypeShape().getNumDims() != 1) {
            return false;
        }

        return true;
    }

    @Override
    public boolean check(MatlabNode token, MatlabToCFunctionData data) {

        // Check if assignment
        if (!(token instanceof AssignmentSt)) {
            return false;
        }
        return true;
    }

    @Override
    public List<MatlabNode> apply(MatlabNode node, MatlabToCFunctionData data) throws TreeTransformException {

        // Check if on the right side there are function call that can be outlined
        AssignmentSt assign = (AssignmentSt) node;

        // Start list of statements
        List<MatlabNode> statements = SpecsFactory.newArrayList();

        List<AccessCallNode> accessCalls = assign.getRightHand().getDescendants(AccessCallNode.class);

        for (AccessCallNode accessCall : accessCalls) {

            if (!data.isFunctionCall(accessCall.getName())) {
                continue;
            }

            if (!MatlabToCKeys.isOutlinable(data.getSettings(), accessCall, data)) {
                continue;
            }

            // Outline access call

            String tempName = data.nextTempVarName();

            // Get temporary identifier to replace the access call
            MatlabNode tempVar = MatlabNodeFactory.newIdentifier(tempName);
            // Replace access call
            NodeInsertUtils.replace(accessCall, tempVar);

            // Create 'temp = accessCall'
            MatlabNode tempVar2 = MatlabNodeFactory.newIdentifier(tempName);
            MatlabNode newAssign = StatementFactory.newAssignment(assign.getData(), tempVar2, accessCall);

            // Add to statements
            statements.add(newAssign);

            // IMPORTANT: We are modifying the MATLAB tree, we have to add the new statement
            NodeInsertUtils.insertBefore(node, newAssign);
        }

        // Add original statement
        statements.add(node);

        return statements;
    }

}
