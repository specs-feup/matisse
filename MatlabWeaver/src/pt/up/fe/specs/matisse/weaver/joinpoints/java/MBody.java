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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CommandNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.GlobalSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.IfSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.LoopSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;

import pt.up.fe.specs.matisse.weaver.MatlabJoinpoints;
import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AAssignment;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ABody;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ACall;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AComment;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AGlobalDeclaration;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AIf;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ALoop;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AOperator;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ASection;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AStatement;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AVar;
import pt.up.fe.specs.matisse.weaver.joinpoints.MJoinpointUtils;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.utils.SelectionUtils;
import pt.up.fe.specs.matisse.weaver.utils.Action;
import pt.up.fe.specs.matisse.weaver.utils.JPUtils;
import pt.up.fe.specs.matisse.weaver.utils.When;
import pt.up.fe.specs.util.Preconditions;
import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.treenode.TreeNodeUtils;

public class MBody extends ABody {

    private final List<StatementNode> bodyStatements;

    public MBody(StatementNode headerStatement, AMWeaverJoinPoint parent) {
        this(headerStatement.getBodyStatements(), parent);
    }

    public MBody(List<StatementNode> bodyStatements, AMWeaverJoinPoint parent) {
        initMWeaverJP(parent);
        this.bodyStatements = bodyStatements;
    }

    @Override
    public Integer getNumberOfStatementsImpl() {

        return bodyStatements.size();
    }

    @Override
    public List<? extends AStatement> selectFirst() {

        if (bodyStatements.isEmpty()) {
            return Collections.emptyList();
        }

        // return Arrays.asList(MStatement.newInstance(bodyStatements.get(0), getFileRoot(), this));
        return Arrays.asList(new MStatement(bodyStatements.get(0), this));
    }

