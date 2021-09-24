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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.specs.matisselib.ssa.InstructionLifetimeInformation;
import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.instructions.ControlFlowIndependentInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

/**
 * Indicates that the specified variable has been initialized in some mechanism exterior to SSA.
 * 
 * @author Luís Reis
 *
 */
public class InitializationInstruction extends ControlFlowIndependentInstruction {
    private String output;

    public InitializationInstruction(String output) {
        this.output = output;
    }

    @Override
    public SsaInstruction copy() {
        return new InitializationInstruction(this.output);
    }

    @Override
    public List<String> getInputVariables() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getOutputs() {
        return Arrays.asList(this.output);
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.NO_SIDE_EFFECT;
    }

    @Override
    public InstructionLifetimeInformation getLifetimeInformation() {
        // Needed for non-direct schedules.
        return InstructionLifetimeInformation.BLOCK_LIFETIME;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
        this.output = newNames.getOrDefault(this.output, this.output);
    }

    @Override
    public String toString() {
        return this.output + " = init";
    }
}
