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

package org.specs.matlabtocl.v2.ssa.instructions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.instructions.ControlFlowIndependentInstruction;
import org.specs.matlabtocl.v2.ssa.ParallelRegionSettings;

public class SetGpuRangeInstruction extends ControlFlowIndependentInstruction {

    private String output;
    private String buffer;
    private String begin;
    private String end;
    private String value;
    private final ParallelRegionSettings settings;

    public SetGpuRangeInstruction(String buffer, String begin, String end, String value,
            ParallelRegionSettings settings) {
        this.buffer = buffer;
        this.begin = begin;
        this.end = end;
        this.value = value;
        this.settings = settings;
    }

    public SetGpuRangeInstruction(String output, String input, String begin, String end, String value,
            ParallelRegionSettings settings) {
        this.output = output;
        this.buffer = input;
        this.begin = begin;
        this.end = end;
        this.value = value;
        this.settings = settings;
    }

    @Override
    public SetGpuRangeInstruction copy() {
        return new SetGpuRangeInstruction(output, buffer, begin, end, value, settings);
    }

    @Override
    public List<String> getInputVariables() {
        List<String> inputs = new ArrayList<>();

        inputs.addAll(Arrays.asList(buffer, begin, end, value));
        if (getSettings() != null) {
            inputs.addAll(getSettings().getInputVariables());
        }

        return inputs;
    }

    @Override
    public List<String> getOutputs() {
        if (output == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(output);
        }
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.HAS_SIDE_EFFECT;
    }

    public String getBuffer() {
        return buffer;
    }

    public String getBegin() {
        return begin;
    }

    public String getEnd() {
        return end;
    }

    public String getValue() {
        return value;
    }

    public ParallelRegionSettings getSettings() {
        return settings;
    }

    public Optional<String> getOutput() {
        return Optional.ofNullable(output);
    }

    public void useGpuBuffer(String gpuBuffer) {
        this.output = null;
        this.buffer = gpuBuffer;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
        output = newNames.getOrDefault(output, output);
        buffer = newNames.getOrDefault(buffer, buffer);
        begin = newNames.getOrDefault(begin, begin);
        end = newNames.getOrDefault(end, end);
        value = newNames.getOrDefault(value, value);
    }

    @Override
    public String toString() {
        return (output == null ? "" : output + " = ") + "set_gpu_range " + buffer + ", " + begin + ", " + end + ", "
                + value;
    }

}
