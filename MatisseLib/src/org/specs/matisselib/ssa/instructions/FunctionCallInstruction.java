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

import com.google.common.base.Preconditions;

public abstract class FunctionCallInstruction extends ControlFlowIndependentInstruction {
    private final String functionName;
    private final List<String> outputs;
    private final List<String> inputs;

    protected FunctionCallInstruction(String functionName,
            List<String> outputs,
            List<String> inputs) {
        Preconditions.checkArgument(functionName != null);
        Preconditions.checkArgument(outputs != null);
        Preconditions.checkArgument(inputs != null);

        this.functionName = functionName;
        this.outputs = new ArrayList<>(outputs);
        this.inputs = new ArrayList<>(inputs);
    }

    public String getFunctionName() {
        return functionName;
    }

    @Override
    public List<String> getInputVariables() {
        return Collections.unmodifiableList(inputs);
    }

    @Override
    public List<String> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    @Override
    public void renameVariables(Map<String, String> newNames) {
        Preconditions.checkArgument(newNames != null);

        renameVariableList(newNames, outputs);
        renameVariableList(newNames, inputs);
    }
}
