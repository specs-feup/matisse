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

public abstract class TemporaryNode extends MatlabNode {

    public final String object;

    public TemporaryNode(String content, Collection<? extends MatlabNode> children) {
	super(children);

	object = content;
    }

    public TemporaryNode() {
	this("");
    }

    public TemporaryNode(String string) {
	object = string;
    }

    @Override
    public boolean isTemporary() {
	return true;
    }

    @Override
    public String toContentString() {
	return object;
    }

}
