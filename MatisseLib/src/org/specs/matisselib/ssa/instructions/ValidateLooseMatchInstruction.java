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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.specs.matisselib.ssa.InstructionType;

import com.google.common.base.Preconditions;

/**
 * Checks that two matrices A and B are identical, after discarding elements set to 1.
 * 
 * @author Luís Reis
 *
 */
public class ValidateLooseMatchInstruction extends ControlFlowIndependentInstruction {

    private String matrix1, matrix2;

    public ValidateLooseMatchInstruction(String matrix1, String matrix2) {
	Preconditions.checkArgument(matrix1 != null);
	Preconditions.checkArgument(matrix2 != null);

	this.matrix1 = matrix1;
	this.matrix2 = matrix2;
    }

    @Override
    public ValidateLooseMatchInstruction copy() {
	return new ValidateLooseMatchInstruction(this.matrix1, this.matrix2);
    }

    public String getMatrix1() {
	return this.matrix1;
    }

    public String getMatrix2() {
	return this.matrix2;
    }

    @Override
    public List<String> getInputVariables() {
	return Arrays.asList(this.matrix1, this.matrix2);
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
    public void renameVariables(Map<String, String> newNames) {
	this.matrix1 = newNames.getOrDefault(this.matrix1, this.matrix1);
	this.matrix2 = newNames.getOrDefault(this.matrix2, this.matrix2);
    }

    @Override
    public String toString() {
	return "validate_loose_match " + this.matrix1 + ", " + this.matrix2;
    }

}
