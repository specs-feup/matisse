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

import java.util.Collection;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.MatlabToC.MFileInstance.MatlabToCEngine;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.sizeinfo.ScalarValueInformation;
import org.specs.matisselib.providers.MatlabFunction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matisselib.unssa.ControlFlowGraph;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.reporting.MessageType;
import pt.up.fe.specs.util.reporting.Reporter;

public abstract class SsaToCBuilderService {

    public abstract TypedInstance getInstance();

    public abstract void generateCodeForBlock(int blockId, CInstructionList currentBlock);

    public abstract ProviderData getCurrentProvider();

    public abstract void addPrefixComment(String comment);

    public abstract Reporter getReporter();

    public abstract VariableManager getVariableManager();

    public abstract void generateAssignmentForFinalNames(CInstructionList currentBlock, String destination,
            String source);

    public abstract boolean generateAssignment(CInstructionList currentBlock, VariableNode leftHand,
            VariableNode rightHand);

    public abstract void setLine(int line);

    public abstract MatlabToCEngine getEngine();

    public abstract void removeUsage(String ssaName);

    public abstract Optional<MatlabFunction> getSystemFunction(String functionName);

    public abstract TypedInstance getSpecializedUserFunctionInScope(String functionName, ProviderData providerData);

    public abstract FunctionInstance buildAuxiliaryImplementation(TypedInstance instance);

    public abstract VariableNode makeNamedTemporary(String name, VariableType type);

    public abstract void addDependency(FunctionInstance instance);

    public VariableNode generateVariableNodeForSsaName(String ssaName) {
        return getVariableManager().generateVariableNodeForSsaName(ssaName);
    }

    public boolean generateAssignmentForSsaNames(CInstructionList currentBlock, String out, String in) {
        final VariableNode leftHand = generateVariableNodeForSsaName(out);
        final VariableNode rightHand = generateVariableNodeForSsaName(in);

        return generateAssignment(currentBlock, leftHand, rightHand);
    }

    public CNode generateVariableExpressionForSsaName(CInstructionList instructionsList, String ssaName) {
        return getVariableManager().generateVariableExpressionForSsaName(instructionsList, ssaName);
    }

    public CNode generateVariableExpressionForSsaName(CInstructionList instructionsList, String ssaName,
            boolean allowSideEffects) {
        return getVariableManager().generateVariableExpressionForSsaName(instructionsList, ssaName, allowSideEffects);
    }

    public CNode generateVariableExpressionForSsaName(CInstructionList instructionsList, String ssaName,
            boolean allowSideEffects, boolean inlineOnlyLiterals) {
        return getVariableManager()
                .generateVariableExpressionForSsaName(instructionsList, ssaName, allowSideEffects, inlineOnlyLiterals);
    }

    public String convertSsaToFinalName(String ssaName) {
        return getVariableManager().convertSsaToFinalName(ssaName);
    }

    public VariableNode generateTemporaryNode(String proposedName, VariableType type) {
        return getVariableManager().generateTemporaryNode(proposedName, type);
    }

    public Variable generateTemporary(String proposedName, VariableType type) {
        return getVariableManager().generateTemporary(proposedName, type);
    }

    public Optional<VariableType> getVariableTypeFromSsaName(String ssaName) {
        return getVariableManager().getVariableTypeFromSsaName(ssaName);
    }

    public Optional<VariableNode> tryGenerateVariableNodeForSsaName(String ssaName) {
        return getVariableManager().tryGenerateVariableNodeForSsaName(ssaName);
    }

    public RuntimeException emitError(MessageType messageType, String message) {
        return getReporter().emitError(messageType, message);
    }

    public Optional<VariableType> getOriginalSsaType(String ssaName) {
        return getInstance().getVariableType(ssaName);
    }

    public abstract void addLiteralVariable(Variable variable);

    public void addLiteralVariableIfNotArgument(Variable variable) {
        if (!getInstance().getFunctionType().getCInputNames().contains(variable.getName())) {
            addLiteralVariable(variable);
        }
    }

    public void addDependencies(Collection<FunctionInstance> instances) {
        instances.forEach(this::addDependency);
    }

    public abstract ControlFlowGraph getControlFlowGraph();

    public abstract DataStore getPassData();

    public ScalarValueInformation newScalarValueInformationSolver() {
        return getPassData()
                .get(ProjectPassServices.SCALAR_VALUE_INFO_BUILDER_PROVIDER)
                .build(getInstance()::getVariableType);
    }

}
