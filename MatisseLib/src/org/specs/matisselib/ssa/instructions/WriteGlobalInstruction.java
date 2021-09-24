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

public class WriteGlobalInstruction extends ControlFlowIndependentInstruction {

    private final String global;
    private String ssaVariable;

    public WriteGlobalInstruction(String global, String ssaVariable) {
        Preconditions.checkArgument(global != null);
        Preconditions.checkArgument(ssaVariable != null);
        Preconditions.checkArgument(!ssaVariable.startsWith("^"));
        Preconditions.checkArgument(global.startsWith("^"));

        this.global = global;
        this.ssaVariable = ssaVariable;
    }

    @Override
    public WriteGlobalInstruction copy() {
        return new WriteGlobalInstruction(global, ssaVariable);
    }

    @Override
    public List<String> getInputVariables() {
        return Arrays.asList(ssaVariable);
    }

    @Override
    public List<String> getOutputs() {
        return Collections.emptyList();
    }

    public String getGlobal() {
        return global;
    }

    public String getSsaVariable() {
        return ssaVariable;
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.HAS_SIDE_EFFECT;
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
        ssaVariable = newNames.getOrDefault(ssaVariable, ssaVariable);
    }

    @Override
    public String toString() {
        return "write_global " + global + ", " + ssaVariable;
    }
}
