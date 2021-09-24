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
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.MatlabUnitNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.BlockSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ElseIfSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNodePass.AMatlabNodePass;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

public class ElseIfUnrollerPass extends AMatlabNodePass {

    @Override
    public MatlabNode apply(MatlabNode node, DataStore data) {
	Preconditions.checkArgument(node != null);
	Preconditions.checkArgument(data != null);

	if (node instanceof BlockSt) {
	    return apply((BlockSt) node, data);
	}
	if (node instanceof MatlabUnitNode || node instanceof FileNode) {
	    MatlabUnitNode function = (MatlabUnitNode) node;
	    applyToChildren(function, data);
	}
	return node;
    }

    public BlockSt apply(BlockSt node, DataStore data) {
	Preconditions.checkArgument(node != null);
	Preconditions.checkArgument(data != null);

	// Process the children
	// We'll go through the tree bottom-up.
	boolean hasElseIf = applyToChildren(node, data);

	if (!hasElseIf) {
	    return node;
	}

	// There is at least one child elseif.
	// We'll remove them here.

	for (int i = node.getNumChildren() - 1; i >= 0; --i) {
	    MatlabNode child = node.getChild(i);
	    if (child instanceof ElseIfSt) {
		ElseIfSt elseIfSt = (ElseIfSt) child;

		List<StatementNode> newIfChildren = new ArrayList<>();

		MatlabNode condition = ((ElseIfSt) child).getExpression();
		newIfChildren.add(StatementFactory.newIf(elseIfSt.getLine(), condition));
		newIfChildren.addAll(node.getStatements().subList(i + 1, node.getNumChildren()));

		BlockSt newIf = StatementFactory.newBlock(node.getLine(), newIfChildren);

		// Delete old children

		while (i != node.getNumChildren()) {
		    node.removeChild(node.getNumChildren() - 1);
		}

		// Add new children

		node.addChild(StatementFactory.newElse(elseIfSt.getLine()));
		node.addChild(newIf);
	    }
	}

	return node;
    }

    private boolean applyToChildren(MatlabNode node, DataStore data) {
	boolean hasElseIf = false;

	MatlabNodeIterator childIt = node.getChildrenIterator();
	while (childIt.hasNext()) {
	    MatlabNode child = childIt.next();
	    if (child instanceof ElseIfSt) {
		hasElseIf = true;
	    }
	    childIt.set(apply(child, data));
	}

	return hasElseIf;
    }
}
