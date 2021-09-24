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

import org.specs.MatlabIR.MatlabNode.MatlabNode;

import com.google.common.collect.Lists;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.exceptions.WrongClassException;

/**
 * An accessCall, which can be an index access to a variable, or a function call (ex.: a(3)). The two cases are
 * ambiguous until runtime.
 * 
 * <p>
 * The first child is always an Identifier. The remaining children represent the expression inside the parenthesis that
 * are separated by commas.
 * 
 * @author JoaoBispo
 *
 */
public class SimpleAccessCallNode extends AccessCallNode {

    private SimpleAccessCallNode(Collection<? extends MatlabNode> children) {
	super(null, children);
    }

    SimpleAccessCallNode(Object content, Collection<MatlabNode> children) {
	this(children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new SimpleAccessCallNode(Collections.emptyList());
    }

    SimpleAccessCallNode(String name, List<? extends MatlabNode> arguments) {
	this(buildChildren(name, arguments));
    }

    SimpleAccessCallNode(MatlabNode left, List<MatlabNode> arguments) {
	this(SpecsCollections.add(Lists.newArrayList(left), arguments));
    }

    private static List<MatlabNode> buildChildren(String name, List<? extends MatlabNode> parameters) {
	List<MatlabNode> children = new ArrayList<>();

	children.add(MatlabNodeFactory.newIdentifier(name));
	children.addAll(parameters);

	return children;
    }

    /*
    @Override
    public IdentifierNode getLeftNode() {
    return (IdentifierNode) super.getLeftNode();
    }
    */

    @Override
    public List<MatlabNode> getArguments() {
	return getChildren().subList(1, getNumChildren());
    }

    /**
     * 
     * @return a String with the name of the AccessCall, if the access call as an IdentifierNode as the left element.
     *         Otherwise, throws an exception
     */
    @Override
    public String getName() {
	// return getLeftNode().getName();
	return getChild(IdentifierNode.class, 0).getName();
	/*
	MatlabNode leftNode = getLeftNode();
	if (!(leftNode instanceof IdentifierNode)) {
	    throw new MatlabNodeException("Expected an IdentifierNode", leftNode);
	}
	return ((IdentifierNode) getLeftNode()).getName();
	*/
    }

    @Override
    public IdentifierNode getNameNode() {
	MatlabNode nameNode = super.getNameNode();

	if (!(nameNode instanceof IdentifierNode)) {
	    throw new WrongClassException(nameNode, IdentifierNode.class);
	}

	return (IdentifierNode) nameNode;
    }

    @Override
    public String getCode() {
	return getName() + joinCode(", ", "(", ")", getArguments());
    }

    public IdentifierNode getIdentifier() {
	return getChild(IdentifierNode.class, 0);
    }
}
