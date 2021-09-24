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

/**
 * A Matlab command (ex.: ls ./d).
 * 
 * <p>
 * Has two or more children. The first children is always an Identifier, the remaining children are Strings representing
 * arguments of the command.
 * 
 * @author JoaoBispo
 *
 */
public class CommandNode extends MatlabNode {

    private CommandNode(Collection<MatlabNode> children) {
	super(children);
    }

    CommandNode(Object content, Collection<MatlabNode> children) {
	this(children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new CommandNode(Collections.emptyList());
    }

    CommandNode(String command, List<String> arguments) {
	this(buildChildren(command, arguments));

    }

    private static Collection<MatlabNode> buildChildren(String command, List<String> arguments) {
	List<MatlabNode> children = new ArrayList<>();
	children.add(MatlabNodeFactory.newIdentifier(command));

	arguments.stream().forEach(arg -> children.add(MatlabNodeFactory.newCharArray(arg)));

	return children;
    }

    /**
     * 
     * @return the node representing the name of the command
     */
    public IdentifierNode getNameNode() {
	return getChild(IdentifierNode.class, 0);
    }

    /**
     * 
     * @return the name of the command
     */
    public String getName() {
	return getNameNode().getName();
    }

    public List<MatlabCharArrayNode> getArgumentNodes() {
	List<MatlabCharArrayNode> arguments = getChildren(MatlabCharArrayNode.class);

	if (arguments.size() != getNumChildren() - 1) {
	    throw new RuntimeException("Command has children after identifier that are not Strings:\n" + this);
	}

	return arguments;
    }

    public List<String> getArguments() {
	return getArgumentNodes().stream()
		.map(arg -> arg.getString())
		.collect(Collectors.toList());
    }

    @Override
    public String getCode() {

	// Print children with a space inbetween
	// StringJoiner joiner = new StringJoiner("' '", " '", "'");
	StringJoiner joiner = new StringJoiner(" ");
	getArgumentNodes().forEach(arg -> joiner.add(arg.getCode()));
	// System.out.println("ARGS:" + getArgumentNodes());
	// System.out.println("JOINER:" + joiner.toString());
	return getName() + " " + joiner.toString();
    }
}
