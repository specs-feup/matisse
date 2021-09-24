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

import java.util.List;
import java.util.stream.Collectors;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CommandNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabCharArrayNode;

import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ACall;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AExpression;
import pt.up.fe.specs.matisse.weaver.joinpoints.MJoinpointUtils;

public class MScriptCall extends ACall {
    private final CommandNode command;

    public MScriptCall(CommandNode command, AMWeaverJoinPoint parent) {
        super(new MExpression(command, parent));
        initMWeaverJP(parent);

        this.command = command;
    }

    @Override
    public String getNameImpl() {
        return this.command.getName();
    }

    @Override
    public Integer getNum_argsImpl() {
        return command.getArguments().size();
    }

    @Override
    public String getTypeImpl() {
        return "script";
    }

    @Override
    public String[] getArgumentsArrayImpl() {
        List<MatlabCharArrayNode> arguments = this.command.getArgumentNodes();

        String[] array = new String[arguments.size()];
        for (int i = 0; i < array.length; ++i) {
            array[i] = arguments.get(i).getCode();
        }

        return array;
    }

    @Override
    public List<? extends AExpression> selectArgument() {

        return this.command.getArgumentNodes().stream()
                .map(arg -> MJoinpointUtils.fromExpression(arg, this))
                .collect(Collectors.toList());
    }

    @Override
    public MatlabNode getNode() {
        return this.command;
    }
}
