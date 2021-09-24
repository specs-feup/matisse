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

package org.specs.MatlabIR.MatlabNodePass.APasses;

import java.util.ListIterator;

import org.specs.MatlabIR.Exceptions.UnsupportedPassTypeException;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.BlockSt;
import org.specs.MatlabIR.MatlabNodePass.AMatlabNodePass;
import org.specs.MatlabIR.MatlabNodePass.interfaces.IteratorPass;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Pass for the statements of a M-file. Iterates over all the statements of the code, by the order they appear.
 * 
 * <p>
 * This pass can only be applied of File, Function or Script.
 * 
 * @author JoaoBispo
 *
 */
public abstract class AllStatementsPass extends AMatlabNodePass {

    /**
     * Method to be implemented by concrete class. Gives a ListIterator, with the cursor before a Statement token to be
     * called, method just has to call next.
     * 
     * <p>
     * A ListIterator is provided, in case changes to the tree needing more than one statement are necessary.
     */
    protected abstract void applyOnStatement(MatlabNodeIterator iterator, DataStore data);

    @Override
    public MatlabNode apply(MatlabNode rootNode, DataStore data) {
	// If File, call function recursively over Script or Function
	// if (rootNode.getType() == MType.File) {
	if (rootNode instanceof FileNode) {
	    rootNode.getChildren().stream()
		    .forEach(node -> apply(node, data));

	    return rootNode;
	}

	// If Script or Function, iterate over children
	// if (rootNode.getType() == MType.Script || rootNode.getType() == MType.Function) {
	if (rootNode instanceof ScriptNode || rootNode instanceof FunctionNode) {
	    iterateChildren(rootNode, data);
	    return rootNode;
	}

	// Otherwise, does not know how to proceed
	throw new UnsupportedPassTypeException(this, rootNode, ScriptNode.class, FunctionNode.class, FileNode.class);
    }

    /**
     * Iterates over the list, each time it finds a statement calls {@link #applyOnStatement(ListIterator)}. If it finds
     * a Block, calls the function recursively over the Block.
     * 
     * @param iterator
     */
    private void iterateChildren(MatlabNode parent, DataStore data) {
	MatlabNodeIterator iterator = parent.getChildrenIterator();

	// Iterate while there are nodes
	while (iterator.hasNext()) {

	    MatlabNode nextNode = iterator.next();

	    // If statement, go back and call apply
	    if (nextNode instanceof StatementNode) {
		iterator.previous();
		applyOnStatement(iterator, data);

		// If block, iterate over children
		if (nextNode instanceof BlockSt) {
		    iterateChildren(nextNode, data);
		}

		continue;
	    }

	    // Otherwise, do nothing

	}

    }

    /**
     * Generic class to be able to implement newInstance.
     * 
     * @author joaobispo
     *
     */
    static class GenericPass extends AllStatementsPass {

	private final IteratorPass pass;

	public GenericPass(IteratorPass pass) {
	    this.pass = pass;
	}

	@Override
	protected void applyOnStatement(MatlabNodeIterator iterator, DataStore data) {
	    pass.apply(iterator, data);
	}

    }

    public static AllStatementsPass newInstance(IteratorPass pass) {
	return new GenericPass(pass);
    }
}
