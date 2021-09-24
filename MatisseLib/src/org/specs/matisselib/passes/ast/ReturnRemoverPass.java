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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.BlockSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CaseSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ElseIfSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ElseSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ForSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.IfSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.OtherwiseSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ReturnSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNode.nodes.statements.WhileSt;
import org.specs.MatlabIR.MatlabNodePass.AMatlabNodePass;
import org.specs.matisselib.PreTypeInferenceServices;
import org.specs.matisselib.services.NamingService;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.treenode.NodeInsertUtils;

public class ReturnRemoverPass extends AMatlabNodePass {

    @Override
    public MatlabNode apply(MatlabNode node, DataStore data) {
	checkArgument(node != null, "node must not be null");
	checkArgument(data != null, "data must not be null");

	NamingService naming = data.get(PreTypeInferenceServices.COMMON_NAMING);

	if (node instanceof FileNode) {
	    for (MatlabNode child : node.getChildren()) {
		if (child instanceof FunctionNode) {
		    removeReturnStatementsInFunction((FunctionNode) child, naming);
		}
	    }
	} else if (node instanceof FunctionNode) {
	    removeReturnStatementsInFunction((FunctionNode) node, naming);
	} else {
	    throw new UnsupportedOperationException("Can't apply ReturnRemover pass to " + node.getNodeName());
	}

	return node;
    }

    private void removeReturnStatementsInFunction(FunctionNode function, NamingService namingService) {
	String returnVariable = namingService.generateTemporaryVariableName("return");

	if (removeReturnStatementsInToken(returnVariable, function)) {
	    MatlabNode insertedReturnVariable = assignToNumber(returnVariable, "0");

	    int declarationIndex = function.getChildren().indexOf(function.getDeclarationNode());
	    assert declarationIndex != -1;

	    function.addChild(declarationIndex + 1, insertedReturnVariable);
	}
    }

    private static MatlabNode assignToNumber(String variableName, String number) {
	return StatementFactory.newAssignment(MatlabNodeFactory.newIdentifier(variableName),
		// TempNodeFactory.newExpression(MatlabNodeFactory.newNumber(number)));
		MatlabNodeFactory.newNumber(number));
    }

    public boolean removeReturnStatementsInToken(String returnVariable, MatlabNode token) {
	boolean removedAnyReturnStatement = false;

	boolean isLoop = isLoopToken(token);

	for (int i = 0; i < token.getChildren().size(); ++i) {
	    MatlabNode child = token.getChildren().get(i);
	    if (child instanceof ReturnSt) {
		// Found a return statement.
		// Remove all statements starting from this one until the next branch (e.g. else case).

		while (token.getChildren().size() != i && !isNewBranch(token.getChildren().get(i))) {

		    NodeInsertUtils.delete(token.getChildren().get(i));
		}

		MatlabNode returnAssignment = assignToNumber(returnVariable, "1");
		token.addChild(i, returnAssignment);

		removedAnyReturnStatement = true;
		continue;
	    }

	    // Child was not a return statement.
	    boolean childRemovedReturn = removeReturnStatementsInToken(returnVariable, child);
	    if (childRemovedReturn) {
		removedAnyReturnStatement = true;

		// Are there any additional children to move to an if statement?
		// First, check if there are any more children
		if (i != token.getChildren().size() - 1) {
		    // There are, so let's move them to an if statement
		    // This if statement will check if the return variable is set.
		    // The code will only be executed if the method hasn't returned.

		    List<StatementNode> blockTokens = new ArrayList<>();

		    MatlabNode condition = MatlabNodeFactory.newOperator("~",
			    MatlabNodeFactory.newIdentifier(returnVariable));

		    // IfSt ifToken = StatementFactory.newIf(0, TempNodeFactory.newExpression(condition));
		    IfSt ifToken = StatementFactory.newIf(0, condition);

		    blockTokens.add(ifToken);

		    while (token.getChildren().size() != i + 1) {
			// Move all other statements to the if statement

			StatementNode tokenToMove = (StatementNode) token.getChildren().get(i + 1);
			blockTokens.add(tokenToMove);
			NodeInsertUtils.delete(tokenToMove);
		    }

		    if (isLoop) {
			// We need to add a break condition
			blockTokens.add(StatementFactory.newElse(0));
			blockTokens.add(StatementFactory.newBreak(0));
		    }

		    token.addChild(StatementFactory.newBlock(0, blockTokens));
		} else if (isLoop) {

		    MatlabNode condition = MatlabNodeFactory.newIdentifier(returnVariable);
		    IfSt ifToken = StatementFactory.newIf(0, condition);

		    List<StatementNode> blockTokens = new ArrayList<>();
		    blockTokens.add(ifToken);
		    blockTokens.add(StatementFactory.newBreak(0));

		    token.addChild(StatementFactory.newBlock(0, blockTokens));

		}
	    }
	}

	return removedAnyReturnStatement;
    }

    private static boolean isNewBranch(MatlabNode token) {
	if (!(token instanceof StatementNode)) {
	    return false;
	}

	if (token instanceof ElseSt) {
	    return true;
	}
	if (token instanceof ElseIfSt) {
	    return true;
	}
	if (token instanceof CaseSt) {
	    return true;
	}

	return token instanceof OtherwiseSt;
    }

    private static boolean isLoopToken(MatlabNode token) {
	if (!(token instanceof BlockSt)) {
	    return false;
	}

	StatementNode firstChild = ((BlockSt) token).getHeaderNode();

	if (firstChild instanceof ForSt) {
	    return true;
	}
	if (firstChild instanceof WhileSt) {
	    return true;
	}

	return false;
    }
}
