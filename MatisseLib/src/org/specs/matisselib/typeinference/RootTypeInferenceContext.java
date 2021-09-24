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

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.helpers.TypesMapUtils;
import org.specs.matisselib.services.InstructionReportingService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

public class RootTypeInferenceContext extends AbstractTypeInferenceContext {
    private final TypedInstance instance;
    private final DataStore passData;
    private final TypesMap defaultTypes;
    private boolean interrupted;
    private final Map<String, String> overrideNames;

    private final Map<InstructionLocation, SsaInstruction> modifications = new HashMap<>();

    public RootTypeInferenceContext(TypedInstance instance, TypesMap defaultTypes, Map<String, String> overrideNames,
            DataStore passData) {
        Preconditions.checkArgument(instance != null);
        Preconditions.checkArgument(passData != null);

        this.instance = instance;
        this.passData = passData;
        this.defaultTypes = defaultTypes;
        this.overrideNames = overrideNames;
    }

    @Override
    public TypedInstance getInstance() {
        return instance;
    }

    @Override
    public DataStore getPassData() {
        return passData;
    }

    @Override
    public ProviderData getProviderData() {
        return instance.getProviderData();
    }

    @Override
    public void addVariable(String outputName, VariableType variableType) {
        instance.addVariable(outputName, variableType);
    }

    @Override
    public Optional<VariableType> getVariableType(String variableName) {
        return instance.getVariableType(variableName);
    }

    @Override
    public Optional<VariableType> getDefaultVariableType(String variableName) {

        String baseName;
        if (overrideNames.containsKey(variableName)) {
            baseName = overrideNames.get(variableName);
        } else {
            int baseNameEnd = variableName.indexOf('$');
            if (baseNameEnd == -1) {
                return Optional.empty();
            }

            baseName = variableName.substring(0, baseNameEnd);
        }

        List<String> scope = TypesMapUtils.getVariableTypeScope(getFunctionIdentification());
        VariableType variableType = defaultTypes.getSymbol(
                scope,
                baseName);
        return Optional.ofNullable(variableType);
    }

    @Override
    public InstructionReportingService getInstructionReportService() {
        return passData.get(TypeInferencePass.INSTRUCTION_REPORT_SERVICE);
    }

    @Override
    public FunctionIdentification getFunctionIdentification() {
        return instance.getFunctionIdentification();
    }

    @Override
    public NumericFactory getNumerics() {
        return getProviderData().getNumerics();
    }

    @Override
    public boolean isInterrupted() {
        return interrupted;
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
        interrupted = true;
    }

    @Override
    public void addValueSpecialization(String variableName) {
        getInstance().addValueSpecialization(variableName);
    }

    @Override
    public void pushInstructionModification(InstructionLocation location, SsaInstruction newInstruction) {
        modifications.put(location, newInstruction);
    }

    @Override
    public void pushInstructionRemoval(InstructionLocation location) {
        modifications.put(location, null);
    }

    public void applyModifications(FunctionBody body) {
        for (int blockId = 0; blockId < body.getBlocks().size(); ++blockId) {
            SsaBlock block = body.getBlock(blockId);

            ListIterator<SsaInstruction> iterator = block.getInstructions().listIterator();
            int instructionId = 0;

            while (iterator.hasNext()) {
                iterator.next();

                InstructionLocation location = new InstructionLocation(blockId, instructionId);

                if (modifications.containsKey(location)) {
                    SsaInstruction instruction = modifications.get(location);

                    if (instruction == null) {
                        iterator.remove();
                    } else {
                        iterator.set(instruction);
                    }
                }

                ++instructionId;
            }
        }
    }
}
