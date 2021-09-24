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

import java.util.Collection;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;

public abstract class LoopSt extends StatementNode {

    protected LoopSt(StatementData data, Collection<? extends MatlabNode> children) {
	super(data, children);
    }

    /**
     * EXPR: Remove and replace with 'getExpressionNormalized' after removing ExpressionNode from parsed tree.
     * 
     * @return
     */
    public abstract MatlabNode getExpression();

    /**
     * Helper method which ensures the returned node is not an ExpressionNode
     * 
     * @return
     */
    public MatlabNode getExpressionNormalized() {
	return getExpression().normalizeExpr();
    }
}
