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

package org.specs.matlabtocl.v2.services;

import java.util.HashMap;
import java.util.Map;

import org.specs.matlabtocl.v2.ssa.ParallelRegionId;
import org.specs.matlabtocl.v2.ssa.ParallelRegionInstance;

import com.google.common.base.Preconditions;

public final class ParallelRegionCollection implements ParallelRegionSink, ParallelRegionSource {

    private final Map<ParallelRegionId, ParallelRegionInstance> instances = new HashMap<>();
    private final Map<String, Integer> ids = new HashMap<>();

    @Override
    public synchronized ParallelRegionId emitRegion(ParallelRegionInstance instance) {
	Preconditions.checkArgument(instance != null);

	String baseName = instance.getBody().getName();
	if (baseName == null) {
	    baseName = "<anonymous>";
	}
	int newId = this.ids.getOrDefault(baseName, 0) + 1;
	this.ids.put(baseName, newId);

	ParallelRegionId id = new ParallelRegionId(baseName, newId);

	this.instances.put(id, instance);

	return id;
    }

    @Override
    public synchronized ParallelRegionInstance getById(ParallelRegionId id) {
	Preconditions.checkArgument(id != null);

	ParallelRegionInstance instance = this.instances.get(id);
	if (instance == null) {
	    throw new RuntimeException("Could not find instance with given ID: " + id);
	}

	return instance;
    }
}
