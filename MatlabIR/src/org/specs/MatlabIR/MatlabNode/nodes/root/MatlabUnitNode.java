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

package org.specs.MatlabIR.MatlabNode.nodes.root;

import java.util.Collection;
import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;

/**
 * Represents a single MATLAB Unit, which can be a Function, a Script or a Classdef.
 * 
 * <p>
 * All children of a MATLAB Unit are StatementNodes.
 * 
 * @author JoaoBispo
 *
 */
public abstract class MatlabUnitNode extends MatlabNode {

    public MatlabUnitNode(Collection<? extends MatlabNode> children) {
	super(children);
    }

    public List<StatementNode> getStatements() {
	return getChildren(StatementNode.class);
    }

    /**
     * The number of inputs of this unit. For functions, it is the number of inputs; for scripts, it is zero.
     * 
     * 
     * @return
     */
    public int getNumInputs() {
	throw new RuntimeException("Not implemented for node " + getNodeName());
    }

    /**
     * The name of the unit. It can be either:
     * <p>
     * - The name of the function, if a FunctionNode;<br>
     * - The name of the script, if a ScriptNode;<br>
     * - The name of the class, if a ClassdefNode;<br>
     * 
     * @return the name of this unit
     */
    public abstract String getUnitName();

}
