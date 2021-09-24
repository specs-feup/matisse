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

package org.specs.matisselib.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.BranchInstruction;
import org.specs.matisselib.ssa.instructions.EndInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.GetOrFirstInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.MatrixGetInstruction;
import org.specs.matisselib.ssa.instructions.MatrixSetInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.RelativeGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.ValidateTrueInstruction;
import org.specs.matisselib.ssa.instructions.VerticalFlattenInstruction;
import org.specs.matisselib.typeinference.TypedInstance;

import com.google.common.base.Preconditions;

public final class BlockEditorHelper {
    private final FunctionBody functionBody;
    private final ProviderData providerData;
    private final SystemFunctionProviderService systemFunctions;
    private final Function<String, Optional<VariableType>> typeGetter;
    private final BiFunction<String, VariableType, String> makeTemporary;
    private final SsaBlock block;
    private final int blockId;

    public BlockEditorHelper(FunctionBody functionBody,
            ProviderData providerData,
            SystemFunctionProviderService systemFunctions,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            int blockId) {

        this.functionBody = functionBody;
        this.providerData = providerData;
        this.systemFunctions = systemFunctions;
        this.typeGetter = typeGetter;
        this.makeTemporary = makeTemporary;
        block = functionBody.getBlock(blockId);
        this.blockId = blockId;
    }

    public BlockEditorHelper(TypedInstance instance,
            SystemFunctionProviderService systemFunctions,
            int blockId) {
        this(instance.getFunctionBody(), instance.getProviderData(), systemFunctions, instance::getVariableType,
                instance::makeTemporary, blockId);
    }

    /**
     * Get the type of a variableName, and throw an exception if the variable doesn't exist or has no assigned type.
     * 
     * @param variableName
     *            The name of the variable
     * @return The type of the variable
     */
    public VariableType requireType(String variableName) {
        Preconditions.checkArgument(variableName != null);

        return typeGetter
                .apply(variableName)
                .orElseThrow(() -> new RuntimeException("Variable " + variableName + " not found"));
    }

    public void addInstruction(SsaInstruction instruction) {
        Preconditions.checkArgument(instruction != null);

        block.addInstruction(instruction);
    }

    public void addInstructions(List<SsaInstruction> instructions) {
        Preconditions.checkArgument(instructions != null);

        block.addInstructions(instructions);
    }

    public void prependInstruction(SsaInstruction instruction) {
        Preconditions.checkArgument(instruction != null);

        block.insertInstruction(0, instruction);
    }

    public SsaBlock getBlock() {
        return block;
    }

    public int getBlockId() {
        return blockId;
    }

    public BlockEditorHelper makeBlockBuilder(int blockId) {
        return new BlockEditorHelper(functionBody, providerData, systemFunctions, typeGetter,
                makeTemporary, blockId);
    }

    public BranchBuilderResult makeBranch(String conditionVariable) {

        SsaBlock ifBlock = new SsaBlock();
        SsaBlock elseBlock = new SsaBlock();
        SsaBlock endBlock = new SsaBlock();

        int ifBlockId = functionBody.addBlock(ifBlock);
        int elseBlockId = functionBody.addBlock(elseBlock);
        int endBlockId = functionBody.addBlock(endBlock);

        functionBody.breakBlock(blockId, blockId, endBlockId);

        addInstruction(new BranchInstruction(conditionVariable, ifBlockId, elseBlockId, endBlockId));

        return new BranchBuilderResult(makeBlockBuilder(ifBlockId),
                makeBlockBuilder(elseBlockId),
                makeBlockBuilder(endBlockId));
    }

    public ForLoopBuilderResult makeForLoop(String start, String interval, String end) {
        SsaBlock loopBlock = new SsaBlock();
        SsaBlock endBlock = new SsaBlock();

        int loopBlockId = functionBody.addBlock(loopBlock);
        int endBlockId = functionBody.addBlock(endBlock);

        functionBody.breakBlock(blockId, blockId, endBlockId);

        addInstruction(new ForInstruction(start, interval, end, loopBlockId, endBlockId));

        return new ForLoopBuilderResult(makeBlockBuilder(loopBlockId),
                makeBlockBuilder(endBlockId));
    }

