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

package org.specs.CIR.FunctionInstance.InstanceBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;

/**
 * @author Joao Bispo
 *
 */
public class InstanceBuilderUtils {

    public enum TypeProperty {
	FLOAT,
	DYNAMICALLY_ALLOCATED;

	public static List<TypeProperty> getClass(VariableType type) {
	    List<TypeProperty> types = new ArrayList<>();

	    if (ScalarUtils.hasScalarType(type)) {
		ScalarType scalarType = ScalarUtils.toScalar(type);
		if (!scalarType.scalar().isInteger()) {
		    types.add(FLOAT);
		}
	    }

	    if (type.usesDynamicAllocation()) {
		types.add(DYNAMICALLY_ALLOCATED);
	    }

	    return types;
	}
    }

    /**
     * If all input types are weak types or non-weak types, return the input types unmodified.
     * 
     * <p>
     * If there is a mix between weak and non-weak types, consider two classes Integer and Real, and drop weak types if
     * they are inside the same class as the non-weak types.
     * 
     * @param inputTypes
     * @return
     */
    public static <T extends ScalarType> List<T> getValidTypes(List<T> inputTypes) {
	List<T> nonWeakTypes = new ArrayList<>();
	List<T> weakTypes = new ArrayList<>();
	Set<TypeProperty> nonWeakClasses = new HashSet<>();

	for (T type : inputTypes) {
	    if (type.isWeakType()) {
		weakTypes.add(type);
	    } else {
		nonWeakTypes.add(type);
		nonWeakClasses.addAll(TypeProperty.getClass(type));
	    }
	}

	// All weak types
	if (nonWeakTypes.isEmpty()) {
	    return inputTypes;
	}

	// No weak types
	if (nonWeakTypes.size() == inputTypes.size()) {
	    return inputTypes;
	}

	// If non-weak types have all classes, all weak types will be dropped
	if (nonWeakClasses.size() == TypeProperty.values().length) {
	    return inputTypes;
	}

	// Add weak type if from a different class than the non-weak types
	for (T weakType : weakTypes) {
	    if (!nonWeakClasses.containsAll(TypeProperty.getClass(weakType))) {
		nonWeakTypes.add(weakType);
	    }
	}

	return nonWeakTypes;
    }

    /*
    public static <T extends VariableType> boolean areAllWeakTypes(List<T> types) {
    for (T type : types) {
        if (!type.isWeakType()) {
    	return false;
        }
    }
    
    return true;
    }
    */
}
