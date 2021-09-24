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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.StringJoiner;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * Field access. S(m).f when S is a structure, accesses the contents of field f of that structure.
 * 
 * E.g., a.b.c(); a.b;
 * 
 * <p>
 * Each child is an element of the fieldAccess, separated by a dot in the MATLAB source.
 * 
 * @author JoaoBispo
 *
 */
public class FieldAccessNode extends MatlabNode {

    // FieldAccessNode(Collection<MatlabNode> children) {
    FieldAccessNode(MatlabNode leftNode, MatlabNode rightNode) {
	this(Arrays.asList(leftNode, rightNode));
    }

    private FieldAccessNode(Collection<MatlabNode> children) {
	super(children);
    }

    FieldAccessNode(Object content, Collection<MatlabNode> children) {
	this(children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new FieldAccessNode(Collections.emptyList());
    }

    @Override
    public String getCode() {
	StringJoiner joiner = new StringJoiner(".");

	getChildren().forEach(child -> joiner.add(child.getCode()));

	return joiner.toString();
    }

    public MatlabNode getLeft() {
	return getChild(0);
    }

    public MatlabNode getRight() {
	return getChild(0);
    }

}
