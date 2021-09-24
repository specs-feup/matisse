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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.functionproperties.FunctionProperty;
import org.specs.matisselib.functionproperties.ValueSpecializationProperty;
import org.specs.matisselib.services.GlobalTypeProvider;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.InstructionLocation;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.providers.StringProvider;

public final class TypedInstance {
    private final FunctionIdentification functionIdentification;
    private final List<String> inputNames;
    private FunctionType functionType;
    private final FunctionBody functionBody;
    private final ProviderData providerData;
    private final Map<String, VariableType> variableTypes = new HashMap<>();
    private final StringProvider codeProvider;

    public TypedInstance(FunctionIdentification functionIdentification,
            List<String> inputNames,
            FunctionBody functionBody,
            StringProvider codeProvider,
            ProviderData providerData) {

        Preconditions.checkArgument(functionIdentification != null);

        this.functionIdentification = functionIdentification;
        this.inputNames = inputNames;
        this.codeProvider = codeProvider;
        this.functionBody = functionBody;
        this.providerData = providerData;
    }

    public FunctionIdentification getFunctionIdentification() {
        return functionIdentification;
    }

    public void setFunctionType(FunctionType functionType) {
        Preconditions.checkArgument(functionType != null);

        this.functionType = functionType;
    }

    public FunctionType getFunctionType() {
        return functionType;
    }

    public List<SsaBlock> getBlocks() {
        return functionBody.getBlocks();
    }

    public void addVariable(String variableName, VariableType variableType) {
        Preconditions.checkArgument(variableName != null);
        Preconditions.checkArgument(variableType != null);
        Preconditions.checkState(!variableTypes.containsKey(variableName),
                "Attempting to overwrite type of variable " + variableName);

        addOrOverwriteVariable(variableName, variableType);
    }

    public void addOrOverwriteVariable(String variableName, VariableType variableType) {
        Preconditions.checkArgument(variableName != null);
        Preconditions.checkArgument(variableType != null);

        variableTypes.put(variableName, variableType);
    }

    public String makeTemporary(String semantics, VariableType variableType) {
        Preconditions.checkArgument(variableType != null);

        String name = functionBody.makeTemporary(semantics);

        addVariable(name, variableType);

        return name;
    }

    public String makeTemporary(String semantics, Optional<VariableType> variableType) {
        Preconditions.checkArgument(variableType != null);

        String name = functionBody.makeTemporary(semantics);

        variableType.ifPresent(type -> addVariable(name, type));

        return name;
    }

    public Map<String, VariableType> getVariableTypes() {
        return Collections.unmodifiableMap(variableTypes);
    }

    public Optional<VariableType> getVariableType(String variableName) {
        Preconditions.checkArgument(variableName != null);

        VariableType variableType = variableTypes.get(variableName);
        return Optional.ofNullable(variableType);
    }

    public boolean isByRef(String variableName) {
        return getFunctionBody().isByRef(variableName);
    }

    public ProviderData getProviderData() {
        return providerData;
    }

    public FunctionBody getFunctionBody() {
        return functionBody;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("function ");
        builder.append(functionIdentification.getName());
        builder.append("\n");

        functionBody.buildBlockString(builder);

        builder.append("Types:\n");
        List<String> keys = variableTypes.keySet().stream().sorted().collect(Collectors.toList());
        for (String varName : keys) {
            builder.append("\t");
            builder.append(varName);
            builder.append(": ");
            builder.append(variableTypes.get(varName));
            builder.append("\n");
        }

        return builder.toString();
    }

    public StringProvider getCodeProvider() {
        return codeProvider;
    }

    public SsaBlock getBlock(int blockId) {
        return functionBody.getBlock(blockId);
    }

    public Optional<VariableType> getCombinedVariableTypeFromVariables(List<String> variableList,
            GlobalTypeProvider globalTypes) {
        List<VariableType> types = new ArrayList<>();

        for (String variable : variableList) {
            assert variable.matches("^\\^?[a-zA-Z0-9_$+]+$") : "Variable '" + variable + "' has wrong name format.";

            Optional<VariableType> possibleVariableType;
            if (variable.startsWith("^")) {
                possibleVariableType = globalTypes.get(variable.substring(1));
            } else {
                possibleVariableType = getVariableType(variable);
            }

            if (!possibleVariableType.isPresent()) {
                return Optional.empty();
            }

            types.add(possibleVariableType.get());
        }

        return TypeCombiner.getCombinedVariableType(getProviderData().getSettings().get(CirKeys.DEFAULT_REAL), types);
    }

    public int getFirstLine() {
        return getFunctionBody().getFirstLine();
    }

    public int getLineFromBlock(SsaInstruction instruction, int startBlock, int lastLine) {
        return getFunctionBody().getLineFromBlock(instruction, startBlock, lastLine);
    }

    public TypesMap getReturnVariableTypes() {
        TypesMap types = new TypesMap();

        for (String name : variableTypes.keySet()) {
            if (name.endsWith("$ret")) {
                String varName = name.substring(0, name.length() - "$ret".length());
                VariableType type = variableTypes.get(name);

                types.addSymbol(varName, type);
            }
        }

        return types;
    }

    public List<String> getInputNames() {
        return Collections.unmodifiableList(inputNames);
    }

    public void addValueSpecialization(String variableName) {
        int argumentIndex = inputNames.indexOf(variableName);
        assert argumentIndex >= 0;

        addProperty(new ValueSpecializationProperty(argumentIndex));
    }

    public boolean isScalarConstantSpecialized(int argumentIndex) {
        return getPropertyStream(ValueSpecializationProperty.class)
                .anyMatch(v -> v.getArgumentIndex() == argumentIndex);
    }

    public void renameVariables(Map<String, String> newNames) {
        functionBody.renameVariables(newNames);

        for (String key : newNames.keySet()) {
            VariableType type = variableTypes.get(key);
            if (type != null) {
                variableTypes.remove(key);
                variableTypes.put(newNames.get(key), type);
            }
        }
    }

    public void addProperty(FunctionProperty property) {
        functionBody.addProperty(property);
    }

    public <T extends FunctionProperty> Stream<T> getPropertyStream(Class<T> cls) {
        return functionBody.getPropertyStream(cls);
    }

    public SsaInstruction getInstructionAt(InstructionLocation instructionLocation) {
        return getFunctionBody().getInstructionAt(instructionLocation);
    }

    public void removeInstructionAt(InstructionLocation instructionLocation) {
        getFunctionBody().removeInstructionAt(instructionLocation);
    }

    public void setInstructionAt(InstructionLocation location, SsaInstruction instruction) {
        getFunctionBody().setInstructionAt(location, instruction);
    }

    public List<SsaInstruction> getFlattenedInstructionsList() {
        return getFunctionBody().getFlattenedInstructionsList();
    }

    public Iterable<SsaInstruction> getFlattenedInstructionsIterable() {
        return getFunctionBody().getFlattenedInstructionsIterable();
    }

    public <T extends SsaInstruction> Iterable<T> getFlattenedInstructionsOfTypeIterable(Class<T> cls) {
        return getFunctionBody().getFlattenedInstructionsIterable(cls);
    }

    public <T extends SsaInstruction> Stream<T> getFlattenedInstructionsOfTypeStream(Class<T> cls) {
        return getFunctionBody().getFlattenedInstructionsOfTypeStream(cls);
    }

    public int addBlock(SsaBlock block) {
        return getFunctionBody().addBlock(block);
    }

    public void breakBlock(int originalBlock, int startBlock, int endBlock) {
        getFunctionBody().breakBlock(originalBlock, startBlock, endBlock);
    }
}
