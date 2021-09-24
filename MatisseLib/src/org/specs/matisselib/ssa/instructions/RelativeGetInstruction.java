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
 * Statements such as <code>A(:, :) = B;</code> are translated to loops with
 * 
 * <pre>
 * <code>
 * $A_dim1$1 = end A$1, 0, 2
 * $A_dim2$1 = end A$1, 1, 2
 * $A_dims$1 = call horzcat $A_dim1$1, $A_dim
 * $B_dims$1 = call size B$1
 * ...
 * $value = relative_get B$1, $A_dims$1, $idx$1, $idx$2
 * A$2 = matrix_set A$1, $idx1, $idx2, $value.
 *</code>
 * </pre>
 * 
 * The semantics of relative_get mimic this behavior:
 * <ul>
 * <li>inputMatrix must not be empty.
 * <li>If inputMatrix is a scalar, then the result is that same scalar value.
 * <li>If inputMatrix is a non-scalar matrix, then this instruction finds the appropriate position and returns it.
 * </ul>
 * 
 * For non-scalar matrices, relative_get indexes input matrix as-if it had a size of $dims$1.
 * 
 * @author Luís Reis
 *
 */
public class RelativeGetInstruction extends IndexedInstruction {

    private String output;
    private String inputMatrix;
    private final List<String> indices;
    private String sizeMatrix;

    public RelativeGetInstruction(String output, String inputMatrix, String sizeMatrix, List<String> indices) {
        Preconditions.checkArgument(output != null);
        Preconditions.checkArgument(inputMatrix != null);
        Preconditions.checkArgument(indices != null);
        Preconditions.checkArgument(sizeMatrix != null);

        this.output = output;
        this.inputMatrix = inputMatrix;
        this.sizeMatrix = sizeMatrix;
        this.indices = new ArrayList<>(indices);
    }

    @Override
    public RelativeGetInstruction copy() {
        return new RelativeGetInstruction(this.output, this.inputMatrix, this.sizeMatrix, this.indices);
    }

    public String getInputMatrix() {
        return this.inputMatrix;
    }

    public List<String> getIndices() {
        return Collections.unmodifiableList(this.indices);
    }

    public String getSizeMatrix() {
        return this.sizeMatrix;
    }

    @Override
    public List<String> getInputVariables() {
        List<String> inputs = new ArrayList<>();

        inputs.add(this.inputMatrix);
        inputs.add(this.sizeMatrix);
        inputs.addAll(this.indices);

        return inputs;
    }

    @Override
    public List<String> getOutputs() {
        return Arrays.asList(this.output);
    }

    public String getOutput() {
        return this.output;
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.NO_SIDE_EFFECT;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(this.output);
        builder.append(" = relative_get ");
        builder.append(this.inputMatrix);

        builder.append(", ");
        builder.append(this.sizeMatrix);

        for (int i = 0; i < this.indices.size(); ++i) {
            builder.append(", ");
            builder.append(this.indices.get(i));
        }

        return builder.toString();
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
        Preconditions.checkArgument(newNames != null);

        this.output = newNames.getOrDefault(this.output, this.output);
        this.inputMatrix = newNames.getOrDefault(this.inputMatrix, this.inputMatrix);
        this.sizeMatrix = newNames.getOrDefault(this.sizeMatrix, this.sizeMatrix);

        renameVariableList(newNames, this.indices);
    }
}