    @Override
    public List<? extends AStatement> selectLast() {
        if (bodyStatements.isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.asList(new MStatement(SpecsCollections.last(bodyStatements), this));
    }

    @Override
    public List<? extends AStatement> selectStatement() {
        return bodyStatements.stream()
                .map(st -> new MStatement(st, this))
                .collect(Collectors.toList());
    }

    @Override
    public List<? extends AVar> selectVar() {
        List<IdentifierNode> ids = TreeNodeUtils.getDescendants(IdentifierNode.class, bodyStatements);

        return MJoinpointUtils.getVars(this, ids);
    }

    @Override
    public List<? extends ALoop> selectLoop() {
        return TreeNodeUtils.getDescendants(LoopSt.class, bodyStatements).stream()
                // Create loop
                .map(loopSt -> new MLoop(loopSt, this))
                .collect(Collectors.toList());
    }

    @Override
    public List<? extends AIf> selectIf() {
        List<? extends AIf> ifs = TreeNodeUtils.getDescendants(IfSt.class, bodyStatements).stream()
                .map(ifSt -> new MIf(ifSt, this))
                .collect(Collectors.toList());

        return ifs;
    }

    @Override
    public List<? extends ASection> selectSection() {
        // Get all comments in the statements of the body, recursively
        // and check if they are sections (start with @)
        List<CommentSt> sectionTokens = SelectionUtils.getSectionNodes(bodyStatements);

        /*
        List<CommentSt> sectionTokens = bodyStatements.stream()
        	.map(statement -> SelectionUtils.getSectionNodes(statement))
        	.reduce(new ArrayList<>(), CollectionUtils::add);
        	*/
        /*
        	List<CommentSt> sectionTokens = FactoryUtils.newArrayList();
        
        	for (MatlabNode token : bodyStatements) {
        	    List<CommentSt> comments = token.getStatements(CommentSt.class);
        	    sectionTokens.addAll(getSections(comments));
        	}
        */

        return sectionTokens.stream()
                .map(sectionSt -> new MSection(sectionSt, this))
                .collect(Collectors.toList());
        /*
        List<MSection> sections = FactoryUtils.newArrayList();
        
        for (CommentSt sectionToken : sectionTokens) {
            // sections.add(MSection.newInstance(sectionToken, getFileRoot(), this));
            sections.add(new MSection(sectionToken, getFileRoot(), this));
        }
        
        return sections;
        */
    }

    /*
        // private static List<MatlabNode> getSections(List<MatlabNode> comments) {
        private static List<CommentSt> getSections(List<CommentSt> comments) {
    	List<CommentSt> sections = FactoryUtils.newArrayList();
    
    	// for (MatlabNode comment : comments) {
    	for (CommentSt comment : comments) {
    	    // String commentString = StatementContent.getComment(comment);
    	    String commentString = comment.getCommentString();
    	    if (!commentString.startsWith("@")) {
    		continue;
    	    }
    
    	    sections.add(comment);
    	}
    
    	return sections;
        }
        */

    @Override
    public List<? extends AComment> selectComment() {
        return TreeNodeUtils.getDescendants(CommentSt.class, bodyStatements).stream()
                .map(commentSt -> new MComment(commentSt, this))
                .collect(Collectors.toList());
    }

    @Override
    public List<? extends ACall> selectCall() {
        List<AccessCallNode> accessCalls = TreeNodeUtils.getDescendants(AccessCallNode.class, bodyStatements);

        MFunction func = JPUtils.getAncestorByType(this, MFunction.class);
        // Should be App
        MFile root = JPUtils.getAncestorByType(func, MFile.class);

        List<ACall> calls = new ArrayList<>();
        accessCalls.stream()
                .filter(node -> MJoinpointUtils.isFunctionCall(func, root, node.getName()))
                .map(node -> new MConventionalCall(node, this))
                .forEach(calls::add);

        for (CommandNode command : TreeNodeUtils.getDescendants(CommandNode.class, bodyStatements)) {
            calls.add(new MScriptCall(command, this));
        }

        List<IdentifierNode> identifiers = TreeNodeUtils.getDescendants(IdentifierNode.class, bodyStatements);
        for (IdentifierNode identifier : identifiers) {
            if (!MJoinpointUtils.isFunctionCall(func, root, identifier.getName())) {
                continue;
            }

            if (identifier.getParent() instanceof CommandNode) {
                continue;
            }
            if (identifier.getParent() instanceof AccessCallNode && identifier.getParent().getChild(0) == identifier) {
                continue;
            }

            calls.add(new MImplicitCall(identifier, this));
        }

        return calls;
    }

    @Override
    public List<? extends AAssignment> selectAssignment() {
        return TreeNodeUtils.getDescendantsAndSelves(AssignmentSt.class, bodyStatements)
                .stream()
                .map(assignment -> new MAssignment(assignment, this))
                .collect(Collectors.toList());
    }

    @Override
    public List<? extends AGlobalDeclaration> selectGlobalDeclaration() {
        return TreeNodeUtils.getDescendantsAndSelves(GlobalSt.class, bodyStatements)
                .stream()
                .map(declaration -> new MGlobalDeclaration(declaration, this))
                .collect(Collectors.toList());
    }

    @Override
    public Long getUidImpl() {
        return (long) bodyStatements.hashCode();
    }

    @Override
    public String toString() {
        return "MBody (" + bodyStatements.size() + " statements)";
    }

    @Override
    public List<? extends AOperator> selectOperator() {
        List<OperatorNode> operators = TreeNodeUtils.getDescendants(OperatorNode.class, bodyStatements);
        return MJoinpointUtils.getOperators(this, operators);
    }

    @Override
    public MatlabNode getNode() {
        return StatementFactory.newBlock(-1, bodyStatements);
        // throw new UnsupportedOperationException("There is not AST node for joinpoint Body");
    }

    @Override
    public boolean compareNodes(AJoinPoint aJoinPoint) {
        return bodyStatements.equals(((MBody) aJoinPoint).bodyStatements);
    }

    @Override
    public AJoinPoint[] insertImpl(String position, String code) {
        // If no statements, not implemented yet
        if (bodyStatements.isEmpty()) {
            throw new RuntimeException("$body.insert not implemented yet when body is empty!");
        }

        switch (When.valueOf(position)) {
        case before:
            return Action.insert(bodyStatements.get(0), position, code);
        case after:
            return Action.insert(SpecsCollections.last(bodyStatements), position, code);
        default:
            throw new RuntimeException("Not implemented for " + position);

        }

    }

    @Override
    public void insertBeginImpl(AJoinPoint node) {
        insertImpl(node, When.before);
        // Action.insertMatlabNode(node.getNode(), getInsertPoint(When.before), When.before.getString());
    }

    @Override
    public void insertBeginImpl(String code) {
        insertImpl(code, When.before);
        // Action.insert(getInsertPoint(When.before), When.before.getString(), code);
    }

    @Override
    public void insertEndImpl(AJoinPoint node) {
        insertImpl(node, When.after);
        // Action.insertMatlabNode(node.getNode(), getInsertPoint(When.after), When.after.getString());
    }

    @Override
    public void insertEndImpl(String code) {
        insertImpl(code, When.after);
        // Action.insert(getInsertPoint(When.after), When.after.getString(), code);
    }

    private void insertImpl(AJoinPoint node, When when) {

        // Check if body is empty
        if (bodyStatements.isEmpty()) {
            // When empty, always insert after
            Action.insertMatlabNode(node.getNode(), getEmptyInsertPoint(), When.after.getString());
            return;
        }

        Action.insertMatlabNode(node.getNode(), getInsertPoint(when), when.getString());
    }

    private void insertImpl(String code, When when) {
        // Check if body is empty
        if (bodyStatements.isEmpty()) {
            // When empty, always insert after
            Action.insert(getEmptyInsertPoint(), When.after.getString(), code);
            return;
        }

        Action.insert(getInsertPoint(when), when.getString(), code);
    }

    private MatlabNode getEmptyInsertPoint() {

        // The insertion point depends of the kind of the parent
        AMWeaverJoinPoint parent = getParentImpl();

        if (parent instanceof MFunction) {
            return ((MFunction) parent).getFunctionNode().getDeclarationNode();
        }

        if (parent instanceof MLoop) {
            return ((MLoop) parent).getLoopHeader();
        }

        if (parent instanceof MIf) {
            return ((MIf) parent).getHeader();
        }

        if (parent instanceof MElseif) {
            return ((MElseif) parent).getHeader();
        }

        if (parent instanceof MElse) {
            return ((MElse) parent).getHeader();
        }

        throw new RuntimeException("Not supported yet for parents of type " + parent);

    }

    private MatlabNode getInsertPoint(When when) {
        Preconditions.checkArgument(!bodyStatements.isEmpty(),
                "This method should not be called when there are not statements in the body");

        switch (when) {
        case before:
            return bodyStatements.get(0);
        case after:
            return SpecsCollections.last(bodyStatements);
        default:
            throw new NotImplementedException(when);
        }

    }

    @Override
    public AJoinPoint getLastStatementImpl() {
        if (bodyStatements.isEmpty()) {
            return null;
        }

        return MatlabJoinpoints.newJoinpoint(SpecsCollections.last(bodyStatements), this);
    }
}
