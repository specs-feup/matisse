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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.specs.matisselib.ssa.InstructionType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

public class ReadGlobalInstruction extends ControlFlowIndependentInstruction {

    private String output;
    private final String global;

    public ReadGlobalInstruction(String output, String global) {
        Preconditions.checkArgument(output != null);
        Preconditions.checkArgument(global != null);
        Preconditions.checkArgument(global.startsWith("^"));

        this.output = output;
        this.global = global;
    }

    @Override
    public ReadGlobalInstruction copy() {
        return new ReadGlobalInstruction(output, global);
    }

    @Override
    public List<String> getInputVariables() {
        return Collections.emptyList();
    }

    public String getOutput() {
        return output;
    }

    public String getGlobal() {
        return global;
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
    public Set<String> getReferencedGlobals() {
        return Sets.newHashSet(global);
    }

    @Override
    public boolean dependsOnGlobalState() {
        return true;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
        output = newNames.getOrDefault(output, output);
    }

    @Override
    public String toString() {
        return output + " = read_global " + global;
    }
}
