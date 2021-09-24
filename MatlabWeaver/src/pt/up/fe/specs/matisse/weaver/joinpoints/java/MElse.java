/**
 * Copyright 2017 SPeCS.
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
import org.specs.MatlabIR.MatlabNode.nodes.statements.ElseSt;

import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ABody;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AElse;

public class MElse extends AElse {

    private final ElseSt elseSt;
    // private final BlockSt block;

    public MElse(ElseSt elseSt, AMWeaverJoinPoint parent) {
        initMWeaverJP(parent);

        this.elseSt = elseSt;
        // block = (BlockSt) elseSt.getParent();
    }

    @Override
    public MatlabNode getNode() {
        return elseSt;
    }

    @Override
    public List<? extends ABody> selectBody() {
        return Arrays.asList(new MBody(elseSt.getBodyStatements(), this));
    }

    public ElseSt getHeader() {
        return elseSt;
    }

}
