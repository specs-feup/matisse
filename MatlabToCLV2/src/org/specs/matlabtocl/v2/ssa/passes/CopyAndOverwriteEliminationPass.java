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

package org.specs.matlabtocl.v2.ssa.passes;

import java.util.HashMap;
import java.util.Map;

import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matlabtocl.v2.ssa.instructions.AllocateMatrixOnGpuInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.CopyToGpuInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.SetGpuRangeInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Finds copy_to_gpu instructions followed by set_gpu_range instructions that cover the entire buffer and replaces the
 * copy with a simple allocation.
 * 
 * @author Luís Reis
 *
 */
public class CopyAndOverwriteEliminationPass implements PostTypeInferencePass {

    @Override
    public void apply(TypedInstance instance, DataStore passData) {
        SizeGroupInformation info = PassUtils.getData(passData, CompilerDataProviders.SIZE_GROUP_INFORMATION);

        for (SsaBlock block : instance.getBlocks()) {
            Map<String, String> sourceBuffers = new HashMap<>();
            Map<String, Integer> sourceLocations = new HashMap<>();

            for (int i = 0; i < block.getInstructions().size(); ++i) {
                SsaInstruction instruction = block.getInstructions().get(i);
                if (instruction instanceof CopyToGpuInstruction) {
                    CopyToGpuInstruction copyInstruction = (CopyToGpuInstruction) instruction;

                    String input = copyInstruction.getInput();
                    String output = copyInstruction.getOutput();

                    sourceBuffers.put(output, input);
                    sourceLocations.put(output, i);

                    continue;
                }
                if (instruction instanceof SetGpuRangeInstruction) {
                    SetGpuRangeInstruction setRange = (SetGpuRangeInstruction) instruction;

                    String buffer = setRange.getBuffer();
                    String source = sourceBuffers.get(buffer);
                    if (source != null &&
                            ConstantUtils.isConstantOne(instance, setRange.getBegin()) &&
                            info.areSameValue(info.getNumelResult(source), setRange.getEnd())) {

                        SsaInstruction newInstruction = new AllocateMatrixOnGpuInstruction(buffer, source);
                        block.getInstructions().set(sourceLocations.get(buffer), newInstruction);
                        continue;
                    }
                }

                // Optimization is only valid if buffer is not used between the copy and the memfill.
                for (String input : instruction.getInputVariables()) {
                    sourceBuffers.remove(input);
                    sourceLocations.remove(input);
                }
            }
        }
    }

}
