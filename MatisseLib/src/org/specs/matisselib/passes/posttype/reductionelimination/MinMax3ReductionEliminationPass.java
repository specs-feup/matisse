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

package org.specs.matisselib.passes.posttype.reductionelimination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.BranchBuilderResult;
import org.specs.matisselib.helpers.ForLoopBuilderResult;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.passes.posttype.InstructionRemovalPass;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class MinMax3ReductionEliminationPass extends InstructionRemovalPass<FunctionCallInstruction> {

    private static final boolean ENABLE_DIAGNOSTICS = false;

    public MinMax3ReductionEliminationPass() {
        super(FunctionCallInstruction.class);
    }

    @Override
    protected boolean canEliminate(TypedInstance instance, FunctionCallInstruction instruction) {

        if (!instruction.getFunctionName().equals("min")) {
            return false;
        }

        if (instruction.getInputVariables().size() != 3) {
            log("Can't inline min: Only version with 3 arguments is supported");
            return false;
        }

        Optional<VariableType> firstArgumentType = instance.getVariableType(instruction.getInputVariables().get(0));
        if (!MatrixUtils.isMatrix(firstArgumentType)) {
            log("Can't inline min: First argument is not a matrix");
            return false;
        }

        int rawNumDims = MatrixUtils.getShape(firstArgumentType.get()).getRawNumDims();
        if (rawNumDims < 2) {
            log("Can't inline min: Unknown number of dimensions");
            return false;
        }

        if (!MatrixUtils.isKnownEmptyMatrix(instance.getVariableType(instruction.getInputVariables().get(1)))) {
            log("Can't inline min: Second argument is not known to be empty");
            return false;
        }

        Optional<VariableType> dimType = instance.getVariableType(instruction.getInputVariables().get(2));
        if (!ScalarUtils.hasConstant(dimType)) {
            log("Can't inline min: Third argument is not a constant, got " + dimType);
            return false;
        }

        Number dimConstant = ScalarUtils.getConstant(dimType.get());
        if (dimConstant.doubleValue() != dimConstant.intValue() || dimConstant.intValue() < 1
                || dimConstant.intValue() > rawNumDims) {

            log("Can't inline min: Invalid third argument");
            return false;
        }

        if (instruction.getOutputs().size() != 2) {
            log("Can't inline min: Unsupported number of outputs");
            return false;
        }

        log("Can eliminate");
        return true;
    }

    @Override
    protected void removeInstruction(TypedInstance instance,
            SsaBlock block,
            int blockId,
            int instructionId,
            FunctionCallInstruction instruction,
            DataStore passData) {

        DataProviderService dataProvider = passData.get(ProjectPassServices.DATA_PROVIDER);
        dataProvider.invalidate(CompilerDataProviders.CONTROL_FLOW_GRAPH);
        dataProvider.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);

        String inputName = instruction.getInputVariables().get(0);
        String inputSemantics = NameUtils.getSuggestedName(inputName);
        MatrixType inputType = (MatrixType) instance.getVariableType(inputName).get();
        TypeShape inputShape = inputType.getTypeShape();
        int inputNumDims = inputShape.getRawNumDims();

        String outputName = instruction.getOutputs().get(0);
        String outputSemantics = NameUtils.getSuggestedName(outputName);
        MatrixType outputType = (MatrixType) instance.getVariableType(outputName).get();
        ScalarType valueType = outputType.matrix().getElementType();
        String outputIndicesName = instruction.getOutputs().get(1);
        String outputIndicesSemantics = NameUtils.getSuggestedName(outputIndicesName);
        MatrixType outputIndicesType = (MatrixType) instance.getVariableType(outputIndicesName).get();

        int alongDim = ScalarUtils
                .getConstant(instance.getVariableType(instruction.getInputVariables().get(2)).get())
                .intValue() - 1;

        List<SsaInstruction> nextInstructions = new ArrayList<>(
                block.getInstructions().subList(instructionId + 1, block.getInstructions().size()));
        block.removeInstructionsFrom(instructionId);

        SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

        BlockEditorHelper editor = new BlockEditorHelper(instance, systemFunctions, blockId);

        // Make initial calculations and allocations

        List<String> inputSizeVariables = new ArrayList<>();

        for (int i = 1; i <= inputNumDims; ++i) {
            String dimIdx = editor.addMakeIntegerInstruction(Integer.toString(i), i);
            String size = editor.addSimpleCallToOutputWithSemantics("size", inputSemantics + "_size" + i, inputName,
                    dimIdx);
            inputSizeVariables.add(size);
        }

        String one = editor.addMakeIntegerInstruction("one", 1);
        String sizeInReductionDim = editor.addSimpleCallToOutputWithSemantics("min", "result_dim" + (alongDim + 1), one,
                inputSizeVariables.get(alongDim));

        List<String> loopStartNames = new ArrayList<>();
        List<String> loopEndNames = new ArrayList<>();
        List<String> loopIndicesStartNames = new ArrayList<>();
        List<String> loopIndicesEndNames = new ArrayList<>();

        List<String> arguments = new ArrayList<>(inputSizeVariables);
        arguments.set(alongDim, sizeInReductionDim);
        String initialAllocation = editor.addTypedOutputCall("matisse_new_array_from_dims",
                outputSemantics, outputType, arguments);
        String initialIndicesAllocation = editor.addTypedOutputCall("matisse_new_array_from_dims",
                outputIndicesSemantics, outputIndicesType, arguments);

        loopStartNames.add(initialAllocation);
        loopIndicesStartNames.add(initialIndicesAllocation);
        loopEndNames.add(outputName);
        loopIndicesEndNames.add(outputIndicesName);

        // Create loops

        List<BlockEditorHelper> loopStarts = new ArrayList<>();
        List<BlockEditorHelper> afterLoops = new ArrayList<>();
        List<String> iters = new ArrayList<>();

        BlockEditorHelper innerMostEditor = editor;

        for (int depth = inputNumDims - 1;; --depth) {
            if (depth < 0) {
                break;
            }
            String sizeInDim = inputSizeVariables.get(depth);

            ForLoopBuilderResult loop = innerMostEditor.makeForLoop(one, one, sizeInDim);

            BlockEditorHelper loopBody = loop.getLoopBuilder();
            iters.add(0, loopBody.addIntItersInstruction("iter"));
            loopStarts.add(0, loopBody);
            afterLoops.add(0, loop.getEndBuilder());

            loopStartNames.add(0, instance.makeTemporary(outputSemantics, outputType));
            loopEndNames.add(0, instance.makeTemporary(outputSemantics, outputType));
            loopIndicesStartNames.add(0, instance.makeTemporary(outputIndicesSemantics, outputIndicesType));
            loopIndicesEndNames.add(0, instance.makeTemporary(outputIndicesSemantics, outputIndicesType));

            innerMostEditor = loopBody;
        }

        // Implement the inner most loop content

        String iterAlongDim = iters.get(alongDim);

        List<String> flatIndices = new ArrayList<>(iters);
        flatIndices.set(alongDim, one);
        String newValue = innerMostEditor.addSimpleGet(inputName, iters, valueType);
        String isStart = innerMostEditor.addSimpleCallToOutputWithSemantics("eq", "is_start", iterAlongDim, one);

        BranchBuilderResult firstBranch = innerMostEditor.makeBranch(isStart);
        String oldValue = firstBranch.getElseBuilder().addSimpleGet(loopStartNames.get(0), flatIndices, valueType);
        String isLess = firstBranch.getElseBuilder().addSimpleCallToOutputWithSemantics("lt", "is_less", newValue,
                oldValue);

        String replace = instance.makeTemporary("replace_value",
                ((ScalarType) instance.getVariableType(one).get()).scalar().setConstant(null));
        firstBranch.getEndBuilder().addInstruction(new PhiInstruction(replace,
                Arrays.asList(one, isLess),
                Arrays.asList(firstBranch.getIfBuilder().getBlockId(), firstBranch.getElseBuilder().getBlockId())));
        BranchBuilderResult secondBranch = firstBranch.getEndBuilder().makeBranch(replace);
        BlockEditorHelper replaceEditor = secondBranch.getIfBuilder();

        String modifiedIndices = replaceEditor.addSimpleSet(loopIndicesStartNames.get(0), flatIndices, iterAlongDim);
        String modifiedOutput = replaceEditor.addSimpleSet(loopStartNames.get(0), flatIndices, newValue);

        BlockEditorHelper innerMostEndEditor = secondBranch.getEndBuilder();
        innerMostEndEditor.addInstruction(new PhiInstruction(loopEndNames.get(0),
                Arrays.asList(modifiedOutput, loopStartNames.get(0)),
                Arrays.asList(secondBranch.getIfBuilder().getBlockId(), secondBranch.getElseBuilder().getBlockId())));
        innerMostEndEditor.addInstruction(new PhiInstruction(loopIndicesEndNames.get(0),
                Arrays.asList(modifiedIndices, loopIndicesStartNames.get(0)),
                Arrays.asList(secondBranch.getIfBuilder().getBlockId(), secondBranch.getElseBuilder().getBlockId())));

        // Finally, add phi nodes

        for (int i = 0; i < inputNumDims; ++i) {
            BlockEditorHelper loopStart = loopStarts.get(i);
            BlockEditorHelper loopEnd;
            BlockEditorHelper afterLoop = afterLoops.get(i);

            if (i == 0) {
                loopEnd = innerMostEndEditor;
            } else {
                loopEnd = afterLoops.get(i - 1);
            }

            int parentId;
            String prevName;
            String prevIndicesName;
            String afterLoopName;
            String afterLoopIndicesName;
            if (i == inputNumDims - 1) {
                parentId = editor.getBlockId();
                prevName = initialAllocation;
                prevIndicesName = initialIndicesAllocation;
                afterLoopName = outputName;
                afterLoopIndicesName = outputIndicesName;
            } else {
                parentId = loopStarts.get(i + 1).getBlockId();
                prevName = loopStartNames.get(i + 1);
                prevIndicesName = loopIndicesStartNames.get(i + 1);
                afterLoopName = loopEndNames.get(i + 1);
                afterLoopIndicesName = loopIndicesEndNames.get(i + 1);
            }

            String loopStartName = loopStartNames.get(i);
            String loopIndicesStartName = loopIndicesStartNames.get(i);

            String loopEndName = loopEndNames.get(i);
            String loopEndIndicesName = loopIndicesEndNames.get(i);

            loopStart.prependInstruction(
                    new PhiInstruction(loopIndicesStartName, Arrays.asList(prevIndicesName, loopEndIndicesName),
                            Arrays.asList(parentId, loopEnd.getBlockId())));
            loopStart.prependInstruction(new PhiInstruction(loopStartName, Arrays.asList(prevName, loopEndName),
                    Arrays.asList(parentId, loopEnd.getBlockId())));

            afterLoop
                    .prependInstruction(
                            new PhiInstruction(afterLoopIndicesName, Arrays.asList(prevIndicesName, loopEndIndicesName),
                                    Arrays.asList(parentId, loopEnd.getBlockId())));
            afterLoop.prependInstruction(
                    new PhiInstruction(afterLoopName, Arrays.asList(prevName, loopEndName),
                            Arrays.asList(parentId, loopEnd.getBlockId())));
        }

        afterLoops.get(0).addInstructions(nextInstructions);
    }

    public static void log(String message) {
        if (MinMax3ReductionEliminationPass.ENABLE_DIAGNOSTICS) {
            System.out.print("[min3_elimination] ");
            System.out.println(message);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                // Explicitly invalidated if needed
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }

}
