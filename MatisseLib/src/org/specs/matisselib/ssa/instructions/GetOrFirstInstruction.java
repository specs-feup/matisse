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
 * If the "matrix" variable is a scalar, then this instruction is the identity function. Otherwise, this instruction
 * behaves like simple_get.
 * 
 * @see SimpleGetInstruction
 * @see CombineSizeInstruction
 * @author Luís Reis
 */
public final class GetOrFirstInstruction extends IndexedInstruction {

    private String output;
    private String matrix;
    private String index;

    public GetOrFirstInstruction(String output, String matrix, String index) {
        this.output = output;
        this.matrix = matrix;
        this.index = index;
    }

    @Override
    public GetOrFirstInstruction copy() {
        return new GetOrFirstInstruction(this.output, this.matrix, this.index);
    }

    @Override
    public List<String> getInputVariables() {
        return Arrays.asList(this.matrix, this.index);
    }

    @Override
    public List<String> getOutputs() {
        return Arrays.asList(this.output);
    }

    public String getInputMatrix() {
        return this.matrix;
    }

    public String getIndex() {
        return this.index;
    }

    @Override
    public List<String> getIndices() {
        return Arrays.asList(index);
    }

    public String getOutput() {
        return this.output;
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.NO_SIDE_EFFECT;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
        this.output = newNames.getOrDefault(this.output, this.output);
        this.matrix = newNames.getOrDefault(this.matrix, this.matrix);
        this.index = newNames.getOrDefault(this.index, this.index);
    }

    @Override
    public String toString() {
        return this.output + " = get_or_first " + this.matrix + ", " + this.index;
    }
}
