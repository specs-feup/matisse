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

package org.specs.matlabtocl.v2.ssa;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.ssa.FunctionBody;

import com.google.common.base.Preconditions;

public final class ParallelRegionInstance {
    private final ParallelRegionSettings parallelSettings;
    private final FunctionBody body;
    private final List<String> ins;
    private final List<String> outs;
    private final Map<String, VariableType> types;

    public ParallelRegionInstance(ParallelRegionSettings parallelSettings, FunctionBody body, List<String> ins,
            List<String> outs,
            Map<String, VariableType> types) {
        Preconditions.checkArgument(body != null);
        Preconditions.checkArgument(types != null);

        this.parallelSettings = parallelSettings;
        this.body = body;
        this.ins = ins;
        this.outs = outs;
        this.types = types;
    }

    public FunctionBody getBody() {
        return this.body;
    }

    public ParallelRegionSettings getParallelSettings() {
        return parallelSettings;
    }

    public Optional<VariableType> getType(String variableName) {
        Preconditions.checkArgument(variableName != null);

        return Optional.ofNullable(this.types.get(variableName));
    }

    public Function<String, Optional<VariableType>> getTypeGetter() {
        return name -> Optional.ofNullable(types.get(name));
    }

    public String makeTemporary(String suggestedName, VariableType type) {
        String name = this.body.makeTemporary(suggestedName);

        this.types.put(name, type);

        return name;
    }

    public List<String> getInputVariables() {
        return Collections.unmodifiableList(this.ins);
    }

    public List<String> getOutputVariables() {
        return Collections.unmodifiableList(this.outs);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Parallel instance:");
        builder.append("\ninputs: ");
        builder.append(ins);
        builder.append("\noutputs: ");
        builder.append(outs);
        builder.append("\n");
        builder.append(body);

        return builder.toString();
    }

    public void addType(String name, VariableType type) {
        types.put(name, type);
    }
}
