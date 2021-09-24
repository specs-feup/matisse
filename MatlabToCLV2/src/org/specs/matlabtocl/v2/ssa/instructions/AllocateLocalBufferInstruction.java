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

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.instructions.ControlFlowIndependentInstruction;

public final class AllocateLocalBufferInstruction extends ControlFlowIndependentInstruction {

    private String output;
    private final VariableType baseType;
    private String localSize;

    public AllocateLocalBufferInstruction(String output, VariableType baseType, String localSize) {
	this.output = output;
	this.baseType = baseType;
	this.localSize = localSize;
    }

    @Override
    public AllocateLocalBufferInstruction copy() {
	return new AllocateLocalBufferInstruction(this.output, this.baseType, this.localSize);
    }

    public String getLocalSize() {
	return this.localSize;
    }

    @Override
    public List<String> getInputVariables() {
	return Arrays.asList(this.localSize);
    }

    public String getOutput() {
	return this.output;
    }

    @Override
    public List<String> getOutputs() {
	return Arrays.asList(this.output);
    }

    public VariableType getBaseType() {
	return this.baseType;
    }

    @Override
    public InstructionType getInstructionType() {
	return InstructionType.HAS_SIDE_EFFECT;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	this.output = newNames.getOrDefault(this.output, this.output);
	this.localSize = newNames.getOrDefault(this.localSize, this.localSize);
    }

    @Override
    public String toString() {
	return this.output + " = allocate_local_buffer " + this.baseType + ", " + this.localSize;
    }
}
