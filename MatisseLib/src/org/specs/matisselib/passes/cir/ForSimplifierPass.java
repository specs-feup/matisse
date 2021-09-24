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

package org.specs.matisselib.passes.cir;

import java.util.ArrayList;
import java.util.List;

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
import org.specs.CIR.Tree.CNodes.FunctionInputsNode;
import org.specs.CIR.Tree.CNodes.InstructionNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.CNodes.VerbatimNode;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Conversion.ConversionUtils;

/**
 * Converts "for (i = 1; i <= N; ++i)" into "for(i = 0; i < N; ++i)" under certain conditions.
 * <p>
 * The for must not have any verbatim children, and must not use i in any way other than "i - 1".
 * <p>
 * This pass will generate incorrect code if "i" is used after the loop without being set first.
 * 
 * @author Luís Reis
 *
 */
public class ForSimplifierPass extends InstructionsBodyPass {

    private static final boolean ENABLE_DIAGNOSTICS = false;

    @Override
    public void apply(CInstructionList instructions, ProviderData providerData) {
        log("Starting");
        for (CNode node : instructions.get()) {
            visitInstruction(node, providerData);
        }
    }

    private boolean visitInstruction(CNode node, ProviderData providerData) {
        assert node instanceof InstructionNode;
        InstructionNode instruction = (InstructionNode) node;

        boolean foundLiteral = false;

        if (instruction.getInstructionType() == InstructionType.Block) {
            assert instruction.getNumChildren() == 1;
            if (apply((BlockNode) instruction.getChild(0), providerData)) {
                foundLiteral = true;
            }
        } else if (instruction.getDescendantsAndSelfStream().anyMatch(VerbatimNode.class::isInstance)) {
            log("Found literal node");
            foundLiteral = true;
        }

        return foundLiteral;
    }

