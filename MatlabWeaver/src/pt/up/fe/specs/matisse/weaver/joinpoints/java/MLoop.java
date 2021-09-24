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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.BlockSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ForSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.LoopSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ParForSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.SimpleForSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNode.nodes.statements.WhileSt;

import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ABody;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AExpression;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ALoop;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AStatement;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AVar;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.enums.ALoopTypeEnum;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.enums.AVarReferenceEnum;
import pt.up.fe.specs.matisse.weaver.joinpoints.MJoinpointUtils;
import pt.up.fe.specs.matisse.weaver.joinpoints.enums.VariableType;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.treenode.NodeInsertUtils;

/**
 * @author Tiago
 * 
 */
public class MLoop extends ALoop {

    private final LoopSt loop;
    private final BlockSt block;

    public MLoop(LoopSt loop, AMWeaverJoinPoint parent) {
        initMWeaverJP(parent);

        this.loop = loop;
        this.block = (BlockSt) loop.getParent();
    }

    @Override
    public String getTypeImpl() {
        if (this.loop instanceof SimpleForSt) {
            return ALoopTypeEnum.FOR.getName();
        }
        if (this.loop instanceof ParForSt) {
            return ALoopTypeEnum.PARFOR.getName();
        }

        if (this.loop instanceof WhileSt) {
            return ALoopTypeEnum.WHILE.getName();
        }

        throw new RuntimeException("Not implemented:" + this.loop.getNodeName());
    }

    @Override
    public Boolean getIs_innermostImpl() {
        // Loop is innermost if none of its descendants is a loop
        Optional<MatlabNode> anotherLoop = this.loop
                .getAncestor(BlockSt.class)
                .getDescendantsStream()
                .filter(node -> node instanceof LoopSt && node != this.loop)
                .findFirst();

        return !anotherLoop.isPresent();

    }

