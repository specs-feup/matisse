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

import static com.google.common.base.Preconditions.*;

import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.BlockSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ElseIfSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ElseSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.IfSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ReturnSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNodePass.AMatlabNodePass;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * A less aggressive variant of {@link ReturnRemoverPass} that handles less cases but generates better code.
 * 
 * @author Luís Reis
 *
 */
public class BasicReturnRemoverPass extends AMatlabNodePass {
    @Override
    public MatlabNode apply(MatlabNode node, DataStore data) {
	checkArgument(node != null, "node must not be null");
	checkArgument(data != null, "data must not be null");

	if (node instanceof FileNode) {
	    for (MatlabNode child : node.getChildren()) {
		if (child instanceof FunctionNode) {
		    removeReturnStatementsInBlock(child);
		}
	    }
	} else if (node instanceof FunctionNode) {
	    removeReturnStatementsInBlock(node);
	} else {
	    throw new UnsupportedOperationException("Can't apply BasicReturnRemover pass to " + node.getNodeName());
	}

	return node;
    }

    private static void removeReturnStatementsInBlock(MatlabNode block) {
	List<MatlabNode> children = block.getChildren();
	for (int i = children.size() - 1; i >= 0; --i) {
	    MatlabNode child = children.get(i);

	    if (!isPlainIfStatement(child)) {
		continue;
	    }

	    List<Integer> indices = child.indexesOf(ReturnSt.class);
	    if (indices.size() > 0) {
		int firstIndex = indices.get(0);
		ReturnSt returnStmt = (ReturnSt) child.getChild(firstIndex);

		// Remove statements after return.
		child.getChildren().subList(firstIndex, child.getNumChildren()).clear();

		child.addChild(StatementFactory.newElse(returnStmt.getLine()));
		List<MatlabNode> childrenAfterIfBlock = children.subList(i + 1, children.size());
		child.addChildren(childrenAfterIfBlock);
		childrenAfterIfBlock.clear();
	    }
	}
    }

    private static boolean isPlainIfStatement(MatlabNode node) {
	if (!(node instanceof BlockSt)) {
	    return false;
	}

	BlockSt block = (BlockSt) node;
	StatementNode header = block.getHeaderNode();

	if (!(header instanceof IfSt)) {
	    return false;
	}

	for (MatlabNode child : block.getChildren()) {
	    if (child instanceof ElseIfSt || child instanceof ElseSt) {
		return false;
	    }
	}

	return true;
    }
}
