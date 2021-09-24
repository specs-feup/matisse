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

import org.specs.MatlabIR.Exceptions.MatlabNodeException;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SpaceNode;

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
public class MatrixNode extends MatlabNode {

    MatrixNode(Collection<MatlabNode> children) {
	super(children);
    }

    MatrixNode(Object content, Collection<MatlabNode> children) {
	this(children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new MatrixNode(Collections.emptyList());
    }

    public List<RowNode> getRows() {
	// The SpaceNode still exists in some matrices when this method is called
	// during the tokenizer stage.
	return getChildrenAll(RowNode.class, SpaceNode.class);
    }

    /**
     * Returns the row of a single-row matrix.
     * 
     * @param matrix
     * @return
     */
    public RowNode getSingleRow() {
	List<RowNode> rows = getRows();

	// Check that it only has one row
	if (rows.size() != 1) {
	    throw new MatlabNodeException("Expected matrix to have a single row", this);
	}

	return rows.get(0);
    }

    @Override
    public String getCode() {
	if (!hasChildren()) {
	    return "[]";

	}

	StringJoiner joiner = new StringJoiner("; ", "[", "]");
	getChildren().forEach(child -> joiner.add(child.getCode()));

	return joiner.toString();
    }
}
