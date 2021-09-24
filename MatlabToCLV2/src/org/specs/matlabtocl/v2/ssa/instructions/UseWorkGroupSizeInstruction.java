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

package org.specs.matlabtocl.v2.ssa.instructions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.instructions.ControlFlowIndependentInstruction;

public class UseWorkGroupSizeInstruction extends ControlFlowIndependentInstruction {
    private String output;
    private String input;

    public UseWorkGroupSizeInstruction(String output, String input) {
        this.output = output;
        this.input = input;
    }

    @Override
    public UseWorkGroupSizeInstruction copy() {
        return new UseWorkGroupSizeInstruction(output, input);
    }

    public String getInput() {
        return input;
    }

    @Override
    public List<String> getInputVariables() {
        return Arrays.asList(input);
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
    public void renameVariables(Map<String, String> newNames) {
        output = newNames.getOrDefault(newNames, output);
        input = newNames.getOrDefault(newNames, input);
    }

    @Override
    public String toString() {
        return output + " = use_work_group " + input;
    }

}
