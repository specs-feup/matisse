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
import java.util.List;
import java.util.Map;

import org.specs.matisselib.ssa.InstructionType;
import org.specs.matisselib.ssa.instructions.ControlFlowIndependentInstruction;
import org.specs.matlabtocl.v2.ssa.ParallelRegionId;

public final class InvokeParallelFunctionInstruction extends ControlFlowIndependentInstruction {

    private final ParallelRegionId regionId;
    private final List<String> outs;
    private final List<String> ins;

    public InvokeParallelFunctionInstruction(ParallelRegionId regionId, List<String> outs, List<String> ins) {
	this.regionId = regionId;
	this.outs = new ArrayList<>(outs);
	this.ins = new ArrayList<>(ins);
    }

    @Override
    public InvokeParallelFunctionInstruction copy() {
	return new InvokeParallelFunctionInstruction(this.regionId, this.outs, this.ins);
    }

    @Override
    public List<String> getInputVariables() {
	return Collections.unmodifiableList(this.ins);
    }

    @Override
    public List<String> getOutputs() {
	return Collections.unmodifiableList(this.outs);
    }

    public ParallelRegionId getRegionId() {
	return this.regionId;
    }

    @Override
    public InstructionType getInstructionType() {
	return InstructionType.HAS_SIDE_EFFECT;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	renameVariableList(newNames, this.outs);
	renameVariableList(newNames, this.ins);
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();

	if (!this.outs.isEmpty()) {
	    for (int i = 0; i < this.outs.size(); i++) {
		String out = this.outs.get(i);
		if (i != 0) {
		    builder.append(", ");
		}
		builder.append(out);
	    }
	    builder.append(" = ");
	}
	builder.append("invoke_parallel ");
	builder.append(this.regionId);
	if (!this.ins.isEmpty()) {
	    builder.append(" ");
	    for (int i = 0; i < this.ins.size(); i++) {
		String in = this.ins.get(i);
		if (i != 0) {
		    builder.append(", ");
		}
		builder.append(in);
	    }
	}

	return builder.toString();
    }
}
