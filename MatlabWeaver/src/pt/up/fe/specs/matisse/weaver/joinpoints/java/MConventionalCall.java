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
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptFileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AccessCallSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ExpressionSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.UndefinedSt;

import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ACall;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AExpression;
import pt.up.fe.specs.matisse.weaver.joinpoints.MJoinpointUtils;
import pt.up.fe.specs.matisse.weaver.utils.Action;

/**
 * @author Tiago Carvalho
 * 
 */
public class MConventionalCall extends ACall {
    private final AccessCallNode accessCall;

    public MConventionalCall(AccessCallNode call, AMWeaverJoinPoint parent) {
        super(new MExpression(call, parent));
        initMWeaverJP(parent);

        this.accessCall = call;
    }

    @Override
    public String getNameImpl() {
        return this.accessCall.getName();
    }

    @Override
    public Integer getNum_argsImpl() {
        return this.accessCall.getArguments().size();
    }

    @Override
    public String getTypeImpl() {
        return "conventional";
    }

    @Override
    public String[] getArgumentsArrayImpl() {
        List<MatlabNode> arguments = this.accessCall.getArguments();

        String[] array = new String[arguments.size()];
        for (int i = 0; i < array.length; ++i) {
            array[i] = arguments.get(i).getCode();
        }

        return array;
    }

    @Override
    public List<? extends AExpression> selectArgument() {

        return this.accessCall.getArguments().stream()
                .map(arg -> MJoinpointUtils.fromExpression(arg, this))
                .collect(Collectors.toList());
        /*
        	// TODO: on tomtoken childs, ignore first child( which is the name) and capture all other tokens
        	List<TomToken> argumentTomTokens = CallStrategies.getArguments(tomToken);
        	List<AExpression> arguments = new ArrayList<AExpression>();
        	// System.out.println(TomPrettyPrinter.prettyPrint(tomToken, 0));
        	for (int i = 0; i < argumentTomTokens.size(); i++) {
        	    TomToken argument = argumentTomTokens.get(i);
        	    AExpression mArgument = new MExpression(argument, getFileRoot(), this);
        	    arguments.add(mArgument);
        	}
        	return arguments;
        	*/
    }

    @Override
    public void appendArgumentImpl(String code) {
        MatlabNode nodeToInsert = getInsertionCode(code);

        if (nodeToInsert != null) {
            accessCall.addChild(nodeToInsert);
        }
    }

    @Override
    public void prependArgumentImpl(String code) {
        if (accessCall.getArguments().size() == 0) {
            appendArgumentImpl(code);
            return;
        }

        MatlabNode nodeToInsert = getInsertionCode(code);

        if (nodeToInsert != null) {
            MatlabNode firstArgument = accessCall.getArguments().get(0);
            accessCall.addChild(accessCall.indexOfChild(firstArgument), nodeToInsert);
        }
    }

    private static MatlabNode getInsertionCode(String code) {
        if (code.trim().equals("~")) {
            return MatlabNodeFactory.newUnusedVariable();
        }

        FileNode fileNode = Action.generateMatlabScript(code);

        if (!(fileNode instanceof ScriptFileNode)) {
            throw new RuntimeException("Not implemented yet for nodes of type " + fileNode.getClass().getSimpleName());
        }

        ScriptNode scriptNode = fileNode.getScript();
        if (scriptNode.getStatements().size() != 1) {
            System.err.println("[MWeaver] Invalid code for argument insertion.");
            return null;
        }

        StatementNode stmt = scriptNode.getStatements().get(0);

        if (stmt instanceof UndefinedSt ||
                stmt instanceof ExpressionSt ||
                stmt instanceof AccessCallSt) {

            return stmt.getChild(0);
        }

        System.err.println("[MWeaver] Invalid code for argument insertion.");
        return null;
    }

    @Override
    public MatlabNode getNode() {
        return this.accessCall;
    }

}
