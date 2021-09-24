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
import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ExpressionNode;

import com.google.common.base.Preconditions;

/**
 * An accessCall, which can be an index access to a variable, or a function call (ex.: a(3)). The two cases are
 * ambiguous until runtime.
 * 
 * @author JoaoBispo
 *
 */
public abstract class AccessCallNode extends MatlabNode {

    public AccessCallNode(Object content, Collection<? extends MatlabNode> children) {
	super(children);
    }

    public MatlabNode getNameNode() {
	return getChild(0);
    }

    public abstract List<MatlabNode> getArguments();

    public int getNumArguments() {
	return getArguments().size();
    }

    /**
     * The variable name this accessCall refers to.
     * 
     * <p>
     * In case of a composite access call (e.g., x.y(1).z()) returns the name of the variable associated with this
     * access call (in this case, z).
     * 
     * @return
     */
    public abstract String getName();

    /**
     * Returns True if the access call has an argument of the given class.
     * 
     * <p>
     * Bypasses ExpressionNode nodes.
     * 
     * @param argumentClass
     * @return true if the access call has an argument of the given class
     */
    public <M extends MatlabNode> boolean hasArgument(Class<M> argumentClass) {
	Preconditions.checkArgument(!argumentClass.equals(ExpressionNode.class), "Method bypasses ExpressionNodes");

	return getArguments().stream()
		.map(argument -> argument.normalizeExpr())
		.filter(matlabNode -> argumentClass.isInstance(matlabNode))
		.findFirst()
		.isPresent();
    }
}