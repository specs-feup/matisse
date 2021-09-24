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

package org.specs.MatlabToC.CodeBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.AssignmentNode;
import org.specs.CIR.Tree.CNodes.CNumberNode;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.InstructionNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.helpers.UsageMap;
import org.specs.matisselib.services.GlobalTypeProvider;
import org.specs.matisselib.unssa.VariableAllocation;

public final class DefaultVariableManager extends VariableManager {
    // For debugging purposes
    private static final boolean DISABLE_INLINE_EXPRESSIONS = false;

    private static final int EXPRESSION_INLINING_THRESHOLD = 100;

    private final List<String> variableNames;
    private final VariableAllocation allocations;
    private final UsageMap usageMap;
    private final GlobalTypeProvider globalTypeGetter;
    private final BiFunction<List<String>, GlobalTypeProvider, Optional<VariableType>> typeCombiner;
    private final BiFunction<String, VariableType, VariableType> typeDecorator;

    public DefaultVariableManager(
            List<String> variableNames,
            VariableAllocation allocations,
            UsageMap usageMap,
            GlobalTypeProvider globalTypeGetter,
            BiFunction<List<String>, GlobalTypeProvider, Optional<VariableType>> typeCombiner,
            BiFunction<String, VariableType, VariableType> typeDecorator) {

        this.variableNames = variableNames;
        this.allocations = allocations;
        this.usageMap = usageMap;
        this.globalTypeGetter = globalTypeGetter;
        this.typeCombiner = typeCombiner;
        this.typeDecorator = typeDecorator;
    }

    @Override
    public Optional<VariableType> getUnprocessedVariableTypeFromFinalName(String finalName) {
        final int groupId = this.variableNames.indexOf(finalName);
        final List<String> variableGroup = this.allocations.getVariableGroups().get(groupId);

        return getCandidateVariableTypeFromVariableGroup(variableGroup);
    }

    @Override
    public Optional<VariableType> getVariableTypeFromFinalName(String finalName) {
        final int groupId = this.variableNames.indexOf(finalName);
        String variableName = this.variableNames.get(groupId);
        final List<String> variableGroup = this.allocations.getVariableGroups().get(groupId);

        return getCandidateVariableTypeFromVariableGroup(variableGroup)
                .map(type -> this.typeDecorator.apply(variableName, type));
    }

    private Optional<VariableType> getCandidateVariableTypeFromVariableGroup(List<String> variableGroup) {
        return this.typeCombiner.apply(variableGroup, globalTypeGetter);
    }

    @Override
    public String convertSsaToFinalName(String variableName) {
        final int groupId = this.allocations.getGroupIdForVariable(variableName);

        assert groupId >= 0 : "Failure for variable " + variableName + ".";

        return this.variableNames.get(groupId);
    }

    @Override
    public CNode generateVariableExpressionForSsaName(CInstructionList instructionsList, String ssaName,
            boolean allowSideEffects, boolean inlineOnlyLiterals) {

        final Optional<InstructionNode> assignment = getVariableExpressionToInline(instructionsList, ssaName,
                allowSideEffects);

        if (assignment.isPresent()) {
            final InstructionNode xAssignment = assignment.get();

            final AssignmentNode assignmentNode = (AssignmentNode) xAssignment.getChild(0);
            final CNode rightHand = assignmentNode.getRightHand();
            if (!inlineOnlyLiterals || rightHand instanceof CNumberNode) {
                instructionsList.get().remove(xAssignment);
                return ((AssignmentNode) xAssignment.getChild(0)).getRightHand();
            }

        }

        return generateVariableNodeForSsaName(ssaName);
    }

    private Optional<InstructionNode> getVariableExpressionToInline(CInstructionList instructionsList, String ssaName,
            boolean allowSideEffects) {

        if (DefaultVariableManager.DISABLE_INLINE_EXPRESSIONS) {
            return Optional.empty();
        }

        if (!ssaName.startsWith("$")) {
            // Not a temporary
            if (!ssaName.endsWith("$ret")) {
                // We want to allow the inlining of return statements.
                return Optional.empty();
            }
        }

        final int groupId = this.allocations.getGroupIdForVariable(ssaName);
        assert groupId >= 0 : "Could not find variable " + ssaName;
        final String finalName = this.variableNames.get(groupId);
        final List<String> variableGroup = this.allocations.getVariableGroups().get(groupId);
        if (variableGroup.size() != 1) {
            return Optional.empty();
        }

        if (this.usageMap.getUsageCount(ssaName) != 1) {
            return Optional.empty();
        }

        final List<CNode> nodes = instructionsList.get();

        final InstructionNode lastNode = findRelevantAssignment(finalName, nodes, allowSideEffects);

        if (lastNode == null) {
            return Optional.empty();
        }
        if (lastNode.getInstructionType() == org.specs.CIR.Tree.Instructions.InstructionType.Assignment) {
            final AssignmentNode assignment = (AssignmentNode) lastNode.getChild(0);

            if (!allowSideEffects) {
                final boolean canHaveSideEffects = mayHaveSideEffects(assignment.getRightHand());
                if (canHaveSideEffects) {
                    return Optional.empty();
                }
            }

            return Optional.of(lastNode);
        }

        return Optional.empty();
    }