    @Override
    public Boolean getIs_outermostImpl() {
        // Loop is outermost if none of its ancestors besides itself is a BlockSt of type loop
        MatlabNode parentBlock = this.loop.getParent();
        while (parentBlock.hasParent()) {
            parentBlock = parentBlock.getParent();
            if (!(parentBlock instanceof BlockSt)) {
                continue;
            }

            if (((BlockSt) parentBlock).getHeaderNode() instanceof LoopSt) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Integer getNestedLevelImpl() {
        // First parent must be a BlockSt
        MatlabNode parent = block;

        // Go back, count how many LoopSt parents there are
        parent = parent.getParent();
        int level = 0;
        while (parent != null) {
            if (parent instanceof BlockSt) {
                BlockSt block = (BlockSt) parent;
                if (block.getHeaderNode() instanceof LoopSt) {
                    level++;
                }
            }

            parent = parent.getParent();
        }

        return level;
    }

    @Override
    public List<? extends AStatement> selectHeader() {
        return Arrays.asList(new MStatement(loop, this));
    }

    /* (non-Javadoc)
     * @see org.specs.mweaver.abstracts.joinpoints.ALoop#selectControl()
     */
    @Override
    public List<? extends AVar> selectControl() {
        List<MVar> vars = new ArrayList<>();
        if (!(this.loop instanceof ForSt)) {
            return vars;
        }

        MVar mVar = new MVar(((ForSt) this.loop).getIndexVar(), this, VariableType.LOCAL, AVarReferenceEnum.WRITE);
        return Arrays.asList(mVar);
    }

    private Optional<MOperator> getColon() {
        if (loop instanceof WhileSt) {
            return Optional.empty();
        }

        MatlabNode expression = this.loop.getExpression();
        if (expression instanceof OperatorNode && ((OperatorNode) expression).getOp() == MatlabOperator.Colon) {
            return Optional.of(new MOperator((OperatorNode) expression, this));
        }
        return Optional.empty();
    }

    @Override
    public List<? extends AExpression> selectExpr() {
        Optional<MOperator> maybeColon = getColon();
        if (!maybeColon.isPresent()) {
            return Collections.emptyList();
        }

        return Arrays.asList(maybeColon.get());
    }

    /* (non-Javadoc)
     * @see org.specs.mweaver.abstracts.joinpoints.ALoop#selectInit()
     */
    @Override
    public List<? extends AExpression> selectInit() {
        return getColon()
                .map(colon -> Arrays.asList(MJoinpointUtils.fromExpression(colon.getNode().getChild(0), this)))
                .orElse(Collections.emptyList());
    }

    /* (non-Javadoc)
     * @see org.specs.mweaver.abstracts.joinpoints.ALoop#selectIncrement()
     */
    @Override
    public List<? extends AExpression> selectStep() {
        Optional<MOperator> maybeColon = getColon();
        if (!maybeColon.isPresent()) {
            return Collections.emptyList();
        }

        MOperator colon = maybeColon.get();
        if (colon.getNode().getChildren().size() == 3) {
            return Arrays.asList(MJoinpointUtils.fromExpression(colon.getNode().getChild(1), this));
        }

        return Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see org.specs.mweaver.abstracts.joinpoints.ALoop#selectCondition()
     */
    @Override
    public List<? extends AExpression> selectCond() {
        throw new RuntimeException("Not implemented");
    }

    /* (non-Javadoc)
     * @see org.specs.mweaver.abstracts.joinpoints.ALoop#selectBody()
     */
    @Override
    public List<? extends ABody> selectBody() {
        return Arrays.asList(new MBody(this.loop.getBodyStatements(), this));
    }

    @Override
    public MatlabNode getNode() {
        return this.block;
    }

    public LoopSt getLoopHeader() {
        return loop;
    }

    @Override
    public String getInductionVarImpl() {
        StatementNode stmt = block.getHeaderNode();

        // Only supported for loops of type 'for'
        if (!(stmt instanceof ForSt)) {
            throw new RuntimeException("Attribute not implemented for loops of type '" + getNode().getNodeName() + "'");
        }

        return ((ForSt) stmt).getIndexVar().getName();
    }

    @Override
    public void interchangeImpl() {

        StatementNode stmt = block.getHeaderNode();

        // Should not swap a par for, it is not guaranteed to be parallel when going to outer (confirm this)
        if (!(stmt instanceof SimpleForSt)) {
            SpecsLogs.warn("'interchange' not implemented for joinpoint '" + get_class()
                    + "', only 'for' loops (not including 'par for')");
            return;

        }

        assert stmt.getParent() instanceof BlockSt;

        if (!(block.getParent() instanceof BlockSt)) {
            SpecsLogs.warn(
                    "Selected 'for' is an outermost loop, should select a loop with a nested level of at least 2'");
            return;
        }

        BlockSt outerBlock = (BlockSt) block.getParent();
        MatlabNode outerFor = outerBlock.getHeaderNode();
        // Confirm parent loop is a for loop
        if (!(outerFor instanceof ForSt)) {
            SpecsLogs.warn(
                    "Parent loop of selected 'for' ('" + outerBlock.getHeaderNode().getNodeName()
                            + "') is not a 'for' loop");
            return;
        }

        // Check that before the block there is not other statements besides comments
        for (StatementNode child : ((BlockSt) outerBlock).getBodyStatements()) {
            // Break if it is the selected loop
            // Testing reference on purpose
            if (child == block) {
                break;
            }

            // Ignore comments
            if (child instanceof CommentSt) {
                continue;
            }

            SpecsLogs.warn(
                    "Cannot interchange selected 'for', it has instructions preceeding it that are not comments");
            return;
        }

        MatlabNode tempNode = MatlabNodeFactory.newComment("Temporary node for swap");
        // System.out.println("BEFORE:" + outerBlock);
        SimpleForSt innerFor = (SimpleForSt) stmt;
        // Replace inner loop with temp
        NodeInsertUtils.replace(innerFor, tempNode);

        // Replace outer loop with inner loop
        NodeInsertUtils.replace(outerFor, innerFor);
        // System.out.println("AFTER:" + outerBlock);
        // Replace temp with outer loop
        NodeInsertUtils.replace(tempNode, outerFor);

        // Insert a comment before
        NodeInsertUtils.insertBefore(innerFor,
                StatementFactory.newComment(innerFor.getLine(), "MatlabWeaver - Loop Interchange"));
        // LoggingUtils.msgInfo("Performed interchange");
    }

    @Override
    public String getKindImpl() {
        return getTypeImpl();
    }

}
