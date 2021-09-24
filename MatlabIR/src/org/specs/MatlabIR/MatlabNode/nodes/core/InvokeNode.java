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
 * A literal invoke '!' symbol.
 * 
 * @author JoaoBispo
 *
 */
public class InvokeNode extends MatlabNode {

    private final String command;

    /**
     * TODO: Should only store command, prefixing "!" because .getCode is not implemented yet.
     * 
     * @param command
     */
    InvokeNode(String command) {
	this.command = command;
    }

    /**
     * TODO: Remove after removing TOM from MatlabWeaver
     * 
     * @param content
     * @param children
     */
    InvokeNode(Object content, Collection<MatlabNode> children) {
	this(content.toString());
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new InvokeNode(command);
    }

    public String getCommand() {
	return command;
    }

    @Override
    public String getCode() {
	return "!" + command;
    }
}
