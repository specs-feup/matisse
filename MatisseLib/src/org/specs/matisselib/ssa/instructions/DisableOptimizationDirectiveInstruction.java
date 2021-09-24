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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.specs.matisselib.ssa.InstructionType;

import com.google.common.base.Preconditions;

public class DisableOptimizationDirectiveInstruction extends ControlFlowIndependentInstruction {
    private final String optimization;

    public DisableOptimizationDirectiveInstruction(String optimization) {
        Preconditions.checkArgument(optimization != null);

        this.optimization = optimization;
    }

    @Override
    public DisableOptimizationDirectiveInstruction copy() {
        return new DisableOptimizationDirectiveInstruction(optimization);
    }

    @Override
    public List<String> getInputVariables() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getOutputs() {
        return Collections.emptyList();
    }

    public String getOptimization() {
        return optimization;
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.DECORATOR;
    }

    @Override
    public String toString() {
        return "%!disable_optimization " + optimization;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
    }
}
