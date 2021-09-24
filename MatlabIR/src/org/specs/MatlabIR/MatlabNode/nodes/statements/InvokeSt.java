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
import org.specs.MatlabIR.MatlabNode.nodes.core.InvokeNode;

/**
 * A Command.
 * 
 * @author JoaoBispo
 *
 */
public class InvokeSt extends StatementNode {

    /**
     * TOM compatibility.
     * 
     * @param data
     * @param children
     */
    InvokeSt(StatementData data, List<MatlabNode> children) {
	this(data.getLine(), data.isDisplay(), (InvokeNode) children.get(0));
    }

    private InvokeSt(int lineNumber, boolean display, List<MatlabNode> children) {
	super(new StatementData(lineNumber, display), children);
    }

    InvokeSt(int lineNumber, boolean display, InvokeNode invoke) {
	this(lineNumber, display, Arrays.asList(invoke));
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new InvokeSt(getData().getLine(), getData().isDisplay(), Collections.emptyList());
    }

    /**
     * 
     * @return the Command Node
     */
    private InvokeNode getInvokeNode() {
	return getChild(InvokeNode.class, 0);
    }

    /**
     * 
     * @return the name of the command.
     */
    public String getCommand() {
	return getInvokeNode().getCommand();
    }

    @Override
    public String getStatementCode() {
	return getInvokeNode().getCode();
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
