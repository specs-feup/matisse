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

import org.specs.matisselib.ssa.InstructionType;

import com.google.common.base.Preconditions;

/**
 * Similar to matrix get, but receives only scalar (non-logical) indices. The position is assumed to be within range.
 * 
 * @author Luís Reis
 *
 */
public class SimpleGetInstruction extends IndexedInstruction {

    private String output;
    private String inputMatrix;
    private final List<String> indices;

    public SimpleGetInstruction(String output, String inputMatrix, List<String> indices) {
        Preconditions.checkArgument(output != null);
        Preconditions.checkArgument(inputMatrix != null);
        Preconditions.checkArgument(indices != null);

        this.output = output;
        this.inputMatrix = inputMatrix;
        this.indices = new ArrayList<>(indices);
    }

    public SimpleGetInstruction(String output, String inputMatrix, String index) {
        this(output, inputMatrix, Arrays.asList(index));
    }

    @Override
    public SimpleGetInstruction copy() {
        return new SimpleGetInstruction(output, inputMatrix, indices);
    }

    public String getInputMatrix() {
        return inputMatrix;
    }

    @Override
    public List<String> getIndices() {
        return Collections.unmodifiableList(indices);
    }

    @Override
    public List<String> getInputVariables() {
        List<String> inputs = new ArrayList<>();

        inputs.add(inputMatrix);
        inputs.addAll(indices);

        return inputs;
    }

    @Override
    public List<String> getOutputs() {
        return Arrays.asList(output);
    }

    public String getOutput() {
        return output;
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.NO_SIDE_EFFECT;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(output);
        str.append(" = simple_get ");
        str.append(inputMatrix);

        for (String index : indices) {
            str.append(", ");
            str.append(index);
        }

        return str.toString();
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
        Preconditions.checkArgument(newNames != null);

        output = newNames.getOrDefault(output, output);
        inputMatrix = newNames.getOrDefault(inputMatrix, inputMatrix);
        renameVariableList(newNames, indices);
    }
}
