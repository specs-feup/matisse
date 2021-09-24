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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.specs.matisselib.ssa.InstructionType;

public final class AssumeMatrixSizesMatchDirectiveInstruction extends ControlFlowIndependentInstruction {

    public static final String DIRECTIVE_CONTENT = "assume_matrix_sizes_match";

    public AssumeMatrixSizesMatchDirectiveInstruction() {
    }

    @Override
    public AssumeMatrixSizesMatchDirectiveInstruction copy() {
	return new AssumeMatrixSizesMatchDirectiveInstruction();
    }

    @Override
    public List<String> getInputVariables() {
	return Collections.emptyList();
    }

    @Override
    public List<String> getOutputs() {
	return Collections.emptyList();
    }

    @Override
    public InstructionType getInstructionType() {
	return InstructionType.DECORATOR;
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
	// Do nothing.
    }

    @Override
    public String toString() {
	return "%!" + AssumeMatrixSizesMatchDirectiveInstruction.DIRECTIVE_CONTENT;
    }
}
