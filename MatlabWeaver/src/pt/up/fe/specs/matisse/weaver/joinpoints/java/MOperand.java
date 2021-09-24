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

package pt.up.fe.specs.matisse.weaver.joinpoints.java;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AOperand;

public class MOperand extends AOperand {

    private final MatlabNode exprNode;

    public MOperand(MatlabNode expression, AMWeaverJoinPoint parent) {
        super(new MExpression(expression, parent));
        initMWeaverJP(parent);

        this.exprNode = expression;
    }

    @Override
    public Integer getIndexImpl() {
        // Return index of this node in parent
        return exprNode.getParent().indexOfChild(exprNode);
    }

    @Override
    public Long getUidImpl() {
        // To differentiate from MExpression
        return (long) (exprNode.hashCode() + 1);
    }

    @Override
    public MatlabNode getNode() {
        return exprNode;
    }
}
