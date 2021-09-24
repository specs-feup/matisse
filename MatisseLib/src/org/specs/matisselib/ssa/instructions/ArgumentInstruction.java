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

/**
 * Represents an assignment from an argument value.
 * <p>
 * Always as a single output argument.
 * 
 * @author Luis Reis
 *
 */
public final class ArgumentInstruction extends ControlFlowIndependentInstruction {

    private String argumentName;
    private final int argumentIndex;

    public ArgumentInstruction(String argumentName, int argumentIndex) {
	Preconditions.checkArgument(argumentName != null);
	Preconditions.checkArgument(argumentIndex >= 0);

	this.argumentName = argumentName;
	this.argumentIndex = argumentIndex;
    }

    @Override
    public ArgumentInstruction copy() {
	return new ArgumentInstruction(this.argumentName, this.argumentIndex);
    }

    public String getOutput() {
	return this.argumentName;
    }

    public int getArgumentIndex() {
	return this.argumentIndex;
    }

    @Override
    public String toString() {
	return this.argumentName + " = arg " + this.argumentIndex;
    }

    @Override
    public List<String> getInputVariables() {
	return Arrays.asList();
    }

    @Override
    public List<String> getOutputs() {
	return Arrays.asList(this.argumentName);
    }

    @Override
    public InstructionType getInstructionType() {
	return InstructionType.NO_SIDE_EFFECT;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	Preconditions.checkArgument(newNames != null);

	this.argumentName = newNames.getOrDefault(this.argumentName, this.argumentName);
    }

}
