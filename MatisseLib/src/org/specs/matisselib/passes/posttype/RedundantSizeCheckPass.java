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
import java.util.function.Predicate;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.CombineSizeInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.ValidateSameSizeInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class RedundantSizeCheckPass
        extends SizeAwareInstructionRemovalPass<SsaInstruction> {

    public RedundantSizeCheckPass() {
        super(SsaInstruction.class);
    }

    @Override
    protected boolean canEliminate(FunctionBody body,
            SsaInstruction instruction,
            Function<String, Optional<VariableType>> typeGetter,
            SizeGroupInformation sizes) {

        if (instruction instanceof ValidateSameSizeInstruction) {
            List<String> inputs = instruction.getInputVariables();
            if (inputs.size() < 2) {
                return true;
            }

            String first = inputs.get(0);
            for (int i = 1; i < inputs.size(); ++i) {
                if (!sizes.areSameSize(first, inputs.get(i))) {
                    return false;
                }
            }

            return true;
        }
        if (instruction instanceof CombineSizeInstruction) {
            List<String> inputs = instruction.getInputVariables();
            List<String> processedInputs = getNonScalarDifferentSizes(sizes, typeGetter, inputs);

            if (inputs.size() != processedInputs.size()) {
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
            SsaInstruction instruction,
            SizeGroupInformation sizes,
            DataStore passData) {

        if (instruction instanceof ValidateSameSizeInstruction) {
            block.removeInstructionAt(instructionId);
        } else if (instruction instanceof CombineSizeInstruction) {
            List<String> inputs = instruction.getInputVariables();
            List<String> processedInputs = getNonScalarDifferentSizes(sizes, typeGetter, inputs);

            String output = instruction.getOutputs().get(0);
            if (processedInputs.size() == 1) {
                InstanceProvider sizeProvider = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER)
                        .getSystemFunction("size")
                        .get();

                String input = processedInputs.get(0);

                FunctionType sizeType = sizeProvider
                        .getCheckedInstance(providerData.create(typeGetter.apply(input).get()))
                        .getFunctionType();

                block.replaceInstructionAt(instructionId, new TypedFunctionCallInstruction("size", sizeType,
                        Arrays.asList(output), Arrays.asList(input)));
            } else {
                block.replaceInstructionAt(instructionId,
                        new CombineSizeInstruction(output, processedInputs));
            }
        }
    }

    private static List<String> getNonScalarDifferentSizes(SizeGroupInformation sizes,
            Function<String, Optional<VariableType>> typeGetter,
            List<String> inputs) {

        Predicate<VariableType> isScalar = RedundantSizeCheckPass::isScalar;

        List<String> nonScalarInputs = new ArrayList<>();
        for (String input : inputs) {
            typeGetter.apply(input)
                    .filter(isScalar.negate())
                    .ifPresent(type -> {
                        nonScalarInputs.add(input);
                    });
        }

        if (nonScalarInputs.size() == 0) {
            return Arrays.asList(inputs.get(0));
        }

        List<String> result = new ArrayList<>();
        String firstInput = nonScalarInputs.get(0);
        result.add(firstInput);
        for (int i = 1; i < nonScalarInputs.size(); ++i) {
            String input = nonScalarInputs.get(i);

            if (!sizes.areSameSize(firstInput, input)) {
                result.add(input);
            }
        }

        return result;
    }

    private static boolean isScalar(VariableType type) {
        if (type instanceof ScalarType) {
            return true;
        }
        if (type instanceof MatrixType) {
            MatrixType matrix = (MatrixType) type;
            TypeShape shape = matrix.getTypeShape();
            return shape.isScalar();
        }

        return false;
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }

}
