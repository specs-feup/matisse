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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.specs.matisselib.ssa.InstructionType;

import com.google.common.base.Preconditions;

public class VerticalFlattenInstruction extends ControlFlowIndependentInstruction {

    private String output;
    private String input;

    public VerticalFlattenInstruction(String output, String input) {
	Preconditions.checkArgument(output != null);
	Preconditions.checkArgument(input != null);

	this.output = output;
	this.input = input;
    }

    @Override
    public VerticalFlattenInstruction copy() {
	return new VerticalFlattenInstruction(this.output, this.input);
    }

    public String getInput() {
	return this.input;
    }

    @Override
    public List<String> getInputVariables() {
	return Arrays.asList(this.input);
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
	this.input = newNames.getOrDefault(this.input, this.input);
    }

    @Override
    public String toString() {
	return this.output + " = vertical_flatten " + this.input;
    }

}
