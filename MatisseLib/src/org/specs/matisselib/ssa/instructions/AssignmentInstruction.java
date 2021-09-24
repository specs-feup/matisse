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

import org.specs.MatlabIR.MatlabLanguage.MatlabNumber;
import org.specs.matisselib.ssa.Input;
import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.NumberInput;
import org.specs.matisselib.ssa.UndefinedInput;
import org.specs.matisselib.ssa.VariableInput;

import com.google.common.base.Preconditions;

public final class AssignmentInstruction extends ControlFlowIndependentInstruction {

    private String output;
    private Input input;

    public AssignmentInstruction(String output, Input input) {
	Preconditions.checkArgument(output != null);
	Preconditions.checkArgument(input != null);

	this.output = output;
	this.input = input;
    }

    @Override
    public AssignmentInstruction copy() {
	return new AssignmentInstruction(this.output, this.input);
    }

    public static AssignmentInstruction fromVariable(String output, String input) {
	Preconditions.checkArgument(output != null);
	Preconditions.checkArgument(input != null);

	return new AssignmentInstruction(output, new VariableInput(input));
    }

    public static AssignmentInstruction fromNumber(String output, MatlabNumber input) {
	Preconditions.checkArgument(output != null);
	Preconditions.checkArgument(input != null);

	return new AssignmentInstruction(output, new NumberInput(false, input));
    }

    public static SsaInstruction fromInteger(String output, int input) {
	Preconditions.checkArgument(output != null);

	return new AssignmentInstruction(output, new NumberInput(input));
    }

    public static AssignmentInstruction fromUndefinedValue(String output) {
	Preconditions.checkArgument(output != null);

	return new AssignmentInstruction(output, new UndefinedInput());
    }

    public Input getInput() {
	return this.input;
    }

    @Override
    public List<String> getInputVariables() {
	return this.input instanceof VariableInput ?
		Arrays.asList(((VariableInput) this.input).getName()) :
		    Arrays.asList();
    }

    public String getOutput() {
	return this.output;
    }

    @Override
    public List<String> getOutputs() {
	return Arrays.asList(this.output);
    }

    @Override
    public String toString() {
	return this.output + " = " + this.input;
    }

    @Override
    public InstructionType getInstructionType() {
	return InstructionType.NO_SIDE_EFFECT;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	Preconditions.checkArgument(newNames != null);

	this.output = newNames.getOrDefault(this.output, this.output);

	if (this.input instanceof VariableInput) {
	    String oldInputName = ((VariableInput) this.input).getName();
	    String newInputName = newNames.get(oldInputName);
	    if (newInputName != null) {
		this.input = new VariableInput(newInputName);
	    }
	}
    }
}
