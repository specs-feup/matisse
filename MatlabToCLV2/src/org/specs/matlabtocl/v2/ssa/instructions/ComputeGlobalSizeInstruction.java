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

public final class ComputeGlobalSizeInstruction extends ControlFlowIndependentInstruction {

    private String output;
    private String workgroupSize;
    private String numWorkgroups;

    public ComputeGlobalSizeInstruction(String output, String workgroupSize, String numWorkgroups) {
	this.output = output;
	this.workgroupSize = workgroupSize;
	this.numWorkgroups = numWorkgroups;
    }

    @Override
    public ComputeGlobalSizeInstruction copy() {
	return new ComputeGlobalSizeInstruction(this.output, this.workgroupSize, this.numWorkgroups);
    }

    public String getWorkgroupSize() {
	return this.workgroupSize;
    }

    public String getNumWorkgroups() {
	return this.numWorkgroups;
    }

    @Override
    public List<String> getInputVariables() {
	return Arrays.asList(this.workgroupSize, this.numWorkgroups);
    }

    public String getOutput() {
	return this.output;
    }

    @Override
    public List<String> getOutputs() {
	return Arrays.asList(this.output);
    }

    @Override
    public InstructionType getInstructionType() {
	return InstructionType.NO_SIDE_EFFECT;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	this.output = newNames.getOrDefault(this.output, this.output);
	this.workgroupSize = newNames.getOrDefault(this.workgroupSize, this.workgroupSize);
	this.numWorkgroups = newNames.getOrDefault(this.numWorkgroups, this.numWorkgroups);
    }

    @Override
    public String toString() {
	return this.output + " = compute_global_size " + this.workgroupSize + ", " + this.numWorkgroups;
    }

}
