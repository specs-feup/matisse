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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.ForLoopBuilderResult;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AccessSizeInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.LineInstruction;
import org.specs.matisselib.ssa.instructions.MatrixGetInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Removes get instructions where indices are matrices, replacing them by lower-level instructions.
 * 
 * @author Luís Reis
 *
 */
public class MultiGetEliminationPass extends LineAwareInstructionRemovalPass<MatrixGetInstruction> {
    public MultiGetEliminationPass() {
        super(MatrixGetInstruction.class);
    }

    @Override
    protected boolean canEliminate(MatrixGetInstruction getInstruction,
            Function<String, Optional<VariableType>> typeGetter,
            LineInstruction line) {

        List<String> indices = getInstruction.getIndices();

        for (String index : indices) {
            VariableType indexType = typeGetter.apply(index).get();

            if (indexType instanceof MatrixType) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void removeInstruction(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            SsaBlock block,
            int blockId,
            int instructionId,
            MatrixGetInstruction instruction,
            LineInstruction line,
            DataStore passData) {

        assert canEliminate(instruction, typeGetter, line);

        DataProviderService dataProvider = passData.get(ProjectPassServices.DATA_PROVIDER);
        dataProvider.invalidate(CompilerDataProviders.CONTROL_FLOW_GRAPH);
        dataProvider.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);

        if (instruction.getIndices().size() == 1) {
            removeSingleIndexGetInstruction(body, providerData, typeGetter, makeTemporary, block, blockId,
                    instructionId,
                    instruction, line, passData);
        } else {
            removeMultipleIndexGetInstruction(body, providerData, typeGetter, makeTemporary, block, blockId,
                    instructionId,
                    instruction, line, passData);
        }

    }

    private static void removeSingleIndexGetInstruction(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            SsaBlock block,
            int blockId,
            int instructionId,
            MatrixGetInstruction instruction,
            LineInstruction line,
            DataStore passData) {

        assert instruction.getIndices().size() == 1;

        List<SsaInstruction> nextInstructions = new ArrayList<>(
                block.getInstructions().subList(instructionId + 1, block.getInstructions().size()));
        block.removeInstructionsFrom(instructionId);

        String inputMatrix = instruction.getInputMatrix();
        MatrixType inputMatrixType = (MatrixType) typeGetter.apply(inputMatrix).get();

        String index = instruction.getIndices().get(0);

        String output = instruction.getOutputs().get(0);
        MatrixType outputMatrixType = (MatrixType) typeGetter.apply(output).get();

        SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

        BlockEditorHelper editor = new BlockEditorHelper(body, providerData, systemFunctions, typeGetter,
                makeTemporary, blockId);

        VariableType intType = providerData.getNumerics().newInt();

        VariableType sizeType = DynamicMatrixType.newInstance(intType);
        String sizeVar = makeTemporary.apply("size", sizeType);
        editor.addInstruction(new AccessSizeInstruction(sizeVar, inputMatrix, index));

        String suggestedOutputName = NameUtils.getSuggestedName(output);

        String numelVar = editor.addSimpleCallToOutput("numel", index);
        String initialMatrix = editor.addTypedOutputCall("matisse_new_array", suggestedOutputName, outputMatrixType,
                sizeVar);

        ForLoopBuilderResult loop = editor.makeForLoop(editor.addMakeIntegerInstruction("start", 1),
                editor.addMakeIntegerInstruction("step", 1), numelVar);
        BlockEditorHelper loopEditor = loop.getLoopBuilder();
        BlockEditorHelper endEditor = loop.getEndBuilder();

        if (line != null) {
            loopEditor.addInstruction(line.copy());
            endEditor.addInstruction(line.copy());
        }

        VariableType valueType = inputMatrixType.matrix().getElementType();

        String finalMatrixVar = makeTemporary.apply(suggestedOutputName, outputMatrixType);

        String loopStartMatrix = loopEditor.addPhiMerge(Arrays.asList(initialMatrix, finalMatrixVar),
                Arrays.asList(editor, loopEditor), outputMatrixType);
        String iterVar = loopEditor.addIntItersInstruction("iter");
        String indexVar = loopEditor.addSimpleGet(index, iterVar, intType);

        String valueVar = makeTemporary.apply("value", valueType);

        loopEditor.addInstruction(new MatrixGetInstruction(valueVar, inputMatrix, Arrays.asList(indexVar)));
        loopEditor.addInstruction(new SimpleSetInstruction(finalMatrixVar, loopStartMatrix, Arrays.asList(iterVar),
                valueVar));

        endEditor.addInstruction(new PhiInstruction(output, Arrays.asList(initialMatrix, finalMatrixVar),
                Arrays.asList(editor.getBlockId(), loopEditor.getBlockId())));

        endEditor.addInstructions(nextInstructions);
    }

    private static void removeMultipleIndexGetInstruction(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            SsaBlock block,
            int blockId,
            int instructionId,
            MatrixGetInstruction instruction,
            LineInstruction line,
            DataStore passData) {

        String inputMatrix = instruction.getInputMatrix();
        List<String> indices = instruction.getIndices();
        String outputMatrix = instruction.getOutputs().get(0);

        MatrixType outputMatrixType = (MatrixType) typeGetter.apply(outputMatrix).get();

        NumericFactory numerics = providerData.getNumerics();

        SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

        List<SsaInstruction> nextInstructions = new ArrayList<>(
                block.getInstructions().subList(instructionId + 1, block.getInstructions().size()));
        block.removeInstructionsFrom(instructionId);

        BlockEditorHelper editor = new BlockEditorHelper(body,
                providerData,
                systemFunctions,
                typeGetter,
                makeTemporary,
                blockId);

        VariableType intType = numerics.newInt();
        VariableType int1Type = numerics.newInt(1);

        List<Integer> indicesToIterate = new ArrayList<>();
        Map<Integer, String> numels = new HashMap<>();

        for (int index = 0; index < indices.size(); ++index) {
            String indexName = indices.get(index);
            VariableType indexType = typeGetter.apply(indexName).get();
            if (indexType instanceof MatrixType) {
                indicesToIterate.add(index);

                String numelVar = editor.addSimpleCallToOutput("numel", indexName);

                numels.put(index, numelVar);
            }
        }

        Collections.reverse(indicesToIterate);

        List<String> zerosInputs = new ArrayList<>();
        List<VariableType> zerosInputTypes = new ArrayList<>();
        for (int index = 0; index < indices.size(); ++index) {
            if (indicesToIterate.contains(index)) {
                String numelVar = numels.get(index);
                zerosInputs.add(numelVar);
                zerosInputTypes.add(typeGetter.apply(numelVar).get());
            } else {
                zerosInputs.add(editor.addMakeIntegerInstruction("one", 1));
                zerosInputTypes.add(int1Type);
            }
        }

        String suggestedOutputName = NameUtils.getSuggestedName(outputMatrix);
        String initialMatrixVar = editor
                .addTypedOutputCall("matisse_new_array_from_dims", suggestedOutputName, outputMatrixType, zerosInputs);

        BlockEditorHelper outerEndBlock = null;
        String outMatrix = initialMatrixVar;
        String targetMatrix = outputMatrix;

        String outCounter = null;
        String targetCounter = null;
        String lastIterVar = null;

        if (indicesToIterate.size() > 1) {
            outCounter = editor.addMakeIntegerInstruction("counter", 1);
            targetCounter = makeTemporary.apply("counter", intType);
        }

        List<String> indexNames = new ArrayList<>();
        for (int index = 0; index < indices.size(); ++index) {
            indexNames.add(indicesToIterate.contains(index) ? null : indices.get(index));
        }

        for (int indexToIterate : indicesToIterate) {
            ForLoopBuilderResult loop = editor.makeForLoop(
                    editor.addMakeIntegerInstruction("start", 1),
                    editor.addMakeIntegerInstruction("step", 1),
                    numels.get(indexToIterate));
            BlockEditorHelper loopEditor = loop.getLoopBuilder();
            BlockEditorHelper endEditor = loop.getEndBuilder();

            if (line != null) {
                loopEditor.addInstruction(line.copy());
                endEditor.addInstruction(line.copy());
            }

            if (outerEndBlock == null) {
                outerEndBlock = endEditor;
            }

            String initialLoopMatrix = makeTemporary.apply(suggestedOutputName, outputMatrixType);
            String finalLoopMatrix = makeTemporary.apply(suggestedOutputName, outputMatrixType);
            String initialLoopCounter = null;
            String finalLoopCounter = null;

            if (outCounter != null) {
                initialLoopCounter = makeTemporary.apply("counter", intType);
                finalLoopCounter = makeTemporary.apply("counter", intType);
            }

            String iterVar = makeTemporary.apply("i", intType);
            String indexVar = makeTemporary.apply("index", intType);

            assert indexNames.get(indexToIterate) == null;
            indexNames.set(indexToIterate, indexVar);

            loopEditor.addInstruction(new PhiInstruction(initialLoopMatrix,
                    Arrays.asList(outMatrix, finalLoopMatrix),
                    Arrays.asList(editor.getBlockId(), loopEditor.getBlockId())));

            if (outCounter != null) {
                loopEditor.addInstruction(new PhiInstruction(initialLoopCounter,
                        Arrays.asList(outCounter, finalLoopCounter),
                        Arrays.asList(editor.getBlockId(), loopEditor.getBlockId())));
            }

            loopEditor.addInstruction(new IterInstruction(iterVar));
            loopEditor.addInstruction(new SimpleGetInstruction(indexVar, indices.get(indexToIterate), iterVar));

            endEditor.addInstruction(new PhiInstruction(targetMatrix, Arrays.asList(outMatrix, finalLoopMatrix),
                    Arrays.asList(editor.getBlockId(), loopEditor.getBlockId())));

            if (outCounter != null) {
                endEditor.addInstruction(new PhiInstruction(targetCounter,
                        Arrays.asList(outCounter, finalLoopCounter),
                        Arrays.asList(editor.getBlockId(), loopEditor.getBlockId())));
            }

            editor = loopEditor;
            targetMatrix = finalLoopMatrix;
            outMatrix = initialLoopMatrix;

            targetCounter = finalLoopCounter;
            outCounter = initialLoopCounter;

            lastIterVar = iterVar;
        }

        VariableType elementType = outputMatrixType.matrix().getElementType();
        String valueVar = makeTemporary.apply("value", elementType);

        editor.addInstruction(new MatrixGetInstruction(valueVar, inputMatrix, indexNames));
        editor.addInstruction(new SimpleSetInstruction(targetMatrix,
                outMatrix,
                Arrays.asList(outCounter == null ? lastIterVar : outCounter),
                valueVar));

        if (outCounter != null) {
            editor.addCallWithExistentOutputs("plus",
                    Arrays.asList(targetCounter),
                    false,
                    Arrays.asList(outCounter, editor.addMakeIntegerInstruction("counter", 1)));
        }

        assert outerEndBlock != null : "Did not iterate over any indices";
        outerEndBlock.addInstructions(nextInstructions);
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                // Explicitly invalidated if needed
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
