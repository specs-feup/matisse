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

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.NestedFunctionSt;

public class NestedFunctionAdapter implements FunctionAdapter {

    private final NestedFunctionSt node;

    public NestedFunctionAdapter(NestedFunctionSt node) {
        this.node = node;
    }

    @Override
    public MatlabNode getNode() {
        return node;
    }

    @Override
    public String getFunctionName() {
        return node.getFunctionName();
    }

    @Override
    public FunctionDeclarationSt getDeclarationNode() {
        return node.getDeclarationNode();
    }

    @Override
    public List<StatementNode> getBodyStatements() {
        return node.getBodyStatements();
    }

    @Override
    public List<String> getScope() {
        return node.getScope();
    }

}
