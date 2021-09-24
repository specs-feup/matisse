/**
 * Copyright 2014 SPeCS.
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

package org.specs.matisselib.passes.ast;

import java.util.List;
import java.util.stream.Collectors;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;
import org.specs.MatlabIR.MatlabNode.nodes.core.CommandNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.SimpleAccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AccessCallSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommandSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNodePass.APasses.AllStatementsPass;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class CommandReplacementPass extends AllStatementsPass {

    @Override
    protected void applyOnStatement(MatlabNodeIterator iterator, DataStore data) {
	MatlabNode statement = iterator.next();
	if (!(statement instanceof CommandSt)) {
	    return;
	}
	CommandSt command = (CommandSt) statement;

	CommandNode commandExpression = (CommandNode) command.getChild(0);

	String identifierName = commandExpression.getName();
	List<MatlabNode> arguments = commandExpression.getChildrenStream()
		.skip(1)
		.collect(Collectors.toList());

	SimpleAccessCallNode newExpression = MatlabNodeFactory.newSimpleAccessCall(identifierName,
		arguments);
	AccessCallSt newStatement = StatementFactory.newAccessCall(command.getLine(), command.isDisplay(),
		newExpression);

	iterator.set(newStatement);
    }
}
