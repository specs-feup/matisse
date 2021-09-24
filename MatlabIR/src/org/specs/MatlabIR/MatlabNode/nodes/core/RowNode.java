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

/**
 * A row of a SquareBrackets or of a Cell.
 * 
 * <p>
 * Each children of the row is considered a single, separate element
 * 
 * @author JoaoBispo
 *
 */
public class RowNode extends MatlabNode {

    RowNode(Collection<MatlabNode> children) {
	// super("", children);
	super(children);
    }
    //
    // RowNode(Object content, Collection<MatlabNode> children) {
    // this(children);
    // }

    @Override
    protected MatlabNode copyPrivate() {
	return new RowNode(Collections.emptyList());
    }

    @Override
    public String getCode() {
	if (!hasChildren()) {
	    return "";
	}

	StringJoiner joiner = new StringJoiner(", ");
	getChildren().forEach(child -> joiner.add(child.getCode()));

	return joiner.toString();
    }

    public int getNumRows() {
	return getNumChildren();
    }

    public List<MatlabNode> getRows() {
	return getChildren();
    }

    public MatlabNode getRow(int index) {
	return getChild(index);
    }
}
