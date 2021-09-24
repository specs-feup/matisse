/**
 * Copyright 2014 SPeCS.
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

package org.specs.MatlabProcessor.MatlabParser.Rules;

import java.util.List;

import org.specs.MatlabIR.Exceptions.CodeParsingException;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.BlockSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.BreakSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.NestedFunctionSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ParForSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ReturnSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.SimpleForSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNode.nodes.statements.WhileSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.mclass.MethodsSt;
import org.specs.MatlabIR.Processor.TreeTransformException;
import org.specs.MatlabIR.Processor.TreeTransformRule;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.treenode.NodeInsertUtils;

/**
 * @author Joao Bispo
 * 
 */
public class PostParsingRules {

    private static final List<TreeTransformRule> RULES;

    static {
        RULES = SpecsFactory.newArrayList();

        PostParsingRules.RULES.add(PostParsingRules::createNestedFunctions);
        PostParsingRules.RULES.add(verifyBreakReturnInParfor());
    }

    public static List<TreeTransformRule> getRules() {
        return PostParsingRules.RULES;
    }

    private static boolean createNestedFunctions(MatlabNode token) throws TreeTransformException {
        // Check if node is a BlockSt
        if (!(token instanceof BlockSt)) {
            return false;
        }

        // First child must be a FunctionDeclarationSt
        if (!(token.getChild(0) instanceof FunctionDeclarationSt)) {
            return false;
        }

        // Must not be inside a "methods" block
        MatlabNode parent = token.getParent();
        if (parent instanceof BlockSt) {
            BlockSt parentBlock = (BlockSt) parent;
            if (parentBlock.getHeaderNode() instanceof MethodsSt) {
                return false;
            }
        }

        // Found nested function
        BlockSt block = (BlockSt) token;
        NestedFunctionSt nested = StatementFactory.newNestedFunction(block.getData().getLine(), block.getStatements());

        // Replace BlockSt node with nested node
        NodeInsertUtils.replace(block, nested);

        return true;
    }

    /**
     * Verifies if a Parfor token contains a break or a return. Throws an exception if one is found.
     * 
     * @return
     */
    private static TreeTransformRule verifyBreakReturnInParfor() {
        return new TreeTransformRule() {

            private boolean check(MatlabNode token) {
                // Apply when token is a statement of type Parfor
                return token instanceof ParForSt;
            }

            @Override
            public boolean apply(MatlabNode token) throws TreeTransformException {

                if (!check(token)) {
                    return false;
                }

                // Get parent (block)
                MatlabNode parent = token.getParent();
                if (!(parent instanceof BlockSt)) {
                    throw new TreeTransformException("Parent of statement Parfor should be a Block, instead is a '"
                            + parent.getNodeName() + "'");
                }

                checkBlock(parent, false);

                // This rule doesn't change the tree, it only verifies whether it is correct.
                return false;
            }

            private void checkBlock(MatlabNode block, boolean allowBreak) {
                for (MatlabNode child : block.getChildren()) {
                    if (child instanceof ReturnSt) {
                        throw new CodeParsingException(
                                "Found a return statement inside a parfor block, which is illegal");
                    }
                    if (!allowBreak && child instanceof BreakSt) {
                        throw new CodeParsingException(
                                "Found a break statement inside a parfor block, which is illegal");
                    }
                    if (child instanceof BlockSt) {
                        MatlabNode blockStatement = ((BlockSt) child).getStatements().get(0);
                        if (blockStatement instanceof ParForSt) {
                            throw new CodeParsingException("Found a nested parfor block, which is illegal");
                        }

                        checkBlock(child, allowBreak || blockStatement instanceof SimpleForSt
                                || blockStatement instanceof WhileSt);
                    }
                }
            }

        };
    }
}
