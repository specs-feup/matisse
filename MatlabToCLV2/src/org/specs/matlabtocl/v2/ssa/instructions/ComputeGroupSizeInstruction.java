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

package org.specs.matlabtocl.v2.ssa.instructions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.instructions.ControlFlowIndependentInstruction;

public final class ComputeGroupSizeInstruction extends ControlFlowIndependentInstruction {

    private String output;
    private String numItems;
    private String blockSize;

    public ComputeGroupSizeInstruction(String output, String numItems, String blockSize) {
        this.output = output;
        this.numItems = numItems;
        this.blockSize = blockSize;
    }

    @Override
    public ComputeGroupSizeInstruction copy() {
        return new ComputeGroupSizeInstruction(this.output, this.numItems, this.blockSize);
    }

    public String getNumItems() {
        return this.numItems;
    }

    public String getBlockSize() {
        return this.blockSize;
    }

    @Override
    public List<String> getInputVariables() {
        return Arrays.asList(this.numItems, this.blockSize);
    }

    @Override
    public List<String> getOutputs() {
        return Arrays.asList(this.output);
    }

    public String getOutput() {
        return this.output;
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.NO_SIDE_EFFECT;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
        this.output = newNames.getOrDefault(this.output, this.output);
        this.numItems = newNames.getOrDefault(this.numItems, this.numItems);
        this.blockSize = newNames.getOrDefault(this.blockSize, this.blockSize);
    }

    @Override
    public String toString() {
        return this.output + " = compute_group_size " + this.numItems + ", " + this.blockSize;
    }
}
