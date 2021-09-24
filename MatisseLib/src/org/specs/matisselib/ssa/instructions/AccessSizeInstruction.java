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

/**
 * <p>
 * Meant to be used with matrix gets with only one index (A = B(I)) where I is a matrix.
 * <p>
 * This instruction returns the size of A:
 * <ul>
 * <li>If B is a row-matrix (but not a scalar), and I is either a row-matrix or column-matrix, then this function
 * returns [1, numel(I)].
 * <li>If B is a column-matrix (but not a scalar), and I is either a row-matrix or column-matrix, then this function
 * returns [numel(I), 1].
 * <li>Otherwise, this function returns size(I).
 * </ul>
 * 
 * @author Luís Reis
 *
 */
public class AccessSizeInstruction extends ControlFlowIndependentInstruction {

    private String outputSize;
    private String accessMatrix;
    private String indexMatrix;

    public AccessSizeInstruction(String outputSize, String accessMatrix, String indexMatrix) {
	this.outputSize = outputSize;
	this.accessMatrix = accessMatrix;
	this.indexMatrix = indexMatrix;
    }

    @Override
    public AccessSizeInstruction copy() {
	return new AccessSizeInstruction(this.outputSize, this.accessMatrix, this.indexMatrix);
    }

    public String getAccessMatrix() {
	return this.accessMatrix;
    }

    public String getIndexMatrix() {
	return this.indexMatrix;
    }

    @Override
    public List<String> getInputVariables() {
	return Arrays.asList(this.accessMatrix, this.indexMatrix);
    }

    @Override
    public List<String> getOutputs() {
	return Arrays.asList(getOutput());
    }

    public String getOutput() {
	return this.outputSize;
    }

    @Override
    public InstructionType getInstructionType() {
	return InstructionType.NO_SIDE_EFFECT;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	this.outputSize = newNames.getOrDefault(this.outputSize, this.outputSize);
	this.accessMatrix = newNames.getOrDefault(this.accessMatrix, this.accessMatrix);
	this.indexMatrix = newNames.getOrDefault(this.indexMatrix, this.indexMatrix);
    }

    @Override
    public String toString() {
	return this.outputSize + " = access_size " + this.accessMatrix + ", " + this.indexMatrix;
    }

}