    private boolean apply(BlockNode block, ProviderData providerData) {
        for (CNode child : block.getChildren()) {
            if (visitInstruction(child, providerData)) {
                return true;
            }
        }

        // CNode blockHeader = block.getChild(0);
        // assert blockHeader instanceof InstructionNode;

        InstructionNode firstInstruction = block.getHeader();
        if (firstInstruction.getInstructionType() != InstructionType.For) {
            return false;
        }

        CNode start = firstInstruction.getChild(1);
        CNode condition = firstInstruction.getChild(2);
        CNode step = firstInstruction.getChild(3);

        if (!(start instanceof AssignmentNode)) {
            log("Non-standard for: Start is not an assignment: " + start.getCode());
            return false;
        }

        CNode inductionVarNode = start.getChild(0);
        CNode initialValue = start.getChild(1);

        if (!(inductionVarNode instanceof VariableNode)) {
            log("Non-standard for: Instead of induction variable, found: " + inductionVarNode.getCode());
            return false;
        }
        Variable inductionVar = ((VariableNode) inductionVarNode).getVariable();

        if (!(initialValue instanceof CNumberNode)
                || ((CNumberNode) initialValue).getCNumber().getNumber().doubleValue() != 1) {

            log("Non-standard for: Expected a start value of 1, got " + initialValue.toReadableString());
            return false;
        }

        if (!(condition instanceof FunctionCallNode)) {
            log("Non-standard for: Expected function call as condition, got " + condition.toReadableString());
            return false;
        }

        FunctionCallNode conditionCall = (FunctionCallNode) condition;
        String functionName = conditionCall.getFunctionInstance().getCName();
        if (!(functionName.equals("LessThanOrEqual"))) {
            log("Non-standard for: Expected <= as condition, got " + condition.toReadableString());
            return false;
        }
        List<CNode> inputs = conditionCall.getInputTokens();
        if (inputs.size() != 2) {
            log("<= should have 2 inputs. What's going on? " + conditionCall.toReadableString());
            assert false;
            return false;
        }
        if (!inputs.get(0).getCode().equals(inductionVar.getName())) {
            log("Non-standard for. Condition does not start with induction variable: "
                    + conditionCall.toReadableString());
            return false;
        }

        CNode end = inputs.get(1);

        if (!(step instanceof AssignmentNode)) {
            log("Non-standard for: Expected assignment as step, got: " + step.toReadableString());
            return false;
        }
        AssignmentNode stepAssignment = (AssignmentNode) step;
        if (!stepAssignment.getLeftHand().getCode().equals(inductionVar.getName())) {
            log("Non-standard for: Step assignment does not set induction variable: "
                    + stepAssignment.toReadableString());
            return false;
        }
        CNode stepValue = stepAssignment.getRightHand();
        if (!(stepValue instanceof FunctionCallNode)) {
            log("Non-standard for: Step is not being set to result of function call: " + stepValue.toReadableString());
            return false;
        }
        FunctionCallNode stepFunctionCall = (FunctionCallNode) stepValue;
        if (!stepFunctionCall.getFunctionInstance().getCName().equals("Addition")) {
            log("Step is not a growth addition: " + stepFunctionCall.toReadableString());
            return false;
        }

        CNode stepFunctionCallInitialValue = stepFunctionCall.getInputTokens().get(0);
        CNode stepFunctionCallIncrement = stepFunctionCall.getInputTokens().get(1);

        if (!stepFunctionCallInitialValue.getCode().equals(inductionVar.getName())) {
            log("Non-standard for: Step is not increasing the induction variable value: " + step.toReadableString());
            return false;
        }
        if (!(stepFunctionCallIncrement instanceof CNumberNode)
                || ((CNumberNode) stepFunctionCallIncrement).getCNumber().getNumber().doubleValue() != 1) {

            log("Non-standard for: Step value is not a constant 1: " + stepFunctionCallIncrement.toReadableString());
            return false;
        }

        log("For header is standard");

        List<CNode> subtractions = new ArrayList<>();
        for (int i = 1; i < block.getNumChildren(); ++i) {
            CNode statement = block.getChild(i);
            for (VariableNode identifier : statement.getDescendants(VariableNode.class)) {
                if (!identifier.getVariableName().equals(inductionVar.getName())) {
                    continue;
                }

                CNode parent = identifier.getParent();
                if (!(parent instanceof FunctionInputsNode)) {
                    log("Found induction variable outside a subtraction: " + parent.toReadableString());
                    return false;
                }
                FunctionCallNode functionCall = (FunctionCallNode) parent.getParent();
                if (!functionCall.getFunctionInstance().getCName().equals("Subtraction")) {
                    log("Found induction variable outside a subtraction: " + functionCall.getCode());
                    return false;
                }

                if (parent.indexOfChild(identifier) != 0) {
                    log("Induction variable must be first child of subtraction: " + functionCall.getCode());
                    return false;
                }

                CNode rightHand = parent.getChild(1);
                if (!(rightHand instanceof CNumberNode)
                        || ((CNumberNode) rightHand).getCNumber().getNumber().doubleValue() != 1) {

                    log("Subtraction right hand is not 1: " + functionCall.getCode());
                    return false;
                }

                log("Found induction variable in: " + functionCall.getCode());
                subtractions.add(functionCall);
            }
        }

        log("Ready for replacement");

        // Fix start value
        start.setChild(1, CNodeFactory.newCNumber(0));

        VariableType endType = end.getVariableType();
        assert endType instanceof ScalarType : "Right side of <= should be a scalar";
        if (!ScalarUtils.isInteger(endType)) {
            end = ConversionUtils.to(end, providerData.getNumerics().newInt());
        }

        // Fix condition
        CNode newCondition = COperator.LessThan
                .getCheckedInstance(providerData.createFromNodes(inductionVarNode, end))
                .newFunctionCall(inductionVarNode, end);
        firstInstruction.setChild(2, newCondition);

        // Replace "i - 1" by "i" in body
        for (CNode subtraction : subtractions) {
            CNode parent = subtraction.getParent();
            parent.setChild(parent.indexOfChild(subtraction), inductionVarNode);
        }

        return false;
    }

    private static void log(String message) {
        if (ForSimplifierPass.ENABLE_DIAGNOSTICS) {
            System.out.print("[for_simplifier] ");
            System.out.println(message);
        }
    }
}
