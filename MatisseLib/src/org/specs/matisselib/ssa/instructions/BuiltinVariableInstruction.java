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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.specs.matisselib.ssa.InstructionType;

import com.google.common.base.Preconditions;

public class BuiltinVariableInstruction extends ControlFlowIndependentInstruction {

    public static enum BuiltinVariable {
	NARGIN,
	PI,
	TRUE,
	FALSE
    }

    private String output;
    private final BuiltinVariable variable;

    public BuiltinVariableInstruction(String output, BuiltinVariable variable) {
	Preconditions.checkArgument(output != null);
	Preconditions.checkArgument(variable != null);

	this.output = output;
	this.variable = variable;
    }

    @Override
    public BuiltinVariableInstruction copy() {
	return new BuiltinVariableInstruction(this.output, this.variable);
    }

    @Override
    public List<String> getInputVariables() {
	return Collections.emptyList();
    }

    public String getOutput() {
	return this.output;
    }

    @Override
    public List<String> getOutputs() {
	return Arrays.asList(this.output);
    }

    public BuiltinVariable getVariable() {
	return this.variable;
    }

    @Override
    public InstructionType getInstructionType() {
	return InstructionType.NO_SIDE_EFFECT;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	Preconditions.checkArgument(newNames != null);

	this.output = newNames.getOrDefault(this.output, this.output);
    }

    @Override
    public String toString() {
	return this.output + " = builtin " + this.variable.toString().toLowerCase(Locale.UK);
    }
}
