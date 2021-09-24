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

package org.specs.matisselib.unssa;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import pt.up.fe.specs.util.collections.MultiMap;

public class ControlFlowGraph {
    private MultiMap<Integer, Integer> toEdges = new MultiMap<>();
    private MultiMap<Integer, Integer> fromEdges = new MultiMap<>();

    public void addEdge(int from, int to) {
	fromEdges.put(to, from);
	toEdges.put(from, to);
    }

    public List<Integer> getAntecedentsOf(int node) {
	return Collections.unmodifiableList(fromEdges.get(node));
    }

    public List<Integer> getSuccessorsOf(int node) {
	return Collections.unmodifiableList(toEdges.get(node));
    }

    public boolean validate() {
	Set<Integer> fromSet = toEdges.keySet();
	Set<Integer> toSet = fromEdges.keySet();

	for (Integer from : fromSet) {
	    for (Integer to : toEdges.get(from)) {
		if (!fromEdges.get(to).contains(from)) {
		    return false;
		}
	    }
	}

	for (Integer to : toSet) {
	    for (Integer from : fromEdges.get(to)) {
		if (!toEdges.get(from).contains(to)) {
		    return false;
		}
	    }
	}

	return true;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();

	builder.append("graph:\n");
	for (Integer from : toEdges.keySet()) {
	    for (Integer to : toEdges.get(from)) {
		builder.append("   #");
		builder.append(from);
		builder.append(" -> #");
		builder.append(to);
		builder.append("\n");
	    }
	}

	return builder.toString();
    }
}
