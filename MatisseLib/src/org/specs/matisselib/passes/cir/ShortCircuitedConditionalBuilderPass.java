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

package org.specs.matisselib.passes.cir;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Passes.InstructionsBodyPass;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.AssignmentNode;
import org.specs.CIR.Tree.CNodes.BlockNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.CNumberNode;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.InstructionNode;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.TypesOld.CNumber;
import org.specs.CIRTypes.Types.Void.VoidType;

/**
 * Converts statements such as "if(x) y = 1; else y = w;" into "y = x || w", provided that w is a proper condition.
 * 
 * @author Luís Reis
 *
 */
public class ShortCircuitedConditionalBuilderPass extends InstructionsBodyPass {

    private static final boolean ENABLE_DIAGNOSTICS = false;

    @Override
    public void apply(CInstructionList instructions, ProviderData providerData) {
        log("Starting");
        List<CNode> list = instructions.get();
        for (int i = 0; i < list.size(); i++) {
            CNode node = list.get(i);
            visitInstruction(i, node, providerData);
        }
    }

    private void visitInstruction(int index, CNode node, ProviderData providerData) {
        assert node instanceof InstructionNode;
        InstructionNode instruction = (InstructionNode) node;
        if (instruction.getInstructionType() == InstructionType.Block) {
            assert instruction.getNumChildren() == 1;
            apply(index, (BlockNode) instruction.getChild(0), providerData);
        }
    }

    private void apply(int index, BlockNode block, ProviderData providerData) {
        List<CNode> children = block.getChildren();
        for (int i = 0; i < children.size(); i++) {
            CNode child = children.get(i);
            visitInstruction(i, child, providerData);
        }

        CNode firstChild = block.getChild(0);
        if (!(firstChild instanceof InstructionNode)) {
            return;
        }

        InstructionNode firstInstruction = (InstructionNode) firstChild;
        if (firstInstruction.getInstructionType() != InstructionType.If) {
            return;
        }

        log("Found if: " + firstInstruction.toReadableString());

        if (block.getNumChildren() != 4) {
            log("Unexpected conditional format.");
            return;
        }

        // Search for else node
        int elseIndex = -1;
        for (int i = 1; i < block.getNumChildren(); ++i) {
            CNode child = block.getChild(i);
            assert child instanceof InstructionNode;

            InstructionNode instruction = (InstructionNode) child;
            if (instruction.getInstructionType() == InstructionType.Else) {
                elseIndex = i;
                break;
            }
        }

        if (elseIndex == -1) {
            // No else found.
            log("If has no else statement");
            return;
        }

        if (elseIndex != 2) {
            log("Unexpected conditional format");
            return;
        }

        CNode condition = firstInstruction.getChild(1);
        if (condition.getVariableType() instanceof VoidType) {
            log("Condition must have type");
            return;
        }
        CNode ifChild = block.getChild(1);
        CNode elseChild = block.getChild(3);

        if (!(ifChild instanceof InstructionNode)) {
            log("If content is not an instruction node. Instead: " + ifChild.getCode());
            return;
        }

        ifChild = ifChild.getChild(0);
        if (!(ifChild instanceof AssignmentNode)) {
            log("If content is not an assignment. Instead: " + ifChild.toString());
            return;
        }

        CNode leftHand = ((AssignmentNode) ifChild).getLeftHand();

        if (!(elseChild instanceof InstructionNode)) {
            log("Else content is not an instruction node. Instead: " + elseChild.getCode());
            return;
        }

        elseChild = elseChild.getChild(0);
        if (!(elseChild instanceof AssignmentNode)) {
            log("Else content is not an assignment. Instead: " + elseChild.toString());
            return;
        }

        if (!leftHand.getCode().equals(((AssignmentNode) elseChild).getLeftHand().getCode())) {
            log("Left side of assignment mismatch.");
            return;
        }

        Optional<Double> ifValue = getAssignmentValue((AssignmentNode) ifChild);
        Optional<Double> elseValue = getAssignmentValue((AssignmentNode) elseChild);

        if (!ifValue.isPresent() && !elseValue.isPresent()) {
            log("Right side of if/else assignment is not a number, got " + ((AssignmentNode) ifChild).getRightHand()
                    + ", " + ((AssignmentNode) elseChild).getRightHand());
            return;
        }

        double value;
        CNode altHand;
        boolean contentOnIf = elseValue.isPresent();
        if (contentOnIf) {
            CNode ifRightHand = ((AssignmentNode) ifChild).getRightHand();

            if (!isAcceptableElseAssignmentRightHand(ifRightHand)) {
                log("Right side of if assignment is not suitable for a conditional");
                return;
            }

            altHand = ifRightHand;
            value = elseValue.get();
        } else {
            assert ifValue.isPresent() : "No if value in " + block.getCode();

            CNode elseRightHand = ((AssignmentNode) elseChild).getRightHand();

            if (!isAcceptableElseAssignmentRightHand(elseRightHand)) {
                log("Right side of else assignment is not suitable for a conditional");
                return;
            }

            altHand = elseRightHand;
            value = ifValue.get();
        }

        log("Can merge");

        // We'll negate if contentOnIf and value == 1, or if contentOnElse and value == 0
        // If contentOnIf and value == 1, then we have if(X) { Y = Z; } else { Y = 1; }. We want !X || Z;
        // If contentOnElse and value == 0, then we have if(X) { Y = 0; } else { Y = Z; }. We want !X && Z;
        if ((value == 1) == contentOnIf) {
            condition = negate(condition, providerData);
        }

        CNode newRightHand;
        if (value == 1) {
            List<CNode> operands = Arrays.asList(condition, altHand);
            newRightHand = COperator.LogicalOr
                    .getCheckedInstance(providerData.createFromNodes(operands))
                    .newFunctionCall(operands);
        } else {
            assert value == 0;
            List<CNode> operands = Arrays.asList(condition, altHand);
            newRightHand = COperator.LogicalAnd
                    .getCheckedInstance(providerData.createFromNodes(operands))
                    .newFunctionCall(operands);
        }
        CNode newAssignment = CNodeFactory.newAssignment(leftHand, newRightHand);

        CNode parent = block.getParent().getParent();
        parent.setChild(index, CNodeFactory.newInstruction(InstructionType.Assignment, newAssignment));
    }

