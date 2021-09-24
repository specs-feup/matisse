/**
 * Copyright 2017 SPeCS.
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

package org.specs.matlabtocl.v2.unssa;

import java.util.List;
import java.util.function.Predicate;

import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.unssa.InterferenceGraph;
import org.specs.matisselib.unssa.VariableAllocation;
import org.specs.matisselib.unssa.allocators.EfficientVariableAllocator;
import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.ssa.instructions.CompleteReductionInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.InvokeKernelInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.SetGpuRangeInstruction;

public class CLAwareVariableAllocator extends EfficientVariableAllocator {
    @Override
    protected void visitMediumPriorityInstruction(Predicate<List<String>> canMerge,
            VariableAllocation allocation,
            InterferenceGraph interferenceGraph,
            SsaInstruction instruction) {

        if (instruction instanceof CompleteReductionInstruction) {
            CompleteReductionInstruction completeReduction = (CompleteReductionInstruction) instruction;

            if (completeReduction.getReductionType() == ReductionType.MATRIX_SET) {
                EfficientVariableAllocator.tryMerge(canMerge, interferenceGraph, allocation,
                        completeReduction.getOutput(), completeReduction.getInitialValue());
                return;
            }
        }
        if (instruction instanceof InvokeKernelInstruction) {
            InvokeKernelInstruction invokeKernel = (InvokeKernelInstruction) instruction;

            List<String> inputs = invokeKernel.getInputVariables();
            List<String> outputs = invokeKernel.getOutputs();
            for (int i = 0; i < outputs.size(); i++) {
                String output = outputs.get(i);
                int outputSourceIndex = invokeKernel.getOutputSources().get(i);
                String outputSource = invokeKernel.getArguments().get(outputSourceIndex);

                if (inputs.stream().filter(outputSource::equals).count() != 1) {
                    // Don't know how to handle this case. The input is referenced multiple times.
                    continue;
                }

                EfficientVariableAllocator.tryMerge(canMerge, interferenceGraph, allocation,
                        output, outputSource);
            }
        }
        if (instruction instanceof SetGpuRangeInstruction) {
            SetGpuRangeInstruction setGpuRange = (SetGpuRangeInstruction) instruction;

            if (setGpuRange.getOutput().isPresent()) {
                EfficientVariableAllocator.tryMerge(canMerge, interferenceGraph, allocation,
                        setGpuRange.getOutput().get(), setGpuRange.getBuffer());
            }
        }

        super.visitMediumPriorityInstruction(canMerge, allocation, interferenceGraph, instruction);
    }
}
