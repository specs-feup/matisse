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

public class CellGetInstruction extends ControlFlowIndependentInstruction {

    private String output;
    private String inputCell;
    private final List<String> indices;

    public CellGetInstruction(String output, String inputCell, List<String> indices) {
	Preconditions.checkArgument(output != null);
	Preconditions.checkArgument(inputCell != null);
	Preconditions.checkArgument(indices != null);

	this.output = output;
	this.inputCell = inputCell;
	this.indices = new ArrayList<>(indices);
    }

    @Override
    public CellGetInstruction copy() {
	return new CellGetInstruction(this.output, this.inputCell, this.indices);
    }

    public String getInputCell() {
	return this.inputCell;
    }

    public List<String> getIndices() {
	return Collections.unmodifiableList(this.indices);
    }

    @Override
    public List<String> getInputVariables() {
	List<String> inputs = new ArrayList<>();

	inputs.add(this.inputCell);
	inputs.addAll(this.indices);

	return inputs;
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
	return InstructionType.HAS_VALIDATION_SIDE_EFFECT;
    }

    @Override
    public String toString() {
	return this.output + " = cell_get " + this.inputCell + ", "
		+ this.indices.stream().collect(Collectors.joining(", "));
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	Preconditions.checkArgument(newNames != null);

	this.output = newNames.getOrDefault(this.output, this.output);
	this.inputCell = newNames.getOrDefault(this.inputCell, this.inputCell);

	renameVariableList(newNames, this.indices);
    }
}
