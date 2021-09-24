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
import java.util.List;
import java.util.StringJoiner;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeUtils;

/**
 * Represents a list of outputs.
 * 
 * <p>
 * Children represent the outputs, usually are Identifiers.
 * 
 * @author JoaoBispo
 *
 */
public class OutputsNode extends MatlabNode {

    OutputsNode(Collection<MatlabNode> children) {
	super(children);
    }

    OutputsNode(Object content, Collection<MatlabNode> children) {
	this(children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new OutputsNode(Collections.emptyList());
    }

    /**
     * 
     * @return a list of Strings with the names of the outputs
     */
    public List<String> getNames() {
	// Using MatlabNodeUtils.getNames, since an OutputsNode can have other nodes beside identifiers, when used as a
	// return of an assignment
	return MatlabNodeUtils.getVariableNames(this);
    }

    public List<MatlabNode> getNodes() {
	return getChildren();
    }

    @Override
    public String getCode() {
	StringJoiner joiner = new StringJoiner(", ", "[", "]");

	getChildren().forEach(child -> joiner.add(child.getCode()));

	return joiner.toString();
    }

    /**
     * Even if it has no children, should return empty.
     */
    @Override
    public String toContentString() {
	return "";
    }

}
