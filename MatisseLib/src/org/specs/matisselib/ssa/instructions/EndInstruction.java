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

public class EndInstruction extends ControlFlowIndependentInstruction {
    private String input;
    private String output;
    private final int index;
    private final int numIndices;

    public EndInstruction(String output, String input, int index, int numIndices) {
	Preconditions.checkArgument(output != null);
	Preconditions.checkArgument(input != null);
	Preconditions.checkElementIndex(index, numIndices);
	Preconditions.checkArgument(numIndices > 0);

	this.output = output;
	this.input = input;
	this.index = index;
	this.numIndices = numIndices;
    }

    @Override
    public EndInstruction copy() {
	return new EndInstruction(this.output, this.input, this.index, this.numIndices);
    }

    @Override
    public List<String> getInputVariables() {
	return Arrays.asList(this.input);
    }

    @Override
    public List<String> getOutputs() {
	return Arrays.asList(this.output);
    }

    public String getInputVariable() {
	return this.input;
    }

    public String getOutput() {
	return this.output;
    }

    public int getIndex() {
	return this.index;
    }

    public int getNumIndices() {
	return this.numIndices;
    }

    @Override
    public InstructionType getInstructionType() {
	return InstructionType.NO_SIDE_EFFECT;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	this.input = newNames.getOrDefault(this.input, this.input);
	this.output = newNames.getOrDefault(this.output, this.output);
    }

    @Override
    public String toString() {
	return this.output + " = end " + this.input + ", " + this.index + ", " + this.numIndices;
    }
}
