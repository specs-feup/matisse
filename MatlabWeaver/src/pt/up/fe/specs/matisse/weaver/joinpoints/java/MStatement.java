/**
 * Copyright 2013 SPeCS Research Group.
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
import org.specs.MatlabIR.MatlabNode.StatementNode;

import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AStatement;

public class MStatement extends AStatement {

    private final StatementNode statement;

    public MStatement(StatementNode statement, AMWeaverJoinPoint parent) {
        // Super
        initMWeaverJP(parent);

        // This
        this.statement = statement;
    }

    @Override
    public Integer getLineImpl() {
        return this.statement.getLine();
    }

    @Override
    public Boolean getDisplayImpl() {
        return this.statement.isDisplay();
    }

    @Override
    public MatlabNode getNode() {
        return this.statement;
    }

    @Override
    public String getTypeImpl() {
        return this.statement.getStatementType();
    }

}
