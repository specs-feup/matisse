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

import java.util.Arrays;
import java.util.List;

/**
 * 
 * Represents code such as <code>y = A(1, :, 2, :);</code>
 * 
 * @author Luís Reis
 *
 */
public final class RangeGetInstruction extends RangeInstruction {

    public RangeGetInstruction(String output, String inputMatrix, Index... indices) {
	this(output, inputMatrix, Arrays.asList(indices));
    }

    public RangeGetInstruction(String output, String inputMatrix, List<Index> indices) {
	super(output, inputMatrix, indices);
    }

    @Override
    public RangeGetInstruction copy() {
	return new RangeGetInstruction(getOutput(), getInputMatrix(), getIndices());
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append(getOutput());

	builder.append(" = range_get ");
	builder.append(getInputMatrix());

	for (Index index : getIndices()) {
	    builder.append(", ");
	    builder.append(index.toString());
	}

	return builder.toString();
    }

}
