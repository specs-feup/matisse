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
 * Colon notation (:) that is not used as an operator. E.g.: C{:}; A(:); A(:,J);
 * 
 * <p>
 * The content is a String with the literal representation of colon (:).
 * 
 * @author JoaoBispo
 *
 */
public class ColonNotationNode extends MatlabNode {

    private static final String COLON = ":";

    ColonNotationNode() {
	// super(ColonNotationNode.COLON, Collections.emptyList());
    }

    /**
     * TOM support.
     * 
     * @param content
     * @param children
     */
    ColonNotationNode(Object content, Collection<MatlabNode> children) {
	this();
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new ColonNotationNode();
    }

    public String getLiteral() {
	return ColonNotationNode.COLON;
    }

    @Override
    public String getCode() {
	return getLiteral();
    }
}