    public String addSimpleCallToOutputWithSemantics(String functionName, String outputSemantics, String... inputs) {
        return addSimpleCallToOutputWithSemantics(functionName, outputSemantics, Arrays.asList(inputs));
    }

    public String addSimpleCallToOutput(String functionName, String... inputs) {
        return addSimpleCallToOutputWithSemantics(functionName,
                functionName + "_result",
                Arrays.asList(inputs));
    }

    public String addSimpleCallToOutputWithSemantics(String functionName, String outputSemantics, List<String> inputs) {
        return addCall(functionName, outputSemantics, 1, inputs).get(0);
    }

    public List<String> addCall(String functionName, String outputSemantics, int numberOfOutputs, String... inputs) {
        return addCall(functionName, outputSemantics, numberOfOutputs, Arrays.asList(inputs));
    }

    public List<String> addCall(String functionName, String outputSemantics, int numberOfOutputs, List<String> inputs) {
        return addFullCall(functionName, outputSemantics, numberOfOutputs, null, inputs);
    }

    private List<String> addFullCall(
            String functionName,
            String outputSemantics,
            int numberOfOutputs,
            List<VariableType> intendedOutputTypes,
            List<String> inputs) {

        Preconditions.checkArgument(functionName != null);
        Preconditions.checkArgument(inputs != null);
        for (String input : inputs) {
            Preconditions.checkArgument(input != null);
        }
        Preconditions.checkArgument(intendedOutputTypes == null || intendedOutputTypes.size() == numberOfOutputs);

        List<VariableType> inputTypesList = inputs.stream()
                .map(this::requireType)
                .collect(Collectors.toList());

        InstanceProvider provider = systemFunctions.getSystemFunction(functionName)
                .orElseThrow(() -> new RuntimeException("Could not find function " + functionName));

        ProviderData callProvider = providerData.create(inputTypesList);
        if (intendedOutputTypes == null) {
            callProvider.setNargouts(numberOfOutputs);
        } else {
            callProvider.setOutputType(intendedOutputTypes);
        }
        FunctionType functionType = provider.getType(callProvider);

        List<VariableType> outputTypes = functionType.getOutputTypes();

        List<String> outputs = new ArrayList<>(numberOfOutputs);
        for (int i = 0; i < numberOfOutputs; ++i) {
            VariableType type = outputTypes.get(i);

            String suggestedName = outputSemantics == null ? functionName + "_result" : outputSemantics;
            if (numberOfOutputs != 1) {
                suggestedName += (i + 1);
            }
            String output = makeTemporary.apply(suggestedName, type);

            outputs.add(output);
        }

        TypedFunctionCallInstruction instruction = new TypedFunctionCallInstruction(functionName, functionType,
                outputs, inputs);
        addInstruction(instruction);

        return outputs;

    }

    public String addTypedOutputCall(String functionName, String outputNameSuggestion, VariableType outputType,
            String... inputs) {
        return addTypedOutputCall(functionName, outputNameSuggestion, outputType, Arrays.asList(inputs));
    }

    public String addTypedOutputCall(String functionName, String outputNameSuggestion, VariableType outputType,
            List<String> inputs) {
        return addFullCall(functionName, outputNameSuggestion, 1, Arrays.asList(outputType), inputs).get(0);
    }

    public String makeTemporary(String semantics, VariableType type) {
        return makeTemporary.apply(semantics, type);
    }

    public String makeTemporary(String semantics, Optional<VariableType> type) {
        if (type.isPresent()) {
            return makeTemporary(semantics, type.get());
        } else {
            return functionBody.makeTemporary(semantics);
        }
    }

    private String makeInteger(String semantics, int integer) {
        VariableType type = getNumerics().newInt(integer);

        return makeTemporary.apply(semantics, type);
    }

