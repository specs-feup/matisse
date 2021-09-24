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

package org.specs.matisselib.ssa.instructions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.specs.matisselib.ssa.InstructionType;

public class SetAllInstruction extends ControlFlowIndependentInstruction {
    private String output;
    private String input;
    private String value;

    public SetAllInstruction(String output, String input, String value) {
	this.output = output;
	this.input = input;
	this.value = value;
    }

    @Override
    public SetAllInstruction copy() {
	return new SetAllInstruction(this.output, this.input, this.value);
    }

    public String getOutput() {
	return this.output;
    }

    public String getInputMatrix() {
	return this.input;
    }

    public String getValue() {
	return this.value;
    }

    @Override
    public List<String> getOutputs() {
	return Arrays.asList(this.output);
    }

    @Override
    public List<String> getInputVariables() {
	return Arrays.asList(this.input, this.value);
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	this.output = newNames.getOrDefault(this.output, this.output);
	this.input = newNames.getOrDefault(this.input, this.input);
	this.value = newNames.getOrDefault(this.value, this.value);
    }

    @Override
    public InstructionType getInstructionType() {
	return InstructionType.HAS_VALIDATION_SIDE_EFFECT;
    }

    @Override
    public String toString() {
	return this.output + " = set_all " + this.input + ", " + this.value;
    }
}
