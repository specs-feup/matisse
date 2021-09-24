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

package org.specs.MatlabIR.MatlabNode.nodes.core;

import java.util.Collection;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * Used to indicate a variable in a function declaration or output that is not used. Consider these two cases:<br/>
 * 
 * Case 1:
 * 
 * <pre>
 * function [x, y] = X(a, ~, b)
 *    x = a;
 *    y = b;
 * end
 * </pre>
 * 
 * Case 2:
 * 
 * <pre>
 * [~, a] = X(1, 2, 3);
 * </pre>
 * 
 * @author Luís Reis
 */
public class UnusedVariableNode extends MatlabNode {
    UnusedVariableNode() {
    }

    UnusedVariableNode(Object content, Collection<MatlabNode> children) {
	this();
    }

    @Override
    protected UnusedVariableNode copyPrivate() {
	return new UnusedVariableNode();
    }

    @Override
    public String getCode() {
	return "~";
    }
}
