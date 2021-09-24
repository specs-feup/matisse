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
import java.util.Map;

import org.specs.matisselib.ssa.InstructionType;

import com.google.common.base.Preconditions;

/**
 * Ensures that the given variable is a boolean.
 * 
 * @author Luís Reis
 *
 */
public final class ValidateBooleanInstruction extends ControlFlowIndependentInstruction {

    private String var;

    public ValidateBooleanInstruction(String var) {
	Preconditions.checkArgument(var != null);

	this.var = var;
    }

    @Override
    public ValidateBooleanInstruction copy() {
	return new ValidateBooleanInstruction(this.var);
    }

    @Override
    public List<String> getInputVariables() {
	return Arrays.asList(this.var);
    }

    public String getInputVariable() {
	return this.var;
    }

    @Override
    public List<String> getOutputs() {
	return Collections.emptyList();
    }

    @Override
    public InstructionType getInstructionType() {
	return InstructionType.HAS_VALIDATION_SIDE_EFFECT;
    }

    @Override
    public String toString() {
	return "validate_boolean " + this.var;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	Preconditions.checkArgument(newNames != null);

	this.var = newNames.getOrDefault(this.var, this.var);
    }
}
