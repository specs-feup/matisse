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

package org.specs.matisselib.matlabinference.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.specs.matisselib.matlabinference.MatlabFunctionType;
import org.specs.matisselib.matlabinference.functions.MatlabOp;

public class SystemFunctionTypes {

    private final Map<String, MatlabFunctionType> mFunctionTypes;

    private SystemFunctionTypes() {
	mFunctionTypes = new HashMap<>();
    }

    private <T extends Enum<T> & MatlabFunctionType> void addFunctionTypes(Class<T> enumTypes) {
	for (T type : enumTypes.getEnumConstants()) {
	    mFunctionTypes.put(type.getName(), type);
	}
    }

    public static final SystemFunctionTypes newInstance() {
	SystemFunctionTypes types = new SystemFunctionTypes();

	// Add operators
	types.addFunctionTypes(MatlabOp.class);

	return types;
    }

    public Optional<MatlabFunctionType> getType(String functionName) {
	return Optional.ofNullable(mFunctionTypes.get(functionName));
    }
    /*
    public static final MatlabFunctionTable buildTable() {
    MatlabFunctionTable prototypeTable = new MatlabFunctionTable();

    // Add MATLAB builtins
    // prototypeTable.addPrototypes(BuiltinFunction.class);

    // Add ArrayCreator prototypes
    prototypeTable.addPrototypes(MatlabBuiltin.class);

    // Add MatlabFunction prototypes
    prototypeTable.addPrototypes(MathFunction.class);

    // Add MatlabOperator prototypes
    prototypeTable.addPrototypes(MatlabOp.class);

    // int matlabfunctions = prototypeTable.getPrototypes().size();
    // System.out.println("MFunctions:" + matlabfunctions);
    // If enabled, add MATISSE Primitives
    // System.out.println("USE?:" + setup.isActive(MatisseOptimization.UseMatissePrimitives));
    // List<MatlabFunction> compatibilityFunctions = MatissePrimitive.buildCompatibilityFunctions();
    // if (setup.isActive(MatisseOptimization.UseMatissePrimitives)) {
    prototypeTable.addPrototypes(MatissePrimitive.class);
    // }

    // Add Probes
    prototypeTable.addPrototypes(Probe.class);

    // Add casts
    prototypeTable.addPrototypes(CastFunctions.getCastPrototypes());
    // System.out.println("Custom:" + (prototypeTable.getPrototypes().size() - matlabfunctions));
    return new MatlabFunctionTable(Collections.unmodifiableMap(prototypeTable.getPrototypes()));

    }
    */
}
