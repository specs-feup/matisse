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

package org.specs.MatlabToC.CodeBuilder.MatlabToCRules.Assignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ColonNotationNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.collections.MultiMap;

public class ColonMap {

    private final MultiMap<AccessCallNode, Integer> indexesWithColon;
    // MATLAB Code with the size of the loop
    // Always start loop at index 1, calls to node have to adapt (e.g., a(2:4) will have size 4-2+1, and access call
    // will be i + 2 - 1.
    private final MultiMap<AccessCallNode, String> accessCallLoopSizes;

    private ColonMap() {
	// Using linked hash map to maintain the order of the keys
	indexesWithColon = new MultiMap<>(() -> new LinkedHashMap<>());
	accessCallLoopSizes = new MultiMap<>(() -> new LinkedHashMap<>());
    }

    public static Optional<ColonMap> newInstance(List<AccessCallNode> accessesWithColonNotation) {
	ColonMap map = new ColonMap();
	// System.out.println(accessesWithColonNotation.stream().map(node -> node.getCode())
	// .collect(Collectors.joining("\n", "STARTING\n", "\n")));

	// Check if all accesses have the same number of indexes, and if there is an access to a single index
	int numIndexes = -1;
	for (AccessCallNode node : accessesWithColonNotation) {
	    int numArgs = node.getArguments().size();

	    // First value found
	    if (numIndexes == -1) {
		numIndexes = numArgs;
	    }

	    // Otherwise, check if it is different and they are both bigger than one
	    else {
		boolean biggerThanOne = numIndexes > 1 && numArgs > 1;
		if (biggerThanOne && numIndexes != numArgs) {
		    SpecsLogs.warn("Does not support this case");
		    return Optional.empty();
		}
	    }

	    // Get indexes with colon notation
	    for (int i = 0; i < numArgs; i++) {
		MatlabNode arg = node.getArguments().get(i).normalizeExpr();

		// When access is done with a colon (e.g., a(:))
		if (arg instanceof ColonNotationNode) {
		    map.addColon(node, i);
		    continue;
		}

		// When access is done with a colon operator
		if (arg instanceof OperatorNode && ((OperatorNode) arg).getOp() == MatlabOperator.Colon) {
		    OperatorNode opNode = (OperatorNode) arg;

		    // When access is done with a simple range (e.g., a(2:N))
		    // Assuming colon operator only has two arguments always
		    assert opNode.getOperands()
			    .size() < 3 : "Assuming colon operator only has two operands in the tree";
		    boolean isComplexRange = opNode.getOperands().stream()
			    .filter(operand -> operand instanceof OperatorNode)
			    .filter(op -> ((OperatorNode) op).getOp() == MatlabOperator.Colon)
			    .findFirst()
			    .isPresent();

		    if (!isComplexRange) {
			map.addSimpleRange(node, i, opNode);
			continue;
		    }

		    SpecsLogs.warn("Ranges besides simple ranges not supported for this transformation");
		    return Optional.empty();
		}
	    }

	}

	return Optional.of(map);

    }

    /**
     * 
     * @param node
     * @param opNode
     * @param i
     */
    private void addSimpleRange(AccessCallNode node, int index, OperatorNode opNode) {
	indexesWithColon.put(node, index);
	accessCallLoopSizes.put(node,
		"(" + opNode.getOperands().get(1).getCode() + ") - (" + opNode.getOperands().get(0).getCode()
			+ ") + 1");
    }

    /**
     * Colon ends at size(node, index), unless colon is the only index, in that case is numel(node).
     * 
     * @param node
     * @param index
     */
    private void addColon(AccessCallNode node, int index) {

	indexesWithColon.put(node, index);

	// If only one child, use numel
	if (node.getNumArguments() == 1) {
	    accessCallLoopSizes.put(node, "numel(" + node.getName() + ")");
	    return;
	}

	// size of the corresponding index
	accessCallLoopSizes.put(node, "size(" + node.getName() + ", " + (index + 1) + ")");
    }

    @Override
    public String toString() {

	StringBuilder builder = new StringBuilder();
	for (AccessCallNode node : indexesWithColon.keySet()) {
	    StringJoiner joiner = new StringJoiner("; ", node.getCode() + ": ", "");
	    List<Integer> indexes = indexesWithColon.get(node);
	    List<String> sizes = accessCallLoopSizes.get(node);

	    for (int i = 0; i < indexes.size(); i++) {
		joiner.add(indexes.get(i) + " -> " + sizes.get(i));
	    }

	    builder.append(joiner.toString()).append("\n");
	}

	return builder.toString();
    }

    /**
     * Calculates the number of 'for' loops needed to implement the functionality of the colons
     * 
     * @return
     */
    public int getNumFors() {
	// Create a number of for loops equal to the total number of different indexes
	Set<Integer> indexes = new HashSet<>();

	for (List<Integer> values : indexesWithColon.values()) {
	    indexes.addAll(values);
	}

	return indexes.size();
    }

    public Map<Integer, String> getLoopSizes() {
	Map<Integer, String> unsortedSizes = getLoopSizesUnsorted();

	List<Integer> keys = new ArrayList<>(unsortedSizes.keySet());
	Collections.sort(keys);

	Map<Integer, String> sortedSizes = new LinkedHashMap<>();
	for (Integer key : keys) {
	    sortedSizes.put(key, unsortedSizes.get(key));
	}

	return sortedSizes;
    }

    private Map<Integer, String> getLoopSizesUnsorted() {

	Map<Integer, String> loopSizes = new HashMap<>();
	int numFors = getNumFors();

	for (Entry<AccessCallNode, List<Integer>> entry : indexesWithColon.entrySet()) {
	    List<Integer> indexes = entry.getValue();
	    for (int i = 0; i < indexes.size(); i++) {
		Integer index = indexes.get(i);
		// for (Integer index : entry.getValue()) {
		// Ignore if index was already considered
		if (loopSizes.containsKey(index)) {
		    continue;
		}

		// Add size
		String size = accessCallLoopSizes.get(entry.getKey()).get(i);
		loopSizes.put(index, size);

		// Check if already have all sizes
		if (loopSizes.size() == numFors) {
		    return loopSizes;
		}
		// }
	    }
	}

	throw new RuntimeException("Only found " + loopSizes.size() + " indexes, expected " + numFors);
    }
}
