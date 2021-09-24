/**
 * Copyright 2017 SPeCS.
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
 * Represents a chain of simple set instructions.
 * 
 * <p>
 * Used to make compilation faster, particularly the final variable allocator.
 * 
 * @author Luís Reis
 *
 */
public class MultiSetInstruction extends ControlFlowIndependentInstruction {

    private String output;
    private String input;
    private List<String> values;

    public MultiSetInstruction(String output, String input, List<String> values) {
        Preconditions.checkArgument(!values.isEmpty());

        this.output = output;
        this.input = input;
        this.values = new ArrayList<>(values);
    }

    @Override
    public MultiSetInstruction copy() {
        return new MultiSetInstruction(output, input, values);
    }

    @Override
    public List<String> getInputVariables() {
        List<String> inputs = new ArrayList<>();

        inputs.add(input);
        inputs.addAll(values);

        return inputs;
    }

    public List<String> getValues() {
        return Collections.unmodifiableList(values);
    }

    public String getInputMatrix() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    @Override
    public List<String> getOutputs() {
        return Arrays.asList(output);
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.NO_SIDE_EFFECT;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
        output = newNames.getOrDefault(output, output);
        input = newNames.getOrDefault(input, input);

        renameVariableList(newNames, values);
    }

    @Override
    public String toString() {
        return output + " = multi_set " + input + ", " + values;
    }
}
