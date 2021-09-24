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

package org.specs.matlabtocl.v2.ssa.instructions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.instructions.ControlFlowIndependentInstruction;
import org.specs.matlabtocl.v2.codegen.ReductionType;

import com.google.common.base.Preconditions;

public class CompleteReductionInstruction extends ControlFlowIndependentInstruction {

    private String output;
    private final ReductionType reductionType;
    private final VariableType underlyingType;
    private String buffer;
    private String numGroups;
    private String initialValue;

    public CompleteReductionInstruction(String output,
            ReductionType reductionType,
            String buffer,
            VariableType underlyingType,
            String numGroups,
            String initialValue) {

        Preconditions.checkArgument(output != null);
        Preconditions.checkArgument(reductionType != null);
        Preconditions.checkArgument(buffer != null);
        Preconditions.checkArgument(underlyingType != null);
        Preconditions.checkArgument(initialValue != null);
        Preconditions.checkArgument(reductionType != ReductionType.MATRIX_SET || numGroups == null,
                "For reductions of type MATRIX_SET, numGroups is not used");

        this.output = output;
        this.reductionType = reductionType;
        this.underlyingType = underlyingType;
        this.buffer = buffer;
        this.numGroups = numGroups;
        this.initialValue = initialValue;
    }

    @Override
    public CompleteReductionInstruction copy() {
        return new CompleteReductionInstruction(this.output, this.reductionType, this.buffer, this.underlyingType,
                this.numGroups, this.initialValue);
    }

    @Override
    public List<String> getInputVariables() {
        List<String> vars = new ArrayList<>();

        vars.add(this.buffer);
        if (this.numGroups != null) {
            vars.add(this.numGroups);
        }
        vars.add(this.initialValue);

        return vars;
    }

    @Override
    public List<String> getOutputs() {
        return Arrays.asList(this.output);
    }

    public String getOutput() {
        return this.output;
    }

    public ReductionType getReductionType() {
        return this.reductionType;
    }

    public String getBuffer() {
        return this.buffer;
    }

    public VariableType getUnderlyingType() {
        return this.underlyingType;
    }

    public String getNumGroups() {
        return this.numGroups;
    }

    public String getInitialValue() {
        return this.initialValue;
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.NO_SIDE_EFFECT;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
        this.output = newNames.getOrDefault(this.output, this.output);
        this.buffer = newNames.getOrDefault(this.buffer, this.buffer);
        this.numGroups = newNames.getOrDefault(this.numGroups, this.numGroups);
        this.initialValue = newNames.getOrDefault(this.initialValue, this.initialValue);
    }

    @Override
    public String toString() {
        return this.output + " = complete_reduction " + this.reductionType + " " + this.buffer + ", " + this.numGroups
                + ", " + this.initialValue;
    }
}
