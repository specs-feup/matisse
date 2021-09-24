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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.ssa.InstructionType;

import com.google.common.base.Preconditions;

public final class TypedFunctionCallInstruction extends FunctionCallInstruction {
    private final FunctionType functionType;

    public TypedFunctionCallInstruction(String functionName,
            FunctionType functionType,
            List<String> outputs,
            List<String> inputs) {

        super(functionName, outputs, inputs);

        Preconditions.checkArgument(functionType != null);

        this.functionType = functionType;
    }

    public TypedFunctionCallInstruction(String functionName,
            FunctionType functionType,
            String output,
            String... inputs) {
        this(functionName, functionType, Arrays.asList(output), Arrays.asList(inputs));

        Preconditions.checkArgument(output != null);
    }

    @Override
    public TypedFunctionCallInstruction copy() {
        return new TypedFunctionCallInstruction(this.getFunctionName(),
                this.functionType,
                this.getOutputs(),
                this.getInputVariables());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(getOutputs().stream().collect(Collectors.joining(", ")));
        if (!getOutputs().isEmpty()) {
            builder.append(" = ");
        }

        builder.append("call ");
        builder.append(getFunctionName());
        builder.append(" ");
        builder.append(this.functionType.getOutputTypes()
                .stream()
                .map(type -> type.toString())
                .collect(Collectors.joining(", ", "[", "]")));

        if (!getInputVariables().isEmpty()) {
            builder.append(" ");
        }

        builder.append(getInputVariables().stream().collect(Collectors.joining(", ")));

        return builder.toString();
    }

    @Override
    public InstructionType getInstructionType() {
        return this.functionType.canHaveSideEffects() ? InstructionType.HAS_SIDE_EFFECT
                : InstructionType.NO_SIDE_EFFECT;
    }

    @Override
    public Set<String> getEntryInterferentOutputs() {
        // Simple functions can't have interferences.
        // Only those with non-scalar outputs-as-inputs can.

        List<VariableType> outputAsInputTypes = this.functionType.getOutputAsInputTypes();
        if (outputAsInputTypes.size() == 0) {
            // No need to worry about by-refs, because functions with by-refs *always* have outputs as inputs.
            return Collections.emptySet();
        }

        Set<String> entryInterferentOutputs = new HashSet<>();

        for (int i = 0; i < functionType.getArgumentsTypes().size(); ++i) {
            if (!functionType.isInputReference(i) && !(functionType.getArgumentsTypes().get(i) instanceof ScalarType)) {
                entryInterferentOutputs.add(getInputVariables().get(i));
            }
        }

        return entryInterferentOutputs;
    }

    @Override
    public boolean dependsOnGlobalState() {
        return functionType.dependsOnGlobalState();
    }

    public FunctionType getFunctionType() {
        return this.functionType;
    }
}
