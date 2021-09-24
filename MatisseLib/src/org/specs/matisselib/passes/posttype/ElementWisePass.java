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

package org.specs.matisselib.passes.posttype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.functionproperties.AssumeMatrixSizesMatchProperty;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.ForLoopBuilderResult;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.passes.TypeTransparentSsaPass;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.Input;
import org.specs.matisselib.ssa.NumberInput;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.CombineSizeInstruction;
import org.specs.matisselib.ssa.instructions.LineInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.ValidateSameSizeInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.collections.MultiMap;

public class ElementWisePass extends TypeTransparentSsaPass {

    public static final String PASS_NAME = "element_wise_conversion";

    @Override
    public void apply(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            DataStore passData) {

        Logger logger = PassUtils.getLogger(passData, PASS_NAME);

        if (PassUtils.skipPass(body, PASS_NAME)) {
            logger.log("Skipping " + body.getName());
            return;
        }

        logger.log("Function: " + body.getName());

        DataProviderService dataProvider = passData.get(ProjectPassServices.DATA_PROVIDER);
        SizeGroupInformation sizes = body.hasProperty(AssumeMatrixSizesMatchProperty.class) ? null
                : dataProvider.buildData(CompilerDataProviders.SIZE_GROUP_INFORMATION);

        Set<String> elementWiseMatrices = computeElementWiseMatrices(body, typeGetter, logger);
        Map<String, SsaInstruction> instructionLabels = computeInstructionLabels(body, elementWiseMatrices);
        Set<String> toInline = PassUtils.skipPass(body, "element_wise_combine_loops") ? Collections.emptySet()
                : computeMatricesToInline(body, elementWiseMatrices);
        MultiMap<String, String> matrixDependencies;
        if (PassUtils.skipPass(body, "element_wise_prevent_redundancy")) {
            matrixDependencies = new MultiMap<>();
        } else {
            matrixDependencies = computeMatrixDependencies(body, toInline);
        }
        adjustToInline(toInline, matrixDependencies);

        boolean performedTransformations = false;
        /*
         * We'll keep trying to remove the instructions until none are left
         */
        while (tryRemoveInstruction(body, providerData, typeGetter, makeTemporary, sizes, toInline,
                matrixDependencies,
                instructionLabels, passData, logger)) {
            performedTransformations = true;
        }

        if (performedTransformations) {
            dataProvider.invalidate(CompilerDataProviders.CONTROL_FLOW_GRAPH);
            dataProvider.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);
        }
    }

    private void adjustToInline(Set<String> toInline, MultiMap<String, String> matrixDependencies) {
        for (String key : matrixDependencies.keySet()) {
            if (matrixDependencies.get(key).size() > 1) {
                toInline.remove(key);
            }
        }
    }

    private MultiMap<String, String> computeMatrixDependencies(FunctionBody body, Set<String> toInline) {
        MultiMap<String, String> dependencies = new MultiMap<>();

        for (SsaInstruction instruction : body.getFlattenedInstructionsIterable()) {
            for (String output : instruction.getOutputs()) {
                for (String input : SpecsCollections.intersection(instruction.getInputVariables(), toInline)) {
                    dependencies.put(input, output);
                }
            }
        }

        return dependencies;
    }

    private static Set<String> computeMatricesToInline(FunctionBody body, Set<String> elementWiseMatrices) {
        Set<String> toInline = new HashSet<>();
        toInline.addAll(elementWiseMatrices);

        Set<String> referenced = new HashSet<>();

        // A matrix can be inlined, unless:
        // 1. It is referenced by a non-element-wise operation
        // 2. It is used multiple times
        // 3. It is a return variable
        // TODO: Maybe only inline if they are in same block?

        for (SsaInstruction instruction : body.getFlattenedInstructionsIterable()) {
            if (instruction.getOutputs().size() != 1
                    || !elementWiseMatrices.contains(instruction.getOutputs().get(0))) {
                toInline.removeAll(instruction.getInputVariables());

                continue;
            }

            // At this point, the operation is element-wise.
            String output = instruction.getOutputs().get(0);
            if (!referenced.add(output)) {
                toInline.remove(output);
            }
        }

        toInline.removeIf(name -> name.endsWith("$ret"));

        return toInline;
    }

    private static Map<String, SsaInstruction> computeInstructionLabels(FunctionBody body,
            Set<String> elementWiseMatrices) {
        Map<String, SsaInstruction> labels = new HashMap<>();

        for (SsaInstruction instruction : body.getFlattenedInstructionsIterable()) {
            List<String> outputs = instruction.getOutputs();
            if (outputs.size() == 1 && elementWiseMatrices.contains(outputs.get(0))) {
                labels.put(outputs.get(0), instruction);
            }
        }

        return labels;
    }

    private static Set<String> computeElementWiseMatrices(FunctionBody body,
            Function<String, Optional<VariableType>> typeGetter,
            Logger logger) {

        Set<String> elementWiseMatrices = new HashSet<>();

        for (SsaInstruction instruction : body.getFlattenedInstructionsIterable()) {
            if (instruction instanceof TypedFunctionCallInstruction) {
                if (isElementWise((TypedFunctionCallInstruction) instruction, typeGetter, logger)) {
                    elementWiseMatrices.add(instruction.getOutputs().get(0));
                }
            }
        }

        return elementWiseMatrices;
    }

    private static boolean tryRemoveInstruction(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            SizeGroupInformation sizes,
            Set<String> toInline,
            MultiMap<String, String> matrixDependencies,
            Map<String, SsaInstruction> instructionLabels,
            DataStore passData,
            Logger logger) {

        List<SsaBlock> blocks = body.getBlocks();
        for (int blockId = 0; blockId < blocks.size(); blockId++) {
            SsaBlock block = blocks.get(blockId);
            LineInstruction lastLine = null;

            Map<String, Input> trivialAssignments = new HashMap<>();

            List<SsaInstruction> instructions = block.getInstructions();
            for (int instructionId = 0; instructionId < instructions.size(); instructionId++) {
                SsaInstruction instruction = instructions.get(instructionId);

                if (instruction instanceof LineInstruction) {
                    lastLine = (LineInstruction) instruction;
                }

                if (instruction instanceof AssignmentInstruction) {
                    AssignmentInstruction assignment = (AssignmentInstruction) instruction;
                    String output = assignment.getOutput();
                    Input input = assignment.getInput();
                    if (input instanceof NumberInput) {
                        trivialAssignments.put(output, input);
                    }
                }

                if (instruction instanceof TypedFunctionCallInstruction) {
                    TypedFunctionCallInstruction castInstruction = (TypedFunctionCallInstruction) instruction;
                    if (isElementWise(castInstruction, typeGetter, logger)) {

                        removeInstruction(body,
                                providerData,
                                typeGetter,
                                makeTemporary,
                                block,
                                blockId,
                                instructionId,
                                trivialAssignments,
                                castInstruction,
                                lastLine,
                                sizes,
                                toInline,
                                matrixDependencies,
                                instructionLabels,
                                passData,
                                logger);

                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean isElementWise(TypedFunctionCallInstruction instruction,
            Function<String, Optional<VariableType>> typeGetter,
            Logger logger) {

        if (!instruction.getFunctionType().isElementWise()) {
            logger.log(
                    "Can't apply optimization because function is not element-wise: " + instruction.getFunctionName());
            return false;
        }

        if (instruction.getOutputs().size() != 1) {
            logger.log("Can't apply optimization because instruction has multiple outputs: " + instruction);
            return false;
        }

        logger.log("Can apply optimization to " + instruction);
        return true;
    }

    private static void removeInstruction(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            SsaBlock block,
            int blockId,
            int instructionId,
            Map<String, Input> trivialAssignments,
            TypedFunctionCallInstruction instruction,
            LineInstruction line,
            SizeGroupInformation sizes,
            Set<String> toInline,
            MultiMap<String, String> matrixDependencies,
            Map<String, SsaInstruction> instructionLabels,
            DataStore passData,
            Logger logger) {

        SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

        List<SsaInstruction> nextInstructions = new ArrayList<>(
                block.getInstructions().subList(instructionId + 1, block.getInstructions().size()));
        block.removeInstructionsFrom(instructionId);

        BlockEditorHelper editor = new BlockEditorHelper(body, providerData, systemFunctions, typeGetter,
                makeTemporary, blockId);

        applyElementWise(typeGetter,
                makeTemporary,
                trivialAssignments,
                instruction,
                nextInstructions,
                editor,
                line,
                sizes,
                toInline,
                matrixDependencies,
                instructionLabels,
                body.hasProperty(AssumeMatrixSizesMatchProperty.class),
                logger);
    }

    static class RelativeSizeInformation {
        boolean sameNumel, sameSize;
    }

    private static void applyElementWise(
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            Map<String, Input> trivialAssignments,
            TypedFunctionCallInstruction instruction,
            List<SsaInstruction> nextInstructions,
            BlockEditorHelper editor,
            LineInstruction line,
            SizeGroupInformation sizes,
            Set<String> toInline,
            MultiMap<String, String> matrixDependencies,
            Map<String, SsaInstruction> instructionLabels,
            boolean assumeSizesMatch,
            Logger logger) {

        logger.log("Applying element-wise operation to more than one matrix.");

        // Step 1: Compute output size.
        VariableType intType = editor.getNumerics().newInt();
        VariableType sizeType = DynamicMatrixType.newInstance(intType);

        List<String> matrices = new ArrayList<>();

        expandMatrices(typeGetter, matrices, instruction.getInputVariables(), toInline,
                instructionLabels);

        RelativeSizeInformation info = computeRelativeSizeInfo(sizes, assumeSizesMatch, matrices, logger);

        VariableType outputType = instruction.getFunctionType().getOutputTypes().get(0);

        String outputName = instruction.getOutputs().get(0);
        String suggestedOutputName = NameUtils.getSuggestedName(outputName);

        // Step 2: Allocate result matrix.
        String initialMatrix = null;
        String numel = null;

        boolean addCode = true;

        if (matrices.size() == 1) {
            if (toInline.contains(outputName)) {
                addCode = false;
            } else {
                String matrix = matrices.get(0);
                initialMatrix = editor.addTypedOutputCall("matisse_new_array_from_matrix", suggestedOutputName,
                        outputType,
                        matrix);
                numel = editor.addSimpleCallToOutput("numel", matrix);
            }
        } else {
            if (info.sameNumel) {
                if (info.sameSize) {
                    if (toInline.contains(outputName)) {
                        addCode = false;
                    } else {
                        String matrix = matrices.get(0);
                        initialMatrix = editor.addTypedOutputCall("matisse_new_array_from_matrix", suggestedOutputName,
                                outputType,
                                matrix);
                        numel = editor.addSimpleCallToOutput("numel", matrix);
                    }
                } else {
                    editor.addInstruction(new ValidateSameSizeInstruction(matrices));

                    if (toInline.contains(outputName)) {
                        addCode = false;
                    } else {
                        String size = makeTemporary.apply("size", sizeType);
                        editor.addCallWithExistentOutputs("size", Arrays.asList(size), false, matrices.get(0));
                        initialMatrix = editor.addTypedOutputCall("matisse_new_array", suggestedOutputName, outputType,
                                size);
                        numel = editor.addSimpleCallToOutput("numel", initialMatrix);
                    }
                }
            } else {
                String size = makeTemporary.apply("size", sizeType);
                editor.addInstruction(new CombineSizeInstruction(size, matrices));

                if (toInline.contains(outputName)) {
                    addCode = false;
                } else {
                    initialMatrix = editor.addTypedOutputCall("matisse_new_array", suggestedOutputName, outputType,
                            size);
                    numel = editor.addSimpleCallToOutput("numel", initialMatrix);
                }
            }
        }

        BlockEditorHelper endEditor = editor;

        if (addCode) {

            // Step 3: Build the loop.
            ForLoopBuilderResult loop = editor.makeForLoop(
                    editor.addMakeIntegerInstruction("start", 1),
                    editor.addMakeIntegerInstruction("step", 1),
                    numel);
            BlockEditorHelper loopEditor = loop.getLoopBuilder();
            endEditor = loop.getEndBuilder();

            if (line != null) {
                loopEditor.addInstruction(line.copy());
                endEditor.addInstruction(line.copy());
            }

            String loopEndMatrix = makeTemporary.apply(suggestedOutputName, outputType);
            String loopInitialMatrix = loopEditor.addPhiMerge(
                    Arrays.asList(initialMatrix, loopEndMatrix),
                    Arrays.asList(editor, loopEditor),
                    outputType);

            String iter = loopEditor.addIntItersInstruction("iter");

            String value = computeGetterValue(typeGetter,
                    makeTemporary,
                    trivialAssignments,
                    instruction,
                    toInline,
                    instructionLabels,
                    assumeSizesMatch,
                    suggestedOutputName,
                    sizes,
                    loopEditor,
                    iter,
                    logger);
            loopEditor
                    .addInstruction(
                            new SimpleSetInstruction(loopEndMatrix, loopInitialMatrix, Arrays.asList(iter), value));

            endEditor.addInstruction(new PhiInstruction(outputName,
                    Arrays.asList(initialMatrix, loopEndMatrix),
                    Arrays.asList(editor.getBlockId(), loopEditor.getBlockId())));

        }

        endEditor.addInstructions(nextInstructions);
    }

    private static void expandMatrices(Function<String, Optional<VariableType>> typeGetter,
            List<String> matrices,
            List<String> inputs,
            Set<String> toInline,
            Map<String, SsaInstruction> instructionLabels) {

        for (String input : inputs) {
            if (toInline.contains(input)) {
                expandMatrices(typeGetter, matrices, instructionLabels.get(input).getInputVariables(),
                        toInline,
                        instructionLabels);
            } else if (MatrixUtils.isMatrix(typeGetter.apply(input))) {
                matrices.add(input);
            }
        }

    }

    private static RelativeSizeInformation computeRelativeSizeInfo(SizeGroupInformation sizes,
            boolean assumeSizesMatch,
            List<String> matrices,
            Logger logger) {

        RelativeSizeInformation info = new RelativeSizeInformation();
        boolean sameNumel = true;
        boolean sameSize = true;
        String prevMatrix = null;
        for (String matrixName : matrices) {
            if (assumeSizesMatch) {
                // No need to do anything.
            } else {
                if (sameSize) {
                    if (prevMatrix == null) {
                        prevMatrix = matrixName;
                    } else if (!sizes.areSameSize(prevMatrix, matrixName)) {
                        logger.log(
                                "Can't prove the two matrices have the same size: " + prevMatrix + ", " + matrixName);
                        sameSize = false;
                    }
                }
                if (!sameSize && sameNumel) {
                    assert prevMatrix != null;
                    if (!sizes.haveSameNumel(prevMatrix, matrixName)) {
                        logger.log(
                                "Can't prove the two matrices have the same numel: " + prevMatrix + ", " + matrixName);
                        sameNumel = false;
                    }
                }
            }
        }
        info.sameNumel = sameNumel;
        info.sameSize = sameSize;
        return info;
    }

    private static String computeGetterValue(Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            Map<String, Input> trivialAssignments,
            TypedFunctionCallInstruction instruction,
            Set<String> toInline,
            Map<String, SsaInstruction> instructionLabels,
            boolean assumeSizesMatch,
            String suggestedOutputName,
            SizeGroupInformation sizes,
            BlockEditorHelper editor,
            String iter,
            Logger logger) {

        List<String> inputVariables = instruction.getInputVariables();
        long numMatrices = inputVariables.stream()
                .map(typeGetter::apply)
                .filter(MatrixUtils::isMatrix)
                .count();
        boolean sameNumel = numMatrices == 1 || computeRelativeSizeInfo(sizes, assumeSizesMatch,
                inputVariables, logger).sameNumel;

        List<String> arguments = new ArrayList<>();
        for (int i = 0; i < instruction.getInputVariables().size(); i++) {
            String input = instruction.getInputVariables().get(i);

            if (toInline.contains(input)) {
                String suggestedOutput = NameUtils.getSuggestedName(input);

                TypedFunctionCallInstruction inlinedInstruction = (TypedFunctionCallInstruction) instructionLabels
                        .get(input);

                String argument = computeGetterValue(typeGetter, makeTemporary, trivialAssignments, inlinedInstruction,
                        toInline,
                        instructionLabels,
                        assumeSizesMatch, suggestedOutput, sizes, editor, iter, logger);
                arguments.add(argument);
            } else {
                if (MatrixUtils.isMatrix(typeGetter.apply(input).get())) {
                    VariableType elementType = MatrixUtils.getElementType(typeGetter.apply(input).get());

                    if (sameNumel) {
                        String argument = editor.addSimpleGet(input, iter, elementType);
                        arguments.add(argument);
                    } else {
                        String argument = editor.addGetOrFirst(input, iter, elementType);
                        arguments.add(argument);
                    }
                } else if (trivialAssignments.containsKey(input)) {
                    VariableType inputType = typeGetter.apply(input).get();
                    String newVar = makeTemporary.apply(instruction.getFunctionName() + "_value", inputType);
                    editor.addInstruction(new AssignmentInstruction(newVar, trivialAssignments.get(input)));
                    arguments.add(newVar);
                } else {
                    arguments.add(input);
                }
            }
        }

        String value = editor.addSimpleCallToOutputWithSemantics(instruction.getFunctionName(),
                suggestedOutputName + "_value",
                arguments);
        return value;
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH, // Explicitly invalidated
                CompilerDataProviders.SIZE_GROUP_INFORMATION // Explicitly invalidated
        );
    }
}