    public String addMakeUndefinedInstruction(String semantics) {
        // Bypass makeTemporary as we don't want to add a variable type.
        String varName = functionBody.makeTemporary(semantics);

        addInstruction(AssignmentInstruction.fromUndefinedValue(varName));

        return varName;
    }

    public String addMakeIntegerInstruction(String semantics, int integer) {
        String var = makeInteger(semantics, integer);

        addInstruction(AssignmentInstruction.fromInteger(var, integer));

        return var;
    }

    public String addMakeIntegerInstruction(String semantics, int integer, ScalarType baseType) {
        ScalarType specializedType = baseType.scalar().setConstant(integer);

        String var = makeTemporary.apply(semantics, specializedType);

        addInstruction(AssignmentInstruction.fromInteger(var, integer));

        return var;
    }

    public NumericFactory getNumerics() {
        return providerData.getNumerics();
    }

    public String makeIntegerTemporary(String semantics) {
        return makeTemporary.apply(semantics, getNumerics().newInt());
    }

    public String addPhiMerge(List<String> variables, List<BlockEditorHelper> sourceBlocks, VariableType targetType) {
        List<Integer> sourceBlockIds = sourceBlocks.stream()
                .map(BlockEditorHelper::getBlockId)
                .collect(Collectors.toList());

        String nameProposal = getCommonNameProposal(variables).orElse("merged");
        String mergedVariable = makeTemporary.apply(nameProposal, targetType);

        addInstruction(new PhiInstruction(mergedVariable, variables, sourceBlockIds));

        return mergedVariable;
    }

    private static Optional<String> getCommonNameProposal(List<String> variables) {
        String proposedName = null;
        for (String variable : variables) {
            String name = NameUtils.getSuggestedName(variable);
            if (proposedName == null) {
                proposedName = name;
            }
            if (!proposedName.equals(name)) {
                return Optional.empty();
            }
        }

        return Optional.of(proposedName);
    }

    public void addAssignment(String output, String input) {
        block.addInstruction(AssignmentInstruction.fromVariable(output, input));
    }

    public String addIntItersInstruction(String semantics) {
        String name = makeIntegerTemporary(semantics);

        addInstruction(new IterInstruction(name));

        return name;
    }

    public String addSimpleGet(String matrix, String index, VariableType outputType) {
        return addSimpleGet(matrix, Arrays.asList(index), outputType);
    }

    public String addSimpleGet(String matrix, List<String> indices, VariableType outputType) {
        String output = makeTemporary.apply(NameUtils.getSuggestedName(matrix) + "_value", outputType);

        addInstruction(new SimpleGetInstruction(output, matrix, indices));

        return output;
    }

    public String addGetOrFirst(String matrix, String index, VariableType outputType) {
        String output = makeTemporary.apply(NameUtils.getSuggestedName(matrix) + "_value", outputType);

        addInstruction(new GetOrFirstInstruction(output, matrix, index));

        return output;
    }

    public String addGet(String matrix, List<String> indices, VariableType outputType) {
        String output = makeTemporary.apply(NameUtils.getSuggestedName(matrix) + "_value", outputType);

        addInstruction(new MatrixGetInstruction(output, matrix, indices));

        return output;
    }

    public String addRelativeGet(String matrix,
            String sizes,
            List<String> indices,
            VariableType outputType) {

        String output = makeTemporary.apply(NameUtils.getSuggestedName(matrix) + "_value", outputType);

        addInstruction(new RelativeGetInstruction(output, matrix, sizes, indices));

        return output;
    }

    public String addSimpleSet(String inputMatrix, List<String> indices, String value) {
        VariableType matrixType = typeGetter.apply(inputMatrix).get();
        String outputMatrix = makeTemporary.apply(NameUtils.getSuggestedName(inputMatrix), matrixType);

        addInstruction(new SimpleSetInstruction(outputMatrix, inputMatrix, indices, value));

        return outputMatrix;
    }

