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

package org.specs.MatlabIR.MatlabNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CellAccessNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.FieldAccessNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OutputsNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.UnusedVariableNode;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.classmap.FunctionClassMap;

/**
 * Utility methods for MatlabNode.
 * 
 * @author JoaoBispo
 *
 */
public class MatlabNodeUtils {

    /**
     * Returns the Strings related to variable names found in the given MatlabToken.
     * 
     * <p>
     * Supports Identifiers, AccessCall and Outputs.
     * 
     * @param node
     * @return
     */
    public static List<String> getVariableNames(MatlabNode node) {
	return GET_NAMES.apply(node);
    }

    /*
        private static final Action<MatlabNode, List<String>> GET_NAMES = new ActionBuilder<MatlabNode, List<String>>()
    	    // .add(IdentifierNode.class, id -> Arrays.asList(id.getName()))
    	    // .add(SimpleAccessCallNode.class, call -> Arrays.asList(call.getName()))
    	    .add(IdentifierNode.class, id -> Arrays.asList(id.getCode()))
    	    .add(AccessCallNode.class, call -> Arrays.asList(call.getName()))
    	    .add(OutputsNode.class, MatlabNodeUtils::getNames)
    	    .add(CellAccessNode.class, cell -> Arrays.asList(cell.getName()))
    	    .add(FieldAccessNode.class, MatlabNodeUtils::getNames)
    	    .build();
    */
    private static final FunctionClassMap<MatlabNode, List<String>> GET_NAMES = new FunctionClassMap<>();

    static {
	GET_NAMES.put(IdentifierNode.class, id -> Arrays.asList(id.getCode()));
	GET_NAMES.put(AccessCallNode.class, call -> Arrays.asList(call.getName()));
	GET_NAMES.put(OutputsNode.class, MatlabNodeUtils::getNames);
	GET_NAMES.put(CellAccessNode.class, cell -> Arrays.asList(cell.getName()));
	GET_NAMES.put(FieldAccessNode.class, MatlabNodeUtils::getNames);
	GET_NAMES.put(UnusedVariableNode.class, unused -> Collections.emptyList());
    }

    private static List<String> getNames(OutputsNode outputs) {

	return outputs.getChildrenStream()
		// Apply on the children of Outputs
		.map(node -> GET_NAMES.apply(node))
		.reduce(new ArrayList<>(), (id, list) -> SpecsCollections.add(id, list));
    }

    private static List<String> getNames(FieldAccessNode fieldAccess) {
	// Call get_names recursively on the left node
	return GET_NAMES.apply(fieldAccess.getLeft());
    }

}
