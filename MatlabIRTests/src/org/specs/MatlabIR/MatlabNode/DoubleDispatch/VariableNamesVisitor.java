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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OutputsNode;

public class VariableNamesVisitor extends VisitorWithOutput<List<String>> {

    private static final Map<Class<?>, VisitorAction<?, List<String>>> visitorMap;

    static {
	visitorMap = new HashMap<>();
	add(visitorMap, IdentifierNode.class, id -> (List<String>) Arrays.asList(id.getCode()));
	add(visitorMap, AccessCallNode.class, call -> (List<String>) Arrays.asList(call.getCode()));
	add(visitorMap, OutputsNode.class, outputs -> outputs.getNames());
    }

    public VariableNamesVisitor() {
	super(visitorMap);
    }

    /*
    @Override
    public List<String> visit(Object node) {
    return super.visit(node);
    if (!result.isPresent()) {
        result = Optional.of(Collections.emptyList());
    }
    
    return result;
    }
    */

    @Override
    public List<String> defaultValue(Object node) {
	throw new RuntimeException("Case not defined:" + node.getClass());
    }

    public static List<String> getNames(Object node) {
	return new VariableNamesVisitor().visit(node);
    }
}
