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

import java.util.Arrays;
import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OutputsNode;

public class VariableNamesVisitorV2 extends VisitorWithOutputV2<MatlabNode, List<String>> {
    /*
        private static final Map<Class<? extends MatlabNode>, VisitorAction<? extends MatlabNode, List<String>>> visitorMap;
        static {
    	visitorMap = new HashMap<>();
    	add(visitorMap, IdentifierNode.class, id -> Arrays.asList(id.getString()));
    	add(visitorMap, AccessCallNode.class, call -> Arrays.asList(call.getNameString()));
    	add(visitorMap, OutputsNode.class, outputs -> outputs.getNames());
        }
    */
    public static final VisitorBuilder<MatlabNode, List<String>> visitors = new VisitorBuilder<>();
    static {
	visitors.add(IdentifierNode.class, id -> Arrays.asList(id.getCode()));
	visitors.add(AccessCallNode.class, call -> Arrays.asList(call.getCode()));
	visitors.add(OutputsNode.class, outputs -> outputs.getNames());
	// visitors.add(FunctionNode.class, VariableNamesVisitorV2::functionNodeVisitor);
    }

    public static List<String> getNamesV2(MatlabNode node) {
	return new GenericVisitorV2<>(VariableNamesVisitorV2.visitors).visit(node);
    }

    public VariableNamesVisitorV2() {
	// super(visitorMap);
	super(visitors);
    }

    /*
    private static List<String> functionNodeVisitor(FunctionNode node) {
    return Arrays.asList(node.getCode());
    }
    */

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

    /*
    public List<String> defaultValue(MatlabNode node) {
    throw new RuntimeException("Case not defined:" + node.getType());
    }
    */

    public static List<String> getNames(MatlabNode node) {
	return new VariableNamesVisitorV2().visit(node);
    }
}
