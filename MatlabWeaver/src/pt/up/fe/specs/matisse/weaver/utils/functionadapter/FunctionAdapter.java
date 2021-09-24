/**
 * Copyright 2016 SPeCS.
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

package pt.up.fe.specs.matisse.weaver.utils.functionadapter;

import java.util.List;
import java.util.Optional;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;

/**
 * Retrieves information about a function node.
 * 
 * @author JoaoBispo
 *
 */
public interface FunctionAdapter {

    MatlabNode getNode();

    default Optional<FunctionNode> getFunctionNode() {
        MatlabNode node = getNode();

        if (node instanceof FunctionNode) {
            return Optional.of((FunctionNode) node);
        }

        return Optional.empty();
    }

    String getFunctionName();

    FunctionDeclarationSt getDeclarationNode();

    List<StatementNode> getBodyStatements();

    List<String> getScope();
}
