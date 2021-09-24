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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.specs.matisselib.passes.ssa.ConvertToCssaPass;
import org.specs.matisselib.ssa.InstructionType;

import com.google.common.base.Preconditions;

/**
 * Used by {@link ConvertToCssaPass}.
 * 
 * @author Luís Reis
 *
 */
public class ParallelCopyInstruction extends ControlFlowIndependentInstruction {

    private final List<String> inputs;
    private final List<String> outputs;

    public ParallelCopyInstruction(List<String> inputs, List<String> outputs) {
	this.inputs = new ArrayList<>(inputs);
	this.outputs = new ArrayList<>(outputs);
    }

    @Override
    public ParallelCopyInstruction copy() {
	return new ParallelCopyInstruction(this.inputs, this.outputs);
    }

    @Override
    public List<String> getInputVariables() {
	return Collections.unmodifiableList(this.inputs);
    }

    @Override
    public List<String> getOutputs() {
	return Collections.unmodifiableList(this.outputs);
    }

    @Override
    public InstructionType getInstructionType() {
	return InstructionType.NO_SIDE_EFFECT;
    }

    @Override
    public String toString() {
	return this.outputs.stream().collect(Collectors.joining(", ")) +
		" = parallel_copy " +
		this.inputs.stream().collect(Collectors.joining(", "));
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	Preconditions.checkArgument(newNames != null);

	renameVariableList(newNames, this.inputs);
	renameVariableList(newNames, this.outputs);
    }

}
