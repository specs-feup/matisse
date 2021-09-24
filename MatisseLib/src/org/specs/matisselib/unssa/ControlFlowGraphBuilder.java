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

package org.specs.matisselib.unssa;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InOrderBlockTraversalInstruction;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.BreakInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.WhileInstruction;

public class ControlFlowGraphBuilder {
    private ControlFlowGraphBuilder() {
    }

    private static class Context {
        public final int activeBlock;
        public final Context parentContext;

        private Context(int activeBlock, Context parentContext) {
            this.activeBlock = activeBlock;
            this.parentContext = parentContext;
        }

        public static Context buildRootContext() {
            return new Context(0, null);
        }

        public static Context buildSimpleContext(int activeBlock, Context parentContext) {
            return new Context(activeBlock, parentContext);
        }

        public static Context buildIfContext(Context parentContext, int bodyBlock, int endBlock) {
            return new IfContext(bodyBlock, endBlock, parentContext);
        }

        public static Context buildWhileContext(Context parentContext, int bodyBlock, int breakBlock,
                int continueBlock) {
            return new WhileContext(bodyBlock, breakBlock, continueBlock, parentContext);
        }

        public static Context buildForContext(Context parentContext, int bodyBlock, int breakBlock, int continueBlock) {
            return new ForContext(bodyBlock, breakBlock, continueBlock, parentContext);
        }

        public static Context buildInOrderContext(Context currentContext, List<Integer> ownedBlocks) {
            return new InOrderContext(currentContext, ownedBlocks);
        }

        public int getBreakTarget() {
            return parentContext.getBreakTarget();
        }

        public Context getBreakTargetParentContext() {
            return parentContext.getBreakTargetParentContext();
        }

        public List<Context> getNextContexts() {
            if (parentContext == null) {
                return Collections.emptyList();
            }
            return parentContext.getNextContexts();
        }
    }

    private static class IfContext extends Context {
        private final int endBlock;

        private IfContext(int activeBlock, int endBlock, Context parentContext) {
            super(activeBlock, parentContext);

            this.endBlock = endBlock;
        }

        @Override
        public List<Context> getNextContexts() {
            return Arrays.asList(Context.buildSimpleContext(endBlock, parentContext));
        }
    }

    private static class WhileContext extends Context {
        private final int breakBlock;
        private final int continueBlock;

        private WhileContext(int activeBlock, int breakBlock, int continueBlock, Context parentContext) {
            super(activeBlock, parentContext);

            this.breakBlock = breakBlock;
            this.continueBlock = continueBlock;
        }

        @Override
        public int getBreakTarget() {
            return breakBlock;
        }

        @Override
        public Context getBreakTargetParentContext() {
            return parentContext;
        }

        @Override
        public List<Context> getNextContexts() {
            return Arrays.asList(Context.buildSimpleContext(continueBlock, this));
        }
    }

    private static class ForContext extends Context {
        private final int breakBlock;
        private final int continueBlock;

        private ForContext(int activeBlock, int breakBlock, int continueBlock, Context parentContext) {
            super(activeBlock, parentContext);

            this.breakBlock = breakBlock;
            this.continueBlock = continueBlock;
        }

        @Override
        public int getBreakTarget() {
            return breakBlock;
        }

        @Override
        public Context getBreakTargetParentContext() {
            return parentContext;
        }

        @Override
        public List<Context> getNextContexts() {
            return Arrays.asList(Context.buildSimpleContext(continueBlock, this),
                    Context.buildSimpleContext(breakBlock, parentContext));
        }
    }

    private static class InOrderContext extends Context {
        private List<Integer> ownedBlocks;
        private Context parentContext;

        private InOrderContext(Context parentContext, List<Integer> ownedBlocks) {
            super(ownedBlocks.get(0), parentContext);

            this.ownedBlocks = ownedBlocks;
            this.parentContext = parentContext;
        }

