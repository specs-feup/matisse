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
import java.util.stream.Collectors;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * The list of base (super) classes in a classdef.
 * 
 * @author JoaoBispo
 *
 */
public class BaseClassesNode extends MatlabNode {
    private BaseClassesNode(Collection<MatlabNode> children) {
	super(children);
    }

    BaseClassesNode(Object content, Collection<MatlabNode> children) {
	this(children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new BaseClassesNode(Collections.emptyList());
    }

    @Override
    public String getCode() {
	return getChildrenStream()
		.map(c -> c.getCode())
		.collect(Collectors.joining(" & "));
    }

}
