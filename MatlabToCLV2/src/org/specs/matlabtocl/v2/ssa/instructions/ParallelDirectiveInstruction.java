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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.instructions.ControlFlowIndependentInstruction;
import org.specs.matlabtocl.v2.ssa.ParallelRegionSettings;

public final class ParallelDirectiveInstruction extends ControlFlowIndependentInstruction {

    private ParallelRegionSettings settings;

    public ParallelDirectiveInstruction(ParallelRegionSettings settings) {
        this.settings = settings;
    }

    @Override
    public ParallelDirectiveInstruction copy() {
        return new ParallelDirectiveInstruction(settings);
    }

    public ParallelRegionSettings getSettings() {
        return settings;
    }

    @Override
    public List<String> getInputVariables() {
        return settings.getInputVariables();
    }

    @Override
    public List<String> getOutputs() {
        return Collections.emptyList();
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.HAS_SIDE_EFFECT;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
        settings.renameVariables(newNames);
    }

    @Override
    public String toString() {
        return "parallel_directive";
    }

}
