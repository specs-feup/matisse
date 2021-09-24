/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.MatlabIR.Processor;

import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public enum TreeTraversalStrategy {

    bottomUp,
    topDown,
    children;

    public void applyRuleList(MatlabNode token, List<TreeTransformRule> rules) throws TreeTransformException {

	// Apply processing methods to current tokens
	for (TreeTransformRule rule : rules) {
	    applyRule(token, rule);
	}
    }

    public void applyRule(MatlabNode token, TreeTransformRule rule) throws TreeTransformException {
	switch (this) {
	case bottomUp:
	    bottomUpTraversal(token, rule);
	    return;
	default:
	    SpecsLogs.warn("Case not defined:" + this);
	    return;
	}
    }

    /**
     * Apply the rule to the given token and all children in the token tree, bottom up.
     * 
     * @param token
     * @param rule
     * @throws TreeTransformException
     */
    private void bottomUpTraversal(MatlabNode token, TreeTransformRule rule) throws TreeTransformException {

	if (token.hasChildren()) {
	    // System.out.println("TokenType:"+token.type);
	    for (MatlabNode child : token.getChildren()) {
		if (child == null) {
		    System.out.println("Child Token is null in type " + token.getNodeName());
		    System.out.println("Children:" + token.getChildren());
		    return;
		}
		// System.out.println("Child:"+child);
		bottomUpTraversal(child, rule);
	    }
	}

	rule.apply(token);
    }
}
