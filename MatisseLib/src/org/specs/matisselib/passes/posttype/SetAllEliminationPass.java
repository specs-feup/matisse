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

package org.specs.matisselib.passes.posttype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.functionproperties.AssumeMatrixSizesMatchProperty;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.BranchBuilderResult;
import org.specs.matisselib.helpers.ForLoopBuilderResult;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SetAllInstruction;
import org.specs.matisselib.ssa.instructions.SimpleSetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.ValidateEqualInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class SetAllEliminationPass extends SizeAwareInstructionRemovalPass<SetAllInstruction> {

    public SetAllEliminationPass() {
        super(SetAllInstruction.class);
    }

    @Override
    protected void removeInstruction(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            SsaBlock block,
            int blockId,
            int instructionId,
            SetAllInstruction instruction,
            SizeGroupInformation sizes,
            DataStore passData) {

        String output = instruction.getOutput();
        String inputMatrix = instruction.getInputMatrix();
        VariableType inputType = typeGetter.apply(inputMatrix).get();
        String value = instruction.getValue();
        VariableType valueType = typeGetter.apply(value).get();

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

        String one = editor.addMakeIntegerInstruction("one", 1);
        String inputSemantics = NameUtils.getSuggestedName(inputMatrix);
        String inputNumel = editor.addSimpleCallToOutputWithSemantics("numel", inputSemantics + "_numel", inputMatrix);

        boolean requireGetOrFirst = false;

        if (valueType instanceof MatrixType) {
            if (!sizes.haveSameNumel(inputMatrix, value)) {
                if (!body.hasProperty(AssumeMatrixSizesMatchProperty.class)) {
                    String valueSemantics = NameUtils.getSuggestedName(value);

                    String valueNumel = editor.addSimpleCallToOutputWithSemantics("numel", valueSemantics + "_numel",
                            value);
                    String isScalar = editor.addSimpleCallToOutputWithSemantics("eq", valueSemantics + "_is_scalar",
                            valueNumel,
                            one);

                    BranchBuilderResult branch = editor.makeBranch(isScalar);

                    BlockEditorHelper notScalarEditor = branch.getElseBuilder();

                    notScalarEditor.addInstruction(new ValidateEqualInstruction(inputNumel, valueNumel));
                    editor = branch.getEndBuilder();
                    requireGetOrFirst = true;
                }
            }
        }

        ForLoopBuilderResult loop = editor.makeForLoop(one, one, inputNumel);
        BlockEditorHelper loopEditor = loop.getLoopBuilder();
        String inLoopStart = makeTemporary.apply(inputSemantics, inputType);
        String inLoopEnd = makeTemporary.apply(inputSemantics, inputType);

        loopEditor.addInstruction(
                new PhiInstruction(inLoopStart,
                        Arrays.asList(inputMatrix, inLoopEnd),
                        Arrays.asList(editor.getBlockId(), loopEditor.getBlockId())));
        String iter = loopEditor.addIntItersInstruction("iter");

        String valueVar;
        if (valueType instanceof ScalarType) {
            valueVar = value;
        } else if (valueType instanceof MatrixType) {
            VariableType outputType = ((MatrixType) valueType).matrix().getElementType();

            if (requireGetOrFirst) {
                valueVar = loopEditor.addGetOrFirst(value, iter, outputType);
            } else {
                valueVar = loopEditor.addSimpleGet(value, iter, outputType);
            }
        } else {
            throw new NotImplementedException(valueType.getClass());
        }

        loopEditor.addInstruction(new SimpleSetInstruction(inLoopEnd, inLoopStart, Arrays.asList(iter), valueVar));

        BlockEditorHelper endEditor = loop.getEndBuilder();
        endEditor.addInstruction(new PhiInstruction(output,
                Arrays.asList(inputMatrix, inLoopEnd),
                Arrays.asList(editor.getBlockId(), loopEditor.getBlockId())));

        endEditor.addInstructions(nextInstructions);
    }

    @Override
    protected void afterPass(FunctionBody body, DataStore dataStore, boolean performedElimination) {

        if (performedElimination) {
            DataProviderService dataProviderService = dataStore.get(ProjectPassServices.DATA_PROVIDER);

            dataProviderService.invalidate(CompilerDataProviders.CONTROL_FLOW_GRAPH);
            dataProviderService.invalidate(CompilerDataProviders.SIZE_GROUP_INFORMATION);
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
