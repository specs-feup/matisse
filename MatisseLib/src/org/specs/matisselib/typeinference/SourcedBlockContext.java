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

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.services.InstructionReportingService;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class SourcedBlockContext implements TypeInferenceContext {
    private final TypeInferenceContext parentContext;
    private final int sourceBlock;

    public SourcedBlockContext(
            TypeInferenceContext parentContext,
            int sourceBlock) {

        this.parentContext = parentContext;
        this.sourceBlock = sourceBlock;
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
    public TypedInstance getInstance() {
        return this.parentContext.getInstance();
    }

    @Override
    public DataStore getPassData() {
        return this.parentContext.getPassData();
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
    public Optional<VariableType> getDefaultVariableType(String variableName) {
        return this.parentContext.getDefaultVariableType(variableName);
    }

    @Override
    public ProviderData getProviderData() {
        return this.parentContext.getProviderData();
    }

    @Override
    public InstructionReportingService getInstructionReportService() {
        return this.parentContext.getInstructionReportService();
    }

    @Override
    public FunctionIdentification getFunctionIdentification() {
        return this.parentContext.getFunctionIdentification();
    }

    @Override
    public NumericFactory getNumerics() {
        return this.parentContext.getNumerics();
    }

    @Override
    public boolean isKnownAllTrue(String condition) {
        return this.parentContext.isKnownAllTrue(condition);
    }

    @Override
    public boolean isKnownAllFalse(String condition) {
        return this.parentContext.isKnownAllFalse(condition);
    }

    @Override
    public boolean isInterrupted() {
        return this.parentContext.isInterrupted();
    }

    @Override
    public void markUnreachable() {
        this.parentContext.markUnreachable();
    }

    @Override
    public int getSourceBlock() {
        return this.sourceBlock;
    }

    @Override
    public void addValueSpecialization(String variableName) {
        this.parentContext.addValueSpecialization(variableName);
    }

    @Override
    public void pushInstructionModification(InstructionLocation location, SsaInstruction newInstruction) {
        this.parentContext.pushInstructionModification(location, newInstruction);
    }

    @Override
    public void pushInstructionRemoval(InstructionLocation location) {
        this.parentContext.pushInstructionRemoval(location);
    }

    @Override
    public void reachEndOfBlock(int blockId) {
        this.parentContext.reachEndOfBlock(blockId);
    }

    @Override
    public Optional<String> getForLoopStartName() {
        return parentContext.getForLoopStartName();
    }

    @Override
    public Optional<String> getForLoopIntervalName() {
        return parentContext.getForLoopIntervalName();
    }
}
