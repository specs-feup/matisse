/**
 * Copyright 2016 SPeCS.
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

import java.util.Collections;
import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;

import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ACall;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AExpression;

public class MImplicitCall extends ACall {
    private final IdentifierNode identifier;

    public MImplicitCall(IdentifierNode identifier, AMWeaverJoinPoint parent) {
        super(new MExpression(identifier, parent));
        initMWeaverJP(parent);

        this.identifier = identifier;
    }

    @Override
    public String getNameImpl() {
        return this.identifier.getName();
    }

    @Override
    public Integer getNum_argsImpl() {
        return 0;
    }

    @Override
    public String getTypeImpl() {
        return "implicit";
    }

    @Override
    public String[] getArgumentsArrayImpl() {
        return new String[0];
    }

    @Override
    public List<? extends AExpression> selectArgument() {

        return Collections.emptyList();
    }

    @Override
    public MatlabNode getNode() {
        return identifier;
    }
}
