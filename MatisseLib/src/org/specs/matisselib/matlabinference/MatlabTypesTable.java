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

package org.specs.matisselib.matlabinference;

import java.util.HashMap;
import java.util.Map;

public class MatlabTypesTable {

    private static final String PREFIX_ARGUMENT = "#ARG_";

    private Map<String, MatlabType> types;

    public MatlabTypesTable() {
	this.types = new HashMap<>();
    }

    public void addArgument(int index, MatlabType type) {

	MatlabType previous = types.put(getArgumentName(index), type);

	if (previous != null) {
	    throw new RuntimeException("Tried to set type '" + type + "' for argument '" + index
		    + "', but there was already a type defined ('" + previous + "')");
	}
    }

    public MatlabType getArgument(int index) {
	MatlabType type = types.get(getArgumentName(index));

	if (type == null) {
	    throw new RuntimeException("No type defined for argument '" + index + "'");
	}

	return type;
    }

    private static String getArgumentName(int index) {
	return PREFIX_ARGUMENT + index;
    }

    public void addVariable(String name, MatlabType type) {
	MatlabType previousType = types.put(name, type);
	if (previousType != null) {
	    throw new RuntimeException("Tried to set type '" + type + "' for variable '" + name
		    + "', but there was already a type defined ('" + previousType + "')");
	}

    }

    public MatlabType getVariable(String name) {
	MatlabType type = types.get(name);

	if (type == null) {
	    throw new RuntimeException("No type defined for variable '" + name + "'");
	}

	return type;
    }

    @Override
    public String toString() {
	return types.toString();
    }
}
