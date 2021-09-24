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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.instructions.ControlFlowIndependentInstruction;
import org.specs.matlabtocl.v2.codegen.GeneratedKernel;

public class InvokeKernelInstruction extends ControlFlowIndependentInstruction {
    private final GeneratedKernel instance;
    private final List<String> globalSizes;
    private final List<String> arguments;
    private final List<String> outputs;
    private final List<Integer> outputSources;

    public InvokeKernelInstruction(GeneratedKernel instance, List<String> globalSizes,
            List<String> arguments, List<String> outputs, List<Integer> outputSources) {
        this.instance = instance;
        this.globalSizes = new ArrayList<>(globalSizes);
        this.arguments = new ArrayList<>(arguments);
        this.outputs = new ArrayList<>(outputs);
        this.outputSources = new ArrayList<>(outputSources);
    }

    @Override
    public InvokeKernelInstruction copy() {
        return new InvokeKernelInstruction(this.instance, this.globalSizes, this.arguments, this.outputs,
                this.outputSources);
    }

    @Override
    public List<String> getInputVariables() {
        List<String> inputs = new ArrayList<>();
        inputs.addAll(this.globalSizes);
        inputs.addAll(this.arguments);
        inputs.addAll(instance.getParallelSettings().getInputVariables());
        return inputs;
    }

    public List<String> getArguments() {
        return Collections.unmodifiableList(this.arguments);
    }

    public void setArgument(int index, String argument) {
        arguments.set(index, argument);
    }

    public List<String> getGlobalSizes() {
        return Collections.unmodifiableList(this.globalSizes);
    }

    public GeneratedKernel getInstance() {
        return this.instance;
    }

    @Override
    public List<String> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    public List<Integer> getOutputSources() {
        return Collections.unmodifiableList(outputSources);
    }

    @Override
    public Set<String> getEntryInterferentOutputs() {
        // TODO: Can we use argument information (isReadOnly) to improve this?
        return new HashSet<>(outputs);
    }

    public void removeOutput(int i) {
        outputs.remove(i);
        outputSources.remove(i);
    }

    @Override
    public InstructionType getInstructionType() {
        return InstructionType.HAS_SIDE_EFFECT;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
        renameVariableList(newNames, this.arguments);
        renameVariableList(newNames, this.globalSizes);
        renameVariableList(newNames, this.outputs);
    }

    @Override
    public String toString() {
        StringBuilder invocation = new StringBuilder();
        if (!outputs.isEmpty()) {
            for (int i = 0; i < outputs.size(); ++i) {
                if (i != 0) {
                    invocation.append(", ");
                }

                invocation.append(outputs.get(i));
                invocation.append("(");
                invocation.append(outputSources.get(i));
                invocation.append(")");
            }
            invocation.append(" = ");
        }
        invocation.append("invoke_kernel ");
        invocation.append(this.instance.getInstanceName());
        invocation.append(" ");
        invocation.append(this.globalSizes);
        invocation.append(", ");
        invocation.append(this.arguments.stream().collect(Collectors.joining(", ")));
        return invocation.toString();
    }
}
