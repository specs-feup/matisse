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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

public class VariableNameChooser {
    private static final List<String> LOW_STRENGTH_NAMES = Arrays.asList("one");

    private VariableNameChooser() {
    }

    public static List<String> getNames(VariableAllocation variableAllocation, Set<String> blacklistedNames) {
        Preconditions.checkArgument(variableAllocation != null);
        Preconditions.checkArgument(blacklistedNames != null);

        AllocatedNamesSet allocatedNames = new AllocatedNamesSet(blacklistedNames);
        List<String> names = new ArrayList<>();
        List<String> globalNames = new ArrayList<>();
        int currentGlobal = 0;

        for (List<String> group : variableAllocation.getVariableGroups()) {
            if (group.stream().anyMatch(v -> v.startsWith("^"))) {
                // Groups with globals are allocated first.
                String name = allocateName(group, allocatedNames);
                globalNames.add(name);

                assert group.stream()
                        .filter(v -> v.startsWith("^"))
                        .findFirst().get()
                        .substring(1)
                        .equals(name) : "Wrong name given to " + group + ": " + name + ", list of groups: "
                                + variableAllocation.getVariableGroups() + ", allocatedNames=" + allocatedNames;
            }
        }

        for (List<String> group : variableAllocation.getVariableGroups()) {
            if (!group.stream().anyMatch(v -> v.startsWith("^"))) {
                // Deal with non-globals, as globals have already been dealt with in the previous loop.
                String name = allocateName(group, allocatedNames);
                names.add(name);
            } else {
                names.add(globalNames.get(currentGlobal++));
            }
        }

        return names;
    }

    private static String allocateName(List<String> group, AllocatedNamesSet allocatedNames) {
        String baseName = getBaseName(group);

        if (baseName.matches("^[0-9]")) {
            // Make sure that the variable name doesn't start with a digit
            baseName = "x" + baseName;
        }

        return allocatedNames.allocateName(baseName);
    }

    private static String getBaseName(List<String> group) {
        List<String> temporaries = new ArrayList<>();

        for (String name : group) {
            if (name.startsWith("^")) {
                return name.substring(1);
            }
        }

        for (String name : group) {
            if (name.startsWith("$")) {
                int semanticsEnd = name.lastIndexOf('$');
                if (semanticsEnd >= 0) {
                    String nameProposal = name.substring(1, semanticsEnd);
                    nameProposal = nameProposal.replace('$', '_');
                    nameProposal = nameProposal.replaceAll("_+", "_");
                    nameProposal = nameProposal.replaceAll("^_+", "");
                    temporaries.add(nameProposal);
                }
            } else {
                String mainName = name.substring(0, name.indexOf('$'));
                assert mainName.matches("^[a-zA-Z_+0-9]+$") : "Invalid name format: " + mainName;

                String[] namePortions = mainName.split("\\+");

                for (String namePortion : namePortions) {
                    assert !namePortion.contains("+");
                }

                assert Stream.of(namePortions)
                        .allMatch(new HashSet<>()::add) : "Name contains duplicate yet identical portions: " + name;

                return namePortions[0];
            }
        }

        // If only *some* of the names are "weaker", then filter those out.
        if (temporaries.stream().anyMatch(name -> !VariableNameChooser.LOW_STRENGTH_NAMES.contains(name))) {
            temporaries.removeIf(VariableNameChooser.LOW_STRENGTH_NAMES::contains);
        }

        Collections.sort(temporaries, (a, b) -> a.length() - b.length());

        // It's a name composed only of temporaries
        if (!temporaries.isEmpty()) {
            String temporaryName = temporaries.get(0).split("\\+")[0];

            return temporaryName;
        }

        // When all else fails
        return "tmp";
    }
}
