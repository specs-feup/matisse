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

package org.specs.MatlabIR.MatlabNode.nodes.statements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;

import com.google.common.base.Preconditions;

/**
 * Represents a nested function. All children are of type 'statement'.
 * 
 * <p>
 * Content is a StatementData, corresponding to the end statement that finishes the nested function.
 * 
 * @author JoaoBispo
 *
 */
public class NestedFunctionSt extends BlockSt {

    NestedFunctionSt(int lineNumber, Collection<StatementNode> children) {
        super(new StatementData(lineNumber, true), new ArrayList<>(children));

        // First child must be a FunctionDeclarationSt
        Preconditions.checkArgument(children.stream().findFirst().get() instanceof FunctionDeclarationSt);
    }

    private NestedFunctionSt(StatementData data, Collection<StatementNode> children) {
        super(data, children);
    }

    @Override
    protected MatlabNode copyPrivate() {
        return new NestedFunctionSt(getData(), Collections.emptyList());
    }

    public FunctionDeclarationSt getDeclarationNode() {
        // First child must be a FunctionDeclarationSt
        return getChild(FunctionDeclarationSt.class, 0);
    }

    public String getFunctionName() {
        return getDeclarationNode().getNameNode().getName();
    }

    /**
     * The name of nested functions are prefixed with a '#'
     * 
     * @return
     */
    public List<String> getScope() {
        List<String> scope = new ArrayList<>();

        // Add name of self
        scope.add("#" + getFunctionName());

        // Travel upwards, store names of functions if finds (FunctionNode, NestedFunctionSt)
        MatlabNode currentNode = this;
        while (currentNode.hasParent()) {
            currentNode = currentNode.getParent();

            if (currentNode instanceof FunctionNode) {
                scope.add(((FunctionNode) currentNode).getFunctionName());
                continue;
            }

            if (currentNode instanceof NestedFunctionSt) {
                scope.add("#" + ((NestedFunctionSt) currentNode).getFunctionName());
                continue;
            }
        }

        // Reverse order
        Collections.reverse(scope);

        return scope;
    }
}
