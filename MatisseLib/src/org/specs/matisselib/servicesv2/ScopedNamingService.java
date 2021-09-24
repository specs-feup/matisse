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

package org.specs.matisselib.servicesv2;

import java.util.HashSet;
import java.util.Set;

import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.MatlabUnitNode;
import org.specs.matisselib.services.NamingService;

import com.google.common.base.Preconditions;

public class ScopedNamingService extends NamingService {

    private final ScopeService scopeService;
    private final MatlabUnitNode unit;
    private final Set<String> nameBlackList;

    public ScopedNamingService(ScopeService scopeService, MatlabUnitNode unit) {
	this.scopeService = scopeService;
	this.unit = unit;
	this.nameBlackList = new HashSet<>(getBlackList());
    }

    @Override
    public boolean isVariableNameReserved(String suggestion) {
	Preconditions.checkArgument(suggestion != null);
	Preconditions.checkArgument(!suggestion.isEmpty());
	Preconditions.checkArgument(!suggestion.contains("\r") && !suggestion.contains("\n"));
	Preconditions.checkArgument(suggestion.matches("^[a-zA-Z][a-zA-Z0-9_]*$"));

	// Check if there is a function accessible from this context with the same name
	if (scopeService.getUserFunction(suggestion).isPresent()) {
	    return true;
	}

	// Check if the suggestion is in the black list
	if (nameBlackList.contains(suggestion)) {
	    return true;
	}

	// Update the black list and check again
	unit.getDescendantsStream()
		.filter(n -> n instanceof IdentifierNode)
		.map(n -> (IdentifierNode) n)
		.forEach(n -> nameBlackList.add(n.getName()));

	if (nameBlackList.contains(suggestion)) {
	    return true;
	}

	return false;
    }

}