    public String addSet(String inputMatrix, List<String> indices, String value) {
        VariableType matrixType = typeGetter.apply(inputMatrix).get();
        String outputMatrix = makeTemporary.apply(NameUtils.getSuggestedName(inputMatrix), matrixType);

        addInstruction(new MatrixSetInstruction(outputMatrix, inputMatrix, indices, value));

        return outputMatrix;
    }

    private TypedFunctionCallInstruction buildCallWithExistentOutputs(String functionName, List<String> outputs,
            boolean useExistentOutputTypes, List<String> inputs) {
        List<VariableType> inputTypesList = inputs.stream()
                .map(this::requireType)
                .collect(Collectors.toList());

        InstanceProvider provider = systemFunctions.getSystemFunction(functionName)
                .orElseThrow(() -> new RuntimeException("Could not find function " + functionName));

        ProviderData callProvider = providerData.create(inputTypesList);
        if (useExistentOutputTypes) {
            List<VariableType> outputTypesList = outputs.stream()
                    .map(this::requireType)
                    .collect(Collectors.toList());

            callProvider.setOutputType(outputTypesList);
        } else {
            callProvider.setNargouts(outputs.size());
        }

        FunctionType functionType = provider.getType(callProvider);

        TypedFunctionCallInstruction instruction = new TypedFunctionCallInstruction(functionName, functionType,
                outputs, inputs);
        return instruction;
    }

    public TypedFunctionCallInstruction setCallWithExistentOutputs(ListIterator<SsaInstruction> iterator,
            String functionName,
            List<String> outputs,
            boolean useExistentOutputTypes,
            List<String> inputs) {

        TypedFunctionCallInstruction instruction = buildCallWithExistentOutputs(functionName, outputs,
                useExistentOutputTypes, inputs);
        iterator.set(instruction);
        return instruction;
    }

    public void addCallWithExistentOutputs(String functionName,
            List<String> outputs,
            boolean useExistentOutputTypes,
            List<String> inputs) {

        TypedFunctionCallInstruction instruction = buildCallWithExistentOutputs(functionName, outputs,
                useExistentOutputTypes, inputs);
        addInstruction(instruction);
    }

    public void addCallWithExistentOutputs(String functionName,
            List<String> outputs,
            boolean useExistentOutputTypes,
            String... inputs) {

        addCallWithExistentOutputs(functionName, outputs, useExistentOutputTypes, Arrays.asList(inputs));
    }

    public SsaInstruction removeLastInstruction() {
        return block.removeLastInstruction();
    }

    public SsaInstruction getLastInstruction() {
        return block.getInstructions().get(block.getInstructions().size() - 1);
    }

    public String addMakeEnd(String semantics, String inputMatrix, int index, int numIndices) {
        int numDims = typeGetter
                .apply(inputMatrix)
                .filter(MatrixType.class::isInstance)
                .map(MatrixType.class::cast)
                .map(matrix -> matrix.getTypeShape().getRawNumDims())
                .orElse(-1);

        if (numDims > 0 && index >= numDims) {
            return addMakeIntegerInstruction(semantics, 1);
        }

        String var = makeIntegerTemporary(semantics);
        addInstruction(new EndInstruction(var, inputMatrix, index, numIndices));

        return var;
    }

    public String addVerticalFlatten(String variable) {
        MatrixType matrixType = (MatrixType) typeGetter.apply(variable).get();
        TypeShape oldShape = matrixType.getTypeShape();
        TypeShape newShape = oldShape.isFullyDefined() ? TypeShape.newColumn(oldShape.getNumElements())
                : TypeShape.newColumn();

        String semantics = NameUtils.getSuggestedName(variable) + "_flat";
        String flatName = makeTemporary.apply(semantics, matrixType.matrix().setShape(newShape));

        addInstruction(new VerticalFlattenInstruction(flatName, variable));

        return flatName;
    }

    public void addValidateTrue(String condition) {
        addInstruction(new ValidateTrueInstruction(condition));
    }

    public Optional<VariableType> getType(String variable) {
        return typeGetter.apply(variable);
    }
}
