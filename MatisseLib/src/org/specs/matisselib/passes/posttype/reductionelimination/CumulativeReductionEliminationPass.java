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

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.ForLoopBuilderResult;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.passes.posttype.InstructionRemovalPass;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class CumulativeReductionEliminationPass extends InstructionRemovalPass<FunctionCallInstruction> {

    private static final boolean ENABLE_DIAGNOSTICS = false;

    public CumulativeReductionEliminationPass() {
        super(FunctionCallInstruction.class);
    }

    @Override
    protected boolean canEliminate(TypedInstance instance, FunctionCallInstruction instruction) {

        if (PassUtils.skipPass(instance, "cumulative_reduction_elimination")) {
            return false;
        }

        String functionName = instruction.getFunctionName();

        if (!functionName.equals("sum") && !functionName.equals("mean")) {
            return false;
        }

        List<String> inputs = instruction.getInputVariables();

        if (inputs.size() != 1) {
            log("Can't remove " + functionName + ": Too many inputs");
            return false;
        }

        if (instruction.getOutputs().size() != 1) {
            log("Can't remove " + functionName + ": Too many outputs");
            return false;
        }

        VariableType type = instance.getVariableType(inputs.get(0)).get();

        if (!MatrixUtils.isMatrix(type)) {
            log("Can't remove " + functionName + ": Input is not a matrix: " + type);
            return false;
        }

        TypeShape shape = MatrixUtils.getShape(type);

        if (shape.isKnown1D()) {
            return true;
        }

        log("Can't remove " + functionName + ": Input matrix is not 1D: " + shape);
        return false;
    }

    @Override
    protected void removeInstruction(TypedInstance instance,
            SsaBlock block,
            int blockId,
            int instructionId,
            FunctionCallInstruction functionCall,
            DataStore passData) {

        passData.get(ProjectPassServices.DATA_PROVIDER).invalidate(CompilerDataProviders.CONTROL_FLOW_GRAPH);
        passData.get(ProjectPassServices.DATA_PROVIDER).invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);

        String functionName = functionCall.getFunctionName();
        assert functionName.equals("sum") || functionName.equals("mean");

        log("Removing instruction: " + functionCall);

        List<String> inputs = functionCall.getInputVariables();
        assert inputs.size() == 1;
        List<String> outputs = functionCall.getOutputs();
        assert outputs.size() == 1;

        String input = inputs.get(0);
        String output = outputs.get(0);

        MatrixType inputType = (MatrixType) instance.getVariableType(input).get();
        log("With input: " + inputType);

        ScalarType inputElementType = inputType.matrix().getElementType();

        List<SsaInstruction> nextInstructions = new ArrayList<>(
                block.getInstructions().subList(instructionId + 1, block.getInstructions().size()));
        block.removeInstructionsFrom(instructionId);

        SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

        BlockEditorHelper editor = new BlockEditorHelper(instance, systemFunctions, blockId);

        String outputNameSuggestion = functionName.equals("sum") ? NameUtils.getSuggestedName(output) : "sum";

        String numel = editor.addSimpleCallToOutputWithSemantics("numel",
                NameUtils.getSuggestedName(input) + "_numel",
                input);
        String accStart = editor.addMakeIntegerInstruction(outputNameSuggestion, 0, inputElementType);
        String start = editor.addMakeIntegerInstruction("start", 1);
        String step = editor.addMakeIntegerInstruction("step", 1);

        ForLoopBuilderResult loop = editor.makeForLoop(start, step, numel);
        BlockEditorHelper loopEditor = loop.getLoopBuilder();
        BlockEditorHelper endEditor = loop.getEndBuilder();

        String accLoopStart = instance.makeTemporary(outputNameSuggestion, inputElementType);
        String accLoopEnd = instance.makeTemporary(outputNameSuggestion, inputElementType);

        loopEditor.addInstruction(new PhiInstruction(accLoopStart,
                Arrays.asList(accStart, accLoopEnd),
                Arrays.asList(editor.getBlockId(), loopEditor.getBlockId())));
        String iter = loopEditor.addIntItersInstruction("iter");
        String value = loopEditor.addSimpleGet(input, iter, inputElementType);
        loopEditor.addCallWithExistentOutputs("plus", Arrays.asList(accLoopEnd), false, accLoopStart, value);

        if (functionName.equals("sum")) {
            endEditor.addInstruction(new PhiInstruction(output,
                    Arrays.asList(accStart, accLoopEnd),
                    Arrays.asList(editor.getBlockId(), loopEditor.getBlockId())));
        } else {
            assert functionName.equals("mean");

            String sum = endEditor.addPhiMerge(Arrays.asList(accStart, accLoopEnd),
                    Arrays.asList(editor, loopEditor), inputElementType);

            endEditor.addCallWithExistentOutputs("rdivide", Arrays.asList(output), true, Arrays.asList(sum, numel));
        }

        endEditor.addInstructions(nextInstructions);
    }

    public static void log(String message) {
        if (CumulativeReductionEliminationPass.ENABLE_DIAGNOSTICS) {
            System.err.print("[reduction_elimination] ");
            System.err.println(message);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                // Explicitly invalidated
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
