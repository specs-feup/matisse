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

package org.specs.matlabtocl.v2.ssa.passes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.passes.TypeTransparentSsaPass;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.AllocateMatrixOnGpuInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.CopyToGpuInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class UndefinedCopyOptimizationPass extends TypeTransparentSsaPass {
    private static final List<String> UNDEFINED_ALLOCATION_FUNCTIONS = Arrays.asList(
            "matisse_new_array",
            "matisse_new_array_from_dims",
            "matisse_new_array_from_matrix",
            "matisse_new_array_from_values");

    @Override
    public void apply(FunctionBody body,
            ProviderData providerData,
            Function<String, Optional<VariableType>> typeGetter,
            BiFunction<String, VariableType, String> makeTemporary,
            DataStore passData) {

        Set<String> undefinedAllocations = new HashSet<>();
        for (SsaInstruction instruction : body.getFlattenedInstructionsIterable()) {
            if (instruction instanceof FunctionCallInstruction
                    && UNDEFINED_ALLOCATION_FUNCTIONS
                            .contains(((FunctionCallInstruction) instruction).getFunctionName())
                    && instruction.getOutputs().size() == 1) {

                undefinedAllocations.add(instruction.getOutputs().get(0));
            }
        }

        for (SsaBlock block : body.getBlocks()) {
            ListIterator<SsaInstruction> iterator = block.getInstructions().listIterator();
            while (iterator.hasNext()) {
                SsaInstruction instruction = iterator.next();

                if (instruction instanceof CopyToGpuInstruction) {
                    CopyToGpuInstruction copy = (CopyToGpuInstruction) instruction;
                    if (undefinedAllocations.contains(copy.getInput())) {
                        iterator.set(new AllocateMatrixOnGpuInstruction(copy.getOutput(), copy.getInput()));
                    }
                }
            }
        }
    }

}
