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

package org.specs.MatlabToC.Utils;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;

public class TestUtils {

    public static CNode getFirstFunction(CNode node, String startsWith) {
	// Get functions
	List<FunctionCallNode> functions = node.getDescendantsAndSelf(FunctionCallNode.class);

	// Find division
	for (FunctionCallNode function : functions) {
	    FunctionInstance fInstance = function.getFunctionInstance();
	    if (fInstance.getCName().startsWith(startsWith)) {
		return function;
	    }
	}

	throw new RuntimeException("Could not find FunctionCall node with function name that starts with '"
		+ startsWith + "'");
    }
}
