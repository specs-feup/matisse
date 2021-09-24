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

/**
 * @see SimpleSetInstruction
 * @author Luís Reis
 *
 */
public class MatrixSetInstruction extends ControlFlowIndependentInstruction {

    private String outputMatrix;
    private String inputMatrix;
    private final List<String> indices;
    private String value;

    public MatrixSetInstruction(String outputMatrix, String inputMatrix, List<String> indices, String value) {
        Preconditions.checkArgument(outputMatrix != null);
        Preconditions.checkArgument(inputMatrix != null);
        Preconditions.checkArgument(indices != null);
        Preconditions.checkArgument(value != null);

        this.outputMatrix = outputMatrix;
        this.inputMatrix = inputMatrix;
        this.indices = new ArrayList<>(indices);
        this.value = value;
    }

    @Override
    public MatrixSetInstruction copy() {
        return new MatrixSetInstruction(this.outputMatrix, this.inputMatrix, this.indices, this.value);
    }

    public String getInputMatrix() {
        return this.inputMatrix;
    }

    public List<String> getIndices() {
        return Collections.unmodifiableList(this.indices);
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public List<String> getInputVariables() {
        List<String> inputs = new ArrayList<>();

        inputs.add(this.inputMatrix);
        inputs.addAll(this.indices);
        inputs.add(this.value);

        return inputs;
    }

    @Override
    public List<String> getOutputs() {
        return Arrays.asList(this.outputMatrix);
    }

    public String getOutput() {
        return this.outputMatrix;
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.HAS_VALIDATION_SIDE_EFFECT;
    }

    @Override
    public String toString() {
        return this.outputMatrix + " = set " +
                this.inputMatrix + ", " +
                this.indices.stream().collect(Collectors.joining(", ")) +
                ", " + this.value;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
        Preconditions.checkArgument(newNames != null);

        this.outputMatrix = newNames.getOrDefault(this.outputMatrix, this.outputMatrix);
        this.inputMatrix = newNames.getOrDefault(this.inputMatrix, this.inputMatrix);
        this.value = newNames.getOrDefault(this.value, this.value);

        renameVariableList(newNames, this.indices);
    }
}
