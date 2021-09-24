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

package org.specs.matisselib.passes.ast;

import java.util.ArrayList;
import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.statements.BlockSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNode.nodes.statements.WhileSt;
import org.specs.MatlabIR.MatlabNodePass.APasses.AllStatementsPass;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class WhileSimplifierPass extends AllStatementsPass {

    @Override
    protected void applyOnStatement(MatlabNodeIterator iterator, DataStore data) {
	MatlabNode stmt = iterator.next();
	if (!(stmt instanceof WhileSt)) {
	    return;
	}

	WhileSt whileSt = (WhileSt) stmt;
	MatlabNode condition = whileSt.getCondition();
	whileSt.setCondition(MatlabNodeFactory.newNumber(1));

	int line = whileSt.getLine();

	List<StatementNode> children = new ArrayList<>();
	children.add(StatementFactory.newIf(line,
		MatlabNodeFactory.newSimpleAccessCall("not", condition)));
	children.add(StatementFactory.newBreak(line));

	BlockSt ifBlock = StatementFactory.newBlock(line, children);
	iterator.add(ifBlock);
	if (iterator.hasNext()) {
	    iterator.next();
	}
    }

}
