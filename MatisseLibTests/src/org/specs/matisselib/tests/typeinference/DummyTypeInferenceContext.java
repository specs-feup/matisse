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

package org.specs.matisselib.tests.typeinference;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.functionproperties.FunctionProperty;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.AbstractTypeInferenceContext;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

public class DummyTypeInferenceContext extends AbstractTypeInferenceContext {

    public final Map<String, VariableType> variableTypes = new HashMap<>();
    public String loopStartName;
    public String loopIntervalName;
    private final ProviderData providerData;

    public DummyTypeInferenceContext(ProviderData providerData) {
        this.providerData = providerData;
    }

    @Override
    public TypedInstance getInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends FunctionProperty> Stream<T> getPropertyStream(Class<T> cls) {
        return Stream.empty();
    }

    @Override
    public DataStore getPassData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProviderData getProviderData() {
        return this.providerData;
    }

    @Override
    public void addVariable(String variableName, VariableType variableType) {
        Preconditions.checkArgument(variableName != null);
        Preconditions.checkArgument(variableType != null);
        Preconditions.checkState(!this.variableTypes.containsKey(variableName));

        this.variableTypes.put(variableName, variableType);
    }

    @Override
    public Optional<VariableType> getVariableType(String variableName) {
        return Optional.ofNullable(this.variableTypes.get(variableName));
    }

    @Override
    public Optional<VariableType> getDefaultVariableType(String variableName) {
        return Optional.empty();
    }

    @Override
    public boolean isInterrupted() {
        return false;
    }

    @Override
    public void doBreak() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void doContinue(int blockId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void markUnreachable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addValueSpecialization(String variableName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void pushInstructionModification(InstructionLocation location, SsaInstruction newInstruction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void pushInstructionRemoval(InstructionLocation location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> getForLoopStartName() {
        return Optional.of(loopStartName);
    }

    @Override
    public Optional<String> getForLoopIntervalName() {
        return Optional.ofNullable(loopIntervalName);
    }
}
