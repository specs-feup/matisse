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

package org.specs.matisselib.typeinference;

import java.util.Optional;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

public abstract class ChildTypeInferenceContext extends AbstractTypeInferenceContext {

    private final TypeInferenceContext parentContext;

    public ChildTypeInferenceContext(TypeInferenceContext parentContext) {
        Preconditions.checkArgument(parentContext != null);

        this.parentContext = parentContext;
    }

    public TypeInferenceContext getParentContext() {
        return this.parentContext;
    }

    @Override
    public TypedInstance getInstance() {
        return this.parentContext.getInstance();
    }

    @Override
    public DataStore getPassData() {
        return this.parentContext.getPassData();
    }

    @Override
    public Optional<VariableType> getDefaultVariableType(String variableName) {
        return this.parentContext.getDefaultVariableType(variableName);
    }

    @Override
    public void addVariable(String variableName, VariableType variableType) {
        this.parentContext.addVariable(variableName, variableType);
    }

    @Override
    public Optional<VariableType> getVariableType(String variableName) {
        return this.parentContext.getVariableType(variableName);
    }

    @Override
    public void doBreak() {
        this.parentContext.doBreak();
    }

    @Override
    public void doContinue(int blockId) {
        this.parentContext.doContinue(blockId);
    }

    @Override
    public final void addValueSpecialization(String variableName) {
        assert false;
    }

    @Override
    public void pushInstructionModification(InstructionLocation location, SsaInstruction newInstruction) {
        this.parentContext.pushInstructionModification(location, newInstruction);
    }

    @Override
    public void pushInstructionRemoval(InstructionLocation location) {
        this.parentContext.pushInstructionRemoval(location);
    }
}
