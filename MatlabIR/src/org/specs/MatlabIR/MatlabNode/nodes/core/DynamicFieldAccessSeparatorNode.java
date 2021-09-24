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

package org.specs.MatlabIR.MatlabNode.nodes.core;

import java.util.Collection;
import java.util.Collections;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.TemporaryNode;

/**
 * Dynamic Field access. S.(df) when S is a structure, accesses the contents of dynamic field df of that structure.
 * 
 * <p>
 * Dynamic field names are defined at runtime.
 * 
 * @author JoaoBispo
 *
 */
public class DynamicFieldAccessSeparatorNode extends TemporaryNode {

    DynamicFieldAccessSeparatorNode() {
	super(".", Collections.emptyList());
    }

    DynamicFieldAccessSeparatorNode(Object content, Collection<MatlabNode> children) {
	this();
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new DynamicFieldAccessSeparatorNode();
    }

    @Override
    public String getCode() {
	return ".";
    }

}
