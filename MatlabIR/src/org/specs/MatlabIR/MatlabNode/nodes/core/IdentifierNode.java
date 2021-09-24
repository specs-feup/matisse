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

import pt.up.fe.specs.util.SpecsLogs;

/**
 * A Matlab identifier.
 * 
 * <p>
 * Must start with a letter, can have letters, numbers and underscores. Matlab has a limit of 64 characters per
 * identifier (it is being tested, if name has more than 64 characters, outputs a warning and accepts the name).
 * 
 * <p>
 * The content is a String with the literal representation of the identifier.
 * 
 * @author JoaoBispo
 *
 */
public class IdentifierNode extends MatlabNode {

    private final String name;

    IdentifierNode(String name) {
	this.name = checkId(name);
    }

    IdentifierNode(Object content, Collection<MatlabNode> children) {
	this((String) content);
    }

    private static String checkId(String name) {
	if (name.length() > 64) {
	    SpecsLogs.msgInfo(" - WARN: Given id is not valid in MATLAB, has more than 64 characters: '" + name
		    + "'");
	    // new RuntimeException().printStackTrace();
	}

	return name;
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new IdentifierNode(getName());
    }

    /**
     * 
     * @return a String with the name of the identifier
     */
    public String getName() {
	return name;
    }

    @Override
    public String getCode() {
	return getName();
    }
}
