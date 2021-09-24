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
 * A Matlab meta class.
 * 
 * <p>
 * The content is a String with the literal representation of the identifier of the meta class.
 * 
 * @author JoaoBispo
 *
 */
public class MetaClassNode extends MatlabNode {

    private final String name;

    MetaClassNode(String name) {
	this.name = name;
    }

    MetaClassNode(Object content, Collection<MatlabNode> children) {
	this((String) content);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new MetaClassNode(getName());
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
	return "?" + getName();
    }
}
