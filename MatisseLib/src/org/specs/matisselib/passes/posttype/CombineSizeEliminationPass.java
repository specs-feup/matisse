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

import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.BranchBuilderResult;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.CombineSizeInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.ValidateSameSizeInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class CombineSizeEliminationPass extends InstructionRemovalPass<CombineSizeInstruction> {

    public CombineSizeEliminationPass() {
        super(CombineSizeInstruction.class);
    }

    @Override
    protected void removeInstruction(TypedInstance instance,
            SsaBlock block,
            int blockId,
            int instructionId,
            CombineSizeInstruction instruction,
            DataStore passData) {

        DataProviderService dataProvider = passData.get(ProjectPassServices.DATA_PROVIDER);
        dataProvider.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);

        List<SsaInstruction> nextInstructions = new ArrayList<>(
                block.getInstructions().subList(instructionId + 1, block.getInstructions().size()));
        block.removeInstructionsFrom(instructionId);

        SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);
        BlockEditorHelper editor = new BlockEditorHelper(instance, systemFunctions, blockId);

        VariableType sizeType = DynamicMatrixType.newInstance(editor.getNumerics().newInt());

        String size = null;
        for (String variable : instruction.getInputVariables()) {
            String partialSize = editor.addSimpleCallToOutput("size", variable);

            if (size == null) {
                size = partialSize;
                continue;
            }

            String isCurrentSizeScalar = getIsCurrentSizeScalar(editor, size);
            BranchBuilderResult branch = editor.makeBranch(isCurrentSizeScalar);
            BlockEditorHelper ifEditor = branch.getIfBuilder();
            BlockEditorHelper elseEditor = branch.getElseBuilder();
            BlockEditorHelper endEditor = branch.getEndBuilder();

            String isNewSizeScalar = getIsCurrentSizeScalar(elseEditor, partialSize);
            BranchBuilderResult isNewSizeScalarBranch = elseEditor.makeBranch(isNewSizeScalar);
            isNewSizeScalarBranch.getElseBuilder().addInstruction(new ValidateSameSizeInstruction(size, partialSize));

            size = endEditor.addPhiMerge(
                    Arrays.asList(partialSize, size),
                    Arrays.asList(ifEditor, isNewSizeScalarBranch.getEndBuilder()),
                    sizeType);

            editor = endEditor;
        }
        assert size != null;

        editor.addAssignment(instruction.getOutput(), size);
        editor.addInstructions(nextInstructions);
    }

    private static String getIsCurrentSizeScalar(BlockEditorHelper editor, String size) {
        String numDims = editor.addSimpleCallToOutput("numel", size);
        String isUpTo2D = editor.addSimpleCallToOutput("eq", numDims, editor.addMakeIntegerInstruction("two", 2));

        String dim1 = editor.addSimpleGet(size,
                editor.addMakeIntegerInstruction("dim", 1),
                editor.getNumerics().newInt());
        String isRow = editor.addSimpleCallToOutput("eq", dim1, editor.addMakeIntegerInstruction("one", 1));

        String dim2 = editor.addSimpleGet(size,
                editor.addMakeIntegerInstruction("dim", 2),
                editor.getNumerics().newInt());
        String isColumn = editor.addSimpleCallToOutput("eq", dim2, editor.addMakeIntegerInstruction("one", 1));

        String partialOr = editor.addSimpleCallToOutput("and", isUpTo2D, isRow);
        return editor.addSimpleCallToOutput("and", partialOr, isColumn);
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                // Explicitly invalidated
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
