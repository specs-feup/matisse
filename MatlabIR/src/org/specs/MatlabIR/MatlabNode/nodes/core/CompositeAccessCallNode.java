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

package org.specs.MatlabIR.MatlabNode.nodes.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

import com.google.common.collect.Lists;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.classmap.FunctionClassMap;

/**
 * An accessCall, which can be an index access to a variable, or a function call (ex.: a(3)). The two cases are
 * ambiguous until runtime.
 * 
 * <p>
 * The first child can be a composite node, such as x.y(2).z. The remaining children represent the expression inside the
 * parenthesis that are separated by commas.
 * 
 * @author JoaoBispo
 *
 */
public class CompositeAccessCallNode extends AccessCallNode {

    private CompositeAccessCallNode(Collection<? extends MatlabNode> children) {
	super(null, children);
    }

    CompositeAccessCallNode(Object content, Collection<MatlabNode> children) {
	this(children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new CompositeAccessCallNode(Collections.emptyList());
    }

    CompositeAccessCallNode(MatlabNode left, List<MatlabNode> arguments) {
	this(SpecsCollections.add(Lists.newArrayList(left), arguments));
    }

    @Override
    public String getName() {
	return CompositeAccessCallNode.GET_NAME_V2.apply(getChild(0));
    }

    /**
     * Returns the name of the access call, according to the type of the left node.
     */
    /*
    private final static Action<MatlabNode, String> GET_NAME = new ActionBuilder<MatlabNode, String>()
        .add(IdentifierNode.class, node -> node.getName())
        .add(CellAccessNode.class, node -> node.getName())
        .add(FieldAccessNode.class, node -> node.getCode())
        .build();
    */
    private final static FunctionClassMap<MatlabNode, String> GET_NAME_V2 = new FunctionClassMap<>();

    static {
	CompositeAccessCallNode.GET_NAME_V2.put(IdentifierNode.class, node -> node.getName());
	CompositeAccessCallNode.GET_NAME_V2.put(CellAccessNode.class, node -> node.getName());
	CompositeAccessCallNode.GET_NAME_V2.put(FieldAccessNode.class, node -> node.getCode());

    }

    /**
     * The name of the field access node is the element on the right.
     * 
     * @param node
     * @return
     */
    /*
    private static String getName(FieldAccessNode node) {
    return GET_NAME.on(node.getRight());
    }
    */

    @Override
    public List<MatlabNode> getArguments() {
	return getChildren().subList(1, getNumChildren());
    }

    @Override
    public String getCode() {
	return getChild(0).getCode() + joinCode(", ", "(", ")", getArguments());
    }
}
