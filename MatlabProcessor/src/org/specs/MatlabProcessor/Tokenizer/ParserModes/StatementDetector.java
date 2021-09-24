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

package org.specs.MatlabProcessor.Tokenizer.ParserModes;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.DynamicFieldAccessSeparatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.FieldAccessSeparatorNode;
import org.specs.MatlabProcessor.Tokenizer.TokenizerState.StateNodes;

public interface StatementDetector {

    /**
     * Nodes that usually do not trigger a new statement when inside a condition
     */
    public final static Set<Class<?>> NON_TRIGGER_NODES = new HashSet<>(Arrays.asList(
	    OperatorNode.class, FieldAccessSeparatorNode.class, DynamicFieldAccessSeparatorNode.class));

    /**
     * Detects if the current node should be in a new statement.
     * 
     * @param currentNode
     * @param isFirstStatementToken
     * @return true, if the given token belongs to the next statement
     */
    boolean pushToken(StateNodes stateNodes, MatlabNode currentNode);

    /**
     * Cleans the state of the detector, usually used after a statement finishes.
     */
    void clear();

    /**
     * 
     * @return true if the detector is currently inside a state that will possibly generate a signal to finish the
     *         statement.
     */
    boolean isActive();

    /**
     * Returns the nodes that should be added to StateNodes after finishing the statement.
     * 
     * @return
     */
    default List<MatlabNode> getNextStatementTokens() {
	return Collections.emptyList();
    }

    /**
     * Auxiliary method for debug purposes.
     * 
     * @return
     */
    default String lastFalse() {
	return "(not implemented)";
    }

}