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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;

public class VariableAllocation {
    private final List<List<String>> variableGroups = new ArrayList<>();

    public VariableAllocation() {

    }

    public int addVariableGroup(List<String> group) {
        Preconditions.checkArgument(group != null);

        // This check is too slow to run when not in debug mode.
        assert !this.variableGroups
                .stream()
                .flatMap(g -> g.stream())
                .anyMatch(e -> group.contains(e)) : "Variable in " + group + " already in different group.";

        this.variableGroups.add(group);
        return this.variableGroups.size() - 1;
    }

    public List<List<String>> getVariableGroups() {
        return Collections.unmodifiableList(this.variableGroups);
    }

    public int addIsolatedVariable(String var) {
        Preconditions.checkArgument(var != null);

        return addVariableGroup(Arrays.asList(var));
    }

    public int getGroupIdForVariable(String variableName) {
        Preconditions.checkArgument(variableName != null);

        for (int groupId = 0; groupId < this.variableGroups.size(); groupId++) {
            List<String> group = this.variableGroups.get(groupId);
            if (group.contains(variableName)) {
                return groupId;
            }
        }

        return -1;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int groupId = 0; groupId < this.variableGroups.size(); groupId++) {
            List<String> group = this.variableGroups.get(groupId);
            builder.append(group.toString());
        }

        return builder.toString();
    }

    public int merge(int groupId1, int groupId2) {
        Preconditions.checkArgument(groupId1 >= 0 && groupId1 < this.variableGroups.size());
        Preconditions.checkArgument(groupId2 >= 0 && groupId2 < this.variableGroups.size());
        Preconditions.checkArgument(groupId1 != groupId2);

        List<String> group1 = this.variableGroups.get(groupId1);
        List<String> group2 = this.variableGroups.get(groupId2);

        List<String> newGroup = new ArrayList<>(group1);
        newGroup.addAll(group2);
        this.variableGroups.remove(group2);

        int index = this.variableGroups.indexOf(group1);
        this.variableGroups.set(index, newGroup);
        return index;
    }
}
