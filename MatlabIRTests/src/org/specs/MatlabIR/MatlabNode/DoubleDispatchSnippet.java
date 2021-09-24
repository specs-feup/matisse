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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.DoubleDispatchV2.GenericVisitorV2;
import org.specs.MatlabIR.MatlabNode.DoubleDispatchV2.VariableNamesVisitorV2;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;

import pt.up.fe.specs.util.SpecsStrings;

public class DoubleDispatchSnippet {

    // private static Map<Class<?>, VisitorAction<?>> visitorMap = new HashMap<>();

    interface VisitorAction<T> {
	void apply(T node);

	@SuppressWarnings("unchecked")
	default void applyUnsafe(Object node) {
	    apply((T) node);
	}
    }

    interface VisitorActionV2<T> {
	Object apply(T node);

	@SuppressWarnings("unchecked")
	default Object applyUnsafe(Object node) {
	    return apply((T) node);
	}
    }

    @Test
    public void test() {

	IdentifierNode idNode = MatlabNodeFactory.newIdentifier("id1");
	List<String> results = new ArrayList<>();

	// VariableNamesVisitor variableNames = new VariableNamesVisitor();

	long tic = System.nanoTime();
	for (int i = 0; i < 1_000_000; i++) {
	    results.addAll(new GenericVisitorV2<>(VariableNamesVisitorV2.visitors).visit(idNode));
	}
	long toc = System.nanoTime();
	System.out.println("RESULT:" + SpecsStrings.parseTime(toc - tic));
    }

    class StrictVisitor {
	private Map<Class<?>, VisitorAction<?>> visitorMap;

	public StrictVisitor() {
	    this(new HashMap<>());
	}

	public StrictVisitor(Map<Class<?>, VisitorAction<?>> visitorMap) {
	    this.visitorMap = visitorMap;
	}

	public <T> void add(Class<T> aClass, VisitorAction<T> action) {
	    visitorMap.put(aClass, action);
	}

	// public <K, T extends ATreeNode<K, T>> void visit(T node) {
	public void visit(Object node) {
	    VisitorAction<?> action = visitorMap.get(node.getClass());
	    if (action != null) {
		action.applyUnsafe(node);
	    }

	}
    }

    abstract class StrictVisitorV2 {

	private Map<Class<?>, VisitorActionV2<?>> visitorMap;

	public StrictVisitorV2() {
	    this(new HashMap<>());
	}

	public StrictVisitorV2(Map<Class<?>, VisitorActionV2<?>> visitorMap) {
	    this.visitorMap = visitorMap;
	}

	public <T> void add(Class<T> aClass, VisitorActionV2<T> action) {
	    visitorMap.put(aClass, action);
	}

	// public Optional<Object> visit(Object node) {
	public Object visit(Object node) {
	    VisitorActionV2<?> action = visitorMap.get(node.getClass());
	    if (action != null) {
		return action.applyUnsafe(node);
	    }

	    return null;
	}
    }

}
