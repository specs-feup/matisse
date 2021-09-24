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
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.ForLoopBuilderResult;
import org.specs.matisselib.passes.posttype.InstructionRemovalPass;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.ValidateEqualInstruction;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class DotReductionEliminationPass extends InstructionRemovalPass<FunctionCallInstruction> {

    public DotReductionEliminationPass() {
        super(FunctionCallInstruction.class);
    }

    @Override
    protected boolean canEliminate(TypedInstance instance, FunctionCallInstruction instruction) {

        String functionName = instruction.getFunctionName();

        if (!functionName.equals("dot")) {
            return false;
        }

        List<String> inputs = instruction.getInputVariables();

        if (inputs.size() != 2) {
            return false;
        }

        VariableType type1 = instance.getVariableType(inputs.get(0)).get();
        VariableType type2 = instance.getVariableType(inputs.get(1)).get();

        if (!MatrixUtils.isMatrix(type1) || !MatrixUtils.isMatrix(type2)) {
            return false;
        }

        // FIXME: Perform validation: Types must be single or double.

        TypeShape shape1 = MatrixUtils.getShape(type1);
        TypeShape shape2 = MatrixUtils.getShape(type2);

        return (shape1.isKnownColumn() || shape1.isKnownRow()) &&
                (shape2.isKnownColumn() || shape2.isKnownRow());
    }

    @Override
    protected void removeInstruction(TypedInstance instance,
            SsaBlock block,
            int blockId,
            int instructionId,
            FunctionCallInstruction instruction,
            DataStore passData) {

        assert instruction.getFunctionName().equals("dot");

        passData.get(ProjectPassServices.DATA_PROVIDER).invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);

        List<String> inputs = instruction.getInputVariables();
        assert inputs.size() == 2;

        String input1 = inputs.get(0);
        String input2 = inputs.get(1);

        // TODO: If we ever add complex numbers, we should apply conj to input1.

        assert instruction.getOutputs().size() == 1;

        VariableType outputType = instance.getVariableType(instruction.getOutputs().get(0)).get();

        ScalarType resultType = (ScalarType) outputType;

        List<SsaInstruction> nextInstructions = new ArrayList<>(
                block.getInstructions().subList(instructionId + 1, block.getInstructions().size()));
        block.removeInstructionsFrom(instructionId);

        SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

        BlockEditorHelper editor = new BlockEditorHelper(instance, systemFunctions, blockId);

        String numel1 = editor.addSimpleCallToOutput("numel", input1);
        String numel2 = editor.addSimpleCallToOutput("numel", input2);

        editor.addInstruction(new ValidateEqualInstruction(numel1, numel2));

        String oneVar = editor.addMakeIntegerInstruction("one", 1);
        String initialAccumulator = editor.addMakeIntegerInstruction("acc", 0, resultType);

        ForLoopBuilderResult loop = editor.makeForLoop(oneVar, oneVar, numel1);

        BlockEditorHelper loopBuilder = loop.getLoopBuilder();
        BlockEditorHelper endBuilder = loop.getEndBuilder();

        VariableType resultTypeNoConstant = resultType.scalar().removeConstant();
        String loopStartAccumulator = instance.makeTemporary("acc", resultTypeNoConstant);
        String loopEndAccumulator = instance.makeTemporary("acc", resultTypeNoConstant);

        loopBuilder.addInstruction(new PhiInstruction(loopStartAccumulator,
                Arrays.asList(initialAccumulator, loopEndAccumulator),
                Arrays.asList(blockId, loopBuilder.getBlockId())));

        String iterVar = loopBuilder.addIntItersInstruction("iter");

        String value1 = loopBuilder.addSimpleGet(input1, iterVar, resultTypeNoConstant);
        String value2 = loopBuilder.addSimpleGet(input2, iterVar, resultTypeNoConstant);

        String timesResult = loopBuilder.addSimpleCallToOutput("times", value1, value2);
        loopBuilder.addCallWithExistentOutputs("plus",
                Arrays.asList(loopEndAccumulator),
                false,
                loopStartAccumulator,
                timesResult);

        String output = instruction.getOutputs().get(0);
        endBuilder.addInstruction(new PhiInstruction(output,
                Arrays.asList(initialAccumulator, loopEndAccumulator),
                Arrays.asList(blockId, loopBuilder.getBlockId())));

        endBuilder.addInstructions(nextInstructions);
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.SIZE_GROUP_INFORMATION // Explicitly invalidated
        );
    }
}
