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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;

/**
 * Represents a list of function inputs.
 * 
 * <p>
 * Children are Identifiers that represent the inputs. Can have no children.
 * 
 * @author JoaoBispo
 *
 */
public class FunctionInputsNode extends MatlabNode {

    FunctionInputsNode(Collection<MatlabNode> children) {
	super(children);
    }

    FunctionInputsNode(Object content, Collection<MatlabNode> children) {
	this(children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new FunctionInputsNode(Collections.emptyList());
    }

    public int getNumInputs() {
	return getNumChildren();
    }

    public List<IdentifierNode> getNamesNodes() {
	List<IdentifierNode> ids = getChildren(IdentifierNode.class);

	if (ids.size() != getNumChildren()) {
	    throw new RuntimeException("FunctionInputs has inputs that are not Identifiers:\n" + this);
	}

	return ids;
    }

    public List<String> getNames() {
	return getNamesNodes().stream()
		.map(id -> id.getName())
		.collect(Collectors.toList());
    }

    /**
     * Gets the list of names, except that unused names use the specified name.
     */
    public List<String> getNames(String placeholderForUnused) {
	List<MatlabNode> children = getChildren();
	List<String> names = new ArrayList<>();

	for (MatlabNode child : children) {
	    if (child instanceof IdentifierNode) {
		names.add(((IdentifierNode) child).getName());
	    } else {
		names.add(placeholderForUnused);
	    }
	}

	return names;
    }

    @Override
    public String getCode() {
	StringJoiner joiner = new StringJoiner(", ", "(", ")");

	getChildren().forEach(child -> joiner.add(child.getCode()));

	return joiner.toString();
    }

    /**
     * Removes an input by name.
     * 
     * @param inputName
     */
    public void removeInput(String varName) {
	MatlabNodeIterator iterator = getChildrenIterator();

	while (iterator.hasNext()) {
	    MatlabNode node = iterator.next();

	    assert node instanceof IdentifierNode;

	    String inputName = ((IdentifierNode) node).getName();
	    if (!inputName.equals(varName)) {
		continue;
	    }

	    // Found input, remove it
	    iterator.remove();
	    return;
	}

	throw new RuntimeException("There is no function input with name '" + varName + "'");
    }

    /**
     * Even if it has no children, should return empty.
     */
    @Override
    public String toContentString() {
	return "";
    }
}
