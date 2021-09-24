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

package org.specs.MatlabIR.MatlabNode.nodes.temporary;

import java.util.Collection;
import java.util.Collections;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * Represents a MatLab expression with operators.
 * 
 * <p>
 * After the tree is completely generated, always contains a child which represents an expression tree which respects
 * the MATLAB operator precedences.
 *
 * <p>
 * ATTENTION: In the future this node will become a temporary node and be removed from the final tree.
 * 
 * @author JoaoBispo
 *
 */
public class ExpressionNode extends MatlabNode {

    ExpressionNode(Collection<MatlabNode> children) {
	super(children);
    }

    public ExpressionNode(Object content, Collection<MatlabNode> children) {
	this(children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new ExpressionNode(Collections.emptyList());
    }

    /**
     * If this node has more than one child, will throw an exception.
     * 
     * @return the child of the node
     */
    public MatlabNode getSingleChild() {

	if (getNumChildren() != 1) {
	    throw new RuntimeException("Expression node has more than one child:\n"
		    + this);
	}

	return getChild(0);
    }

    @Override
    public String getCode() {
	StringBuilder builder = new StringBuilder();

	getChildren().forEach(child -> builder.append(child.getCode()));

	return builder.toString();
    }

    @Override
    public boolean isTemporary() {
	return true;
    }
}