    private Optional<Double> getAssignmentValue(AssignmentNode assignment) {
        CNode right = assignment.getRightHand();
        if (!(right instanceof CNumberNode)) {
            return Optional.empty();
        }

        CNumberNode rightNumber = (CNumberNode) right;
        CNumber number = rightNumber.getCNumber();
        double value = number.getNumber().doubleValue();

        if (value != 0 && value != 1) {
            return Optional.empty();
        }

        return Optional.of(value);
    }

    private static CNode negate(CNode condition, ProviderData providerData) {
        if (condition instanceof FunctionCallNode) {
            FunctionCallNode call = (FunctionCallNode) condition;
            if (call.getFunctionInstance().getCName().equals(COperator.LogicalNegation.name())) {
                return call.getFunctionInputs().getInputs().get(0);
            }
        }

        return COperator.LogicalNegation
                .getCheckedInstance(providerData.createFromNodes(condition))
                .newFunctionCall(condition);
    }

    private static final List<COperator> ACCEPTABLE_OPERATORS = Arrays.asList(
            COperator.LogicalAnd,
            COperator.LogicalOr,
            COperator.LogicalNegation,
            COperator.Equal,
            COperator.NotEqual,
            COperator.GreaterThan,
            COperator.GreaterThanOrEqual,
            COperator.LessThan,
            COperator.LessThanOrEqual);

    private static boolean isAcceptableElseAssignmentRightHand(CNode node) {
        if (node instanceof CNumberNode) {
            CNumberNode number = (CNumberNode) node;
            double value = number.getCNumber().getNumber().doubleValue();

            return value == 0 || value == 1;
        }
        if (node instanceof FunctionCallNode) {
            FunctionCallNode call = (FunctionCallNode) node;

            FunctionInstance functionInstance = call.getFunctionInstance();
            if (!functionInstance.isInlined()) {
                return false;
            }

            String name = functionInstance.getCName();

            for (COperator acceptableOperator : ShortCircuitedConditionalBuilderPass.ACCEPTABLE_OPERATORS) {
                if (name.equals(acceptableOperator.name())) {
                    return true;
                }
            }

            return false;
        }

        return false;
    }

    private static void log(String message) {
        if (ShortCircuitedConditionalBuilderPass.ENABLE_DIAGNOSTICS) {
            System.out.print("[short_circuit_builder] ");
            System.out.println(message);
        }
    }
}
