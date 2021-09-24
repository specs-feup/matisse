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

package org.specs.MatlabIR.MatlabNode.DoubleDispatchV2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author JoaoBispo
 *
 * @param <B>
 *            the base class of the inputs
 * @param <O>
 *            the type of the output
 */
public abstract class VisitorWithOutputV2<B, O> {
    /*
        class Builder {
    	private final Map<Class<? extends B>, VisitorAction<? extends B, O>> builderVisitors;

    	public Builder() {
    	    builderVisitors = new HashMap<>();
    	}

    	private Map<Class<? extends B>, VisitorAction<? extends B, O>> getVisitorMap() {
    	    return builderVisitors;
    	}

    	/*
    	public <T extends B> void add(Map<Class<? extends B>, VisitorAction<? extends B, O>> visitorMap,
    		Class<T> aClass,
    		VisitorAction<T, O> action) {

    	    visitorMap.put(aClass, action);
    	}
    	*/
    /*
    	public <T extends B> void add(Class<T> aClass, VisitorAction<T, O> action) {
    	    builderVisitors.put(aClass, action);
    	}
        }
    */
    private Map<Class<? extends B>, VisitorAction<? extends B, O>> visitorMap;

    public VisitorWithOutputV2() {
	visitorMap = new HashMap<>();
    }

    // public VisitorWithOutput(Map<Class<? extends B>, VisitorAction<? extends B, O>> visitorMap) {
    public VisitorWithOutputV2(VisitorBuilder<B, O> builder) {
	this.visitorMap = Collections.unmodifiableMap(builder.getVisitorMap());
    }

    public <T extends B> void add(Class<T> aClass, VisitorAction<T, O> action) {
	visitorMap.put(aClass, action);
    }

    public static <B, O, T extends B> void add(Map<Class<? extends B>, VisitorAction<? extends B, O>> visitorMap,
	    Class<T> aClass,
	    VisitorAction<T, O> action) {

	visitorMap.put(aClass, action);
    }

    // public Optional<Object> visit(Object node) {
    @SuppressWarnings("unchecked")
    public O visit(B node) {
	VisitorAction<?, O> action = visitorMap.get(node.getClass());
	if (action != null) {
	    return (O) action.applyUnsafe(node);
	}

	return defaultValue(node);
    }

    /**
     * What should be returned in case no visitor was called.
     * 
     * <p>
     * As default, throws an exception.
     * 
     * @return
     */
    public O defaultValue(B visited) {
	throw new RuntimeException("Visitor not defined for class '" + visited.getClass() + "'");
    }
}
