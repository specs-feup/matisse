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

package org.specs.MatlabIR.MatlabNode.nodes.statements;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CommandNode;

/**
 * A Command.
 * 
 * @author JoaoBispo
 *
 */
public class CommandSt extends StatementNode {

    /**
     * TOM compatibility.
     * 
     * @param data
     * @param children
     */
    CommandSt(StatementData data, List<MatlabNode> children) {
	this(data.getLine(), data.isDisplay(), (CommandNode) children.get(0));
    }

    private CommandSt(int lineNumber, boolean display, List<MatlabNode> children) {
	super(new StatementData(lineNumber, display), children);
    }

    CommandSt(int lineNumber, boolean display, CommandNode comment) {
	this(lineNumber, display, Arrays.asList(comment));
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new CommandSt(getData().getLine(), getData().isDisplay(), Collections.emptyList());
    }

    /**
     * 
     * @return the Command Node
     */
    private CommandNode getCommandNode() {
	return getChild(CommandNode.class, 0);
    }

    /**
     * 
     * @return the name of the command.
     */
    public String getName() {
	return getCommandNode().getName();
    }

    /**
     * The arguments of the command.
     * 
     * @return
     */
    public List<String> getArguments() {
	return getCommandNode().getArguments();
    }

    @Override
    public String getStatementCode() {
	return getCommandNode().getCode();
	/*
	StringBuilder builder = new StringBuilder();
	
	builder.append(getName());
	
	// Print children with a space inbetween
	StringJoiner joiner = new StringJoiner("' '", " '", "'");
	getArguments().forEach(joiner::add);
	builder.append(joiner.toString());
	
	return builder.toString();
	*/
    }

}
