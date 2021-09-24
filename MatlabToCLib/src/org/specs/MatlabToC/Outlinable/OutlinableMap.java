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

package org.specs.MatlabToC.Outlinable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import pt.up.fe.specs.util.SpecsLogs;

public class OutlinableMap {

    private final Map<String, OutlineCheck> outlineChecks;

    public OutlinableMap() {
	this.outlineChecks = new HashMap<>();
    }

    public Optional<OutlineCheck> getRule(String name) {
	if (!this.outlineChecks.containsKey(name)) {
	    return Optional.empty();
	}

	return Optional.of(this.outlineChecks.get(name));
    }

    public void addRule(String name, OutlineCheck outlineCheck) {
	OutlineCheck previousRule = this.outlineChecks.put(name, outlineCheck);

	if (previousRule != null) {
	    SpecsLogs.warn("Replaced rule for access call '" + name + "'");
	}

    }

}
