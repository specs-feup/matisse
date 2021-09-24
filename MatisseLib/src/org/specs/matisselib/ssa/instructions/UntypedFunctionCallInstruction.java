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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.specs.matisselib.ssa.InstructionType;

public final class UntypedFunctionCallInstruction extends FunctionCallInstruction {

    public UntypedFunctionCallInstruction(String functionName,
            List<String> outputs,
            List<String> inputs) {

        super(functionName, outputs, inputs);
    }

    @Override
    public UntypedFunctionCallInstruction copy() {
        return new UntypedFunctionCallInstruction(getFunctionName(), getOutputs(), getInputVariables());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(getOutputs().stream().collect(Collectors.joining(", ")));
        if (!getOutputs().isEmpty()) {
            builder.append(" = ");
        }

        builder.append("untyped_call ");
        builder.append(getFunctionName());

        if (!getInputVariables().isEmpty()) {
            builder.append(" ");
        }

        builder.append(getInputVariables().stream().collect(Collectors.joining(", ")));

        return builder.toString();
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.HAS_SIDE_EFFECT;
    }

    @Override
    public Set<String> getEntryInterferentOutputs() {
        // Until type inference, we can't be sure whether a function has interferences.
        // We default to the conservative approach.
        return new HashSet<>(getInputVariables());
    }

    @Override
    public boolean dependsOnGlobalState() {
        // Assume the worst-case scenario.
        // After type inference, we'll have a better idea.
        return true;
    }
}
