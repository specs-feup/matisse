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

import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * A cell access (ex.: a{4}).
 * 
 * <p>
 * The first child is always an Identifier. Each remaining child is treated as a single expression inside the
 * parenthesis and is separated by commas.
 * 
 * @author JoaoBispo
 *
 */
public class CellAccessNode extends MatlabNode {

    private CellAccessNode(Collection<MatlabNode> children) {
	super(children);
    }

    CellAccessNode(Object content, Collection<MatlabNode> children) {
	this(children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new CellAccessNode(Collections.emptyList());
    }

    CellAccessNode(MatlabNode left, List<MatlabNode> arguments) {
	this(buildChildren(left, arguments));
    }

    private static Collection<MatlabNode> buildChildren(MatlabNode left, List<MatlabNode> elements) {
	List<MatlabNode> nameAndElements = new ArrayList<>();

	nameAndElements.add(left);
	nameAndElements.addAll(elements);

	return nameAndElements;
    }

    /*
    public IdentifierNode getNameNode() {
    return getChild(IdentifierNode.class, 0);
    }
    */

    public String getName() {
	return getChild(IdentifierNode.class, 0).getName();
    }

    public MatlabNode getLeft() {
	return getChild(0);
    }

    public List<MatlabNode> getArguments() {
	return getChildren().subList(1, getNumChildren());
    }

    @Override
    public String getCode() {
	StringBuilder builder = new StringBuilder();

	// First children is always an identifier
	// Example breaks rule:
	// file:/C:/Users/JoaoBispo/Dropbox/Code-Repositories/Classification/SOM%20MATLAB%20Toolbox/somtoolbox2_Mar_17_2005/somtoolbox/som_autolabel.m

	// builder.append(getName().getCode());
	builder.append(getChild(0).getCode());

	// Remaining children are inside round parenthesis and are
	// separated by commas
	StringJoiner joiner = new StringJoiner(", ", "{", "}");
	getArguments().forEach(arg -> joiner.add(arg.getCode()));
	builder.append(joiner.toString());

	return builder.toString();
    }
}
