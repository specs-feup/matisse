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

package org.specs.MatlabToC.CodeBuilder.SsaToC;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.MatlabToC.CodeBuilder.VariableManager;
import org.specs.MatlabToC.MFileInstance.MatlabToCEngine;
import org.specs.matisselib.providers.MatlabFunction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matisselib.unssa.ControlFlowGraph;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.reporting.Reporter;

public class MockSsaToCBuilderService extends SsaToCBuilderService {

    public Map<String, MatlabFunction> systemFunctions = new HashMap<>();
    public Reporter reporter;

    @Override
    public TypedInstance getInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void generateCodeForBlock(int blockId, CInstructionList currentBlock) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProviderData getCurrentProvider() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPrefixComment(String comment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reporter getReporter() {
        if (reporter == null) {
            throw new UnsupportedOperationException();
        }

        return reporter;
    }

    @Override
    public VariableManager getVariableManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void generateAssignmentForFinalNames(CInstructionList currentBlock, String destination, String source) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean generateAssignment(CInstructionList currentBlock, VariableNode leftHand, VariableNode rightHand) {
        currentBlock.addLiteralInstruction("<Assign " + leftHand.getCode() + " = " + rightHand.getCode() + ">\n");

        return true;
    }

    @Override
    public void setLine(int line) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MatlabToCEngine getEngine() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeUsage(String ssaName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<MatlabFunction> getSystemFunction(String functionName) {
        return Optional.ofNullable(systemFunctions.get(functionName));
    }

    @Override
    public TypedInstance getSpecializedUserFunctionInScope(String functionName, ProviderData providerData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FunctionInstance buildAuxiliaryImplementation(TypedInstance instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VariableNode makeNamedTemporary(String name, VariableType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addDependency(FunctionInstance instance) {
        throw new UnsupportedOperationException();
    }

    public void addDummyFunction(String functionName, FunctionType functionType) {
        systemFunctions.put(functionName, new MatlabFunction() {

            @Override
            public String getFunctionName() {
                return functionName;
            }

            @Override
            public Optional<InstanceProvider> accepts(ProviderData data) {
                return Optional.of(this);
            }

            @Override
            public FunctionType getType(ProviderData data) {
                return functionType;
            }

            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                return new InstructionsInstance(functionName, "dummy.c", new CInstructionList(functionType));
            }
        });
    }

    @Override
    public void addLiteralVariable(Variable variable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ControlFlowGraph getControlFlowGraph() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataStore getPassData() {
        throw new UnsupportedOperationException();
    }
}
