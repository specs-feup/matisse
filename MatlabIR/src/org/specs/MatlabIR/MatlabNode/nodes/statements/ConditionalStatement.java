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

import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * Statements which have conditional expressions (if, elseif, while).
 * 
 * <p>
 * TODO: Check if we should add for, because of WholeCodeRules.correctConditionals <br>
 * TODO: Switch also?
 * 
 * @author JoaoBispo
 *
 */
public interface ConditionalStatement {

    /**
     * EXPR: Replace with getExpressionNormalized, after ExpressionNode is removed from parsed tree
     * 
     * @return
     */
    MatlabNode getExpression();

    default MatlabNode getExpressionNormalized() {
	return getExpression().normalizeExpr();
    }
}
