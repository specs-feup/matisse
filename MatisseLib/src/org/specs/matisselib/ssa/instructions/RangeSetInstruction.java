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
import java.util.List;
import java.util.Map;

/**
 * 
 * Represents code such as <code>y = A(1, :, 2, :);</code>
 * 
 * @author Luís Reis
 *
 */
public final class RangeSetInstruction extends RangeInstruction {

    private String value;

    public RangeSetInstruction(String output, String inputMatrix, List<Index> indices, String value) {
	super(output, inputMatrix, indices);

	this.value = value;
    }

    @Override
    public List<String> getInputVariables() {
	List<String> inputs = new ArrayList<>(super.getInputVariables());

	inputs.add(this.value);

	return inputs;
    }

    public String getValue() {
	return this.value;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	super.renameVariables(newNames);

	this.value = newNames.getOrDefault(this.value, this.value);
    }

    @Override
    public RangeSetInstruction copy() {
	return new RangeSetInstruction(getOutput(), getInputMatrix(), getIndices(), this.value);
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append(getOutput());

	builder.append(" = range_set ");
	builder.append(getInputMatrix());

	for (Index index : getIndices()) {
	    builder.append(", ");
	    builder.append(index.toString());
	}

	builder.append(", ");
	builder.append(this.value);

	return builder.toString();
    }

}
