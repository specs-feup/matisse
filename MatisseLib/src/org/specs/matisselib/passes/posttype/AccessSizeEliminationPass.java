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
import java.util.List;

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.BranchBuilderResult;
import org.specs.matisselib.helpers.ComputationBuilderResult;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AccessSizeInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class AccessSizeEliminationPass extends InstructionRemovalPass<AccessSizeInstruction> {

    public AccessSizeEliminationPass() {
        super(AccessSizeInstruction.class);
    }

    @Override
    protected void removeInstruction(TypedInstance instance,
            SsaBlock block,
            int blockId,
            int instructionId,
            AccessSizeInstruction instruction,
            DataStore passData) {

        DataProviderService dataProvider = passData.get(ProjectPassServices.DATA_PROVIDER);
        SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

        dataProvider.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);
        dataProvider.invalidate(CompilerDataProviders.CONTROL_FLOW_GRAPH);

        String outputMatrix = instruction.getOutput();
        VariableType outputType = instance.getVariableType(outputMatrix).get();

        String accessMatrix = instruction.getAccessMatrix();
        MatrixType accessMatrixType = (MatrixType) instance.getVariableType(accessMatrix).get();
        TypeShape accessMatrixShape = accessMatrixType.getTypeShape();

        String indexMatrix = instruction.getIndexMatrix();
        MatrixType indexMatrixType = (MatrixType) instance.getVariableType(indexMatrix).get();

        List<SsaInstruction> nextInstructions = new ArrayList<>(
                block.getInstructions().subList(instructionId + 1, block.getInstructions().size()));
        block.removeInstructionsFrom(instructionId);

        BlockEditorHelper editor = new BlockEditorHelper(instance, systemFunctions, blockId);

        String indexSize = editor.addSimpleCallToOutputWithSemantics("size",
                NameUtils.getSuggestedName(indexMatrix)
                        + "_size",
                indexMatrix);
        ComputationBuilderResult is1DResult = getIs1D(editor, indexMatrix, indexMatrixType);
        editor = is1DResult.getEndBuilder();

        String indexIs1D = is1DResult.getResultVariable();
        VariableType indexIs1DType = instance.getVariableType(indexIs1D).get();
        boolean indexIsKnown1D = ConstantUtils.isConstantOne(indexIs1DType);

        BlockEditorHelper ifIndex1DEditor;
        BlockEditorHelper ifIndexNot1DEditor;
        BlockEditorHelper ifIndex1DEndEditor;
        if (indexIsKnown1D) {
            ifIndex1DEditor = editor;
            ifIndexNot1DEditor = null;
            ifIndex1DEndEditor = null;
        } else {
            BranchBuilderResult is1DBranch = editor.makeBranch(indexIs1D);

            ifIndex1DEditor = is1DBranch.getIfBuilder();
            ifIndexNot1DEditor = is1DBranch.getElseBuilder();
            ifIndex1DEndEditor = is1DBranch.getEndBuilder();
        }

        int aShapeDims = accessMatrixShape.getNumDims();

        BlockEditorHelper ifAIsUpTo2D;
        BlockEditorHelper ifAIsMoreThan2D;
        BlockEditorHelper ifAIsUpTo2DEnd;
        if (aShapeDims < 0 || aShapeDims > 2) {
            String aNDims = ifIndex1DEditor.addSimpleCallToOutput("ndims", accessMatrix);
            String aIsUpTo2D = ifIndex1DEditor.addSimpleCallToOutput("eq", aNDims,
                    ifIndex1DEditor.addMakeIntegerInstruction("two", 2));

            BranchBuilderResult aIsUpTo2DBranch = ifIndex1DEditor.makeBranch(aIsUpTo2D);
            ifAIsUpTo2D = aIsUpTo2DBranch.getIfBuilder();
            ifAIsMoreThan2D = aIsUpTo2DBranch.getElseBuilder();
            ifAIsUpTo2DEnd = aIsUpTo2DBranch.getEndBuilder();
        } else {
            ifAIsUpTo2D = ifIndex1DEditor;
            ifAIsMoreThan2D = null;
            ifAIsUpTo2DEnd = null;
        }

        ComputationBuilderResult aUpTo2DComputationResult = computeAIsUpTo2DResult(ifAIsUpTo2D,
                accessMatrix,
                indexMatrix,
                indexSize,
                accessMatrixType,
                indexMatrixType,
                outputType);

        String AUpTo2DMergedSize = aUpTo2DComputationResult.getResultVariable();
        BlockEditorHelper AUpTo2DMergedBranch = aUpTo2DComputationResult.getEndBuilder();
        BlockEditorHelper AUpTo2DEndEditor;

        String finalMergedSize;
        int aUpTo2DEndId;
        if (ifAIsMoreThan2D != null) {
            assert ifAIsUpTo2DEnd != null;
            finalMergedSize = ifAIsUpTo2DEnd.addPhiMerge(Arrays.asList(AUpTo2DMergedSize, indexSize),
                    Arrays.asList(AUpTo2DMergedBranch, ifAIsMoreThan2D),
                    outputType);
            AUpTo2DEndEditor = ifAIsUpTo2DEnd;
        } else {
            finalMergedSize = AUpTo2DMergedSize;
            AUpTo2DEndEditor = AUpTo2DMergedBranch;
        }

        if (ifIndex1DEndEditor != null) {
            assert ifIndexNot1DEditor != null;

            ifIndex1DEndEditor.addInstruction(new PhiInstruction(outputMatrix,
                    Arrays.asList(finalMergedSize, indexSize),
                    Arrays.asList(AUpTo2DEndEditor.getBlockId(), ifIndexNot1DEditor.getBlockId())));
            editor = ifIndex1DEndEditor;
        } else {
            assert ifIndexNot1DEditor == null;

            AUpTo2DEndEditor.addAssignment(outputMatrix, finalMergedSize);
            editor = AUpTo2DEndEditor;
        }

        editor.addInstructions(nextInstructions);
    }

    private static ComputationBuilderResult computeAIsUpTo2DResult(BlockEditorHelper ifAIsUpTo2D,
            String accessMatrix,
            String indexMatrix,
            String indexSize,
            MatrixType accessMatrixType,
            MatrixType indexMatrixType,
            VariableType outputType) {

        TypeShape accessMatrixShape = accessMatrixType.getTypeShape();

        if (accessMatrixShape.getRawNumDims() == 2) {
            int knownARows = accessMatrixShape.getDim(0);
            int knownACols = accessMatrixShape.getDim(1);
            if (knownARows == 1 && (knownACols == 0 || knownACols > 1)) {
                String rowNumel = ifAIsUpTo2D.addSimpleCallToOutputWithSemantics("numel",
                        NameUtils.getSuggestedName(indexMatrix) + "_numel", indexMatrix);
                String rowHorzcat = ifAIsUpTo2D.addSimpleCallToOutput("horzcat",
                        ifAIsUpTo2D.addMakeIntegerInstruction("one", 1),
                        rowNumel);

                return new ComputationBuilderResult(rowHorzcat, ifAIsUpTo2D);
            }

            if (knownARows > 1) {
                String suggestedName = NameUtils.getSuggestedName(indexMatrix) + "_cols";
                String aCols = knownACols == 1 ? null
                        : ifAIsUpTo2D.addSimpleCallToOutputWithSemantics("size", suggestedName, indexMatrix,
                                ifAIsUpTo2D.addMakeIntegerInstruction("two", 2));

                return computeNotSingleRowResult(ifAIsUpTo2D,
                        aCols,
                        indexMatrix,
                        indexSize,
                        accessMatrixType,
                        outputType);
            }
        }

        List<String> aDims = ifAIsUpTo2D.addCall("size",
                NameUtils.getSuggestedName(accessMatrix) + "_size",
                2,
                accessMatrix);
        String aRows = aDims.get(0);
        String aCols = aDims.get(1);

        String isSingleRow = ifAIsUpTo2D.addSimpleCallToOutput("eq", aRows,
                ifAIsUpTo2D.addMakeIntegerInstruction("one", 1));
        BranchBuilderResult isSingleRowBranch = ifAIsUpTo2D.makeBranch(isSingleRow);
        BlockEditorHelper ifSingleRowBranch = isSingleRowBranch.getIfBuilder();
        BlockEditorHelper ifNotSingleRowBranch = isSingleRowBranch.getElseBuilder();
        BlockEditorHelper ifSingleRowEndBranch = isSingleRowBranch.getEndBuilder();

        String rowNumel = ifSingleRowBranch.addSimpleCallToOutput("numel", indexMatrix);
        String rowHorzcat = ifSingleRowBranch.addSimpleCallToOutput("horzcat",
                ifSingleRowBranch.addMakeIntegerInstruction("one", 1),
                rowNumel);

        ComputationBuilderResult notSingleRowComputationResult = computeNotSingleRowResult(ifNotSingleRowBranch,
                aCols,
                indexMatrix,
                indexSize,
                accessMatrixType,
                outputType);
        String notSingleRowMergedSize = notSingleRowComputationResult.getResultVariable();
        BlockEditorHelper ifSingleColEndBranch = notSingleRowComputationResult.getEndBuilder();

        String AUpTo2DMergedSize = ifSingleRowEndBranch.addPhiMerge(Arrays.asList(rowHorzcat, notSingleRowMergedSize),
                Arrays.asList(ifSingleRowBranch, ifSingleColEndBranch),
                outputType);

        return new ComputationBuilderResult(AUpTo2DMergedSize, ifSingleRowEndBranch);
    }

    private static ComputationBuilderResult computeNotSingleRowResult(BlockEditorHelper ifNotSingleRowBranch,
            String aCols,
            String indexMatrix, String indexSize,
            VariableType accessMatrixType, VariableType outputType) {

        TypeShape accessShape = accessMatrixType.getTypeShape();

        if (accessShape.getRawNumDims() == 2 && accessShape.getDim(1) == 1) {
            String colNumel = ifNotSingleRowBranch.addSimpleCallToOutput("numel", indexMatrix);
            String colHorzcat = ifNotSingleRowBranch.addSimpleCallToOutput("horzcat",
                    colNumel,
                    ifNotSingleRowBranch.addMakeIntegerInstruction("one", 1));

            return new ComputationBuilderResult(colHorzcat, ifNotSingleRowBranch);
        }

        String isSingleCol = ifNotSingleRowBranch.addSimpleCallToOutput("eq", aCols,
                ifNotSingleRowBranch.addMakeIntegerInstruction("one", 1));
        BranchBuilderResult isSingleColBranch = ifNotSingleRowBranch.makeBranch(isSingleCol);
        BlockEditorHelper ifSingleColBranch = isSingleColBranch.getIfBuilder();
        BlockEditorHelper ifNotSingleColBranch = isSingleColBranch.getElseBuilder();
        BlockEditorHelper ifSingleColEndBranch = isSingleColBranch.getEndBuilder();

        String colNumel = ifSingleColBranch.addSimpleCallToOutput("numel", indexMatrix);
        String colHorzcat = ifSingleColBranch.addSimpleCallToOutput("horzcat",
                colNumel,
                ifSingleColBranch.addMakeIntegerInstruction("one", 1));

        String notSingleRowMergedSize = ifSingleColEndBranch.addPhiMerge(
                Arrays.asList(colHorzcat, indexSize),
                Arrays.asList(ifSingleColBranch, ifNotSingleColBranch),
                outputType);

        return new ComputationBuilderResult(notSingleRowMergedSize, ifSingleColEndBranch);
    }

    private static ComputationBuilderResult getIs1D(BlockEditorHelper editor, String matrix, MatrixType matrixType) {
        TypeShape typeShape = matrixType.getTypeShape();

        if (typeShape.isFullyDefined()) {
            String result = editor.addMakeIntegerInstruction("is_1d",
                    typeShape.getNumDims() == 0 || typeShape.getNumDims() == 1 ? 1 : 0);
            return new ComputationBuilderResult(result, editor);
        }

        int numDims = typeShape.getNumDims();
        boolean isKnown2D = numDims >= 0 && numDims <= 2;

        BlockEditorHelper ifEditor;
        BlockEditorHelper elseEditor;
        BlockEditorHelper endEditor;
        BlockEditorHelper returnEditor;

        if (isKnown2D) {
            ifEditor = editor;
            elseEditor = null;
            endEditor = null;
            returnEditor = editor;
        } else {
            String nDims = editor.addSimpleCallToOutputWithSemantics("ndims",
                    NameUtils.getSuggestedName(matrix) + "_ndims",
                    matrix);
            String eq = editor.addSimpleCallToOutput("eq", nDims, editor.addMakeIntegerInstruction("two", 2));

            BranchBuilderResult ndimsBranch = editor.makeBranch(eq);

            ifEditor = ndimsBranch.getIfBuilder();
            elseEditor = ndimsBranch.getElseBuilder();
            endEditor = ndimsBranch.getEndBuilder();
            returnEditor = endEditor;
        }

        List<String> dims = ifEditor.addCall("size",
                NameUtils.getSuggestedName(matrix) + "_dim",
                2,
                matrix);
        String rows = dims.get(0);
        String cols = dims.get(1);

        String isSingleRow = ifEditor.addSimpleCallToOutput("eq", rows, ifEditor.addMakeIntegerInstruction("one", 1));
        String isSingleCol = ifEditor.addSimpleCallToOutput("eq", cols, ifEditor.addMakeIntegerInstruction("one", 1));

        String is1DinIf = ifEditor.addSimpleCallToOutput("or", isSingleRow, isSingleCol);

        String is1D;
        if (elseEditor != null) {
            assert endEditor != null;

            String is1DinElse = elseEditor.addMakeIntegerInstruction("zero", 0);

            is1D = ifEditor.makeIntegerTemporary("is_1d");
            endEditor.addInstruction(new PhiInstruction(is1D,
                    Arrays.asList(is1DinIf, is1DinElse),
                    Arrays.asList(ifEditor.getBlockId(), elseEditor.getBlockId())));
        } else {
            is1D = is1DinIf;
        }

        return new ComputationBuilderResult(is1D, returnEditor);
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                // Explicitly invalidated
                CompilerDataProviders.CONTROL_FLOW_GRAPH, CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
