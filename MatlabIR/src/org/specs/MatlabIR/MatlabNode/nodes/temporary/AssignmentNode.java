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

package org.specs.MatlabIR.MatlabNode.nodes.temporary;

import java.util.Collection;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * Literal representation of assignment (=)
 * 
 * <p>
 * The content is a String.
 * 
 * @author JoaoBispo
 *
 */
public class AssignmentNode extends TemporaryNode {

    AssignmentNode() {
	super("=");

    }

    public AssignmentNode(Object content, Collection<MatlabNode> children) {
	this();
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new AssignmentNode();
    }

    public String getLiteral() {
	return "=";
    }

    @Override
    public String getCode() {
	return getLiteral();
    }
}
