/**
 * Copyright 2017 SPeCS.
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AllocatedNamesSet {
    private Set<String> regexBlacklistedNames = new HashSet<>();
    private Set<String> allocatedNames = new HashSet<>();
    private Map<String, Integer> firstNames = new HashMap<>();

    public AllocatedNamesSet(Set<String> blacklistedNames) {
        for (String blacklistedName : blacklistedNames) {
            if (blacklistedName.matches("^[a-zA-Z0-9_]+$")) {
                allocatedNames.add(blacklistedName);
            } else {
                regexBlacklistedNames.add(blacklistedName);
            }
        }
    }

    boolean isValidName(String candidateName) {
        return !allocatedNames.contains(candidateName) &&
                !regexBlacklistedNames.stream().anyMatch(n -> candidateName.matches("^" + n + "$"));
    }

    int getFirstPossibleName(String baseName) {
        return firstNames.getOrDefault(baseName, 0);
    }

    void add(String baseName, String name, int number) {
        firstNames.put(baseName, number + 1);
        allocatedNames.add(name);
    }

    public String allocateName(String baseName) {
        String name;
        int i = getFirstPossibleName(baseName);
        for (;;) {
            String candidateName = baseName + (i == 0 ? "" : "_" + i);
            if (isValidName(candidateName)) {
                name = candidateName;
                break;
            }
            ++i;
        }

        add(baseName, name, i);

        return name;
    }
}
