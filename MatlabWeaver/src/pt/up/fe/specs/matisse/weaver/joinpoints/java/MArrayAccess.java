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

import java.util.Arrays;
import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;

import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AArrayAccess;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AExpression;
import pt.up.fe.specs.matisse.weaver.joinpoints.MJoinpointUtils;

public class MArrayAccess extends AArrayAccess {
    private final AccessCallNode accessCall;

    public MArrayAccess(AccessCallNode call, AMWeaverJoinPoint parent) {
        super(new MExpression(call, parent));
        initMWeaverJP(parent);

        this.accessCall = call;
    }

    @Override
    public List<? extends AExpression> selectLhs() {
        return Arrays.asList(MJoinpointUtils.fromExpression(accessCall.getChild(0), this));
    }

    @Override
    public Integer getNum_argsImpl() {
        return accessCall.getNumArguments();
    }

    @Override
    public MatlabNode getNode() {
        return this.accessCall;
    }

}
