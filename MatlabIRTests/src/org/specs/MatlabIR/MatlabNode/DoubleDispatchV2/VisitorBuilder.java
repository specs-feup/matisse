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

import java.util.HashMap;
import java.util.Map;

public class VisitorBuilder<B, O> {
    private final Map<Class<? extends B>, VisitorAction<? extends B, O>> builderVisitors;

    public VisitorBuilder() {
	builderVisitors = new HashMap<>();
    }

    Map<Class<? extends B>, VisitorAction<? extends B, O>> getVisitorMap() {
	return builderVisitors;
    }

    /*
    public <T extends B> void add(Map<Class<? extends B>, VisitorAction<? extends B, O>> visitorMap,
    	Class<T> aClass,
    	VisitorAction<T, O> action) {

        visitorMap.put(aClass, action);
    }
    */

    public <T extends B> void add(Class<T> aClass, VisitorAction<T, O> action) {
	builderVisitors.put(aClass, action);
    }
}