        @Override
        public List<Context> getNextContexts() {
            if (ownedBlocks.size() == 1) {
                return super.getNextContexts();
            }

            return Arrays
                    .asList(Context.buildInOrderContext(parentContext, ownedBlocks.subList(1, ownedBlocks.size())));
        }
    }

    public static ControlFlowGraph build(FunctionBody body) {
        ControlFlowGraph graph = new ControlFlowGraph();

        Queue<Context> pendingBlocks = new LinkedList<>();
        Set<Integer> visitedBlocks = new HashSet<>();
        pendingBlocks.add(Context.buildRootContext());

        while (!pendingBlocks.isEmpty()) {
            Context currentContext = pendingBlocks.poll();
            int currentBlockId = currentContext.activeBlock;

            if (!visitedBlocks.add(currentBlockId)) {
                continue;
            }

            SsaBlock block = body.getBlock(currentBlockId);
            List<SsaInstruction> instructions = block.getInstructions();
            if (!instructions.isEmpty()) {
                // Nodes such as branches, whiles and fors are always the last instruction
                SsaInstruction instruction = instructions.get(instructions.size() - 1);
                if (instruction instanceof BranchInstruction) {
                    BranchInstruction branch = (BranchInstruction) instruction;

                    int trueBlock = branch.getTrueBlock();
                    int falseBlock = branch.getFalseBlock();
                    int endBlock = branch.getEndBlock();

                    graph.addEdge(currentBlockId, trueBlock);
                    graph.addEdge(currentBlockId, falseBlock);

                    pendingBlocks.add(Context.buildIfContext(currentContext, trueBlock, endBlock));
                    pendingBlocks.add(Context.buildIfContext(currentContext, falseBlock, endBlock));

                    continue;
                }
                if (instruction instanceof WhileInstruction) {
                    WhileInstruction xwhile = (WhileInstruction) instruction;

                    int loopBlock = xwhile.getLoopBlock();
                    int endBlock = xwhile.getEndBlock();

                    // We don't add edge from currentBlockId to endBlock, because at this stage all whiles
                    // have the format while(1) ... end

                    graph.addEdge(currentBlockId, loopBlock);

                    pendingBlocks.add(Context.buildWhileContext(currentContext, loopBlock, endBlock, loopBlock));

                    continue;
                }
                if (instruction instanceof ForInstruction) {
                    ForInstruction xfor = (ForInstruction) instruction;

                    int loopBlock = xfor.getLoopBlock();
                    int endBlock = xfor.getEndBlock();

                    graph.addEdge(currentBlockId, loopBlock);
                    graph.addEdge(currentBlockId, endBlock);

                    pendingBlocks.add(Context.buildForContext(currentContext, loopBlock, endBlock, loopBlock));

                    continue;
                }
                if (instruction instanceof InOrderBlockTraversalInstruction) {
                    List<Integer> ownedBlocks = instruction.getOwnedBlocks();

                    assert !ownedBlocks.isEmpty();

                    graph.addEdge(currentBlockId, ownedBlocks.get(0));
                    pendingBlocks.add(Context.buildInOrderContext(currentContext, ownedBlocks));

                    continue;
                }
                if (instruction instanceof BreakInstruction) {
                    int breakTarget = currentContext.getBreakTarget();
                    Context breakTargetParentContext = currentContext.getBreakTargetParentContext();

                    graph.addEdge(currentBlockId, breakTarget);

                    pendingBlocks.add(Context.buildSimpleContext(breakTarget, breakTargetParentContext));

                    continue;
                }

                // TODO: Continue

                assert !instruction.isEndingInstruction() : "Unrecognized instruction " + instruction;
            }

            for (Context nextContext : currentContext.getNextContexts()) {
                graph.addEdge(currentBlockId, nextContext.activeBlock);

                pendingBlocks.add(nextContext);
            }
        }

        return graph;
    }
}
