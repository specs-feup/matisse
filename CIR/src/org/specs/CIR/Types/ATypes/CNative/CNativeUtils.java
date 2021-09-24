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

package org.specs.CIR.Types.ATypes.CNative;

import java.util.List;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.CNative.Utils.ToCNative;
import org.specs.CIR.Types.ATypes.Scalar.Utils.ToScalar;

import com.google.common.collect.Lists;

public class CNativeUtils {

    /**
     * Tries to extract a CNativeType from the given VariableType, throws an exception if not successful.
     * 
     * <p>
     * 1. Checks if type is an instance of CNativeType; <br>
     * 2. Checks if type implements ToCNative; <br>
     * 
     * 
     * @param type
     * @return
     */
    public static CNativeType toCNative(VariableType type) {
	// Cast if type is an instance of CNative
	if (type instanceof CNativeType) {
	    return (CNativeType) type;
	}

	// Check if implements interface ToCNative
	if (type instanceof ToCNative) {
	    return ((ToCNative) type).toCNativeType();
	}

	// Check if implements interface ToScalar
	if (type instanceof ToScalar) {
	    return toCNative(((ToScalar) type).toScalarType());
	}

	// Does not know how to convert
	throw new RuntimeException("Does not know how to extract a '" + CNativeType.class + "' from a '"
		+ type.getClass() + "'. Consider implementing the interface '" + ToCNative.class + "'");

    }

    /**
     * Helper method for a list of VariableTypes.
     * 
     * @param types
     * @return
     */
    public static List<CNativeType> toCNative(List<VariableType> types) {
	List<CNativeType> nativeTypes = Lists.newArrayList();

	// Convert each element and add it to the list
	types.forEach(type -> nativeTypes.add(toCNative(type)));

	/*
	for(VariableType type : types) {
	    nativeTypes.add(toCNative(type));
	}
	*/

	return nativeTypes;
    }
    /**
     * 
     * @param type
     * @return a Scalar, if the given type implements ScalarType. Otherwise, throws an exception
     */
    /*
    public static CNative getCNative(VariableType type) {
    return getCNativeType(type).cnative();
    }
    */

    /*
    public static CNativeType cast(VariableType type) {
    return ParseUtils.cast(type, CNativeType.class);
    }
    */

}
