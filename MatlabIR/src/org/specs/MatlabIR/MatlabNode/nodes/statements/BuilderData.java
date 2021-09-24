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

package org.specs.MatlabIR.MatlabNode.nodes.statements;

import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * Data for building a statement node.
 * 
 * 
 * @author JoaoBispo
 *
 */
class BuilderData {

    // Using short names so that ActionMap 'add' can be one-liners
    public final int l;
    public final boolean d;
    public final List<MatlabNode> nodes;

    public BuilderData(int lineNumber, boolean displayResults, List<MatlabNode> children) {
	this.l = lineNumber;
	this.d = displayResults;
	this.nodes = children;
    }

    public MatlabNode first() {
	return nodes.get(0);
    }

    public <K extends MatlabNode> K get(Class<K> nodeClass, int index) {
	return nodeClass.cast(nodes.get(index));
    }

}
