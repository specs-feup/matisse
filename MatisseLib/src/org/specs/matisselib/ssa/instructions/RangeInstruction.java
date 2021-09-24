/**
 * Copyright 2016 SPeCS.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.specs.matisselib.ssa.InstructionType;

import com.google.common.base.Preconditions;

public abstract class RangeInstruction extends ControlFlowIndependentInstruction {
    private String output;
    private String inputMatrix;
    private final List<Index> indices;

    protected RangeInstruction(String output, String inputMatrix, List<Index> indices) {
	Preconditions.checkArgument(output != null);
	Preconditions.checkArgument(inputMatrix != null);
	Preconditions.checkArgument(indices != null);
	Preconditions.checkArgument(indices.size() >= 2);

	this.output = output;
	this.inputMatrix = inputMatrix;
	this.indices = new ArrayList<>();
	indices.forEach(index -> this.indices.add(index.copy()));
    }

    public static Index variable(String name) {
	return new NormalIndex(name);
    }

    public static Index fullRange() {
	return new FullRangeIndex();
    }

    public static Index partialRange(String start, String end) {
	return new PartialRangeIndex(start, end);
    }

    public static abstract class Index {
	protected Index() {
	}

	abstract void renameVariables(Map<String, String> newNames);

	protected abstract Index copy();

	protected abstract List<String> getInputVariables();
    }

    public static class NormalIndex extends Index {
	private String index;

	private NormalIndex(String index) {
	    this.index = index;
	}

	@Override
	public NormalIndex copy() {
	    return new NormalIndex(this.index);
	}

	public String getIndex() {
	    return this.index;
	}

	@Override
	void renameVariables(Map<String, String> newNames) {
	    this.index = newNames.getOrDefault(this.index, this.index);
	}

	@Override
	protected List<String> getInputVariables() {
	    return Arrays.asList(this.index);
	}

	@Override
	public String toString() {
	    return this.index;
	}
    }

    public static class FullRangeIndex extends Index {
	public FullRangeIndex() {
	}

	@Override
	public FullRangeIndex copy() {
	    return new FullRangeIndex();
	}

	@Override
	void renameVariables(Map<String, String> newNames) {
	}

	@Override
	protected List<String> getInputVariables() {
	    return Collections.emptyList();
	}

	@Override
	public String toString() {
	    return ":";
	}
    }

    public static class PartialRangeIndex extends Index {
	String start, end;

	public PartialRangeIndex(String start, String end) {
	    this.start = start;
	    this.end = end;
	}

	public String getStart() {
	    return this.start;
	}

	public String getEnd() {
	    return this.end;
	}

	@Override
	public PartialRangeIndex copy() {
	    return new PartialRangeIndex(this.start, this.end);
	}

	@Override
	void renameVariables(Map<String, String> newNames) {
	    this.start = newNames.getOrDefault(this.start, this.start);
	    this.end = newNames.getOrDefault(this.end, this.end);
	}

	@Override
	protected List<String> getInputVariables() {
	    return Arrays.asList(this.start, this.end);
	}

	@Override
	public String toString() {
	    return this.start + ":" + this.end;
	}
    }

    public String getInputMatrix() {
	return this.inputMatrix;
    }

    public List<Index> getIndices() {
	return Collections.unmodifiableList(this.indices);
    }

    public String getOutput() {
	return this.output;
    }

    @Override
    public List<String> getOutputs() {
	return Arrays.asList(this.output);
    }

    @Override
    public InstructionType getInstructionType() {
	return InstructionType.HAS_VALIDATION_SIDE_EFFECT;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	this.output = newNames.getOrDefault(this.output, this.output);
	this.inputMatrix = newNames.getOrDefault(this.inputMatrix, this.inputMatrix);
	this.indices.forEach(index -> index.renameVariables(newNames));
    }

    @Override
    public List<String> getInputVariables() {
	List<String> inputs = new ArrayList<>();

	inputs.add(getInputMatrix());
	this.getIndices().forEach(index -> inputs.addAll(index.getInputVariables()));

	return Collections.unmodifiableList(inputs);
    }
}
