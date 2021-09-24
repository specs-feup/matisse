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
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.ForLoopBuilderResult;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.VerticalFlattenInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Removes vertical_flatten instructions.
 * 
 * @author Luís Reis
 *
 */
public class VerticalFlattenEliminationPass extends InstructionRemovalPass<VerticalFlattenInstruction> {
    public VerticalFlattenEliminationPass() {
        super(VerticalFlattenInstruction.class);
    }

    @Override
    protected void removeInstruction(TypedInstance instance,
            SsaBlock block,
            int blockId,
            int instructionId,
            VerticalFlattenInstruction instruction,
            DataStore passData) {

        assert canEliminate(instance, instruction);

        DataProviderService dataProviderService = passData.get(ProjectPassServices.DATA_PROVIDER);
        dataProviderService.invalidate(CompilerDataProviders.CONTROL_FLOW_GRAPH);
        dataProviderService.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);

        List<SsaInstruction> nextInstructions = new ArrayList<>(
                block.getInstructions().subList(instructionId + 1, block.getInstructions().size()));
        block.removeInstructionsFrom(instructionId);

        SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

        BlockEditorHelper editor = new BlockEditorHelper(instance, systemFunctions, blockId);

        String inputMatrix = instruction.getInput();
        String outputMatrix = instruction.getOutput();

        VariableType outputType = instance.getVariableType(outputMatrix).get();

        String suggestedOutputName = NameUtils.getSuggestedName(outputMatrix);

        String numelVar = editor.addSimpleCallToOutputWithSemantics("numel",
                NameUtils.getSuggestedName(inputMatrix) + "_numel",
                inputMatrix);
        String zerosVar = editor.addTypedOutputCall("matisse_new_array_from_dims", suggestedOutputName, outputType,
                numelVar,
                editor.addMakeIntegerInstruction("one", 1));

        ForLoopBuilderResult loop = editor.makeForLoop(
                editor.addMakeIntegerInstruction("start", 1),
                editor.addMakeIntegerInstruction("step", 1),
                numelVar);
        BlockEditorHelper loopEditor = loop.getLoopBuilder();
        BlockEditorHelper endEditor = loop.getEndBuilder();

        String loopEndMatrix = instance.makeTemporary(suggestedOutputName, outputType);
        String loopStartMatrix = loopEditor.addPhiMerge(Arrays.asList(zerosVar, loopEndMatrix),
                Arrays.asList(editor, loopEditor),
                outputType);

        String iterVar = loopEditor.addIntItersInstruction("iter");
        String valueVar = loopEditor.addSimpleGet(inputMatrix, iterVar, MatrixUtils.getElementType(outputType));
        loopEditor.addInstruction(new SimpleSetInstruction(loopEndMatrix, loopStartMatrix, Arrays.asList(iterVar),
                valueVar));

        endEditor.addInstruction(new PhiInstruction(outputMatrix,
                Arrays.asList(zerosVar, loopEndMatrix), Arrays
                        .asList(editor.getBlockId(), loopEditor.getBlockId())));

        endEditor.addInstructions(nextInstructions);

    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
