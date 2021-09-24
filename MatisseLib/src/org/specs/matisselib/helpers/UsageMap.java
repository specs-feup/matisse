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

package org.specs.matisselib.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

public final class UsageMap {
    private final Map<String, Integer> map = new HashMap<>();

    private UsageMap() {
    }

    public static UsageMap buildEmpty() {
        return new UsageMap();
    }

    public static UsageMap build(FunctionBody body, String... trackVariableUsages) {
        return build(body, Arrays.asList(trackVariableUsages));
    }

    public static UsageMap build(FunctionBody body, List<String> trackVariableUsages) {
        UsageMap usageMap = new UsageMap();

        for (SsaInstruction instruction : body.getFlattenedInstructionsIterable()) {
            for (String input : instruction.getInputVariables()) {
                if (trackVariableUsages.contains(input)) {
                    System.out.println("Variable used in " + instruction);
                }
                usageMap.increment(input);
            }

            for (String output : instruction.getOutputs()) {
                assert output != null : "Output is null in " + instruction;

                if (output.endsWith("$ret")) {
                    // Ensure output variables are referenced.

                    usageMap.increment(output);
                }
            }
        }

        return usageMap;
    }

    public int getUsageCount(String variable) {
        return this.map.getOrDefault(variable, 0);
    }

    public void decrement(String variable) {
        int oldUsage = getUsageCount(variable);
        assert oldUsage > 0;

        this.map.put(variable, oldUsage - 1);
    }

    public void increment(String input) {
        int usages = this.map.getOrDefault(input, 0);
        this.map.put(input, usages + 1);
    }

    public void remove(UsageMap other) {
        for (String key : other.map.keySet()) {
            int usages = other.map.get(key);

            int newUsages = this.map.get(key) - usages;
            assert newUsages >= 0;
            this.map.put(key, newUsages);
        }
    }

    @Override
    public String toString() {
        List<String> variables = new ArrayList<>(map.keySet());
        variables.sort((s1, s2) -> s1.compareTo(s2));

        return variables.stream()
                .filter(variable -> map.get(variable) > 0)
                .map(variable -> variable + ": " + getUsageCount(variable))
                .collect(Collectors.joining(", ", "[UsageMap ", "]"));
    }
}
