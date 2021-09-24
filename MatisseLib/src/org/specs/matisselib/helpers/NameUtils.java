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

package org.specs.matisselib.helpers;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CellAccessNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CellNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNumberNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabCharArrayNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ParenthesisNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.SimpleAccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ExpressionNode;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.SpecsLogs;

public class NameUtils {
    private NameUtils() {
    }

    public static String getSuggestedName(String variableName) {
        Preconditions.checkArgument(variableName != null);
        Preconditions.checkArgument(!variableName.isEmpty());
        Preconditions.checkArgument(variableName.contains("$"));

        if (variableName.startsWith("$")) {
            int end = variableName.indexOf('$', 1);
            if (end < 0) {
                end = variableName.length();
            }
            return variableName.substring(1, end);
        }

        return variableName.substring(0, variableName.indexOf('$'));
    }

    public static String getSuggestedName(MatlabNode node) {
        while (node instanceof ExpressionNode || node instanceof ParenthesisNode) {
            node = node.getChild(0);
        }

        if (node instanceof SimpleAccessCallNode) {
            return ((SimpleAccessCallNode) node).getName();
        }

        if (node instanceof CellAccessNode) {
            return getSuggestedName(((CellAccessNode) node).getLeft()) + "_value";
        }

        if (node instanceof IdentifierNode) {
            return ((IdentifierNode) node).getName();
        }
        if (node instanceof MatlabCharArrayNode) {
            return "string";
        }

        if (node instanceof MatlabNumberNode) {
            return "number";
        }

        if (node instanceof CellNode) {
            return "cell";
        }

        if (node instanceof OperatorNode) {
            OperatorNode op = (OperatorNode) node;

            switch (op.getOp()) {
            case ShortCircuitAnd:
                return "and";
            case ShortCircuitOr:
                return "or";
            default:
                // Unexpected
                return "operator";
            }
        }

        SpecsLogs.warn("suggested_name of type " + node.getClass().getName());

        // TODO
        return "";
    }
}
