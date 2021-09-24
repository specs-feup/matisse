package org.specs.matisselib.passes.cir;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Passes.InstructionsBodyPass;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.AssignmentNode;
import org.specs.CIR.Tree.CNodes.BlockNode;
import org.specs.CIR.Tree.CNodes.CNumberNode;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.InstructionNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Types.Variable;

/**
 * <p>
 * Eliminates "x = <constant>;" by replacing all references to "x" with "<constant>".
 * <p>
 * The pass is not applied for literal variables, when x is set multiple times, or when x is an output-as-input.
 * <p>
 * It is also excluded when x is an output-as-input variable.
 * 
 * @author Luís Reis
 *
 */
public class ConstantPropagationPass extends InstructionsBodyPass {

    private static final boolean ENABLE_DIAGNOSTICS = false;

    @Override
    public void apply(CInstructionList instructions, ProviderData providerData) {
        log("Starting");

        // Find all applicable assignments
        Map<String, CNode> assignments = new HashMap<>();
        Set<String> excludedVariables = new HashSet<>();

        FunctionType functionTypes = instructions.getFunctionTypes();
        if (functionTypes != null) {
            for (String input : functionTypes.getCInputNames()) {
                excludedVariables.add(input);
            }
        }
        for (Variable literal : instructions.getLiteralVariables()) {
            excludedVariables.add(literal.getName());
        }
        findAssignments(instructions.get(), assignments, excludedVariables);

        excludeOutputsAsInputs(instructions.get(), assignments);

        // Perform replacement.
        performReplacement(null, instructions.get(), assignments);
    }

    private static void excludeOutputsAsInputs(List<CNode> instructions, Map<String, CNode> assignments) {
        instructions.stream().flatMap(instruction -> instruction.getDescendantsAndSelfStream())
                .filter(FunctionCallNode.class::isInstance)
                .map(FunctionCallNode.class::cast)
                .forEach(functionCall -> {

                    FunctionType functionType = functionCall.getFunctionInstance().getFunctionType();
                    int numInputs = functionType.getCNumInputs();
                    int outputsAsInputsOffset = numInputs - functionType.getNumOutsAsIns();

                    for (int i = outputsAsInputsOffset; i < numInputs; ++i) {
                        CNode argument = functionCall.getInputTokens().get(i);
                        if (argument instanceof VariableNode) {
                            VariableNode outputAsInput = (VariableNode) argument;
                            String outputName = outputAsInput.getVariableName();
                            if (assignments.containsKey(outputName)) {
                                log("Excluding " + outputAsInput.getVariableName()
                                        + ", because it is used as output of "
                                        + functionCall.getCode());
                                assignments.remove(outputAsInput.getVariableName());
                            }
                        } else {
                            log("Output-as-input is not a variable node: " + argument);
                        }
                    }

                });
    }

    private void performReplacement(CNode parent, List<CNode> instructions, Map<String, CNode> assignments) {
        log("Assignments to replace: " + (assignments.size() < 16 ? assignments : "<Too many to show>"));

        ListIterator<CNode> iterator = instructions.listIterator();
        while (iterator.hasNext()) {
            CNode node = iterator.next();
            if (node instanceof InstructionNode) {
                InstructionNode instruction = (InstructionNode) node;
                if (instruction.getChild(0) instanceof AssignmentNode) {
                    CNode left = ((AssignmentNode) instruction.getChild(0)).getLeftHand();
                    if (left instanceof VariableNode
                            && assignments.containsKey(((VariableNode) left).getVariableName())) {

                        iterator.remove();
                        continue;
                    }
                }
            }

            if (node instanceof VariableNode) {
                VariableNode var = (VariableNode) node;
                CNode value = assignments.get(var.getVariableName());
                if (value != null) {
                    parent.setChild(iterator.nextIndex() - 1, value);
                }
            } else {
                performReplacement(node, node.getChildren(), assignments);
            }
        }
    }

    private void findAssignments(List<CNode> instructions,
            Map<String, CNode> assignments,
            Set<String> excludedVariables) {

        for (CNode node : instructions) {
            if (node instanceof InstructionNode || node instanceof BlockNode) {
                findAssignments(node.getChildren(), assignments, excludedVariables);
                continue;
            }

            if (node instanceof AssignmentNode) {
                AssignmentNode assignment = (AssignmentNode) node;
                CNode left = assignment.getLeftHand();
                CNode right = assignment.getRightHand();

                if (!(left instanceof VariableNode)) {
                    continue;
                }

                Variable leftVar = ((VariableNode) left).getVariable();
                if (leftVar.getType().pointer().isByReference()) {
                    log("Not inlining " + node + ": left hand is a pointer dereference");
                    continue;
                }
                if (leftVar.isGlobal()) {
                    log("Not inlining " + node + ": Variable is a global");
                    continue;
                }

                String varName = leftVar.getName();
                if (excludedVariables.contains(varName)) {
                    continue;
                }
                if (assignments.containsKey(varName)) {
                    excludedVariables.add(varName);
                    assignments.remove(varName);
                    log("Not inlining " + varName + ": Set multiple times");
                    continue;
                }
                if (node.getParent() instanceof InstructionNode
                        && ((InstructionNode) node.getParent()).getInstructionType() == InstructionType.For) {

                    excludedVariables.add(varName);
                    assignments.remove(varName);
                    log("Not inlining " + varName + ": Assigned in a for loop header");
                    continue;
                }
                if (!(right instanceof CNumberNode)) {
                    log("Can't inline " + right.getCode() + ": right hand is not a number node");
                    excludedVariables.add(varName);
                    assignments.remove(varName);
                    // TODO: Maybe we should handle cast nodes?
                    continue;
                }

                assignments.put(varName, right);
                continue;
            }
        }

    }

    private static void log(String message) {
        if (ConstantPropagationPass.ENABLE_DIAGNOSTICS) {
            System.out.print("[constant_propagation] ");
            System.out.println(message);
        }
    }

}
