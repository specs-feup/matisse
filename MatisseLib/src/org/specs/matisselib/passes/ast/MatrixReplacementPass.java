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

package org.specs.matisselib.passes.ast;

import java.util.ArrayList;
import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatrixNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.RowNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabIR.MatlabNodePass.AMatlabNodePass;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

/**
 * A pass that converts operators such as "*" and "-" into their function counterparts.
 * <p>
 * This pass does NOT replace '&&' nor '||', since there is no function with the exact same behavior.
 * 
 * @author luiscubal
 *
 */
public class MatrixReplacementPass extends AMatlabNodePass {

    @Override
    public MatlabNode apply(MatlabNode rootNode, DataStore data) {
        Preconditions.checkArgument(rootNode != null);
        Preconditions.checkArgument(data != null);

        MatlabNodeIterator it = rootNode.getChildrenIterator();
        while (it.hasNext()) {
            it.set(apply(it.next(), data));
        }

        if (rootNode instanceof MatrixNode) {

            MatrixNode matrixNode = (MatrixNode) rootNode;

            List<RowNode> rows = matrixNode.getRows();

            if (rows.isEmpty()) {
                MatlabNode parent = rootNode.getParent();
                // In A = [];, don't convert [] into anything else.
                if (parent instanceof AssignmentSt) {
                    return rootNode;
                }
            }

            List<MatlabNode> vertcatArguments = new ArrayList<>();
            for (RowNode row : rows) {
                MatlabNode rowNode;
                // if (row.getChildren().size() == 0) {
                int numRows = row.getNumRows();
                if (numRows == 0) {
                    continue;
                } else if (numRows == 1) {
                    rowNode = row.getRow(0);
                } else {
                    rowNode = MatlabNodeFactory.newSimpleAccessCall("horzcat", row.getRows());
                }
                vertcatArguments.add(rowNode);
            }

            if (vertcatArguments.size() == 1) {
                return vertcatArguments.get(0);
            }
            return MatlabNodeFactory.newSimpleAccessCall("vertcat", vertcatArguments);
        }

        return rootNode;
    }

    @Override
    public String getName() {
        return "OperatorReplacementPass";
    }

}