    private static InstructionNode findRelevantAssignment(String finalName, List<CNode> nodes,
            boolean allowSideEffects) {
        final List<String> blacklistedVariables = new ArrayList<>();

        for (int i = nodes.size() - 1; i >= Math.max(0, nodes.size() - EXPRESSION_INLINING_THRESHOLD); --i) {
            final InstructionNode node = (InstructionNode) nodes.get(i);

            if (node.getInstructionType() == org.specs.CIR.Tree.Instructions.InstructionType.Comment) {
                continue;
            }

            if (node.getInstructionType() == org.specs.CIR.Tree.Instructions.InstructionType.FunctionCall) {
                final FunctionCallNode functionCall = (FunctionCallNode) node.getChild(0);

                if (usesVariableInList(functionCall, Arrays.asList(finalName))) {
                    return null;
                }

                if (mayHaveSideEffects(functionCall) && allowSideEffects) {
                    // If not, we'd risk reordering side-effects
                    return null;
                }

                blacklistedVariables.addAll(getReferencedVariables(functionCall));

                continue;
            }

            if (node.getInstructionType() != org.specs.CIR.Tree.Instructions.InstructionType.Assignment) {
                return null;
            }

            final AssignmentNode assignment = (AssignmentNode) node.getChild(0);
            final CNode leftSide = assignment.getLeftHand();
            if (!(leftSide instanceof VariableNode)) {
                return null;
            }

            final VariableNode leftVariable = (VariableNode) leftSide;
            final String leftVariableName = leftVariable.getVariableName();

            if (leftVariableName.equals(finalName)) {
                if (!usesVariableInList(node, blacklistedVariables)) {
                    return node;
                }
                return null;
            }

            blacklistedVariables.add(leftVariableName);

            if (mayHaveSideEffects(assignment) && allowSideEffects) {
                continue;
            }

            if (usesVariableInList(assignment, Arrays.asList(finalName))) {
                return null;
            }
        }

        return null;
    }

    private static List<String> getReferencedVariables(CNode node) {
        return node.getDescendantsAndSelfStream()
                .filter(VariableNode.class::isInstance)
                .map(VariableNode.class::cast)
                .map(v -> v.getVariableName())
                .distinct()
                .collect(Collectors.toList());
    }

    private static boolean usesVariableInList(CNode node, List<String> vars) {
        return node.getDescendantsAndSelfStream()
                .filter(VariableNode.class::isInstance)
                .map(VariableNode.class::cast)
                .anyMatch(v -> vars.contains(v.getVariableName()));
    }

    private static boolean mayHaveSideEffects(CNode assignment) {
        return assignment.getDescendantsAndSelfStream()
                .filter(FunctionCallNode.class::isInstance)
                .map(FunctionCallNode.class::cast)
                .anyMatch(call -> call.getFunctionInstance().getFunctionType().canHaveSideEffects());
    }

    @Override
    public Optional<VariableType> getGlobalType(String global) {
        return globalTypeGetter.get(global);
    }

    @Override
    public Variable generateTemporary(String proposedName, VariableType type) {
        proposedName = proposedName.replace('$', '_');
        proposedName = proposedName.replaceAll("^_+", ""); // Remove leading underscores
        proposedName = proposedName.replaceAll("_+", "_");
        if (proposedName.matches("^[0-9]")) {
            proposedName = "v_" + proposedName; // Names can't start with digits
        }

        String candidateName;
        int i = 0;
        for (;;) {
            candidateName = proposedName + "_" + (++i);

            if (!this.variableNames.contains(candidateName)) {
                break;
            }
        }

        variableNames.add(candidateName);

        final Variable variable = new Variable(candidateName, type);
        return variable;
    }

    @Override
    public boolean isGlobal(String finalName) {
        int groupId = variableNames.indexOf(finalName);
        assert groupId >= 0;

        List<String> group = allocations.getVariableGroups().get(groupId);
        for (String var : group) {
            if (var.startsWith("^")) {
                assert var.substring(1).equals(finalName) : "Global " + var + " was given incorrect final name "
                        + finalName
                        + ". At: " + allocations;
                return true;
            }
        }
        return false;
    }
}
