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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;

public class ParallelCopySequentializer {
    private ParallelCopySequentializer() {
    }

    public static class Copy {
	private final String source;
	private final String destination;

	public Copy(String source, String destination) {
	    Preconditions.checkArgument(source != null);
	    Preconditions.checkArgument(destination != null);

	    this.source = source;
	    this.destination = destination;
	}

	public String getSource() {
	    return source;
	}

	public String getDestination() {
	    return destination;
	}

	@Override
	public boolean equals(Object other) {
	    if (other == null || !(other instanceof Copy)) {
		return false;
	    }

	    Copy otherCopy = (Copy) other;

	    return source.equals(otherCopy.source) && destination.equals(otherCopy.destination);
	}

	@Override
	public int hashCode() {
	    return source.hashCode() ^ destination.hashCode();
	}

	@Override
	public String toString() {
	    return source + " -> " + destination;
	}
    }

    /**
     * Sequentializes parallel copies, as specified in Algorithm 1 of "Revisiting Out-of-SSA Translation for
     * Correctness, Code Quality, and Efficiency".
     * 
     * @param parallelCopies
     *            The parallel copies to sequentialize. The source must never be equal to the destination. This is known
     *            as "P" in the paper.
     * @param freshVariable
     *            A fresh variable to use in case swaps are needed. This is known as "n" in the paper.
     * @return The list of copies in sequential order.
     */
    public static List<Copy> sequentializeParallelCopies(Set<Copy> parallelCopies, String freshVariable) {
	Preconditions.checkArgument(parallelCopies != null);

	List<Copy> output = new ArrayList<>();

	Deque<String> ready = new ArrayDeque<>();
	Deque<String> todo = new ArrayDeque<>();
	Map<String, String> pred = new HashMap<>();
	Map<String, String> loc = new HashMap<>();
	pred.put(freshVariable, null);

	for (Copy copy : parallelCopies) {
	    loc.put(copy.destination, null);
	    pred.put(copy.source, null);
	}

	for (Copy copy : parallelCopies) {
	    loc.put(copy.source, copy.source);
	    pred.put(copy.destination, copy.source);
	    todo.push(copy.destination);
	}

	for (Copy copy : parallelCopies) {
	    if (loc.get(copy.destination) == null) {
		ready.push(copy.destination);
	    }
	}

	while (!todo.isEmpty()) {
	    while (!ready.isEmpty()) {
		String b = ready.pop();
		String a = pred.get(b);
		String c = loc.get(a);

		output.add(new Copy(c, b));
		loc.put(a, b);
		if (a.equals(c) && pred.get(a) != null) {
		    ready.push(a);
		}
	    }

	    {
		String b = todo.pop();
		if (b.equals(loc.get(b))) {
		    output.add(new Copy(b, freshVariable));
		    loc.put(b, freshVariable);
		    ready.push(b);
		}
	    }
	}

	return output;
    }
}
