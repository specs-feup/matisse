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

package org.specs.MatlabToC.CodeBuilder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.Instances.GlobalVariableInstance;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;

public abstract class VariableManager {
    private final Set<FunctionInstance> dependencies = new HashSet<>();

    public abstract Optional<VariableType> getUnprocessedVariableTypeFromFinalName(String finalName);

    public abstract Optional<VariableType> getVariableTypeFromFinalName(String finalName);

    public abstract String convertSsaToFinalName(String variableName);

    public abstract Variable generateTemporary(String proposedName, VariableType type);

    public abstract CNode generateVariableExpressionForSsaName(CInstructionList instructionsList, String ssaName,
            boolean allowSideEffects, boolean inlineOnlyLiterals);

    public Optional<VariableNode> tryGenerateVariableNodeForSsaName(String variableName) {
        final String finalName = convertSsaToFinalName(variableName);

        Optional<VariableType> variableType = getVariableTypeFromFinalName(finalName);
        if (!variableType.isPresent()) {
            return Optional.empty();
        }
        VariableType type = variableType.get();

        VariableNode node;
        if (isGlobal(finalName)) {
            String global = finalName;
            VariableType globalType = getGlobalType(global)
                    .orElseThrow(() -> new RuntimeException("Missing type for global " + global));

            GlobalVariableInstance globalInstance = new GlobalVariableInstance(global, globalType);
            addDependency(globalInstance);

            node = globalInstance.getGlobalNode();
        } else {
            node = CNodeFactory.newVariable(finalName, type);
        }
        return Optional.of(node);
    }

    public Optional<VariableType> getVariableTypeFromSsaName(String ssaName) {
        final String finalName = convertSsaToFinalName(ssaName);

        return getVariableTypeFromFinalName(finalName);
    }

    public Optional<VariableType> getUnprocessedVariableTypeFromSsaName(String ssaName) {
        final String finalName = convertSsaToFinalName(ssaName);

        return getUnprocessedVariableTypeFromFinalName(finalName);
    }

    public VariableNode generateVariableNodeForSsaName(String variableName) {
        return tryGenerateVariableNodeForSsaName(variableName)
                .orElseThrow(() -> new RuntimeException("Could not generate node for variable " + variableName));
    }

    public Optional<VariableNode> generateVariableNodeForFinalName(String variableName) {
        final Optional<VariableType> potentialVariableType = getVariableTypeFromFinalName(variableName);

        if (!potentialVariableType.isPresent()) {
            return Optional.empty();
        }
        VariableType variableType = potentialVariableType.get();
        if (isGlobal(variableName)) {
            GlobalVariableInstance globalInstanceVariable = new GlobalVariableInstance(variableName, variableType);
            addDependency(globalInstanceVariable);

            return Optional.of(globalInstanceVariable.getGlobalNode());
        }
        return Optional.of(CNodeFactory.newVariable(variableName, variableType));

    }

    public abstract boolean isGlobal(String finalName);

    public abstract Optional<VariableType> getGlobalType(String global);

    /**
     * <p>
     * Generates an expression for the given SSA name.
     * <p>
     * May inline the expression that generated that variable.
     * 
     * @param ssaName
     *            The name of the variable
     * @return The expression node
     */
    public CNode generateVariableExpressionForSsaName(CInstructionList instructionsList, String ssaName) {
        return generateVariableExpressionForSsaName(instructionsList, ssaName, true);
    }

    public CNode generateVariableExpressionForSsaName(CInstructionList instructionsList, String ssaName,
            boolean allowSideEffects) {
        return generateVariableExpressionForSsaName(instructionsList, ssaName, allowSideEffects, false);
    }

    public VariableNode generateTemporaryNode(String proposedName, VariableType type) {
        final Variable variable = generateTemporary(proposedName, type);
        return CNodeFactory.newVariable(variable);
    }

    public void addDependency(FunctionInstance instance) {
        dependencies.add(instance);
    }

    public Set<FunctionInstance> getDependencies() {
        return dependencies;
    }
}
