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

package org.specs.MatlabIR.MatlabNodePass;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;
import org.specs.MatlabIR.MatlabNodePass.interfaces.MatlabNodePass;
import org.specs.MatlabIR.Processor.TreeTransformException;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class MatlabPassUtils {

    /**
     * Apply the rule to the given node and all children in the token tree, bottom up.
     * 
     * @param rootNode
     * @param rule
     * @throws TreeTransformException
     */
    public static MatlabNode bottomUpTraversal(MatlabNode rootNode, MatlabNodePass rule, DataStore data) {

	MatlabNodeIterator iterator = rootNode.getChildrenIterator();

	// Iterate while there are children
	while (iterator.hasNext()) {
	    MatlabNode child = iterator.next();
	    MatlabNode newChild = bottomUpTraversal(child, rule, data);

	    // If child are not the same object, replace
	    if (child != newChild) {
		iterator.set(newChild);
	    }
	}

	// Apply processing rule to current token
	return rule.apply(rootNode, data);
    }

}
