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

package org.specs.matisselib.unssa;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Preconditions;

public class ValueIdentification {
    private int activeValueTag;
    private final Map<String, Integer> values = new HashMap<>();
    private final Map<Double, Integer> constantValues = new HashMap<>();

    public ValueIdentification() {
    }

    public int newValueTag() {
        Preconditions.checkState(activeValueTag != Integer.MAX_VALUE);

        return ++activeValueTag;
    }

    public void setValueTag(String variable, int valueTag) {
        Preconditions.checkArgument(variable != null);

        values.put(variable, valueTag);
    }

    public Optional<Integer> getValueTag(String variable) {
        Preconditions.checkArgument(variable != null);

        return Optional.ofNullable(values.get(variable));
    }

    public boolean haveSameValueTag(String var1, String var2) {
        Preconditions.checkArgument(var1 != null);
        Preconditions.checkArgument(var2 != null);

        return getValueTag(var1).equals(getValueTag(var2));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        values.keySet()
                .stream()
                .sorted((x, y) -> x.compareTo(y))
                .forEach(key -> {
                    builder.append(key);
                    builder.append(": ");
                    builder.append(values.get(key));
                    builder.append("\n");
                });

        return builder.toString();
    }

    public int valueTagForConstant(double number) {
        Integer candidate = constantValues.get(number);
        if (candidate == null) {
            candidate = newValueTag();
            constantValues.put(number, candidate);
        }
        return candidate;
    }
}
