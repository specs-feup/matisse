/**
 * Copyright 2016 SPeCS.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;

public class DataDependencyGraph {
    private final Map<String, Set<String>> dependsOn = new HashMap<>();
    private final Map<String, Set<String>> isDependencyOf = new HashMap<>();

    public DataDependencyGraph() {
    }

    public void addDependency(String dependent, String dependency) {
	Preconditions.checkArgument(dependent != null);
	Preconditions.checkArgument(dependency != null);

	Set<String> dependencies = this.dependsOn.get(dependent);
	if (dependencies == null) {
	    this.dependsOn.put(dependent, dependencies = new HashSet<>());
	}

	Set<String> dependents = this.isDependencyOf.get(dependency);
	if (dependents == null) {
	    this.isDependencyOf.put(dependency, dependents = new HashSet<>());
	}

	dependencies.add(dependency);
	dependents.add(dependent);
    }

    public Set<String> getDependenciesOf(String dependent) {
	Set<String> dependencies = this.dependsOn.get(dependent);
	if (dependencies == null) {
	    return Collections.emptySet();
	}
	return Collections.unmodifiableSet(dependencies);
    }

    public Set<String> getDependentsOf(String dependency) {
	Set<String> dependents = this.dependsOn.get(dependency);
	if (dependents == null) {
	    return Collections.emptySet();
	}
	return Collections.unmodifiableSet(dependents);
    }
}
