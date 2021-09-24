/**
 * Copyright 2014 SPeCS.
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

package org.specs.MatlabToC.jOptions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.specs.CIR.FunctionInstance.InstanceProvider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import pt.up.fe.specs.util.SpecsLogs;

/**
 * Maps a MATLAB function name to a list of builders.
 * 
 * @author Joao Bispo
 * 
 */
public class BuildersMap {

    private final Map<String, List<InstanceProvider>> buildersMap;

    public BuildersMap() {
	this.buildersMap = Maps.newHashMap();
    }

    public Collection<String> getFunctions() {
	return buildersMap.keySet();
    }

    public List<InstanceProvider> getBuilders(String functionName) {
	List<InstanceProvider> builders = buildersMap.get(functionName);

	// Return empty list if null
	if (builders == null) {
	    return Collections.emptyList();
	}

	return builders;
    }

    public void add(String functionName, InstanceProvider builder) {
	List<InstanceProvider> builders = buildersMap.get(functionName);

	// Check if exists
	if (builders == null) {
	    builders = Lists.newArrayList();
	    buildersMap.put(functionName, builders);
	}

	// Check if builder was already added
	if (builders.contains(builder)) {
	    SpecsLogs.warn("Map already contains builder '" + builder + "' for function '" + functionName + "'");
	    return;
	}

	// Add builder
	builders.add(builder);
    }

}
