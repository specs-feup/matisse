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

package org.specs.matisselib.ssa.instructions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.specs.matisselib.ssa.InstructionType;

import com.google.common.base.Preconditions;

public final class CombineSizeInstruction extends ControlFlowIndependentInstruction {
    private String output;
    private final List<String> inputs;

    public CombineSizeInstruction(String output, List<String> inputs) {
	Preconditions.checkArgument(output != null);
	Preconditions.checkArgument(inputs != null);
	Preconditions.checkArgument(!inputs.isEmpty());

	this.output = output;
	this.inputs = new ArrayList<>(inputs);
    }

    @Override
    public CombineSizeInstruction copy() {
	return new CombineSizeInstruction(this.output, this.inputs);
    }

    @Override
    public List<String> getInputVariables() {
	return Collections.unmodifiableList(this.inputs);
    }

    @Override
    public List<String> getOutputs() {
	return Arrays.asList(this.output);
    }

    public String getOutput() {
	return this.output;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	this.output = newNames.getOrDefault(this.output, this.output);
	renameVariableList(newNames, this.inputs);
    }

    @Override
    public InstructionType getInstructionType() {
	return InstructionType.HAS_VALIDATION_SIDE_EFFECT;
    }

    @Override
    public String toString() {
	return this.output + " = combine_size " + this.inputs.stream().collect(Collectors.joining(", "));
    }
}
