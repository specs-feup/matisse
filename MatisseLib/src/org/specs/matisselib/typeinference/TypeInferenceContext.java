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
import java.util.stream.Stream;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.functionproperties.FunctionProperty;
import org.specs.matisselib.services.InstructionReportingService;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

public interface TypeInferenceContext {
    public TypedInstance getInstance();

    public DataStore getPassData();

    public void addVariable(String variableName, VariableType variableType);

    public default void addVariable(String variableName, VariableType variableType,
            Optional<VariableType> overrideType) {
        addVariable(variableName, overrideType.orElse(variableType));
    }

    public Optional<VariableType> getVariableType(String variableName);

    public Optional<VariableType> getDefaultVariableType(String variableName);

    public ProviderData getProviderData();

    public InstructionReportingService getInstructionReportService();

    public FunctionIdentification getFunctionIdentification();

    public NumericFactory getNumerics();

    public boolean isKnownAllTrue(String condition);

    public boolean isKnownAllFalse(String condition);

    /**
     * Indicates whether type inference of the current block should stop, because the current point is unreachable (e.g.
     * after a break).
     * 
     * @return
     */
    public boolean isInterrupted();

    public void doBreak();

    public void doContinue(int blockId);

    public void markUnreachable();

    public int getSourceBlock();

    public default VariableType requireVariableType(String variableName) {
        return getVariableType(variableName)
                .orElseThrow(
                        () -> new RuntimeException("Could not find type of variable " + variableName));
    }

    public void addValueSpecialization(String variableName);

    public default boolean isScalarConstantSpecialized(int argumentIndex) {
        return getInstance().isScalarConstantSpecialized(argumentIndex);
    }

    public default InferenceRuleList getTypeInferenceRules() {
        return getPassData().get(TypeInferencePass.TYPE_INFERENCE_RULES);
    }

    public void pushInstructionModification(InstructionLocation location, SsaInstruction newInstruction);

    public void pushInstructionRemoval(InstructionLocation location);

    public void reachEndOfBlock(int blockId);

    public default void addFunctionProperty(FunctionProperty property) {
        getInstance().addProperty(property);
    }

    public default <T extends FunctionProperty> Stream<T> getPropertyStream(Class<T> cls) {
        return getInstance().getPropertyStream(cls);
    }

    public Optional<String> getForLoopStartName();

    public Optional<String> getForLoopIntervalName();
}
