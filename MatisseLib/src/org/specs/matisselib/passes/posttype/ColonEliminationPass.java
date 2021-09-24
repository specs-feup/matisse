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
import java.util.Optional;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.helpers.ForLoopBuilderResult;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Removes colon call instructions, replacing them by lower-level instructions.
 * 
 * @author Luís Reis
 *
 */
public class ColonEliminationPass extends InstructionRemovalPass<FunctionCallInstruction> {
    private static final boolean ENABLE_DIAGNOSTICS = false;

    public ColonEliminationPass() {
        super(FunctionCallInstruction.class);
    }

    @Override
    protected boolean canEliminate(TypedInstance instance, FunctionCallInstruction functionCallInstruction) {

        if (!functionCallInstruction.getFunctionName().equals("colon")) {
            return false;
        }

        List<String> outputs = functionCallInstruction.getOutputs();
        if (outputs.size() != 1) {
            log("Incorrect number of outputs: " + outputs);
            return false;
        }

        List<String> inputs = functionCallInstruction.getInputVariables();
        if (inputs.size() < 2 || inputs.size() > 3) {
            log("Incorrect number of inputs: " + inputs);
            return false;
        }

        String start = inputs.get(0);
        VariableType startType = instance.getVariableType(start).get();

        if (!ScalarUtils.isScalar(startType) || !ScalarUtils.isInteger(startType)) {
            log("Start is not scalar integer: " + startType);
            return false;
        }

        if (inputs.size() == 3) {
            String iter = inputs.get(1);
            VariableType iterType = instance.getVariableType(iter).get();

            if (!ScalarUtils.isScalar(iterType)) {
                log("Interval is not scalar: " + iterType);
                return false;
            }

            String iterConstant = ScalarUtils.getConstantString(iterType);
            if (iterConstant == null) {
                log("Could not determine value of interval");
                return false;
            }

            if (!iterConstant.equals("1") && !iterConstant.equals("1.0")) {
                log("Interval is not 1: " + iterType + ", value=" + iterConstant);
                return false;
            }
        }

        String size = inputs.get(inputs.size() - 1);
        VariableType sizeType = instance.getVariableType(size).get();

        if (!ScalarUtils.isScalar(sizeType)) {
            log("End is not scalar: " + sizeType);
            return false;
        }

        log("Can remove");
        return true;
    }

    @Override
    protected void removeInstruction(TypedInstance instance,
            SsaBlock block,
            int blockId,
            int instructionId,
            FunctionCallInstruction instruction,
            DataStore passData) {

        assert canEliminate(instance, instruction);

        Optional<SizeGroupInformation> sizeGroupInformation = passData
                .getTry(ProjectPassServices.DATA_PROVIDER)
                .flatMap(provider -> provider.tryGet(CompilerDataProviders.SIZE_GROUP_INFORMATION));

        String output = instruction.getOutputs().get(0);
        List<String> inputs = instruction.getInputVariables();

        String start = inputs.get(0);
        String originalEnd = inputs.get(inputs.size() - 1);

        VariableType outputType = instance.getVariableType(output).get();

        if (outputType instanceof ScalarType) {
            log("Found scalar colon");

            AssignmentInstruction newInstruction = AssignmentInstruction.fromVariable(output, start);
            updateSizeInfo(sizeGroupInformation, newInstruction);
            block.replaceInstructionAt(instructionId, newInstruction);

            return;
        }

        SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

        List<SsaInstruction> nextInstructions = new ArrayList<>(
                block.getInstructions().subList(instructionId + 1, block.getInstructions().size()));
        block.removeInstructionsFrom(instructionId);

        BlockEditorHelper editor = new BlockEditorHelper(instance, systemFunctions, blockId);

        String oneVar = editor.addMakeIntegerInstruction("one", 1);

        String itersVar;
        String offsetVar;

        if (ConstantUtils.isConstantOne(instance.getVariableType(start).get())) {
            offsetVar = null;
            itersVar = getTruncatedVariable(editor, originalEnd);
        } else {
            offsetVar = editor.addSimpleCallToOutputWithSemantics("minus", "offset", start, oneVar);
            String end = getTruncatedVariable(editor, originalEnd);

            itersVar = editor.addSimpleCallToOutputWithSemantics("minus",
                    NameUtils.getSuggestedName(output) + "_size",
                    end,
                    offsetVar);
        }

        String suggestedOutputName = NameUtils.getSuggestedName(output);
        String initialMatrix = editor.addTypedOutputCall("matisse_new_array_from_dims", suggestedOutputName,
                outputType, oneVar, itersVar);

        ForLoopBuilderResult loop = editor.makeForLoop(oneVar, oneVar, itersVar);

        BlockEditorHelper loopEditor = loop.getLoopBuilder();
        BlockEditorHelper endEditor = loop.getEndBuilder();

        String loopStartMatrix = instance.makeTemporary(suggestedOutputName, outputType);

        String loopEndMatrix = instance.makeTemporary(suggestedOutputName, outputType);

        loopEditor.addInstruction(new PhiInstruction(loopStartMatrix,
                Arrays.asList(initialMatrix, loopEndMatrix),
                Arrays.asList(blockId, loopEditor.getBlockId())));

        String iterVar = loopEditor.addIntItersInstruction("iter");

        String indexVar;
        if (offsetVar == null) {
            indexVar = iterVar;
        } else {
            indexVar = loopEditor.addSimpleCallToOutput("plus", iterVar, offsetVar);
        }

        loopEditor.addInstruction(new SimpleSetInstruction(loopEndMatrix, loopStartMatrix, Arrays.asList(iterVar),
                indexVar));

        endEditor.addInstruction(new PhiInstruction(output,
                Arrays.asList(initialMatrix, loopEndMatrix),
                Arrays.asList(blockId, loopEditor.getBlockId())));

        endEditor.addInstructions(nextInstructions);
    }

    private void updateSizeInfo(Optional<SizeGroupInformation> sizeGroupInformation,
            SsaInstruction newInstruction) {

        sizeGroupInformation.ifPresent(info -> {
            info.addInstructionInformation(newInstruction);
        });
    }

    private static String getTruncatedVariable(
            BlockEditorHelper editor,
            String original) {

        ScalarType originalType = (ScalarType) editor.getType(original).get();
        if (ScalarUtils.isInteger(originalType)) {
            return original;
        }

        return editor.addSimpleCallToOutput("fix", original);
    }

    private static void log(String message) {
        if (ColonEliminationPass.ENABLE_DIAGNOSTICS) {
            System.out.print("[colon] ");
            System.out.println(message);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.SIZE_GROUP_INFORMATION); // FIXME: Are the new injected instructions a problem?
    }
}
