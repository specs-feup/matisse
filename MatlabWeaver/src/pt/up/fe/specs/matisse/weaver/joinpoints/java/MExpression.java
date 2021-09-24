/*
 * Copyright 2013 SPeCS.
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
package pt.up.fe.specs.matisse.weaver.joinpoints.java;

import java.util.List;
import java.util.stream.Collectors;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;

import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AExpression;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AVar;
import pt.up.fe.specs.matisse.weaver.joinpoints.MJoinpointUtils;

public class MExpression extends AExpression {
    private final MatlabNode expression;

    public MExpression(MatlabNode expression, AMWeaverJoinPoint parent) {
        initMWeaverJP(parent);

        this.expression = expression;
    }

    @Override
    public String getValueImpl() {
        return expression.getCode();
    }

    @Override
    public List<? extends AVar> selectVar() {
        List<IdentifierNode> ids = expression.getDescendantsAndSelfStream()
                .filter(node -> node instanceof IdentifierNode)
                .map(node -> (IdentifierNode) node)
                .collect(Collectors.toList());

        return MJoinpointUtils.getVars(this, ids);
    }

    @Override
    public MatlabNode getNode() {
        return expression;
    }
}
