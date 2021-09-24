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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.GlobalSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ReturnSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.matisse.weaver.MatlabJoinpoints;
import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ABody;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AComment;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AFunction;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AFunctionHeader;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AVar;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.enums.AFunctionFtypeEnum;
import pt.up.fe.specs.matisse.weaver.joinpoints.MJoinpointUtils;
import pt.up.fe.specs.matisse.weaver.joinpoints.enums.VariableType;
import pt.up.fe.specs.matisse.weaver.utils.Action;
import pt.up.fe.specs.matisse.weaver.utils.When;
import pt.up.fe.specs.matisse.weaver.utils.functionadapter.FunctionAdapter;
import pt.up.fe.specs.util.SpecsCollections;

/**
 * @author Tiago
 * 
 */
public class MFunction extends AFunction {

    private final FunctionAdapter functionNode;
    private final AFunctionFtypeEnum ftype;

    public MFunction(FunctionAdapter function, AMWeaverJoinPoint parent,
            AFunctionFtypeEnum funtionType) {
        initMWeaverJP(parent);

        this.functionNode = function;
        this.ftype = funtionType;
        // this.isMainFunction = isMainFunction;
        // this.isSubFunction = !this.isMainFunction;
    }

    @Override
    public String getNameImpl() {
        return this.functionNode.getFunctionName();
    }

    @Override
    public List<? extends AVar> selectVar() {
        List<AVar> vars = new ArrayList<>();

        selectBody().forEach(body -> vars.addAll(body.selectVar()));

        return vars;
    }

    /* (non-Javadoc)
     * @see org.specs.mweaver.abstracts.joinpoints.AFunction#selectInput()
     */
    @Override
    public List<? extends AVar> selectInput() {
        List<IdentifierNode> inputs = this.functionNode.getDeclarationNode().getInputs().getNamesNodes();

        return inputs.stream()
                .map(input -> new MVar(input, this, VariableType.INPUT))
                .collect(Collectors.toList());
    }

    /* (non-Javadoc)
     * @see org.specs.mweaver.abstracts.joinpoints.AFunction#selectOutput()
     */
    @Override
    public List<? extends AVar> selectOutput() {
        List<MatlabNode> outputs = this.functionNode.getDeclarationNode().getOutputs().getChildren();

        return outputs.stream()
                .filter(node -> node instanceof IdentifierNode)
                .map(node -> (IdentifierNode) node)
                .map(output -> new MVar(output, this, VariableType.OUTPUT))
                .collect(Collectors.toList());
    }

    /* (non-Javadoc)
     * @see org.specs.mweaver.abstracts.joinpoints.AFunction#selectBody()
     */
    @Override
    public List<? extends ABody> selectBody() {

        // Get all children after finding a FunctionDeclaration
        // int functionDecIndex = StatementUtils.indexOf(MStatementType.FunctionDeclaration, matlabToken);
        // List<MatlabNode> body = CollectionUtils.subList(matlabToken.getChildren(), functionDecIndex + 1);

        // Function only has one body
        MBody mbody = new MBody(this.functionNode.getBodyStatements(), this);

        return Arrays.asList(mbody);
    }

    @Override
    public List<? extends AFunctionHeader> selectHeader() {
        return Arrays.asList(new MFunctionHeader(functionNode.getDeclarationNode(), this));
    }

    public FunctionAdapter getFunctionNode() {
        return functionNode;
    }

    @Override
    public List<? extends AComment> selectHeaderComment() {
        return functionNode.getNode().getDescendantsAndSelfStream()
                .filter(CommentSt.class::isInstance)
                .map(CommentSt.class::cast)
                .filter(comment -> comment.getLine() < functionNode.getDeclarationNode().getLine())
                .map(comment -> new MComment(comment, this))
                .collect(Collectors.toList());
    }

    @Override
    public MatlabNode getNode() {
        return this.functionNode.getNode();
    }

    @Override
    public String getFtypeImpl() {
        return this.ftype.getName();
    }

