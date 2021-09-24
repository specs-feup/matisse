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
import java.util.List;
import java.util.Map;

import org.specs.matisselib.ssa.InstructionType;

import com.google.common.base.Preconditions;

public class StringInstruction extends ControlFlowIndependentInstruction {

    private String output;
    private final String string;

    public StringInstruction(String output, String string) {
	Preconditions.checkArgument(output != null);
	Preconditions.checkArgument(string != null);

	this.output = output;
	this.string = string;
    }

    @Override
    public StringInstruction copy() {
	return new StringInstruction(this.output, this.string);
    }

    @Override
    public List<String> getInputVariables() {
	return Arrays.asList();
    }

    public String getOutput() {
	return this.output;
    }

    @Override
    public List<String> getOutputs() {
	return Arrays.asList(this.output);
    }

    public String getValue() {
	return this.string;
    }

    @Override
    public InstructionType getInstructionType() {
	return InstructionType.NO_SIDE_EFFECT;
    }

    @Override
    public String toString() {
	return this.output + " = str \"" + escape(this.string) + "\"";
    }

    private static String escape(String str) {
	StringBuilder builder = new StringBuilder();

	for (int i = 0; i < str.length(); ++i) {
	    char ch = str.charAt(i);

	    if (ch == '"') {
		builder.append("\\\"");
	    }
	    else if (ch == '\\') {
		builder.append("\\\\");
	    }
	    else if (ch >= 32 && ch <= 126) {
		builder.append(ch);
	    }
	    else {
		builder.append("\\u" + Integer.toHexString(ch));
	    }
	}

	return builder.toString();
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	Preconditions.checkArgument(newNames != null);

	this.output = newNames.getOrDefault(this.output, this.output);
    }
}
