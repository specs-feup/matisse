package org.specs.matisselib.passes.cir;

import java.util.Collection;
import java.util.List;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Passes.InstructionsBodyPass;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.BlockNode;
import org.specs.CIR.Tree.CNodes.CommentNode;
import org.specs.CIR.Tree.CNodes.InstructionNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Types.Variable;

/**
 * Replaces "if(x) { w } if(x) { z }" with "if(x) { w z }"
 * 
 * @author Luís Reis
 *
 */
public class RepeatedIfPass extends InstructionsBodyPass {

    private static final boolean ENABLE_DIAGNOSTICS = false;

    @Override
    protected void apply(CInstructionList instructions, ProviderData providerData) {
        log("Starting");

        visit(instructions.getLiteralVariables(), instructions.get(), providerData);
    }

    private void visit(Collection<Variable> literals, List<CNode> list, ProviderData providerData) {
        for (int i = list.size() - 1; i >= 1; --i) {
            int secondIndex = i;

            CNode secondNode = list.get(i);
            log("Visiting " + secondNode.toReadableString());

            BlockNode secondBlock = getBlockOf(secondNode);
            if (secondBlock == null) {
                log("Not a block");
                continue;
            }
            if (!isValidIf(secondBlock)) {
                log("Not an if");
                continue;
            }

            log("Checking " + secondBlock.getCode());

            CNode firstNode = null;
            boolean found = false;
            for (--i; i >= 0; --i) {
                firstNode = list.get(i);
                if (firstNode instanceof CommentNode) {
                    continue;
                }
                if (firstNode instanceof InstructionNode
                        && ((InstructionNode) firstNode).getInstructionType() == InstructionType.Block) {
                    found = true;
                    break;
                }

                found = false;
                break;
            }

            // We moved "one too many" in the inner loop. Go back so we won't impact future iterations.
            ++i;

            if (!found) {
                log("No prior if found");
                continue;
            }
            BlockNode firstBlock = getBlockOf(firstNode);

            if (!isValidIf(firstBlock)) {
                log("Prior block is not a valid if");
                continue;
            }

            log("Found chain of ifs");

            // Check if conditions are the same
            InstructionNode header1 = firstBlock.getHeader();
            InstructionNode header2 = secondBlock.getHeader();

            if (header1.getChildren().size() != 2) {
                log("Condition isn't variable " + header1);
                continue;
            }
            if (header2.getChildren().size() != 2) {
                log("Condition isn't variable");
                continue;
            }

            // First node is "if" keyword
            CNode cond1 = header1.getChild(1);
            CNode cond2 = header2.getChild(1);

            if (!(cond1 instanceof VariableNode) || !(cond2 instanceof VariableNode)) {
                log("Conditions aren't variable: " + cond1.getCode() + ", " + cond2.getCode());
                continue;
            }

            VariableNode v1 = (VariableNode) cond1;
            VariableNode v2 = (VariableNode) cond2;

            if (!v1.getVariableName().equals(v2.getVariableName())) {
                log("Not same variable");
                continue;
            }

            log("Checking if condition variable is modified");

            Variable var = v1.getVariable();
            if (literals.contains(var)) {
                continue;
            }

            if (uses(firstBlock, var)) {
                continue;
            }

            log("Merging if blocks for " + var.getName());

            // Contents of second block are moved to the body of the first block
            // and the second block is removed.
            // But only after the header
            List<CNode> children2 = secondBlock.getChildren();
            list.remove(secondIndex);

            boolean foundHeader = false;
            for (CNode child : children2) {
                if (foundHeader) {
                    firstBlock.addChild(child);
                } else {
                    foundHeader = child instanceof InstructionNode;
                }
            }
        }

        for (CNode node : list) {
            visitInstruction(literals, node, providerData);
        }
    }

    private BlockNode getBlockOf(CNode node) {
        if (!(node instanceof InstructionNode)) {
            return null;
        }

        InstructionNode inst = (InstructionNode) node;
        if (inst.getInstructionType() != InstructionType.Block) {
            return null;
        }

        return (BlockNode) inst.getChild(0);
    }

    private boolean uses(BlockNode firstBlock, Variable var) {
        return firstBlock.getDescendantsStream()
                .filter(VariableNode.class::isInstance)
                .map(VariableNode.class::cast)
                .skip(1) // Skip loop header
                .anyMatch(varNode -> varNode.getVariableName().equals(var.getName()));
    }

    private boolean isValidIf(BlockNode block) {
        InstructionNode header = block.getHeader();
        if (header.getInstructionType() != InstructionType.If) {
            return false;
        }

        if (hasElseOrElseIf(block)) {
            return false;
        }

        return true;
    }

    private boolean hasElseOrElseIf(BlockNode block) {
        return block.getChildren().stream()
                .filter(InstructionNode.class::isInstance)
                .map(InstructionNode.class::cast)
                .anyMatch(instruction -> instruction.getInstructionType() == InstructionType.Else ||
                        instruction.getInstructionType() == InstructionType.ElseIf);
    }

    private void visitInstruction(Collection<Variable> literals, CNode node, ProviderData providerData) {
        visit(literals, node.getChildren(), providerData);
    }

    private static void log(String message) {
        if (RepeatedIfPass.ENABLE_DIAGNOSTICS) {
            System.out.print("[repeated_if] ");
            System.out.println(message);
        }
    }

}
