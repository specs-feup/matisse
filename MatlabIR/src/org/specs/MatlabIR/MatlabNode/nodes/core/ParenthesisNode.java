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
import java.util.Collections;
import java.util.StringJoiner;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * Parentheses are used to indicate precedence in arithmetic expressions in the usual way. They are used to enclose
 * arguments of functions in the usual way. They are also used to enclose subscripts of vectors and matrices in a manner
 * somewhat more general than usual. If X and V are vectors, then X(V) is [X(V(1)), X(V(2)), ..., X(V(n))]. The
 * components of V must be integers to be used as subscripts. An error occurs if any such subscript is less than 1 or
 * greater than the size of X.
 * 
 * <p>
 * Each children token of parenthesis is considered as a single, separate element. E.g., (1, 2, 3)
 * 
 * @author JoaoBispo
 *
 */
public class ParenthesisNode extends MatlabNode {

    ParenthesisNode(Collection<MatlabNode> children) {
	super(children);
    }

    ParenthesisNode(Object content, Collection<MatlabNode> children) {
	this(children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new ParenthesisNode(Collections.emptyList());
    }

    @Override
    public String getCode() {
	StringJoiner joiner = new StringJoiner(", ", "(", ")");

	getChildren().forEach(child -> joiner.add(child.getCode()));

	return joiner.toString();
    }
}