    @Override
    public String[] getQualifiedNameArrayImpl() {
        List<String> scope = this.functionNode.getScope();
        return scope.toArray(new String[scope.size()]);
    }

    @Override
    public Integer getNumberOfOutputsImpl() {
        return selectOutput().size();
    }

    @Override
    public Integer getNumberOfInputsImpl() {
        return selectInput().size();
    }

    @Override
    public void appendInputImpl(String name) {
        functionNode.getDeclarationNode().getInputs().addChild(MatlabNodeFactory.newIdentifier(name));
    }

    @Override
    public void prependInputImpl(String name) {
        functionNode.getDeclarationNode().getInputs().addChild(0, MatlabNodeFactory.newIdentifier(name));
    }

    @Override
    public void appendOutputImpl(String name) {
        functionNode.getDeclarationNode().getOutputs().addChild(MatlabNodeFactory.newIdentifier(name));
    }

    @Override
    public void prependOutputImpl(String name) {
        functionNode.getDeclarationNode().getOutputs().addChild(0, MatlabNodeFactory.newIdentifier(name));
    }

    @Override
    public void defTypeImpl(String variable, String type) {

        MApp mFile = MJoinpointUtils.getAncestor(this, MApp.class).orElseThrow(
                () -> new RuntimeException("Could not find file parent for function " + getName()));

        // New method of storing value types (directly into the Setup)
        List<String> scope = this.functionNode.getScope();
        System.out.println("Scope:" + scope);
        mFile.addVariableDef(scope, variable, type);
    }

    @Override
    public ABody getBodyImpl() {
        List<? extends ABody> body = selectBody();
        Preconditions.checkArgument(body.size() < 2, "Expected at most 1 element, got " + body.size());

        return body.isEmpty() ? null : body.get(0);
    }

    @Override
    public String getIdImpl() {
        return functionNode.getFunctionNode().map(FunctionNode::getId).orElse(Integer.toString(getNode().hashCode()));
    }

    @Override
    public void addGlobalImpl(String name) {

        List<StatementNode> bodyStmts = functionNode.getBodyStatements();

        // Find a top-level global statement
        GlobalSt globalStmt = bodyStmts.stream()
                .filter(GlobalSt.class::isInstance)
                .map(GlobalSt.class::cast)
                .findFirst()
                .orElse(null);

        // If not present, create a global statement and insert it at the top of the body
        if (globalStmt == null) {
            globalStmt = StatementFactory.newGlobalFromIds(-1, false, name);
            selectBody().get(0).insertBegin(MatlabJoinpoints.newJoinpoint(globalStmt, null));
            return;
        }

        // Add global variable
        globalStmt.addIdentifier(name);
    }

    @Override
    public void insertReturnImpl(String code) {
        // Does not take into account situations where functions returns in all paths of an if/else.
        // This means it can lead to dead-code, although for C/C++ that does not seem to be problematic.

        List<StatementNode> bodyStmts = this.functionNode.getBodyStatements();

        // Check if it has return statement
        List<ReturnSt> returnStmts = bodyStmts.stream()
                .filter(ReturnSt.class::isInstance)
                .map(ReturnSt.class::cast)
                .collect(Collectors.toList());
        ReturnSt bodyReturn = !returnStmts.isEmpty() ? returnStmts.get(0) : null;

        // Get list of all return statements inside children
        List<ReturnSt> returnStatements = bodyStmts.stream()
                .flatMap(StatementNode::getDescendantsStream)
                .filter(ReturnSt.class::isInstance)
                .map(ReturnSt.class::cast)
                .collect(Collectors.toList());

        // Add body return to the list
        if (bodyReturn != null) {
            returnStatements = SpecsCollections.concat(returnStatements, bodyReturn);
        }
        // If there is no return in the body, add at the end of the function
        else {
            getBodyImpl().insertEnd(code);
        }

        for (ReturnSt returnStmt : returnStatements) {
            Action.insert(returnStmt, When.before.getString(), code);
        }

    }
}
