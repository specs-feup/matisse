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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.collections.MultiMap;

public class InterferenceGraph {
    private Map<String, Integer> variables = new HashMap<>();
    private MultiMap<Integer, Integer> interferences = new MultiMap<>();
    private int maxIndex = 0;

    public void addInterference(String var1, String var2) {
        Preconditions.checkArgument(var1 != null);
        Preconditions.checkArgument(var2 != null);

        int var1Idx = createIfNotExists(var1);
        int var2Idx = createIfNotExists(var2);

        if (interferences.get(var1Idx).contains(var2Idx)) {
            return;
        }

        interferences.put(var1Idx, var2Idx);
        interferences.put(var2Idx, var1Idx);
    }

    public boolean hasInterference(String var1, String var2) {
        Preconditions.checkArgument(var1 != null);
        Preconditions.checkArgument(var2 != null);

        // About the variables.contains check:
        // We could just return false in these cases (as would happen if we just removed these two checks)
        // However, if we call hasInterference for a variable not in variables
        // That is almost certainly a bug in the caller -- one we want to detect.
        // Additionally, any legitimate use cases can be easily worked around this.
        Preconditions.checkArgument(variables.containsKey(var1));
        Preconditions.checkArgument(variables.containsKey(var2));

        return interferences.get(variables.get(var1)).contains(variables.get(var2));
    }

    /**
     * Modifies interferences so that if any variable in the group V has an interference with another variable W, then
     * all variables in the group have interference with W.
     * 
     * @param group
     *            The group of variables
     */
    public void mergeGroup(List<String> group, String newGroupName) {
        Preconditions.checkArgument(group.contains(newGroupName));
        Preconditions.checkArgument(variables.containsKey(newGroupName),
                variables + " does not contain " + newGroupName);

        List<Integer> groupIndices = new ArrayList<>();
        for (String element : group) {
            groupIndices.add(variables.get(element));
        }

        Set<Integer> combinedInterferences = new HashSet<>();
        for (String element : group) {
            assert element != null;
            if (!variables.containsKey(element)) {
                continue;
            }

            int elementIndex = variables.get(element);
            for (int interference : interferences.get(elementIndex)) {
                assert !group.contains(interference);
                combinedInterferences.add(interference);
            }

            interferences.remove(elementIndex);
        }

        int newGroupIndex = variables.get(newGroupName);

        for (int element : combinedInterferences) {
            interferences.put(element, newGroupIndex);
            interferences.put(newGroupIndex, element);
        }

        for (String obsoleteVar : group) {
            variables.remove(obsoleteVar);
        }
        variables.put(newGroupName, newGroupIndex);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(variables);
        builder.append('\n');
        for (int var1 : interferences.keySet()) {
            builder.append("Interferences with " + var1 + ":\n");
            for (int var2 : interferences.get(var1)) {
                builder.append("\t" + var2 + "\n");
            }
        }

        return builder.toString();
    }

    public void addVariables(Collection<String> newVars) {
        for (String var : newVars) {
            createIfNotExists(var);
        }
    }

    private int createIfNotExists(String varName) {
        if (!variables.containsKey(varName)) {
            int index = maxIndex++;
            variables.put(varName, index);

            return index;
        }

        return variables.get(varName);
    }
}
