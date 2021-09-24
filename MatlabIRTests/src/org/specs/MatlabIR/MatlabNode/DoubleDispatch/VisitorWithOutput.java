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

package org.specs.MatlabIR.MatlabNode.DoubleDispatch;

import java.util.HashMap;
import java.util.Map;

public abstract class VisitorWithOutput<O> {

    private Map<Class<?>, VisitorAction<?, O>> visitorMap;

    public VisitorWithOutput() {
	this(new HashMap<>());
    }

    public VisitorWithOutput(Map<Class<?>, VisitorAction<?, O>> visitorMap) {
	this.visitorMap = visitorMap;
    }

    public <T> void add(Class<T> aClass, VisitorAction<?, O> action) {
	visitorMap.put(aClass, action);
    }

    public static <T, O> void add(Map<Class<?>, VisitorAction<?, O>> visitorMap, Class<T> aClass,
	    VisitorAction<T, O> action) {

	visitorMap.put(aClass, action);
    }

    // public Optional<Object> visit(Object node) {
    @SuppressWarnings("unchecked")
    public O visit(Object node) {
	VisitorAction<?, O> action = visitorMap.get(node.getClass());
	if (action != null) {
	    return (O) action.applyUnsafe(node);
	}

	return defaultValue(node);
    }

    /**
     * What should be returned in case no visitor was called.
     * 
     * @return
     */
    public abstract O defaultValue(Object node);
}
