/**
 * Copyright 2014 SPeCS.
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

package org.specs.CIR.Utilities;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.Tree.CNode;

import pt.up.fe.specs.util.providers.ResourceProvider;
import pt.up.fe.specs.util.utilities.Replacer;

public class CodeReplacer extends Replacer {

    private final List<CNode> currentCNodes;

    public CodeReplacer(String template) {
	super(template);

	currentCNodes = new ArrayList<>();
    }

    public CodeReplacer(ResourceProvider resource) {
	super(resource);

	currentCNodes = new ArrayList<>();
    }

    public List<CNode> getCNodes() {
	return currentCNodes;
    }

    @Override
    public CodeReplacer replace(CharSequence target, CharSequence replacement) {
	return (CodeReplacer) super.replace(target, replacement);
    }

    public CodeReplacer replace(CharSequence target, CNode cnode) {
	// Get CNode code
	String code = cnode.getCode();

	// Add CNode to list
	currentCNodes.add(cnode);

	// Replace
	return (CodeReplacer) super.replace(target, code);
    }

}
