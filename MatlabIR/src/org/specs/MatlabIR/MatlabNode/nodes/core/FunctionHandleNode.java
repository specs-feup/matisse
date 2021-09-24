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
 * Used to form vectors and matrices. [6.9 9.64 sqrt(-1)] is a vector with three elements. Is always used on the left
 * side of an assignment.
 * 
 * <p>
 * The children are always of type 'row'.
 * 
 * @author JoaoBispo
 *
 */
public class FunctionHandleNode extends MatlabNode {

    private final String identifier;

    FunctionHandleNode(String identifier) {
	this.identifier = identifier;
    }

    FunctionHandleNode(Object content, Collection<MatlabNode> children) {
	this(content.toString());
	// super(MType.FunctionHandle, null, children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new FunctionHandleNode(identifier);
    }

    public String getName() {
	return identifier;
    }

    @Override
    public String getCode() {
	return "@" + identifier;
    }
}
