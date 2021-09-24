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

import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * A literal string.
 * 
 * <p>
 * The content is a String.
 * 
 * @author JoaoBispo
 *
 */
public class MatlabCharArrayNode extends MatlabNode {

    private final String string;

    MatlabCharArrayNode(String string) {
	this.string = string;
    }

    MatlabCharArrayNode(Object content, Collection<MatlabNode> children) {
	this((String) content);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new MatlabCharArrayNode(getString());
    }

    public String getString() {
	return string;
    }

    @Override
    public String getCode() {
	// Escape ' and prefix and suffix '
	return "'" + getString().replace("'", "''") + "'";
    }

    @Override
    public String toContentString() {
	return string;
    }
}
